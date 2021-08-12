package learn.cwb.im.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import learn.cwb.common.transport.Msg;
import learn.cwb.common.transport.MsgRcd;
import learn.cwb.common.kafka.KafkaOps;
import learn.cwb.common.kafka.impl.KafkaOpsImpl;
import learn.cwb.common.redis.RedisOps;
import learn.cwb.common.redis.impl.RedisOpsImpl;
import learn.cwb.im.system.SystemConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 1:38 下午
 */
public class InstantMsgHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstantMsgHandler.class);

    private static final ConcurrentHashMap<Long, Channel> CHANNEL_MAP = GlobalVariable.CHANNEL_MAP;

    private static final ExecutorService THREAD_POOL = GlobalVariable.THREAD_POOL;

    private static final RedisOps REDIS_OPS = new RedisOpsImpl();

    private static final KafkaOps KAFKA_OPS = new KafkaOpsImpl();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg0) {
        Msg msg = (Msg) msg0;
        long senderId = msg.getHead().getSenderId();
        long receiverId = msg.getHead().getReceiverId();
        if (receiverId < 0) {
            CHANNEL_MAP.entrySet().stream()
                    .filter(e -> e.getKey() > 0)
                    .forEach(e -> e.getValue().writeAndFlush(msg));
        } else if (!CHANNEL_MAP.containsKey(receiverId)) {
            LOGGER.info("对{}的请求转发到其他服务器处理", receiverId);
            THREAD_POOL.execute(() -> {
                // 设置用户收件箱
                REDIS_OPS.setZSet(SystemConstant.USER_INBOX_PREFIX + receiverId, msg, msg.getHead().getArrivedTime());
            });
            ctx.fireChannelRead(msg0);
        } else {
            Channel userChannel = CHANNEL_MAP.get(receiverId);
            MsgRcd msgRcd = MsgRcd.withMsg(msg);
            THREAD_POOL.execute(() -> {
                // 设置用户收件箱
                REDIS_OPS.setZSet(SystemConstant.USER_INBOX_PREFIX + receiverId, msg, msg.getHead().getArrivedTime());
                // 存放到同步队列
                KAFKA_OPS.put(msgRcd.getQueueId(), msgRcd);
            });
            userChannel.writeAndFlush(msg);
        }
    }
}
