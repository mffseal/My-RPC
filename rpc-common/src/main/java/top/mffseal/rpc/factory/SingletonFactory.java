package top.mffseal.rpc.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通过双重检查锁实现的单例工厂，支持无参构造函数，有参构造函数，工厂类进行对象构造。
 *
 * @author mffseal
 */
public class SingletonFactory {
    /**
     * 记录所有已经实例化过的单例。
     */
    private static final Map<Class<?>, Object> objectMap = new HashMap<>();
    private static final Object lock = new Object();

    /**
     * 私有构造函数，防止被实例化。
     */
    private SingletonFactory() {
    }

    /**
     * 调用类的无参构造函数。
     *
     * @param clazz 目标类
     * @param <T>   目标类型
     * @return 目标对象
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

    /**
     * 调用类的有参构造函数。
     *
     * @param clazz      目标类
     * @param paramTypes 构造函数参数类型列表
     * @param args       构造函数参数列表
     * @param <T>        目标类型
     * @return 目标对象
     */
    public static <T> T getInstance(Class<T> clazz, Class<?>[] paramTypes, Object[] args) {
        Object instance = objectMap.get(clazz);
        if (instance == null) {
            synchronized (lock) {
                instance = objectMap.get(clazz);
                if (instance == null) {
                    try {
                        Constructor<T> constructor = clazz.getDeclaredConstructor(paramTypes);
                        instance = constructor.newInstance(args);
                        objectMap.put(clazz, instance);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                             InstantiationException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return clazz.cast(instance);
    }

    /**
     * 通过提供的工厂静态方法实例化类。
     *
     * @param clazz      目标类
     * @param Factory    工厂类
     * @param methodName 工厂静态方法
     * @param paramTypes 实例化所需参数类型列表
     * @param args       实例化所需参数列表
     * @param <T>        目标类型
     * @return 目标对象
     */
    public static <T> T getInstance(Class<T> clazz, Class<?> Factory, String methodName, Class<?>[] paramTypes, Object[] args) {
        Object instance = objectMap.get(clazz);
        if (instance == null) {
            synchronized (lock) {
                instance = objectMap.get(clazz);
                if (instance == null) {
                    try {
                        Method method = Factory.getMethod(methodName, paramTypes);
                        instance = method.invoke(Factory, args);
                        objectMap.put(clazz, instance);
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return clazz.cast(instance);
    }

    /**
     * 本地测试。
     * @param args 无用
     */
    public static void main(String[] args) {
        Logger testLog = getInstance(Logger.class, LoggerFactory.class, "getLogger", new Class[]{Class.class}, new Object[]{SingletonFactory.class});
        testLog.error("123");

        Integer integer = getInstance(Integer.class, new Class[]{int.class}, new Object[]{1});
        testLog.error("{}", integer);

        List<Integer> list = getInstance(ArrayList.class);
        testLog.error("{}", list);
    }
}
