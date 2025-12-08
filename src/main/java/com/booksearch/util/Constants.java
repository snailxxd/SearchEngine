package com.booksearch.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class Constants {
    public static final int MAX_PAGES = 10;
    public static final String FILE_PATH = "D:\\Files\\Java\\hw\\hw1\\SearchEngine\\data\\books.json";
    public static final String INDEX_FILE_DIR = "D:\\Files\\Java\\hw\\hw1\\SearchEngine\\data\\index";
    public static final int MAX_SEARCH_RESULTS = 20;
    public static final Map<String, String> CATEGORIES = new LinkedHashMap<>();
    static {
        CATEGORIES.put("人文社科", "34");
        CATEGORIES.put("文学艺术", "33");
        CATEGORIES.put("少儿童书", "35");
        CATEGORIES.put("教育考试", "36");
        CATEGORIES.put("经济金融", "37");
        CATEGORIES.put("生活休闲", "38");
        CATEGORIES.put("科学技术", "39");
        CATEGORIES.put("计算机类", "40");
        CATEGORIES.put("外文原版", "586");
    }
}
