package learn.cwb.im;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
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
        RunOnAppStart.hook();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        ChannelFuture channelFuture = serverBootstrap
                .channel(NativeUtils.serverChannel())
                .group(NativeUtils.bossEventLoopGroup(), NativeUtils.workerEventLoopGroup())
                .childHandler(new ChildChannelPipelineInitializer())
                .bind("127.0.0.1", SystemConstant.MY_PORT)
                .syncUninterruptibly();
    }
}
