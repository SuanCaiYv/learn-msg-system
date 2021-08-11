package learn.cwb.ns;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import learn.cwb.common.util.NativeUtils;
import learn.cwb.ns.handler.ChildChannelInitializer;
import learn.cwb.ns.system.RunOnAppStart;
import learn.cwb.ns.system.SystemConstant;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/10 9:11 下午
 */
public class NotificationApplication {

    public static void main(String[] args) {
        RunOnAppStart.hookBeforeStart();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        ChannelFuture channelFuture = serverBootstrap
                .channel(NativeUtils.serverChannel())
                .group(NativeUtils.bossEventLoopGroup(), NativeUtils.workerEventLoopGroup())
                .childHandler(new ChildChannelInitializer())
                .bind("127.0.0.1", SystemConstant.MY_PORT)
                .syncUninterruptibly();
        RunOnAppStart.hookAfterStart();
    }
}
