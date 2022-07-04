[![OSCS Status](https://www.oscs1024.com/platform/badge/mffseal/My-RPC.svg?size=small)](https://www.oscs1024.com/project/mffseal/My-RPC?ref=badge_small)

# My-RPC

A small and simple implement of rpc framework.

## OSCS

[![OSCS Status](https://www.oscs1024.com/platform/badge/mffseal/My-RPC.svg?size=large)](https://www.oscs1024.com/project/mffseal/My-RPC?ref=badge_large)

## Netty客户端生产消费模型

1. 客户端通过动态代理，实际调用到RpcClientProxy的invoke方法。
    1. 该方法会调用NettyClient的sendRequest方法，并通过completableFuture.get()方法阻塞等待结果。
2. NettyClient发送rpc请求，并向ResponseLocker存入一个CompletableFuture。
    1. 这样NettyClient就可以接着执行其它的invoke调用，不用等待response阻塞。
3. 服务器收到请求后会调用方法，并返回一个RpcResponse。
4. 客户端的ResponseHandler收到RpcResponse后会向ResponseLocker对应位置的CompletableFuture调用complete（填充response）。
5. complete会唤醒RpcClientProxy调用的get处，并将结果传递过去。