package learn.cwb.ns.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import learn.cwb.common.transport.Msg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/10 9:16 下午
 */
public class NotificationHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationHandler.class);

    private static final ConcurrentHashMap<Long, Channel> CHANNEL_MAP = GlobalVariable.CHANNEL_MAP;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg0) throws Exception {
        Msg msg = (Msg) msg0;
        long senderId = msg.getHead().getSenderId();
        long receiverId = msg.getHead().getReceiverId();
        if (!CHANNEL_MAP.containsKey(receiverId)) {
            LOGGER.info("对{}的请求转发到其他服务器处理", receiverId);
            ctx.fireChannelRead(msg0);
        } else {
            System.out.println(receiverId);
            Channel userChannel = CHANNEL_MAP.get(receiverId);
            userChannel.writeAndFlush(msg);
        }
    }
}
