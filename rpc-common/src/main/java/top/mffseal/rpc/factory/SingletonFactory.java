package top.mffseal.rpc.factory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mffseal
 */
public class SingletonFactory {
    /**
     * 记录所有已经实例化过的单例
     */
    private static final Map<Class<?>, Object> objectMap = new HashMap<>();
    private static final Object lock = new Object();

    /**
     * 私有构造函数，防止被实例化。
     */
    private SingletonFactory() {
    }

    /**
     * 双重校验锁实现的单例方法。
     *
     * @param clazz 需要创建单例的类
     * @param <T>   对象类型
     * @return 单例对象
     */
    public static <T> T getInstance(Class<T> clazz) {
        Object instance = objectMap.get(clazz);
        if (instance == null) {
            synchronized (lock) {
                instance = objectMap.get(clazz);
                if (instance == null) {
                    try {
                        instance = clazz.newInstance();
                        objectMap.put(clazz, instance);
                    } catch (InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return clazz.cast(instance);
    }
}
