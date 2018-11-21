package cn.tinbat.andu.crypto.outer.server;

import cn.tinbat.andu.codec.MessageConfusion;
import cn.tinbat.andu.codec.TransportMessage;
import cn.tinbat.andu.config.ServerConfig;
import cn.tinbat.andu.example.HttpProxyInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;

/**
 * Created by songhangbo on 2018/11/6.
 */
@SuppressWarnings("Duplicates")
public class CryptoOuterServerHandleTM extends SimpleChannelInboundHandler<ByteBuf> {
    private ChannelFuture cf;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("channelRead");
        this.channelRead0(ctx, (ByteBuf) msg);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx,final ByteBuf in) throws Exception {
        ServerConfig serverConfig;
        System.out.println(
                "Client received: " + in);
        TransportMessage tm = null;
        if (in != null) {
            System.out.println("TransportMessage received");
            tm = new TransportMessage(in);
            serverConfig = MessageConfusion.getServerConfig(tm);
        } else {
            System.out.println("null received");
            return;
        }
        final Object msg = tm.getMessage();
        if (msg == null) {
            System.out.println("message == null");
            return;
        }
        System.out.println(this.getClass().getSimpleName() + " channelRead : msg is a " + msg.getClass().getSimpleName());
        if (msg instanceof FullHttpRequest) {
            //http 请求
            //连接至目标服务器
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(ctx.channel().eventLoop()) // 注册线程池
                    .channel(ctx.channel().getClass()) // 使用NioSocketChannel来作为连接用的channel类
                    .handler(new HttpProxyInitializer(ctx.channel()));
            System.out.println("http connect " + serverConfig.getHost() + ":" + serverConfig.getPort());
            ChannelFuture cf = bootstrap.connect(serverConfig.getHost(), serverConfig.getPort());
            cf.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        future.channel().writeAndFlush(msg);
                    } else {
                        ctx.channel().close();
                    }
                }
            });
        } else { // https 请求
            if (cf == null) {
                //连接至目标服务器
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(ctx.channel().eventLoop()) // 复用客户端连接线程池
                        .channel(ctx.channel().getClass()) // 使用NioSocketChannel来作为连接用的channel类
                        .handler(new ChannelInitializer() {

                            @Override
                            protected void initChannel(Channel ch) throws Exception {
                                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx0, Object msg) throws Exception {
                                        ctx.channel().writeAndFlush(msg);
                                    }
                                });
                            }
                        });
                System.out.println("https connect " + serverConfig.getHost() + ":" + serverConfig.getPort());
                cf = bootstrap.connect(serverConfig.getHost(), serverConfig.getPort());
                cf.addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            future.channel().writeAndFlush(msg);
                        } else {
                            ctx.channel().close();
                        }
                    }
                });
            } else {
                cf.channel().writeAndFlush(msg);
            }
        }

    }


}
