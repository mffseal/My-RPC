package top.mffseal.rpc.transport.netty.client;

import top.mffseal.rpc.entity.RpcResponseMessage;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 记录每个请求对应的响应future，由客户端发起rpc请求并把对应的rpc响应future填入；
 * 由客户端响应处理类在收到响应后完成对应的future。
 * <p>
 * 一个生产者消费者模型，生产者负责向消费者指定的位置填充{@link RpcResponseMessage}。
 * 你可以类比一下快递柜的工作原理：
 * <p>
 * 由用户指定一个请求对应的响应放在哪一个格子；
 * 响应到来后存入对应的格子，用户从该格子中取响应。
 *
 * @author mffseal
 */
public class ResponseLocker {
    /**
     * 保存等待response的future。
     */
    private static final Map<String, CompletableFuture<RpcResponseMessage<?>>> locker = new ConcurrentHashMap<>();

    /**
     * 记录请求的序列号和与之对应的response的future。
     *
     * @param sequenceId 请求序列号
     * @param future     future任务
     */
    public void put(String sequenceId, CompletableFuture<RpcResponseMessage<?>> future) {
        locker.put(sequenceId, future);
    }

    /**
     * 移除某个序列号对应的future。
     *
     * @param sequenceId 请求序列号
     */
    public void remove(String sequenceId) {
        locker.remove(sequenceId);
    }

    /**
     * 向柜子指定位置填充一个response。
     *
     * @param response RpcResponseMessage
     */
    public void complete(RpcResponseMessage<?> response) {
        CompletableFuture<RpcResponseMessage<?>> future = locker.remove(response.getSequenceId());
        if (future != null) {
            future.complete(response);
        } else {
            throw new IllegalStateException();
        }
    }
}
