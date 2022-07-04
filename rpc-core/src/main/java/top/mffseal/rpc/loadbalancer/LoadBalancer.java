package top.mffseal.rpc.loadbalancer;

import com.alibaba.nacos.api.naming.pojo.Instance;

import java.util.List;

/**
 * 负载均衡器的统一接口。
 *
 * @author mffseal
 */
public interface LoadBalancer {
    /**
     * 应用负载均衡算法，从列表中选择一个{@link Instance}。
     *
     * @param instances 被挑选集合
     * @return 某个项
     */
    Instance select(List<Instance> instances);

    enum Library implements LoadBalancer {
        Random {
            private final LoadBalancer loadBalancer = new RandomLoadBalancer();

            @Override
            public Instance select(List<Instance> instances) {
                return loadBalancer.select(instances);
            }
        },
        RoundRobin {
            private final LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

            @Override
            public Instance select(List<Instance> instances) {
                return loadBalancer.select(instances);
            }
        }
    }

}
