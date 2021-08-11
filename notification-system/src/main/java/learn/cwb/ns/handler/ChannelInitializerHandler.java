package learn.cwb.ns.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import learn.cwb.common.codec.Byte2MsgCodec;
import learn.cwb.common.handler.HeartbeatHandler;
import learn.cwb.common.transport.Msg;
import learn.cwb.ns.system.SystemConstant;

import java.util.concurrent.TimeUnit;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/10 9:13 下午
 */
public class ChannelInitializerHandler extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 4, 8, Msg.EMPTY_SIZE - 12, 0));
        pipeline.addLast(new Byte2MsgCodec());
        pipeline.addLast(new IdleStateHandler(0, 0, SystemConstant.IDLE_TIME, TimeUnit.HOURS));
        pipeline.addLast(new HeartbeatHandler());
        pipeline.addLast(new KeepAliveHandler());
        pipeline.addLast(new NotificationHandler());
        pipeline.addLast(new ForwardHandler());
        pipeline.addLast(new ExceptionHandler());
    }
}
