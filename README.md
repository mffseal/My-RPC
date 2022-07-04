# My-RPC

[![OSCS Status](https://www.oscs1024.com/platform/badge/mffseal/My-RPC.svg?size=small)](https://www.oscs1024.com/project/mffseal/My-RPC?ref=badge_small)
![GitHub](https://img.shields.io/github/license/mffseal/MY-RPC)
![jdk](https://img.shields.io/static/v1?label=oraclejdk&message=8&color=blue)

MY-RPC 是一个RPC框架，支持接入多种服务管理平台（目前接入Nacos）、多种序列化算法、多种负载均衡算法，使用 Java 原生 Socket 于 Natty 实现了两套网络传输模块。

## OSCS

[![OSCS Status](https://www.oscs1024.com/platform/badge/mffseal/My-RPC.svg?size=large)](https://www.oscs1024.com/project/mffseal/My-RPC?ref=badge_large)

## 架构

![系统架构](images/architecture.png)

## 特性

- 接口设计合理，模块之间低耦合，可以**灵活配置**诸如序列化算法、负载均衡算法等。
- 实现通过**配置文件设置参数**，包括日志级别、序列化算法、负载均衡算法、IP地址等。
- 实现基于 Java 原生 Socket 和 Netty 两套网络传输方式。
  - 使用 Netty 的 帧解码器(ProtocolFrameDecoder)配合协议长度字段，**解决粘包半包问题**。
  - Netty 自定义编解码器和请求响应 handler 使用 Sharable 设计进行 handler 复用，避免不必要的实例化。
  - Netty 客户端采用 Channel 池进行连接复用，避免重复连接同一服务器。
- 客户端接收 response 使用**生产者消费者模型**，配合 CompletableFuture 实现客户端同时多次 rpc 请求间不会相互阻塞。
- 解决 json 类序列化 Object 集合中类型信息丢失问题（通过接口内部类实现公共方法）。
- 可以**方便的接入不同序列化算法**，目前接入：
  - Gson
  - Hessian
  - Jackson
  - Kryo
  - Java原生
  - Protostuff (Protobuf)
- 可以**方便的接入不同的负载均衡算法**，目前实现：
  - 随机算法
  - 轮询算法
- 使用 Nacos 作为服务注册和发现平台。
  - 服务端下线通过回调钩子自动向 Nacos 注销对应服务。
- 实现单例工厂(用于)，支持无参构造，有参构造和构造工厂三种方式。
- 项目注释完整，逻辑清晰。

## 模块

- rpc-api:     通用接口包
- rpc-common:  消息实体对象、工具类等共用类
- rpc-core:    rpc核心
- test-client: 测试用服务端
- test-server: 测试用客户端

## 传输协议 MFF

```text
+---------------+---------------+-----------------+-------------+
|  Magic Number |  Package Type | Serializer Type | Data Length |
|    4 bytes    |    4 bytes    |     4 bytes     |   4 bytes   |
+---------------+---------------+-----------------+-------------+
|                          Data Bytes                           |
|                   Length: ${Data Length}                      |
+---------------------------------------------------------------+
```

| 字段              |                     解释                      |
|:----------------|:-------------------------------------------:|
| Magic Number    |               标识一个数据包是否为MFF协议               |
| Package Type    |               标识数据是一个请求还是一个响应               |
| Serializer Type |                标识数据包内容的序列化方式                |
| Data Length     |                 标识数据字段的字节数                  |
| Data Bytes      | 传输对象，RpcRequestMessage或RpcResponseMessage对象 |

## 使用

### 定义调用接口

接口定义：

```java
package top.mffseal.rpc.api;

/**
 * 测试用某服务调用接口。
 *
 * @author mffseal
 */
public interface HelloService {
    /**
     * 测试服务
     *
     * @param object 测试用的调用参数
     * @return 测试用的调用结果
     */
    String hello(HelloObject object);

    String bye(HelloObject object);
}
```

接口参数定义：

```java
package top.mffseal.rpc.api;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 测试调用接口中，客户端向服务端传递的参数对应的类。
 *
 * @author mffseal
 */
@Data
@AllArgsConstructor
public class HelloObject implements Serializable {
    private Integer id;
    private String message;

    public HelloObject() {
    }
}
```

### 服务端实现接口

```java
package top.mffseal.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.api.HelloObject;
import top.mffseal.rpc.api.HelloService;

/**
 * 服务端测试用服务实现。
 *
 * @author mffseal
 */

public class HelloServiceImpl implements HelloService {
    private final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(HelloObject object) {
        logger.info("接收到: {}", object.getMessage());
        return "hello 这是rpc调用的返回值, id=" + object.getId();
    }

    @Override
    public String bye(HelloObject object) {
        logger.info("接收到: {}", object.getMessage());
        return "bye 这是rpc调用的返回值, id=" + object.getId();
    }
}
```

### 定义服务端

Netty为例:

```java
package top.mffseal.test;

import top.mffseal.rpc.api.HelloService;
import top.mffseal.rpc.transport.RpcServer;
import top.mffseal.rpc.transport.netty.server.NettyServer;

/**
 * 测试用Netty服务端
 *
 * @author mffseal
 */
public class NettyTestServer {
    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        RpcServer rpcServer = new NettyServer();
        rpcServer.publishService(helloService, HelloService.class);
        rpcServer.start();
    }
}
```

### 定义客户端

Netty为例:

```java
package top.mffseal.test;

import top.mffseal.rpc.api.HelloObject;
import top.mffseal.rpc.api.HelloService;
import top.mffseal.rpc.transport.RpcClient;
import top.mffseal.rpc.transport.RpcClientProxy;
import top.mffseal.rpc.transport.netty.client.NettyClient;

/**
 * 测试用Netty客户端。
 *
 * @author mffseal
 */
public class NettyTestClient {
    public static void main(String[] args) {
        RpcClient client = new NettyClient();
        RpcClientProxy rpcClientProxy = new RpcClientProxy(client);
        HelloService helloServer = rpcClientProxy.getProxy(HelloService.class);
        HelloObject helloObject = new HelloObject(12, "this is a message");
        // 轮询算法的效果要同一个客户端多次调用同一个接口才能看出
        String res = helloServer.hello(helloObject);
        String res2 = helloServer.hello(helloObject);
        String res3 = helloServer.hello(helloObject);
        System.out.println(res);
    }
}
```

### 配置客户端

- 序列化算法选用Gson
- 负载均衡算法选择轮询算法

```properties
serializer.library=Gson
loadBalancer=RoundRobin
serializer.kryo.compress=true
netty.loglevel=INFO
netty.retry=5
netty.timeout=5000
namingServer.platform=Nacos
namingServer.host=Nacos服务器ip地址
namingServer.port=Nacos服务器端口
```

### 配置服务端

- 序列化算法选择Protostuff (Protobuf)

```properties
serializer.library=Protostuff
serializer.kryo.compress=true
host=localhost
port=8080
netty.loglevel=INFO
namingServer.platform=Nacos
namingServer.host=Nacos服务器ip地址
namingServer.port=Nacos服务器端口
```

### 启动

1. 启动前，请确保服务端和客户端均能连接到Nacos服务器，并且在服务端和客户端的配置文件中配置了Nacos服务器地址和端口。
2. 分别启动服务端和客户端，观察控制台输出内容。

## 重点设计

### Netty客户端生产消费模型

1. 客户端通过动态代理，实际调用到RpcClientProxy的invoke方法。
2. 该方法会调用NettyClient的sendRequest方法，并通过completableFuture.get()方法阻塞等待结果。
3. NettyClient发送rpc请求，并向ResponseLocker存入一个CompletableFuture。
4. 这样NettyClient就可以接着执行其它的invoke调用，不用等待response阻塞。
5. 服务器收到请求后会调用方法，并返回一个RpcResponse。
6. 客户端的ResponseHandler收到RpcResponse后会向ResponseLocker对应位置的CompletableFuture调用complete（填充response）。
7. complete会唤醒RpcClientProxy调用的get处，并将结果传递过去。

## LICENSE

My-RPC-Framework is under the MIT license. See the [LICENSE](https://github.com/mffseal/My-RPC/blob/master/LICENSE) file
for details.