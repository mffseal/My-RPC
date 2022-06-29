package top.mffseal.rpc.serializer;

/**
 * 通用反序列化接口
 *
 * @author mffseal
 */
// TODO: 2022/6/28 使用配置文件配置序列化架构
public interface Serializer {
    /**
     * 序列化方法
     *
     * @param obj 待序列化对象
     * @return byte数组
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化方法
     *
     * @param bytes byte数组
     * @param clazz 目标类
     * @return 目标对象
     */
    Object deserialize(byte[] bytes, Class<?> clazz);

    /**
     * 定义序列化枚举的内部枚举类，与Config配置类配合使用。
     */
    enum Library implements Serializer {
        Jackson {
            private final Serializer jackson = new JacksonSerializer();

            @Override
            public byte[] serialize(Object obj) {
                return jackson.serialize(obj);
            }

            @Override
            public Object deserialize(byte[] bytes, Class<?> clazz) {
                return jackson.deserialize(bytes, clazz);
            }
        },
    }

}
