package learn.cwb.im;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import learn.cwb.common.util.NativeUtils;
import learn.cwb.im.handler.ChildChannelPipelineInitializer;
import learn.cwb.im.handler.GlobalVariable;
import learn.cwb.im.system.RunOnAppStart;
import learn.cwb.im.system.SystemConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.locks.LockSupport;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/7 12:59 上午
 */
public class InstantMessageApplication {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstantMessageApplication.class);

    public static void main(String[] args) {
        if (args.length != 0) {
            GlobalVariable.PORT = Integer.parseInt(args[0]);
        }
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
        while (true) {
            LockSupport.parkNanos(Duration.ofSeconds(5).toNanos());
            for (long a : GlobalVariable.CHANNEL_MAP.keySet()) {
                System.out.print(a + " ");
            }
            System.out.println();
        }
    }
}
