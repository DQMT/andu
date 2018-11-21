package cn.tinbat.andu.codec;

import cn.tinbat.andu.config.ServerConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledUnsafeDirectByteBuf;

/**
 * Created by songhangbo on 2018/11/6.
 */
public class MessageConfusion {
    public static ByteBuf encode(Object message, ServerConfig serverConfig) {
        TransportMessage t = new TransportMessage();
        t.setMessage(message);
        t.setServerConfig(serverConfig);
        return t.toByteBuf();
    }

    public static Object decode(ByteBuf byteBuf) {
        return new TransportMessage(byteBuf);
    }

    public static ServerConfig getServerConfig(TransportMessage transportMessage) {
        return transportMessage.getServerConfig();
    }

    public static Object decode(TransportMessage tm) {
        return tm.getMessage();
    }
}
