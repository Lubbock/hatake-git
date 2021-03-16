package com.lame.hatake.hgit.utils;

public class FlowPrint {
    public static void println(String flow, String msg) {
        printflow(flow, msg);
        int jg = msg.length() / 2;
        centerText(flow, jg, 0);
        System.out.print(msg);
        centerText(flow, jg, jg-1);
        System.out.println();
        printflow(flow, msg);
    }

    private static void centerText(String flow, int jg, int i2) {
        for (int i = 0; i < jg; i++) {
            if (i == i2) {
                System.out.print(flow);
            } else {
                System.out.print(" ");
            }
        }
    }


    private static void printflow(String flow, String msg) {
        for (int i = 0; i < msg.length(); i++) {
            System.out.print(flow + "-");
        }
        System.out.println();
    }
}
