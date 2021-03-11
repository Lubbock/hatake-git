package com.lame.hatake.hgit;

import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

/**
 * java 二进制流读写
 **/
public class ByteIOUtils {

    public static void write() throws Exception {
        File f = new File("randomIo");
        File xf = new File("/media/lame/0DD80F300DD80F30/code/hatake-git/gradle/wrapper/gradle-wrapper.properties");
        try (
                FileInputStream fis = new FileInputStream(xf);
                RandomAccessFile raf = new RandomAccessFile(f, "rw");
        ) {
            raf.writeChars("jar");
            raf.writeLong(xf.length());
            int byteread = 0;
            byte[] read = new byte[1024];
            while ((byteread = fis.read(read)) > -1) {
                raf.write(read);
            }
        }
    }

    public static void invoke() throws Exception {
        File f = new File("randomIo");
        try (
                RandomAccessFile raf = new RandomAccessFile(f, "r");
        ) {
            String suffix = String.valueOf(new char[]{raf.readChar(), raf.readChar(), raf.readChar()});
            long l = raf.readLong();
            byte[] bytes = new byte[(int) l];
            raf.read(bytes);
            System.out.println(String.format("后缀:%s\n长度:%d\n内容:\n%s", suffix, l, new String(bytes)));
        }
    }

    public static void main(String[] args) throws Exception {
        write();
        invoke();
    }
}
