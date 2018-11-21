package cn.tinbat.andu.codec;

import io.netty.util.CharsetUtil;
import io.netty.util.internal.logging.Slf4JLoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.logging.Logger;

public class CoolBytes {
    private byte[] hb;
    private int cap;
    private int offset;
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    public CoolBytes() {
        this.cap = DEFAULT_INITIAL_CAPACITY;
        hb = new byte[DEFAULT_INITIAL_CAPACITY];
        offset = 0;
    }

    public CoolBytes(int cap) {
        hb = new byte[cap];
        this.cap = cap;
        offset = 0;
    }

    public void add(byte b) {
        if (offset == cap) {
            hb = resize();
        }
        hb[offset] = b;
        offset++;
    }

    public String toString() {
        return new String(toArray());
    }

    public byte[] toArray() {
        byte[] newHb = new byte[offset];
        for (int i = 0; i < offset; i++) {
            newHb[i] = hb[i];
        }
        return newHb;
    }

    public int length() {
        return offset;
    }

    final byte[] resize() {
        byte[] oldHb = hb;
        byte[] newHb = new byte[cap << 1];
        for (int i = 0; i < oldHb.length; i++) {
            newHb[i] = oldHb[i];
        }
        cap = cap << 1;
        return newHb;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * Convert hex string to byte[]
     *
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    public static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * 对象转Byte数组
     *
     * @param obj
     * @return
     */
    public static byte[] objectToByteArray(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        ObjectOutputStream objectOutputStream = null;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            bytes = byteArrayOutputStream.toByteArray();

        } catch (IOException e) {
            System.out.println("objectToByteArray failed, " + e);
        } finally {
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    System.out.println("close objectOutputStream failed, " + e);
                }
            }
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                } catch (IOException e) {
                    System.out.println("close byteArrayOutputStream failed, " + e);
                }
            }

        }
        return bytes;
    }

    /**
     * Byte数组转对象
     *
     * @param bytes
     * @return
     */
    public static Object byteArrayToObject(byte[] bytes) {
        Object obj = null;
        ByteArrayInputStream byteArrayInputStream = null;
        ObjectInputStream objectInputStream = null;
        try {
            byteArrayInputStream = new ByteArrayInputStream(bytes);
            objectInputStream = new ObjectInputStream(byteArrayInputStream);
            obj = objectInputStream.readObject();
        } catch (Exception e) {
            System.out.println("byteArrayToObject failed, " + e);
        } finally {
            if (byteArrayInputStream != null) {
                try {
                    byteArrayInputStream.close();
                } catch (IOException e) {
                    System.out.println("close byteArrayInputStream failed, " + e);
                }
            }
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    System.out.println("close objectInputStream failed, " + e);
                }
            }
        }
        return obj;
    }

    public static int bytes2Int(byte[] bytes) {
        return (bytes[0] & 0xff) << 24
                | (bytes[1] & 0xff) << 16
                | (bytes[2] & 0xff) << 8
                | (bytes[3] & 0xff);
    }

    public static byte[] int2Bytes(int num) {
        byte[] bytes = new byte[4];
        bytes[0] = (byte) ((num >> 24) & 0xff);
        bytes[1] = (byte) ((num >> 16) & 0xff);
        bytes[2] = (byte) ((num >> 8) & 0xff);
        bytes[3] = (byte) (num & 0xff);
        return bytes;
    }

    public static int[] bytes2Ints(byte[] bytes) {
        if (bytes.length % 4 != 0) {
            return null;
        }
        int len = bytes.length / 4;
        int[] ints = new int[len];
        for (int i = 0; i < len; i++) {
            ints[i] = (bytes[4 * i] & 0xff) << 24
                    | (bytes[1 + 4 * i] & 0xff) << 16
                    | (bytes[2 + 4 * i] & 0xff) << 8
                    | (bytes[3 + 4 * i] & 0xff);
        }
        return ints;
    }

    public static byte[] ints2Bytes(int[] ints) {
        int len = ints.length * 4;
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[4 * i] = (byte) ((ints[i] >> 24) & 0xff);
            bytes[1 + 4 * i] = (byte) ((ints[i] >> 16) & 0xff);
            bytes[2 + 4 * i] = (byte) ((ints[i] >> 8) & 0xff);
            bytes[3 + 4 * i] = (byte) (ints[i] & 0xff);
        }
        return bytes;
    }

    public static byte [] string2Bytes(String str) {
        return str.getBytes(CharsetUtil.UTF_8);
    }

    public static String bytes2String(byte[] bytes) {
        return new String(bytes, CharsetUtil.UTF_8);
    }


}
