package com.booksearch.cli;

import com.booksearch.searcher.BookSearcher;
import com.booksearch.model.Book;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * 命令行客户端应用，用于与 BookSearcher 交互。
 */
public class ClientApp {

    private static BookSearcher searcher;
    private static final String SEPARATOR = "----------------------------------------";

    /**
     * 程序入口
     */
    public static void main(String[] args) {
        System.out.println("启动图书搜索客户端...");

        try {
            // 尝试初始化搜索器
            searcher = new BookSearcher();
            System.out.println("搜索引擎初始化成功！索引已加载");
            startInteractiveLoop();

        } catch (IOException e) {
            System.err.println("无法初始化搜索引擎或加载索引" + e.getMessage());
        } catch (Exception e) {
            System.err.println("发生未知错误：" + e.getMessage());
        }
    }

    /**
     * 启动交互式命令行循环。
     */
    private static void startInteractiveLoop() throws IOException {
        Scanner scanner = new Scanner(System.in);
        String query;

        while (true) {
            System.out.println("\n" + SEPARATOR);
            System.out.println("请输入搜索关键词 (输入 ':quit' 退出):");
            System.out.print("----关键词: ");
            if (scanner.hasNextLine()) {
                query = scanner.nextLine().trim();
            } else {
                break;
            }
            if (query.equalsIgnoreCase(":quit")) {
                System.out.println("即将关闭...");
                break;
            }
            if (query.isEmpty()) {
                System.out.println("请输入有效的关键词");
                continue;
            }

            // 执行搜索
            List<Book> results = performSearch(query);
            if (results != null && results.isEmpty()) {
                continue;
            }

            int index;
            System.out.println("输入序号查看相应图书详细信息 (输入 ':skip' 跳过)");
            System.out.print("----序号: ");
            if (scanner.hasNextLine()) {
                String str = scanner.nextLine().trim();
                if (str.isEmpty() || str.equals("skip")) {
                    continue;
                }
                index = Integer.parseInt(str);
                if (results != null && (index < 0 || index > results.size())) {
                    continue;
                }
            } else {
                continue;
            }
            Book book = null;
            if (results != null) {
                book = results.get(index);
            }
            System.out.println(SEPARATOR);
            System.out.printf("%s\n", book.getTitle());
            System.out.printf("    作者：%s\n", book.getAuthor());
            System.out.printf("    ISBN：%s\n", book.getIsbn());
            System.out.printf("    出版社：%s\n", book.getPublisher());
            System.out.println("    出版日期：" + book.getPublicationDate());
            System.out.printf("    语言：%s\n", book.getAuthor());
            System.out.printf("    售价：%.2f\n", book.getSalePrice());
            if (book.getOriginalPrice() != 0) {
                System.out.printf("    原价：%.2f\n", book.getOriginalPrice());
            }
            System.out.printf("    类别：%s\n", book.getCategory());
            System.out.printf("    目录：%s\n", book.getContents());
            System.out.printf("    简介：%s\n", book.getBookDescription());
            System.out.printf("    作者简介：%s\n", book.getAuthorDescription());
            System.out.printf("    编辑推荐：%s\n", book.getEditorRecommendation());
            System.out.println(SEPARATOR);
        }
        searcher.close();
        scanner.close();
    }

    /**
     * 执行搜索并显示结果。
     *
     * @param query 搜索关键词
     */
    private static List<Book> performSearch(String query) {
        System.out.println(SEPARATOR);
        System.out.printf("正在搜索: \"%s\"...\n", query);

        try {
            List<Book> results = searcher.search(query, com.booksearch.util.Constants.MAX_SEARCH_RESULTS);
            if (results.isEmpty()) {
                System.out.println("未找到匹配图书,请尝试其他关键词");
                return null;
            }

            System.out.printf("前 %d 本图书：\n", results.size());
            int rank = 1;
            for (Book book : results) {
                System.out.println(SEPARATOR);
                System.out.printf("[%d] %s\n", rank++, book.getTitle());
                System.out.printf("    作者: %s\n", book.getAuthor());
                System.out.printf("    ISBN: %s\n", book.getIsbn());
                System.out.printf("    出版社: %s (%s)\n", book.getPublisher(), book.getPublicationDate());
                System.out.printf("    价格: ¥%.2f ", book.getSalePrice());
                if (book.getOriginalPrice() != 0) {
                    System.out.printf("(原价: ¥%.2f)\n", book.getOriginalPrice());
                } else {
                    System.out.print("\n");
                }
            }
            System.out.println(SEPARATOR);
            return results;
        } catch (Exception e) {
            System.err.println("搜索过程中发生错误" + e.getMessage());
        }
        return null;
    }
}