package top.mffseal.rpc.socket.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.RpcClient;
import top.mffseal.rpc.entity.RpcRequest;
import top.mffseal.rpc.entity.RpcResponse;
import top.mffseal.rpc.enumeration.ResponseCode;
import top.mffseal.rpc.enumeration.RpcError;
import top.mffseal.rpc.exception.RpcException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * rpc客户端，基于原生Socket的BIO网络通讯，负责：
 * 向服务端发起rpc请求，并接收服务器端发回的网络数据；
 * 解析收到的数据，转换成成RpcResponse。
 * @author mffseal
 */
public class SocketClient implements RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(SocketClient.class);

    private final String host;
    private final int port;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * 向服务者发送rpc请求。
     * @param rpcRequest 请求对象
     * @return 响应对象
     */
    public Object sendRequest(RpcRequest rpcRequest) {
        // 使用自带Socket通讯
        try (Socket socket = new Socket(host, port)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            // 使用java自带序列化
            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();

            // 在这里解析收到的RpcResponse
            RpcResponse rpcResponse = (RpcResponse)objectInputStream.readObject();
            if (rpcResponse==null) {
                logger.error("服务调用失败, 服务: {}", rpcRequest.getInterfaceName());
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, "服务: " + rpcRequest.getInterfaceName());
            }
            if (rpcResponse.getStatusCode() == null || rpcResponse.getStatusCode() != ResponseCode.SUCCESS.getCode()) {
                logger.error("服务调用失败, 服务: {}, 响应: {}", rpcRequest.getInterfaceName(), rpcResponse);
                throw new RpcException(RpcError.SERVICE_INVOCATION_FAILURE, "服务: " + rpcRequest.getInterfaceName());
            }
            // 返回远程方法接口指定的类型
            return rpcResponse.getData();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("rpc调用时发生错误: ", e);
            throw new RpcException("服务调用失败: ", e);
        }
    }
}
