package top.mffseal.rpc.serializer;

/**
 * 通用反序列化接口
 *
 * @author mffseal
 */
// TODO: 2022/6/28 使用配置文件配置序列化架构
public interface CommonSerializer {
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
     * 获得序列化器编号。
     *
     * @return 序列化器编号
     */
    int getCode();

    /**
     * 根据编号获取序列化器。
     *
     * @param code 序列化器编号
     * @return 序列化器
     */
    static CommonSerializer getByCode(int code) {
        switch (code) {
            case 1:
                return new JsonSerializer();
            default:
                return null;
        }
    }
}
