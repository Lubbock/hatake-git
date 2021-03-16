package com.lame.hatake.hgit.entry;

import lombok.Data;

@Data
public class ModifierFile {
    private long ctime; //创建时间
    private long mtime;//修改时间
    private long dev;//开发分支
    private long ino;//提交分支
    private int mode;// 提交模式 0 修改 1 增加 2 删除
    private int uid ;
    private int gid;
    private long size;
    private String sha1;
    private short namelen;
    private String name = "";


}
