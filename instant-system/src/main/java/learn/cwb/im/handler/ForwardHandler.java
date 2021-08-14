package learn.cwb.im.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import learn.cwb.common.transport.Msg;
import learn.cwb.common.redis.RedisOps;
import learn.cwb.common.redis.impl.RedisOpsImpl;
import learn.cwb.im.system.SystemConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device MacBookPro
 * @time 2021/8/10 13:59
 */
public class ForwardHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForwardHandler.class);

    private static final RedisOps REDIS_OPS = new RedisOpsImpl();

    private static final ConcurrentHashMap<String, Channel> OTHER_SERVERS = GlobalVariable.OTHER_SERVERS;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg0) throws Exception {
        Msg msg = (Msg) msg0;
        long senderId = msg.getHead().getSenderId();
        long receiverId = msg.getHead().getReceiverId();
        String address = (String) REDIS_OPS.getObj(SystemConstant.USER_IN_IM_CLUSTER_PREFIX + receiverId);
        if (address == null) {
            LOGGER.info("用户{}不在线", receiverId);
        } else {
            Channel userChannel = OTHER_SERVERS.get(address);
            if (userChannel != null) {
                userChannel.writeAndFlush(msg);
            }
        }
    }
}
