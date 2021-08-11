package learn.cwb.im;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import learn.cwb.common.util.NativeUtils;
import learn.cwb.im.handler.ChildChannelPipelineInitializer;
import learn.cwb.im.system.RunOnAppStart;
import learn.cwb.im.system.SystemConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/7 12:59 上午
 */
public class InstantMessageApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstantMessageApplication.class);

    public static void main(String[] args) {
        RunOnAppStart.hookBeforeStart();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        ChannelFuture channelFuture = serverBootstrap
                .channel(NativeUtils.serverChannel())
                .group(NativeUtils.bossEventLoopGroup(), NativeUtils.workerEventLoopGroup())
                .childHandler(new ChildChannelPipelineInitializer())
                .bind("127.0.0.1", SystemConstant.MY_PORT)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        LOGGER.info("服务器已绑定到: {}", "127.0.0.1:" + SystemConstant.MY_PORT);
                    } else {
                        LOGGER.error("服务器绑定{}失败", SystemConstant.MY_PORT);
                    }
                })
                .syncUninterruptibly();
        RunOnAppStart.hookAfterStart();
    }
}
