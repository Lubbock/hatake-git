package com.lame.hatake.hgit;

import com.lame.hatake.hgit.utils.FingerprintUtils;
import com.lame.hatake.hgit.utils.HexToByteUtils;


import java.io.File;
import java.io.RandomAccessFile;

/**
 * commit 对象,parent.n 指向了父类的commit 和 sha1
 * //real 应该指向所有父类commit,这里简单处理只计算一个
 * */
public class CommitTree {

    static byte[] type = new byte[]{'c', 'o', 'm', 'm'};

    public static void commitTree(String treeName, String lastCommit) throws Exception{
        //后续仿照writeTree就可以完成了，这里就不做处理了
        try (
                RandomAccessFile raf = new RandomAccessFile(new File(SysConstant.cachePath, "commit"), "rw");
                RandomAccessFile lastRaf = new RandomAccessFile(new File(lastCommit), "rw")
        ) {
            raf.write(type);
            raf.writeLong(0);
            raf.write(new byte[]{'t', 'r', 'e', 'e'});
            String ten = FingerprintUtils.calcFileFingerprint(new File(treeName), FingerprintUtils.SHA1);
            byte[] treesha1 = HexToByteUtils.hexToByte(ten);
            raf.write(treesha1);
            raf.write(new byte[]{'p', 'a', 'r', 'e', 'n', 't'});
            String lten = FingerprintUtils.calcFileFingerprint(new File(lastCommit), FingerprintUtils.SHA1);
            byte[] psha1 = HexToByteUtils.hexToByte(lten);
            raf.write(psha1);
            raf.write(new byte[]{'a','u','t','o','r'});
            raf.write("++++*++++".getBytes());// commit信息，从+++开始
        }
        //该名称存入目录这里就不处理了
    }

    public static void main(String[] args) {
        //传入参数
        //最新的commit 解压后的文件
        //要提交的tree
    }
}
