package com.lame.hatake.hgit.utils;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FingerprintUtils {

    public static String SHA1 = "SHA1";

    public static String SHA256 = "SHA-256";

    public static String MD5 = "MD5";

    public static String calcFileFingerprint(File file, String algorithm) {

        if (!file.isFile()) {
            return null;
        }

        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;

        try {
            digest = MessageDigest.getInstance(algorithm);
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        BigInteger bigInt = new BigInteger(1, digest.digest());
        StringBuilder s = new StringBuilder(bigInt.toString(16));
        for (int j = 0; j < 40 - s.length(); j++) {
            s.insert(0, "0");
        }
        return s.toString();
    }

    /**
     * 获取文件夹中文件的MD5值
     *
     * @param dirFile
     * @param algorithm 所请求算法的名称  for example: MD5, SHA1, SHA-256, SHA-384, SHA-512 etc.
     * @param listChild 是否递归子目录中的文件
     * @return
     */
    public static Map<String, String> calcDirFingerprint(File dirFile,String algorithm, boolean listChild) {

        if (!dirFile.isDirectory()) {
            return null;
        }

        // <filepath,algCode>
        Map<String, String> pathAlgMap = new HashMap<String, String>();
        String algCode;
        File files[] = dirFile.listFiles();

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory() && listChild) {
                pathAlgMap.putAll(Objects.requireNonNull(calcDirFingerprint(file, algorithm, listChild)));
            } else {
                algCode = calcFileFingerprint(file, algorithm);
                if (algCode != null) {
                    pathAlgMap.put(file.getPath(), algCode);
                }
            }
        }
        return pathAlgMap;
    }


}
