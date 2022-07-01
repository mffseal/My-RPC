package top.mffseal.rpc.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * 用于创建ThreadPool的工具类。
 *
 * @author mffseal
 */
public class ThreadPoolFactory {
    /**
     * 线程池中核心线程数最大值。
     */
    private static final int CORE_POOL_SIZE = 10;

    /**
     * 线程池中线程总数最大值。
     */
    private static final int MAXIMUM_POOL_SIZE = 100;

    /**
     * 非核心线程闲置超时时长，单位为分钟。
     */
    private static final int KEEP_ALIVE_TIME = 1;

    /**
     * 阻塞队列大小。
     */
    private static final int BLOCKING_QUEUE_CAPACITY = 100;

    /**
     * 创建线程池，并且其中线程默认为非守护线程。
     *
     * @param threadNamePrefix 内部线程名前缀
     * @return ExecutorService 线程池
     */
    public static ExecutorService createDefaultThreadPool(String threadNamePrefix) {
        return createDefaultThreadPool(threadNamePrefix, false);
    }

    /**
     * 创建线程池，需要指定线程的名称前缀和是否为守护线程。
     *
     * @param threadNamePrefix 内部线程名前缀
     * @param daemon           内部线程是否为守护线程
     * @return ExecutorService 线程池
     */
    public static ExecutorService createDefaultThreadPool(String threadNamePrefix, Boolean daemon) {
        // 使用有界的ArrayBlockingQueue
        // ArrayBlockingQueue：数组阻塞队列，底层数据结构是数组，需要指定队列的大小。
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.MINUTES, workQueue, threadFactory);
    }

    /**
     * 创建线程工厂。如果threadNamePrefix不为空则自建ThreadFactory，否则使用defaultThreadFactory。
     *
     * @param threadNamePrefix 创建出的线程名前缀
     * @param daemon           指定指定创建出的线程是否为守护线程
     * @return ThreadFactory
     */
    private static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        // 设置工厂创建出的线程名称前缀和是否为守护线程
        if (threadNamePrefix != null) {
            if (daemon != null) {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").setDaemon(daemon).build();
            } else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        // 未指定则返回默认工厂
        return Executors.defaultThreadFactory();
    }

}
