package top.mffseal.rpc.hook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.mffseal.rpc.util.NacosUtil;

/**
 * 服务提供者关闭服务器时，向服务管理平台注销对应服务的回调类。
 *
 * @author mffseal
 */
public class ShutdownHook {
    private static final Logger log = LoggerFactory.getLogger(ShutdownHook.class);
    private static final ShutdownHook shutdownHock = new ShutdownHook();

    /**
     * 获取shutdown钩子实例。
     *
     * @return shutdown钩子
     */
    public static ShutdownHook getShutdownHock() {
        return shutdownHock;
    }

    /**
     * 添加关闭时注销服务的钩子。
     */
    public void addClearAllHock() {
        log.info("关闭后自动注销本机提供的所有服务");
        Runtime.getRuntime().addShutdownHook(new Thread(NacosUtil::clearRegistry, "shutdownHockThread"));
    }

}
