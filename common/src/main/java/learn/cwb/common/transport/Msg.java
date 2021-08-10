package learn.cwb.common.transport;

import io.netty.buffer.ByteBuf;
import lombok.*;

/**
 * @author CodeWithBuff(给代码来点Buff)
 * @device MacBookPro
 * @time 2021/8/4 15:27
 */
@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Msg {

    public static final int EMPTY_SIZE = Head.HEAD_SIZE;

    @Data
    @With
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Head {
        public enum Type {
            /**
             * 连接状态相关
             */
            ESTABLISH(1 << 1),

            CLOSE(1 << 2),

            PING(1 << 3),

            PONG(1 << 4),

            /**
             * 唯一的异常
             */
            ERROR(1 << 5),

            /**
             * 具体的业务类型
             */
            TEXT(1 << 6),

            BINARY(1 << 7),

            IMG(1 << 8),

            VIDEO(1 << 9),

            VOICE(1 << 10),

            // 小型文件
            BASE64(1 << 11),

            FILE(1 << 12),

            NA(0);

            private final int mark;

            Type(int mark) {
                this.mark = mark;
            }

            @Override
            public String toString() {
                return String.valueOf(mark);
            }

            public static Type build(int mark) {
                return switch (mark) {
                    case 1 << 1 -> ESTABLISH;
                    case 1 << 2 -> CLOSE;
                    case 1 << 3 -> PING;
                    case 1 << 4 -> PONG;
                    case 1 << 5 -> ERROR;
                    case 1 << 6 -> TEXT;
                    case 1 << 7 -> BINARY;
                    case 1 << 8 -> IMG;
                    case 1 << 9 -> VIDEO;
                    case 1 << 10 -> VOICE;
                    case 1 << 11 -> BASE64;
                    case 1 << 12 -> FILE;
                    default -> NA;
                };
            }
        }

        private static final int AUTH_TOKEN_SIZE = 32;

        public static final int HEAD_SIZE = 60 + AUTH_TOKEN_SIZE;

        private Type type;

        // 这里把size定义为消息体的大小，即Body的大小。
        private long size;

        private long[] id;

        private long createdTime;

        private long arrivedTime;

        private long senderId;

        private long receiverId;

        private byte[] authToken;
    }

    @Data
    @With
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Body {
        private byte[] body;
    }

    private Head head;

    private Body body;

    public void serialize(ByteBuf out) {
        // 硬核编码
        out.writeInt(this.head.type.mark);
        out.writeLong(this.head.size);
        out.writeLong(this.head.id[0]);
        out.writeLong(this.head.id[1]);
        out.writeLong(this.head.createdTime);
        out.writeLong(this.head.arrivedTime);
        out.writeLong(this.head.senderId);
        out.writeLong(this.head.receiverId);
        out.writeBytes(this.head.authToken);
        out.writeBytes(this.body.body);
    }

    public static Msg deserialize(ByteBuf src) {
        int type = src.readInt();
        long size = src.readLong();
        long[] id = new long[2];
        id[0] = src.readLong();
        id[1] = src.readLong();
        long createdTime = src.readLong();
        long arrivedTime = src.readLong();
        arrivedTime = System.currentTimeMillis();
        long senderId = src.readLong();
        long receiverId = src.readLong();
        byte[] authToken = new byte[Head.AUTH_TOKEN_SIZE];
        src.readBytes(authToken, 0, authToken.length);
        byte[] body = new byte[(int) size];
        src.readBytes(body, 0, body.length);
        Msg msg = withEmpty();
        Head h = msg.head;
        h.setType(Head.Type.build(type));
        h.setSize(size);
        h.setId(id);
        h.setCreatedTime(createdTime);
        h.setArrivedTime(arrivedTime);
        h.setSenderId(senderId);
        h.setReceiverId(receiverId);
        h.setAuthToken(authToken);
        Body b = msg.body;
        b.setBody(body);
        return msg;
    }

    private static Msg withEmpty() {
        Head h = Head.builder()
                .type(Head.Type.NA)
                .size(0)
                .id(new long[]{0, 0})
                .createdTime(System.currentTimeMillis())
                .arrivedTime(System.currentTimeMillis())
                .senderId(0)
                .receiverId(0)
                .authToken(new byte[Head.AUTH_TOKEN_SIZE])
                .build();
        Body b = Body.builder()
                .body(new byte[0])
                .build();
        Msg msg = Msg.builder()
                .head(h)
                .body(b)
                .build();
        return msg;
    }

    public static Msg withEstablish(long senderId) {
        Msg msg = withEmpty();
        msg.getHead().setType(Head.Type.ESTABLISH);
        msg.getHead().setSenderId(senderId);
        msg.getHead().setReceiverId(0);
        return msg;
    }

    public static Msg withPing() {
        Msg msg = withEmpty();
        Head h = msg.head;
        h.setType(Head.Type.PING);
        return msg;
    }

    public static Msg withPing(long senderId) {
        Msg msg = withEmpty();
        Head h = msg.head;
        h.setType(Head.Type.PING);
        h.setSenderId(senderId);
        return msg;
    }

    public static Msg withPong() {
        Msg msg = withEmpty();
        Head h = msg.head;
        h.setType(Head.Type.PONG);
        return msg;
    }

    public static Msg withPong(long senderId) {
        Msg msg = withEmpty();
        Head h = msg.head;
        h.setType(Head.Type.PONG);
        h.setSenderId(senderId);
        return msg;
    }

    public static Msg withError() {
        Msg msg = withEmpty();
        Head h = msg.head;
        h.setType(Head.Type.ERROR);
        return msg;
    }

    public static Msg withText(String text) {
        byte[] src = text.getBytes();
        Msg msg = withEmpty();
        Head h = msg.head;
        h.setType(Head.Type.TEXT);
        h.setSize(src.length);
        Body b = msg.body;
        b.setBody(src);
        return msg;
    }

    public static Msg withText(String text, long senderId, long receiverId) {
        byte[] src = text.getBytes();
        Msg msg = withEmpty();
        Head h = msg.head;
        h.setType(Head.Type.TEXT);
        h.setSize(src.length);
        h.setSenderId(senderId);
        h.setReceiverId(receiverId);
        Body b = msg.body;
        b.setBody(src);
        return msg;
    }

    public static Msg withBase64(byte[] src) {
        Msg msg = withEmpty();
        Head h = msg.head;
        h.setType(Head.Type.BASE64);
        h.setSize(src.length);
        Body b = msg.body;
        b.setBody(src);
        return msg;
    }
}
