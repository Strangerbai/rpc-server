package org.example.rpc.server.registry;

import java.util.List;

/**
 * 注册规范
 */
public interface RegistryCenter {
    /**
     *
     * @param service  注册到注册中心的服务名称，一般为业务接口名
     * @param serviceAddress  提供该服务的主机的ip:port
     */
    void register(Class<?> service, String serviceAddress) throws Exception;

}
