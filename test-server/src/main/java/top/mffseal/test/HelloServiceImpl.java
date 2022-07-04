package top.mffseal.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.api.HelloObject;
import top.mffseal.rpc.api.HelloService;

/**
 * 服务端测试用服务实现。
 *
 * @author mffseal
 */

public class HelloServiceImpl implements HelloService {
    private final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(HelloObject object) {
        logger.info("接收到: {}", object.getMessage());
        return "hello 这是rpc调用的返回值, id=" + object.getId();
    }

    @Override
    public String bye(HelloObject object) {
        logger.info("接收到: {}", object.getMessage());
        return "bye 这是rpc调用的返回值, id=" + object.getId();
    }
}
