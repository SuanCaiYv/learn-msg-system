package learn.cwb.gateway;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import learn.cwb.common.util.NativeUtils;
import learn.cwb.gateway.handler.ChannelInitializerHandler;
import learn.cwb.gateway.system.RunOnAppStart;
import learn.cwb.gateway.system.SystemConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 8:45 下午
 */
public class GatewayApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayApplication.class);

    public static void main(String[] args) {
        RunOnAppStart.hook();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        ChannelFuture channelFuture = serverBootstrap
                .channel(NativeUtils.serverChannel())
                .group(NativeUtils.bossEventLoopGroup(), NativeUtils.workerEventLoopGroup())
                .childHandler(new ChannelInitializerHandler())
                .bind("127.0.0.1", SystemConstant.MY_PORT)
                .syncUninterruptibly();
    }
}
