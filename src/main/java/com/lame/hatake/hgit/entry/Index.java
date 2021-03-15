package com.lame.hatake.hgit.entry;

import java.util.ArrayList;
import java.util.List;

public class Index {

    private byte[] signature = new byte[4];
    private byte[] version = new byte[4];
    private byte[] entries = new byte[4];
    private byte[] sha1 = new byte[20];
    private List<ModifierFile> modifierFiles = new ArrayList<>();
}
