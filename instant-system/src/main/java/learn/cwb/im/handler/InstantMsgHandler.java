package learn.cwb.im.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import learn.cwb.common.transport.Msg;
import learn.cwb.common.transport.MsgRcd;
import learn.cwb.im.kafka.KafkaOps;
import learn.cwb.im.kafka.KafkaOpsImpl;
import learn.cwb.im.redis.RedisOps;
import learn.cwb.im.redis.impl.RedisOpsImpl;
import learn.cwb.im.system.SystemConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 1:38 下午
 */
public class InstantMsgHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstantMsgHandler.class);

    private static final ConcurrentHashMap<Long, Channel> CHANNEL_MAP = GlobalVariable.CHANNEL_MAP;

    private static final RedisOps REDIS_OPS = new RedisOpsImpl();

    private static final KafkaOps KAFKA_OPS = new KafkaOpsImpl();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg0) throws Exception {
        Msg msg = (Msg) msg0;
        System.out.println(msg.getHead().getReceiverId());
        long receiverId = msg.getHead().getReceiverId();
        // 接收者不在此服务器，或下线了
        if (!CHANNEL_MAP.containsKey(receiverId)) {
            // 转发到网关查找
            GlobalVariable.GATEWAY.writeAndFlush(msg);
        } else {
            Channel userChannel = CHANNEL_MAP.get(receiverId);
            MsgRcd msgRcd = MsgRcd.withMsg(msg);
            // 存放到同步队列
            // TODO 开启异步写入
            KAFKA_OPS.put(msgRcd.getQueueId(), msgRcd);
            userChannel.writeAndFlush(msg);
        }
        // 设置用户收件箱
        REDIS_OPS.setZSet(SystemConstant.USER_INBOX_PREFIX + receiverId, msg, msg.getHead().getArrivedTime());
    }
}
