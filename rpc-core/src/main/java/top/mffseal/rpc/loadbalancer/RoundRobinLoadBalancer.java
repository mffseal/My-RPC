package top.mffseal.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * 轮转算法负载均衡。
 *
 * @author mffseal
 */
public class RoundRobinLoadBalancer implements LoadBalancer {
    static int index = 0;

    @Override
    public Instance select(List<Instance> instances) {
        if (index >= instances.size()) {
            index %= instances.size();
        }
        return instances.get(index++);
    }
}
