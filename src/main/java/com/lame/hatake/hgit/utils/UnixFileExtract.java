package com.lame.hatake.hgit.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class UnixFileExtract {
    public static void main(String[] args)throws Exception {
        Path p = Paths.get("/a/b/Log.txt");
        PosixFileAttributes posix = Files.readAttributes(p, PosixFileAttributes.class);
        //Set Permissions if needs be for the file Log.txt
        Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-r--r--");
        Files.setPosixFilePermissions(p, perms);
        //Output the various attributes of the file named Log.txt
        System.out.println(posix.group());
        System.out.println(posix.permissions());
        System.out.println(posix.owner());
    }
}