package com.lame.hatake.hgit.utils;

public class HexToByteUtils {

    public static byte[] hexToByte(String sha1) {
        char[] chars = sha1.toCharArray();
        byte[] temps = new byte[sha1.length() / 2];
        System.out.println(sha1);
        System.out.println(sha1.length());
        for (int i = 0; i < chars.length; i++) {
            String hex = String.valueOf(new char[]{chars[i], chars[++i]});
            byte b = (byte) Integer.parseInt(hex, 16);
            temps[i / 2] = b;
        }
        return temps;
    }
}
