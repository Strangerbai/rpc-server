package org.example.rpc.server.server;

import com.alibaba.dubbo.common.utils.ReflectUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.StringUtil;
import org.example.rpc.server.common.Convert;
import org.example.rpc.server.common.Invocation;
import org.example.rpc.server.engine.BaseInterface;
import org.example.rpc.server.engine.BeanProvider;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.lang.reflect.Method;
import java.util.Map;

// SimpleChannelInboundHandler：channelRead()方法msg参数会被自动释放
// ChannelInboundHandlerAdapter：channelRead()方法msg参数不会被自动释放
public class RpcServerHandler extends SimpleChannelInboundHandler<Invocation> {
    private Map<String, Object> registryMap;
    private ThreadPoolTaskExecutor rpcExecuter;
    private BeanProvider beanProvider;

    public RpcServerHandler(Map<String, Object> registryMap) {
        this.registryMap = registryMap;
    }

    public RpcServerHandler(ThreadPoolTaskExecutor rpcExecuter, BeanProvider beanProvider) {
        this.beanProvider = beanProvider;
        this.rpcExecuter = rpcExecuter;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Invocation msg) throws Exception {
        Object result = "没有指定的提供者";

        // 获取要访问的业务接口名
        String interfaceName = msg.getClassName();
        // 获取接口的简单类名
        String simpleInterfaceName = interfaceName.substring(interfaceName.lastIndexOf(".") + 1);
        // 获取接口所在的包名
//        String basePackage = interfaceName.substring(0, interfaceName.lastIndexOf("."));
        // 获取用户要访问的提供者实现类的前辍
        String prefix = msg.getPrefix();
        // 构建客户端要访问的提供者的key
        String key = prefix + simpleInterfaceName;

        final BaseInterface action = beanProvider.getBean(key, BaseInterface.class);
        if(action == null){
            return;
        }
        // 通过反射获取当前对象方法的参数类型
        Method method = ReflectUtils.findMethodByMethodName(action.getClass(), msg.getMethodName());
        Class<?>[] paramsType = method.getParameterTypes();

        Object[] argsConvert = new Object[paramsType.length];

        for(int i=0;i<paramsType.length;i++){
            argsConvert[i] = Convert.compatibleTypeConvert(msg.getParamValues()[i], paramsType[i]);
        }

        // 调用接口
        if(msg.isAsyn()){
            result = "异步调用";
            rpcExecuter.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        method.invoke(action, argsConvert);
                    } catch (Throwable e) {
                        System.out.println("执行异常");
                    }

                }
            });
        }else {
            result = method.invoke(action, argsConvert);
        }
        ctx.writeAndFlush(result);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
