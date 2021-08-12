package learn.cwb.im.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import learn.cwb.common.handler.HeartbeatHandler;
import learn.cwb.common.transport.Msg;
import learn.cwb.common.codec.Byte2MsgCodec;
import learn.cwb.im.system.SystemConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 1:24 上午
 */
public class ChildChannelPipelineInitializer extends ChannelInitializer<Channel> {
    static Logger LOGGER = LoggerFactory.getLogger(ChildChannelPipelineInitializer.class);

    @Override
    protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("LengthBasedFrameDecoder", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 4, 8, Msg.Head.HEAD_SIZE - 12, 0));
        pipeline.addLast("ByteToMsgCodec", new Byte2MsgCodec());
        pipeline.addLast("IdleEventHandler", new IdleStateHandler(0, 0, SystemConstant.IDLE_TIME, TimeUnit.SECONDS));
        pipeline.addLast("HeartbeatHandler", new HeartbeatHandler());
        pipeline.addLast("KeepAliveHandler", new KeepAliveHandler());
        pipeline.addLast("InstantMsgHandler", new InstantMsgHandler());
        // 这里稍微切换了一下执行上下文，前面采用线程池是因为内部调用时间可控，前面含外部调用，时间不可控，或者我自己不确定
        // 所以为了减少Netty的EventExecutorGroup的压力，而且我希望I/O操作得益于NIO+事件驱动，尽可能保留在EventLoop去执行
        pipeline.addLast(GlobalVariable.EVENT_EXECUTOR_GROUP, "ForwardHandler", new ForwardHandler());
        pipeline.addLast("ExceptionHandler", new ExceptionHandler());
    }
}
