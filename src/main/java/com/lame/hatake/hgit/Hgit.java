package com.lame.hatake.hgit;

import java.io.File;

public class Hgit {

    static void initIndex() throws Exception {
        new File(".hit/.dirache/").mkdirs();
        File f = new File(".hit/.dirache/index");
        f.createNewFile();
    }

    static void init() throws Exception {
        initIndex();
    }

    public static void main(String[] args) throws Exception {
        init();
    }
}
