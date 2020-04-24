package org.example.rpc.server.engine;

import lombok.Data;
import lombok.Setter;
import org.example.rpc.server.registry.ZKRegistryCenter;
import org.example.rpc.server.server.RpcServer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Data
public class RpcEngine {

    private ThreadPoolTaskExecutor rpcExecuter;

    private BeanProvider beanProvider;

    private RpcServer rpcServer;

    private ZKRegistryCenter zkRegistryCenter;

    private String servcieAddress;

    private String basePackage;


    public void init() throws Exception {
        rpcServer.setCenter(zkRegistryCenter);
        rpcServer.setServiceAddress(servcieAddress);
        rpcServer.setBeanProvider(beanProvider);
        rpcServer.setRpcExecuter(rpcExecuter);
        rpcServer.publish(basePackage);
//        rpcServer.start();
    }

    public RpcEngine(){
    }



}
