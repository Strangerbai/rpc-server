package org.example.rpc.server.registry;


import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * zk做注册中心
 */
public class ZKRegistryCenter implements RegistryCenter {
    private CuratorFramework client;

    private String zkCluster;

    private static final String ZK_DUBBO_ROOT_PATH = "/mydubbo";

    public void init(){
        // 创建并初始化zk的客户端
        client = CuratorFrameworkFactory.builder()
                // 指定要连接的zk集群
                .connectString(zkCluster)
                // 指定连接超时时限
                .connectionTimeoutMs(10000)
                // 指定会话超时时间
                .sessionTimeoutMs(4000)
                // 指定重试策略：每重试一次，休息1秒，最多重试10次
                .retryPolicy(new ExponentialBackoffRetry(1000, 10))
                .build();
        // 启动zk的客户端
        client.start();
    }

    public ZKRegistryCenter() {
    }

    @Override
    public void register(Class<?> service, String serviceAddress) throws Exception {
        // 创建持久节点
        String servicePath = ZK_DUBBO_ROOT_PATH + "/" + service.getName();
         if (client.checkExists().forPath(servicePath) == null) {
             client.create()
                     .creatingParentsIfNeeded() // 若父节点不存在，则会自动创建父节点
                     .withMode(CreateMode.PERSISTENT)  // 指定创建持久节点
                     .forPath(servicePath);  // 指定要创建的节点
         }
        List<String> methodNames = Arrays.stream(service.getMethods()).map(Method::getName).collect(Collectors.toList());

        client.setData().forPath(servicePath,methodNames.toString().getBytes());

        // 创建临时节点
        String hostPath = servicePath + "/" + serviceAddress;
        if (client.checkExists().forPath(hostPath) == null) {
            String host = client.create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .forPath(hostPath);
            System.out.println(host);
        }

    }

    public void setZkCluster(String zkCluster) {
        this.zkCluster = zkCluster;
    }
}
