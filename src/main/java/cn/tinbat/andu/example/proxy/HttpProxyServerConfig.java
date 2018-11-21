package cn.tinbat.andu.example.proxy;

import io.netty.channel.EventLoopGroup;

/**
 * Created by songhangbo on 2018/10/27.
 */
public class HttpProxyServerConfig {

    private EventLoopGroup loopGroup;

    public EventLoopGroup getLoopGroup() {
        return loopGroup;
    }

    public void setLoopGroup(EventLoopGroup loopGroup) {
        this.loopGroup = loopGroup;
    }
}
