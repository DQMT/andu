package cn.tinbat.andu.codec;

import cn.tinbat.andu.config.ServerConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.*;
import jdk.nashorn.internal.objects.annotations.Constructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Created by songhangbo on 2018/11/6.
 */
@Data
@ToString
@NoArgsConstructor
public class TransportMessage {
    private ServerConfig serverConfig;
    private Object message;//
    private static volatile int index = 0;

    public TransportMessage(ByteBuf byteBuf) {
        int readableBytes = byteBuf.readableBytes();
        int flag = byteBuf.getInt(readableBytes - 4);
        System.out.println("readableBytes = " + readableBytes);
        System.out.println("flag = " + flag);
        int bc = byteBuf.getInt(readableBytes - 8);
        System.out.println("bc = " + bc);
        int port = byteBuf.getInt(readableBytes - 12);
        byte[] hostBytes = new byte[bc - 4];
        byteBuf.getBytes(readableBytes - 8 - bc, hostBytes, 0, bc - 4);
        String host = CoolBytes.bytes2String(hostBytes);
        System.out.println("host = " + host);
        System.out.println("port = " + port);
        this.serverConfig = new ServerConfig().host(host).port(port);

        if (flag == 0) {
            System.out.println("HTTPS处理");
            int index = byteBuf.getInt(readableBytes - 8 - bc - 4);
            System.out.println("http index = " + index);
            this.message = byteBuf.slice(0, readableBytes - 8 - bc - 4);
        } else {
            int index = byteBuf.getInt(readableBytes - 8 - bc - 4);
            System.out.println("http index = " + index);
           /* this.message = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
                    host,byteBuf.slice(0, readableBytes - 8 - bc)
                    );*/
            System.out.println("HTTP暂不处理");
        }
    }

    public ByteBuf toByteBuf() {
        ByteBuf byteBuf = null;
        byte[] sc = new byte[0];
        System.out.println("host = " + serverConfig.getHost());
        System.out.println("port = " + serverConfig.getPort());
        if (message instanceof FullHttpRequest) {
            byteBuf = ((FullHttpRequest) message).content();
            sc = serverConfig.toBytes();
            byteBuf.capacity(byteBuf.capacity() + 4 + sc.length + 4 + 4);
            byteBuf.writeInt(index);
            System.out.println("http index = " + index);
            index++;
            byteBuf.writeBytes(sc);
            byteBuf.writeInt(sc.length);
            byteBuf.writeInt(1);
            System.out.println("flag = " + 1);
        }
        if (message instanceof ByteBuf) {
            byteBuf = (ByteBuf) message;
            sc = serverConfig.toBytes();
            byteBuf.capacity(byteBuf.capacity() + 4 + sc.length + 4 + 4);
            byteBuf.writeInt(index);
            System.out.println("https index = " + index);
            index++;
            byteBuf.writeBytes(sc);
            byteBuf.writeInt(sc.length);
            byteBuf.writeInt(0);
            System.out.println("flag = " + 0);
        }
        System.out.println("bc = " + sc.length);
        return byteBuf;
    }

}
