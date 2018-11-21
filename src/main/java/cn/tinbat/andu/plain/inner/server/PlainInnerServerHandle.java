package cn.tinbat.andu.plain.inner.server;

import cn.tinbat.andu.config.ServerConfig;
import cn.tinbat.andu.example.HttpProxyInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;

/**
 * Created by songhangbo on 2018/11/6.
 */
@SuppressWarnings("Duplicates")
public class PlainInnerServerHandle extends ChannelInboundHandlerAdapter {
    private ServerConfig serverConfig;
    private ChannelFuture cf;
    private String host;
    private int port;

    public PlainInnerServerHandle(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx,final Object msg) throws Exception {
        System.out.println(this.getClass().getSimpleName()+" channelRead : msg is a "+msg.getClass().getSimpleName());
        if (msg instanceof FullHttpRequest) {// http请求 或 HTTPS建立握手
            FullHttpRequest request = (FullHttpRequest) msg;
            System.out.println(request);
            String host = request.headers().get("host");
            String[] temp = host.split(":");
            int port = 80;
            if (temp.length > 1) {
                port = Integer.parseInt(temp[1]);
            } else {
                if (request.uri().indexOf("https") == 0) {
                    port = 443;
                }
            }
            this.host = temp[0];
            this.port = port;
            System.out.println(this.getClass().getSimpleName() + "host = " + host + " port = " + port);
            if ("CONNECT".equalsIgnoreCase(request.method().name())) {//HTTPS建立代理握手
                System.out.println("HTTPS建立代理握手");
                HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                ctx.writeAndFlush(response);
                ctx.pipeline().remove("httpCodec");
                ctx.pipeline().remove("httpObject");
                return;
            }
            //连接至目标服务器 http请求
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(ctx.channel().eventLoop()) // 注册线程池
                    .channel(ctx.channel().getClass()) // 使用NioSocketChannel来作为连接用的channel类
                    .handler(new HttpProxyInitializer(ctx.channel()));
            System.out.println("http connect "+serverConfig.getHost()+":"+serverConfig.getPort());
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
        } else { // https 此时msg是一个PooledUnsafeDirectByteBuf
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
                System.out.println("https connect "+host+":"+port);
                cf = bootstrap.connect(host, port);
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
