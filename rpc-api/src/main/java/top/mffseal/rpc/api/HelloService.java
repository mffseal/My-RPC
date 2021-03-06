package top.mffseal.rpc.api;

/**
 * 测试用某服务调用接口。
 *
 * @author mffseal
 */
public interface HelloService {
    /**
     * 测试服务
     *
     * @param object 测试用的调用参数
     * @return 测试用的调用结果
     */
    String hello(HelloObject object);

    String bye(HelloObject object);
}
