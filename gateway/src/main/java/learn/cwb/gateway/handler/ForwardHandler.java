package learn.cwb.gateway.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import learn.cwb.common.transport.Msg;
import learn.cwb.gateway.redis.RedisOps;
import learn.cwb.gateway.redis.impl.RedisOpsImpl;
import learn.cwb.gateway.system.SystemConstant;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device iMacPro
 * @time 2021/8/8 8:49 下午
 */
public class ForwardHandler extends ChannelInboundHandlerAdapter {
    private static final RedisOps REDIS_OPS = new RedisOpsImpl();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg0) throws Exception {
        Msg msg = (Msg) msg0;
        long receiverId = msg.getHead().getReceiverId();
        String address = (String) REDIS_OPS.getObj(SystemConstant.USER_IN_CLUSTER_PREFIX + receiverId);
        System.out.println(receiverId);
        System.out.println(address);
        // 用户未上线
        if (address == null) {
            ;
        } else {
            Channel channel = GlobalVariable.SERVERS.get(address);
            // 服务器宕机
            if (channel == null) {
                Msg err = Msg.withError();
                Msg.Head head = err.getHead();
                // TODO 错误处理
                ctx.writeAndFlush(err);
            } else {
                System.out.println("write: " + address);
                channel.writeAndFlush(msg);
            }
        }
    }
}
