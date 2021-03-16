package com.lame.hatake.hgit;

import java.io.File;

public class ObjectInit {
    static String objPath = ".hit/objects/";

    static void initObject(){
        File f = new File(objPath);
        if (!f.exists()) {
            f.mkdirs();
        }
        //循环生成子目录 00-FF
        for (int i = 0; i < 256; i++) {
            String s = Integer.toHexString(i);
            if (i < 16) {
                s = "0" + s;
            }
            File temp = new File(objPath, s);
            if (!temp.exists()) {
                temp.mkdirs();
            }
        }
    }

    public static void main(String[] args) {
        initObject();
    }
}
