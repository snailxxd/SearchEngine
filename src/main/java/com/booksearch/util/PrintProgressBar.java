package com.booksearch.util;

import java.io.PrintStream;

public class PrintProgressBar {
    /**
     * 打印进度条
     * @param completed 已完成项目数
     * @param total 总数
     */
    public static void printProgressBar(int completed, int total) {
        if (total <= 0) return;

        int barLength = 50;
        double progress = (double) completed / total;
        int percent = (int) (progress * 100);
        int filledLength = (int) (progress * barLength);

        // 构造进度条字符串
        String bar = "#".repeat(filledLength)
                + "-".repeat(barLength - filledLength);

        PrintStream out = System.out;
        out.print("\r[" + bar + "] " + percent + "% (" + completed + "/" + total + ")");
        out.flush();

        if (completed == total) {
            System.out.println();
        }
    }
}
