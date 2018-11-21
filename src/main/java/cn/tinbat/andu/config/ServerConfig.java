package cn.tinbat.andu.config;

import cn.tinbat.andu.codec.CoolBytes;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Arrays;

/**
 * Created by songhangbo on 2018/11/6.
 */
@Data
@ToString
@NoArgsConstructor
public class ServerConfig {

    private String host;
    private int port;

    public ServerConfig host(String host) {
        this.host = host;
        return this;
    }

    public ServerConfig port(int port) {
        this.port = port;
        return this;
    }

    public byte[] toBytes() {
        byte[] b1 = CoolBytes.string2Bytes(this.host);
        byte[] b2 = CoolBytes.int2Bytes(this.port);
        int len = b1.length;
        b1 = Arrays.copyOf(b1, len + 4);
        System.arraycopy(b2, 0, b1, len, 4);
        return b1;
    }
}
