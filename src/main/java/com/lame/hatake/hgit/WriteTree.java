package com.lame.hatake.hgit;

import com.lame.hatake.hgit.utils.FingerprintUtils;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class WriteTree {

    static byte[] type = new byte[]{'t', 'r', 'e', 'e'};

    public static long writeTree(long pos) throws Exception{
        // commit 或者其他配置文件得到当前读到了那个位置
        List<ModifyFile> files = new ArrayList<>();
        long tpos = pos;
        try (
                RandomAccessFile raf = new RandomAccessFile(new File(SysConstant.indexPath, SysConstant.indexfile), "rw");
              ) {
            tpos = raf.length();
            raf.seek(pos);
            while (raf.getFilePointer() < raf.length()){
                ModifyFile mf = new ModifyFile();
                raf.skipBytes(32);
                mf.mode = raf.readInt();
                raf.skipBytes(16);
                byte[] sha1 = new byte[20];
                raf.read(sha1);
                mf.sha1 = sha1;
                mf.namelen = raf.readShort();
                byte[] nb = new byte[mf.namelen];
                raf.read(nb);
                mf.name = nb;
                files.add(mf);
            }
//            byte[] bf = new byte[1024];
//            oaf.write(type);
//            oaf.writeLong(raf.length() - 32);
//
        }
        if (files.size() == 0) {
            return pos;
        }
        try (
                RandomAccessFile oaf = new RandomAccessFile(new File(SysConstant.cachePath, "tree_temp"), "rw")
        ){
            oaf.write(type);
            oaf.writeLong(0);
            for (ModifyFile file : files) {
                oaf.writeInt(file.mode);
                oaf.writeShort(file.namelen);
                oaf.write(file.name);
                oaf.write(file.sha1);
            }
            oaf.seek(4);
            oaf.writeLong(oaf.length());
        }
        String fingerprint = FingerprintUtils.calcFileFingerprint(new File(SysConstant.cachePath, "tree_temp"), FingerprintUtils.SHA1);
        File f = new File(SysConstant.cachePath, "tree_temp");
        System.out.println(fingerprint);
        f.renameTo(new File(SysConstant.objPath + File.separator + fingerprint.substring(0, 2), fingerprint));
        return tpos;
    }

    public static class ModifyFile {
        private int mode;// 提交模式
        private short namelen;
        private byte[] name;
        private byte[] sha1;
    }

    public static void main(String[] args) throws Exception{
        writeTree(32);
    }
}
