package top.mffseal.test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.api.HelloObject;
import top.mffseal.rpc.api.HelloService;

/**
 * @author mffseal
 */

public class HelloServiceImpl implements HelloService {
    private final Logger logger = LoggerFactory.getLogger(HelloServiceImpl.class);

    @Override
    public String hello(HelloObject object) {
        logger.info("接收到: {}", object.getMessage());
        return "这是rpc调用的返回值, id=" + object.getId();
    }
}
