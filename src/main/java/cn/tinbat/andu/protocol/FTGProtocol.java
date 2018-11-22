package cn.tinbat.andu.protocol;

import cn.tinbat.andu.config.ConstantValue;

/**
 *  数据包格式
 * +——----——+——-----——+——----——+
 * |协议开始标志|  长度       |   数据     |
 * +——----——+——-----——+——----——+
 * 1.协议开始标志head，为int类型的数据，16进制表示为0X222
 * 2.传输数据的长度len，int类型
 * 3.要传输的数据
 *
 */
public class FTGProtocol  {

    private int head = ConstantValue.FTG_HEAD;
    private int index;
    private int payloadLength;

    private byte[] payload;


}
