package com.lame.hatake.hgit;

import com.lame.hatake.hgit.entry.ModifierFile;
import com.lame.hatake.hgit.utils.FingerprintUtils;
import com.lame.hatake.hgit.utils.HexToByteUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.NotFileFilter;

import java.io.File;
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

    static String lock = "index.lock";

    static String indexfile = "index";

    static String SIGNATURE = "DIRC";

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
        File f = new File(indexPath, indexfile);
        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
            raf.writeChars(SIGNATURE);
            raf.writeInt(version);// 版本号
            raf.writeInt(0);
        }
        String sha1 = FingerprintUtils.calcFileFingerprint(new File(indexPath, indexfile), FingerprintUtils.SHA1);

        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
            raf.seek(12);
            byte[] fingerprint = HexToByteUtils.hexToByte(sha1);
            raf.write(fingerprint);
        }
        System.out.println(f.length());
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
        }
    }

    public static List<ModifierFile> productModifierFile(String fp)throws Exception {
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
            Long dev =(Long) Files.getAttribute(file.toPath(), "unix:dev");
            Long ino =(Long) Files.getAttribute(file.toPath(), "unix:ino");
            Integer mode =(Integer) Files.getAttribute(file.toPath(), "unix:mode");
            Integer uid = (Integer)Files.getAttribute(file.toPath(), "unix:uid");
            Integer gid = (Integer) Files.getAttribute(file.toPath(), "unix:gid");
            Long size = (Long)Files.getAttribute(file.toPath(), "unix:size");
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
        }
        modifierFiles.forEach(e -> System.out.println(e.toString()));
        return modifierFiles;
    }

    public static void readIndex() throws Exception {
        File f = new File(indexPath, indexfile);
        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
            byte[] signature = new byte[4];
            raf.read(signature);
            int version = raf.readInt();// 版本号
            int entries = raf.readInt();
            System.out.println(String.format("signature:%s version:%d entries:%d ", new String(signature), version, entries));
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
        List<ModifierFile> modifierFiles = productModifierFile("/media/lame/0DD80F300DD80F30/code/hatake-git");
        System.out.println(modifierFiles.size());
        writeIndex(modifierFiles);
        readIndex();
    }
}
