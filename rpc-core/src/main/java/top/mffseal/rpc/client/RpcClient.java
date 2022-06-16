package top.mffseal.rpc.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.entity.RpcRequest;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * rpc客户端，用于向服务端发起rpc请求。
 * @author mffseal
 */
public class RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    /**
     * 向服务者发送rpc请求。
     * @param rpcRequest 请求对象
     * @param host 目标主机
     * @param port 目标端口
     * @return 响应对象
     */
    public Object sendRequest(RpcRequest rpcRequest, String host, int port) {
        // 使用自带Socket通讯
        try (Socket socket = new Socket(host, port)) {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            // 使用java自带序列化
            objectOutputStream.writeObject(rpcRequest);
            objectOutputStream.flush();
            return objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            logger.error("rpc调用时发生错误: ", e);
            return null;
        }
    }
}
