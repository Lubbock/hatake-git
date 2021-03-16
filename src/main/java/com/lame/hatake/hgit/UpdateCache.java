package com.lame.hatake.hgit;

import com.lame.hatake.hgit.entry.ModifierFile;
import com.lame.hatake.hgit.utils.FingerprintUtils;
import com.lame.hatake.hgit.utils.FlowPrint;
import com.lame.hatake.hgit.utils.HexToByteUtils;
import com.lame.hatake.hgit.utils.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.NotFileFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p> update Cache 生成index 文件</p>
 **/
public class UpdateCache {
    static String indexPath = ".hit/.dirache/";

    static String cachePath = ".hit/temp/";

    static String objPath = ".hit/objects/";

    static String lock = "index.lock";

    static String indexfile = "index";

    static byte[] SIGNATURE = new byte[]{'D', 'I', 'R', 'C'};

    static int FILE_HEADER = 32;

    static int version = 1;

    public void releaseLock() {
        new File(indexPath, lock).delete();
    }

    public void addLock() throws Exception {
        new File(indexPath, lock).createNewFile();
    }

    public static boolean isLock() {
        return new File(indexPath, indexPath).exists();
    }

    public static void initIndex() throws Exception {
        File cp = new File(cachePath);
        if (!cp.exists()) {
            cp.mkdirs();
        }
        File f = new File(indexPath, indexfile);
        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
            raf.write(SIGNATURE);
            raf.writeInt(version);// 版本号
            raf.writeInt(0);
        }
        String sha1 = FingerprintUtils.calcFileFingerprint(new File(indexPath, indexfile), FingerprintUtils.SHA1);

        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
            raf.seek(12);
            byte[] fingerprint = HexToByteUtils.hexToByte(sha1);
            raf.write(fingerprint);
        }
    }


    public static void writeIndex(List<ModifierFile> modifierFiles) throws Exception {
        if (isLock()) {
            throw new RuntimeException("存储库正在被使用");
        }
        File f = new File(indexPath, indexfile);
        if (!f.exists()) {
            initIndex();
        }
        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
            raf.seek(raf.length());
            // 快进到文件末尾
            for (ModifierFile modifierFile : modifierFiles) {
                raf.writeLong(modifierFile.getCtime());
                raf.writeLong(modifierFile.getMtime());
                raf.writeLong(modifierFile.getDev());
                raf.writeLong(modifierFile.getIno());
                raf.writeInt(modifierFile.getMode());
                raf.writeInt(modifierFile.getUid());
                raf.writeInt(modifierFile.getGid());
                raf.writeLong(modifierFile.getSize());
                byte[] temps = HexToByteUtils.hexToByte(modifierFile.getSha1());
                raf.write(temps);
                raf.writeShort(modifierFile.getNamelen());
                raf.write(modifierFile.getName().getBytes(StandardCharsets.UTF_8));
            }
        }
        String sha1 = FingerprintUtils.calcFileFingerprint(f, FingerprintUtils.SHA1);
        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
            raf.seek(FILE_HEADER);
            raf.write(HexToByteUtils.hexToByte(sha1));
            raf.seek(8);// 到entry 位置
            int oldEntry = raf.readInt();
            raf.seek(8);
            oldEntry = oldEntry + modifierFiles.size();
            raf.writeInt(oldEntry);
        }
    }

    public static List<ModifierFile> productModifierFile(String fp) throws Exception {
        NotFileFilter filter = new NotFileFilter(
                FileFilterUtils.or(
                        FileFilterUtils.suffixFileFilter("gitignore"),
                        FileFilterUtils.suffixFileFilter("class"),
                        FileFilterUtils.suffixFileFilter("jar")
                )
        );

        NotFileFilter dirfilter = new NotFileFilter(
                FileFilterUtils.or(
                        FileFilterUtils.nameFileFilter(".idea"),
                        FileFilterUtils.nameFileFilter("gradle"),
                        FileFilterUtils.nameFileFilter(".git"),
                        FileFilterUtils.nameFileFilter(".hit")));
        Collection<File> files = FileUtils.listFiles(new File(fp), filter, dirfilter);
        List<ModifierFile> modifierFiles = new ArrayList<>();
        for (File file : files) {
//            Files.getFileStore(file.toPath).supportsFileAttributeView("unix")
            //todo 先不做操作系统适配
            Long dev = (Long) Files.getAttribute(file.toPath(), "unix:dev");
            Long ino = (Long) Files.getAttribute(file.toPath(), "unix:ino");
            Integer mode = (Integer) Files.getAttribute(file.toPath(), "unix:mode");
            Integer uid = (Integer) Files.getAttribute(file.toPath(), "unix:uid");
            Integer gid = (Integer) Files.getAttribute(file.toPath(), "unix:gid");
            Long size = (Long) Files.getAttribute(file.toPath(), "unix:size");
            PosixFileAttributes posix = Files.readAttributes(Paths.get(file.getAbsolutePath()), PosixFileAttributes.class);
            ModifierFile mf = new ModifierFile();
            mf.setCtime(posix.creationTime().toMillis());
            mf.setMtime(posix.lastModifiedTime().toMillis());
            mf.setDev(dev);
            mf.setIno(ino);
            mf.setMode(mode);
            mf.setUid(uid);
            mf.setGid(gid);
            mf.setSize(size);
            mf.setName(file.getAbsolutePath()); // 存储文件的相对路径相对当前执行路径的
            mf.setNamelen((short) file.getAbsolutePath().length());
            modifierFiles.add(mf);
            String fingerprint = FingerprintUtils.calcFileFingerprint(file, FingerprintUtils.SHA1);
            mf.setSha1(fingerprint);
            createBlob(file, fingerprint);
        }
//        modifierFiles.forEach(e -> System.out.println(e.toString()));
        return modifierFiles;
    }

    public static void createBlob(File f, String sha1) throws Exception {
        try (FileInputStream fis = new FileInputStream(f);
             RandomAccessFile raf = new RandomAccessFile(new File(indexPath, sha1), "rw")) {
            raf.write(new byte[]{'b', 'l', 'o', 'b'});
            raf.writeLong(f.length());
            byte[] bt = new byte[1024];
            while (fis.read(bt) > -1) {
                raf.write(bt);
            }
        }

        File waitYs = new File(indexPath, sha1);
        System.out.println(sha1);
        ZipUtil.archiveFiles2Zip(new File[]{waitYs}, new File(objPath + File.separator + sha1.substring(0, 2), sha1), true);
    }

    public static File extractBlob(File file) throws Exception {
        String sha1 = file.getName();
        ZipUtil.decompressZip2Files(file, new File(cachePath));
        try (
                RandomAccessFile raf = new RandomAccessFile(new File(cachePath, sha1), "rw");
                FileOutputStream fos = new FileOutputStream(new File(cachePath, sha1 + "_temp"))
        ) {
            raf.seek(12);
            byte[] bt = new byte[1024];
            while (raf.read(bt) > -1) {
                fos.write(bt);
            }
        }
        File tempFile = new File(cachePath, sha1);
        if (tempFile.exists()) {
            tempFile.delete();
        }
        return new File(cachePath, sha1 + "_temp");
    }


    public static void readIndex() throws Exception {
        File f = new File(indexPath, indexfile);
        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
            byte[] signature = new byte[4];
            raf.read(signature);
            int version = raf.readInt();// 版本号
            int entries = raf.readInt();
            FlowPrint.println("=", String.format("signature:%s version:%d entries:%d ", new String(signature), version, entries));
            raf.skipBytes(20);
            int i = 0;
            while (true) {
                if (raf.getFilePointer() >= raf.length()) {
                    break;
                }
                long ctime = raf.readLong();
                long mtime = raf.readLong();
                long dev = raf.readLong();
                long ino = raf.readLong();
                int mode = raf.readInt();
                int uid = raf.readInt();
                int gid = raf.readInt();
                long size = raf.readLong();
                byte[] bytes = new byte[20];
                raf.read(bytes);
                short namelen = raf.readShort();
                byte[] names = new byte[namelen];
                raf.read(names);
                String name = new String(names);
                System.out.println(String.format("filenumber:%d names:%s uid:%d", ++i, name, uid));

            }
        }
    }

    public static void main(String[] args) throws Exception {
//        List<ModifierFile> modifierFiles = productModifierFile("/media/lame/0DD80F300DD80F30/code/hatake-git");
//        writeIndex(modifierFiles);
//        readIndex();
    }
}
