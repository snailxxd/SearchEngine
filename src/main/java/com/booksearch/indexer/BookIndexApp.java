package com.booksearch.indexer;

import com.booksearch.model.Book;
import static com.booksearch.util.BookSerializer.*;
import static com.booksearch.util.Constants.*;
import static com.booksearch.util.PrintProgressBar.printProgressBar;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class BookIndexApp {
    public static void main(String[] args) {
        try {
            BookIndexer bookIndexer = new BookIndexer();

            System.out.println("正在读取 Book 文件...");
            List<Book> books = deserializeAll(new File(FILE_PATH));
            if (books.isEmpty()) {
                System.out.println("未加载到任何书籍数据");
                return;
            }

            System.out.printf("已加载 %d 本书籍，开始创建索引...\n", books.size());
            int total = books.size();
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                bookIndexer.indexBook(book);
                printProgressBar(i+1, total);
            }
            long endTime = System.currentTimeMillis();
            System.out.printf("所有书籍索引完成。耗时: %.2f 秒\n", (endTime - startTime) / 1000.0);
            bookIndexer.close();
        } catch (IOException e) {
            System.out.println("构建索引过程失败" + e);
        }
    }
}
