package cn.tinbat.andu.example.proxy;

import cn.tinbat.andu.example.echo.EchoClientHandler;
import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.util.AsciiString;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private AsciiString contentType = HttpHeaderValues.TEXT_PLAIN;
    private HttpProxyServerConfig serverConfig;

    public HttpHandler(HttpProxyServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest) throws Exception {
        System.out.println("class:" + fullHttpRequest.getClass().getName());
        byte[] dst = new byte[fullHttpRequest.content().readableBytes()];
        ByteBuf bf = fullHttpRequest.content().readBytes(dst);
        System.out.println("isSuccess [" + new String(dst) + "]");
        System.out.println("通过代理");
        // 构造返回数据
        JSONObject jsonRootObj = new JSONObject();
        JSONObject jsonUserInfo = new JSONObject();
        jsonUserInfo.put("id", 1);
        jsonUserInfo.put("name", "张三");
        jsonUserInfo.put("password", "123");
        jsonRootObj.put("userInfo", jsonUserInfo);
        // 获取传递的数据
        Map<String, Object> params = getParamsFromChannel(channelHandlerContext, fullHttpRequest);
        jsonRootObj.put("params", params);
        handleProxyRequest(channelHandlerContext.channel(), fullHttpRequest);
       /* StringBuilder bufResponse = new StringBuilder();
        bufResponse.append(jsonRootObj.toJSONString());
        ByteBuf buffer = Unpooled.copiedBuffer(bufResponse, CharsetUtil.UTF_8);
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK);
        response.content().writeBytes(buffer);
        HttpHeaders heads = response.headers();
        heads.add(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        heads.add(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes()); // 3
        heads.add(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        channelHandlerContext.write(response);*/
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(serverConfig.getLoopGroup()) // 注册线程池
                .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch)
                            throws Exception {
                        ch.pipeline().addLast(
                                new EchoClientHandler());
                    }
                });
    }

    private void handleProxyRequest(Channel channel, FullHttpRequest fullHttpRequest){

    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelReadComplete");
        super.channelReadComplete(ctx);
        ctx.flush(); // 4
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("exceptionCaught");
        if (null != cause) cause.printStackTrace();
        if (null != ctx) ctx.close();
    }

    /**
     * 获取传递的参数
     */
    private static Map<String, Object> getParamsFromChannel(
            ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest)
            throws UnsupportedEncodingException {
        HttpHeaders headers = fullHttpRequest.headers();
//        String strContentType = headers.get("Content-Type").trim();
        String strContentType = "";
        System.out.println("ContentType:" + strContentType);
        Map<String, Object> mapReturnData = new HashMap<String, Object>();
        if (fullHttpRequest.method() == HttpMethod.GET) {
            // 处理get请求
            QueryStringDecoder decoder = new QueryStringDecoder(
                    fullHttpRequest.uri());
            System.out.println("url = " + decoder.uri());
            System.out.println("path = " + decoder.path());
            Map<String, List<String>> parame = decoder.parameters();
            for (Map.Entry<String, List<String>> entry : parame.entrySet()) {
                mapReturnData.put(entry.getKey(), entry.getValue().get(0));
            }
            System.out.println("GET方式：" + parame.toString());
        } else if (fullHttpRequest.method() == HttpMethod.POST) {
            // 处理POST请求
            if (strContentType.contains("x-www-form-urlencoded")) {
                HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(
                        new DefaultHttpDataFactory(false), fullHttpRequest);
                List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();
                for (InterfaceHttpData data : postData) {
                    if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                        MemoryAttribute attribute = (MemoryAttribute) data;
                        mapReturnData.put(attribute.getName(),
                                attribute.getValue());
                    }
                }
            } else if (strContentType.contains("application/json")) {
                // 解析json数据
                ByteBuf content = fullHttpRequest.content();
                byte[] reqContent = new byte[content.readableBytes()];
                content.readBytes(reqContent);
                String strContent = new String(reqContent, "UTF-8");
                System.out.println("接收到的消息" + strContent);
                JSONObject jsonParamRoot = JSONObject.parseObject(strContent);
                for (String key : jsonParamRoot.keySet()) {
                    mapReturnData.put(key, jsonParamRoot.get(key));
                }
            } else {
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                ctx.writeAndFlush(response).addListener(
                        ChannelFutureListener.CLOSE);
            }
            System.out.println("POST方式：" + mapReturnData.toString());
        }
        return mapReturnData;
    }
}
