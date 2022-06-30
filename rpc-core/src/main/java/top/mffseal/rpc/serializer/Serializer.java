package top.mffseal.rpc.serializer;

import top.mffseal.rpc.entity.RpcRequestMessage;

/**
 * 通用反序列化接口
 *
 * @author mffseal
 */
public interface Serializer {
    /**
     * 序列化方法。
     *
     * @param obj 待序列化对象
     * @return byte数组
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化方法。
     *
     * @param bytes byte数组
     * @param clazz 目标类
     * @return 目标对象
     */
    Object deserialize(byte[] bytes, Class<?> clazz);

    /**
     * 处理参数列表的内部工具类。
     * 由于RpcRequest中的调用参数为Object数组，json序列化的Object会丢失类型信息，
     * (其它反序列化方法转成字节数组会保留信息，不会遇到上述问题)，
     * 因此需要通过RpcRequest中的参数类型列表对参数列表中的每一个object进行回炉重造打上对应的类信息。
     */
    class Util {
        /**
         * 针对参数列表，遍历拿到每个object和ParamTypes数组中对应的每个参数类型，
         * 将每个参数重新序列化，回复json丢失的类信息。
         *
         * @param _this this指针
         * @param obj   rpc请求
         * @return 处理后的rpc请求
         */
        public static Object handleRequestParametersList(Serializer _this, Object obj) {
            RpcRequestMessage rpcRequestMessage = (RpcRequestMessage) obj;

            // 遍历参数类型列表中的每个类型
            for (int i = 0; i < rpcRequestMessage.getParamTypes().length; i++) {
                Class<?> clazz = rpcRequestMessage.getParamTypes()[i];
                // 判断参数列表中的所有参数是否和参数类型列表中的类型一一对应
                // 如果不对应了，那么用指定类型原地序列化再反序列化一次，并写回
                if (!clazz.isAssignableFrom(rpcRequestMessage.getParameters()[i].getClass())) {
                    byte[] paramBytes = _this.serialize(rpcRequestMessage.getParameters()[i]);
                    rpcRequestMessage.getParameters()[i] = _this.deserialize(paramBytes, clazz);
                }
            }
            return rpcRequestMessage;
        }
    }

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
        },
        Hessian {
            private final Serializer hessian = new HessianSerializer();

            @Override
            public byte[] serialize(Object obj) {
                return hessian.serialize(obj);
            }

            @Override
            public Object deserialize(byte[] bytes, Class<?> clazz) {
                return hessian.deserialize(bytes, clazz);
            }
        },
        Gson {
            private final Serializer gson = new GsonSerializer();

            @Override
            public byte[] serialize(Object obj) {
                return gson.serialize(obj);
            }

            @Override
            public Object deserialize(byte[] bytes, Class<?> clazz) {
                return gson.deserialize(bytes, clazz);
            }
        },

    }

}
