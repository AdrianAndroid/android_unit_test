package com.joyy.android_project;

/**
 * Created by mingli on 2020/05/20.
 * 字符串混淆的工具类，即把字符串的bye高低位交换
 */
public class StringCodeUtils {
    public static byte[] encryptByte(byte[] bb) {
        byte[] res = new byte[bb.length];
        for (int ix = 0; ix < bb.length; ++ix) {
            res[ix] = (byte) (((bb[ix] & 0x0F) << 4) | ((bb[ix] >> 4) & 0x0F));
        }
        return res;
    }

    public static byte[] encryptString(String str) {
        return encryptByte(str.getBytes());
    }

    public static String decodeString(byte[] bb) {
        return new String(encryptByte(bb));
    }
}
