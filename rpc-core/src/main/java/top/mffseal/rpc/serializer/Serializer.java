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
     * 序列化框架注册表。
     */
    enum Library implements Serializer {
        // TODO: 2022/6/29 优化代码冗余
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
        Native {
            private final Serializer nativeSer = new NativeSerializer();

            @Override
            public byte[] serialize(Object obj) {
                return nativeSer.serialize(obj);
            }

            @Override
            public Object deserialize(byte[] bytes, Class<?> clazz) {
                return nativeSer.deserialize(bytes, clazz);
            }
        },
        Kryo {
            private final Serializer kryo = new KryoSerializer();

            @Override
            public byte[] serialize(Object obj) {
                return kryo.serialize(obj);
            }

            @Override
            public Object deserialize(byte[] bytes, Class<?> clazz) {
                return kryo.deserialize(bytes, clazz);
            }
        }

    }

}
