package top.mffseal.rpc.factory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 用于创建和维护多个ThreadPool的工具类。
 *
 * @author mffseal
 */
public class ThreadPoolFactory {
    private static final Logger log = LoggerFactory.getLogger(ThreadPoolFactory.class);
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
     * 线程池的池，维护多个线程池。
     */
    private static final Map<String, ExecutorService> threadPoolsMap = new ConcurrentHashMap<>();

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
        ExecutorService pool = threadPoolsMap.computeIfAbsent(threadNamePrefix, k -> createThreadPool(threadNamePrefix, daemon));
        if (pool.isShutdown() || pool.isTerminated()) {
            threadPoolsMap.remove(threadNamePrefix);
            pool = createThreadPool(threadNamePrefix, daemon);
            threadPoolsMap.put(threadNamePrefix, pool);
        }
        return pool;
    }

    /**
     * 初始化线程池
     *
     * @param threadNamePrefix 内部线程名前缀
     * @param daemon           内部线程是否为守护线程
     * @return ExecutorService 线程池
     */
    private static ExecutorService createThreadPool(String threadNamePrefix, Boolean daemon) {
        // 使用有界的ArrayBlockingQueue
        // ArrayBlockingQueue：数组阻塞队列，底层数据结构是数组，需要指定队列的大小。
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<>(BLOCKING_QUEUE_CAPACITY);
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.MINUTES, workQueue, threadFactory);
    }

    /**
     * 关闭所有线程池。
     */
    public static void shutdownAll() {
        log.info("开始关闭所有线程池...");
        threadPoolsMap.forEach((poolName, pool) -> {
            // 线程池不再接收新任务，等待旧任务执行完毕
            pool.shutdown();
            log.info("关闭线程池 {} -- {}", poolName, pool.isTerminated());
            boolean shutdown = true;
            try {
                // 阻塞直到所有线程关闭或超时
                shutdown = pool.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("线程池 {} 关闭失败", poolName);
                // 通过中断等方式强制停止运行中线程，但无法停止不响应中断的线程
                pool.shutdownNow();
            }
            if (!shutdown) {
                log.error("线程池 {} 关闭超时", poolName);
                pool.shutdownNow();
            }
        });
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
