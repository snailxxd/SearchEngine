package com.booksearch.crawler;

import com.booksearch.model.Book;
import static com.booksearch.util.BookSerializer.*;
import static com.booksearch.util.Constants.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.*;

public class BookCrawler {
    private static final Gson GSON = new Gson();
    private static final String BASE_URL = "https:";

    private static class DetailItem {
        String title;
        String content;
    }

    /**
     * 抓取网页 html 文档
     * @param url 要抓取的网页 url
     * @return 抓取得到的 html 文档
     */
    private static Document getDoc(String url) {
        Document doc = null;
        try {
            doc = Jsoup.connect(url).timeout(10000).get();
        }
        catch (Exception e) {
            System.err.println("爬取失败：" + e.getMessage());
        }
        return doc;
    }

    /**
     * 从 html 文档中提取图书分类信息
     * @param doc html 文档
     * @return 图书分类
     */
    private static String extractCategory(Document doc) {
        Elements categoryLinks = doc.select("ol.breadcrumb a");
        if (categoryLinks.isEmpty()) {
            return "<UNK>";
        }
        Element categoryLink = categoryLinks.first();

        if (categoryLink != null) {
            return categoryLink.text();
        }
        return "<UNK>";
    }

    /**
     * 从 html 文档中提取图书标题
     * @param doc html 文档
     * @return 图书标题
     */
    private static String extractTitle(Document doc) {
        Element titleLink = doc.selectFirst("#js-item-name");
        if (titleLink == null) {
            return "<UNK>";
        }
        return titleLink.text();
    }

    /**
     * 从 html 文档中提取图书售价
     * @param doc html 文档
     * @return 图书售价
     */
    private static double extractSalePrice(Document doc) {
        Element priceLink = doc.selectFirst("#js-item-price");
        if (priceLink == null) {
            return 0;
        }
        String price = priceLink.text().replace("￥", "");
        return Double.parseDouble(price);
    }

    /**
     * 从 html 文档中提取图书原价
     * @param doc html 文档
     * @return 图书原价
     */
    private static double extractOriginalPrice(Document doc) {
        Element priceLink = doc.selectFirst("#js-item-originalPrice");
        if (priceLink == null) {
            return 0;
        }
        String price = priceLink.text().replace("￥", "");
        return Double.parseDouble(price);
    }

    /**
     * 从 html 文档中提取图书信息数据
     * @param doc html 文档
     * @return 详细信息表格
     */
    private static Map<String, String> extractTableData(Document doc) {
        Map<String, String> tableData = new LinkedHashMap<>();

        // 定位详细信息表格
        Element tbody = doc.select("table.attribute-tab tbody").first();
        if (tbody == null) {
            System.err.println("找不到详细信息表格");
            return tableData;
        }

        // 逐行读取
        Elements rows = tbody.children();
        for (Element row : rows) {
            if (!row.tagName().equals("tr")) {
                continue;
            }
            Elements keyElement = row.select("td.main-parameter");
            Elements valueElement = row.select("td.parameter-espercial");
            if (keyElement.size() == 1 && valueElement.size() == 1) {
                String key = keyElement.text().trim();
                String value = valueElement.text().trim();
                tableData.put(key, value);
            }
        }
        return tableData;
    }

    private static String extractAndCleanContent(Document doc, String targetTitle) {
        // 定位包含 data-detail 的元素
        Element detailDiv = doc.selectFirst("div.spu-tab-item-detail");
        if (detailDiv == null || !detailDiv.hasAttr("data-detail")) {
            return "<UNK>";
        }

        String dataDetailJson = detailDiv.attr("data-detail");
        try {
            Type listType = new TypeToken<List<DetailItem>>() {}.getType();
            List<DetailItem> details = GSON.fromJson(dataDetailJson, listType);
            if (details == null) {
                return "<UNK>";
            }
            for (DetailItem item : details) {
                if (targetTitle.equals(item.title) && item.content != null) {
                    // 将 content 视为新的 HTML 片段，使用 Jsoup 进行解析和清理
                    return Jsoup.parse(item.content).text();
                }
            }
        } catch (Exception e) {
            System.err.println("解析 data-detail 属性时出错：" + e.getMessage());
            return "<UNK>";
        }
        return "<UNK>";
    }

    /**
     * 从 html 中提取内容简介
     * @param doc html 文档
     * @return 图书内容简介
     */
    private static String extractContentSummary(Document doc) {
        return extractAndCleanContent(doc, "内容简介");
    }

    /**
     * 从 html 中提取作者简介
     * @param doc html 文档
     * @return 作者简介内容
     */
    private static String extractAuthorIntroduction(Document doc) {
        return extractAndCleanContent(doc, "作者简介");
    }

    /**
     * 从 html 中提取编辑推荐
     * @param doc html 文档
     * @return 编辑推荐内容
     */
    private static String extractEditorRecommendation(Document doc) {
        return extractAndCleanContent(doc, "编辑推荐");
    }

    /**
     * 从 html 中提取目录
     * @param doc html 文档
     * @return 目录
     */
    private static String extractContents(Document doc) {
        return extractAndCleanContent(doc, "目录");
    }

    /**
     * 从 url 爬取图书信息
     * @param url 要爬取的图书页面链接
     * @return 图书结构体
     */
    public static Book getBook(String url) {
        Book book = new Book();
        Document doc = getDoc(url);
        book.setTitle(extractTitle(doc));
        book.setCategory(extractCategory(doc));
        book.setSalePrice(extractSalePrice(doc));
        book.setOriginalPrice(extractOriginalPrice(doc));
        Map<String, String> tableData = extractTableData(doc);
        for (Map.Entry<String, String> entry : tableData.entrySet()) {
            switch (entry.getKey()) {
                case "商品编码（ISBN）" -> book.setIsbn(entry.getValue());
                case "出版社" -> book.setPublisher(entry.getValue());
                case "作者" -> book.setAuthor(entry.getValue());
                case "出版时间" -> {
                    String dateText = entry.getValue().trim();
                    if (dateText.length() > 10 && dateText.charAt(10) == ' ') {
                        dateText = dateText.substring(0, 10);
                    } else if (dateText.length() == 7) {
                        dateText = dateText.concat("-01");
                    } else {
                        continue;
                    }
                    LocalDate localDate = LocalDate.parse(dateText);
                    book.setPublicationDate(localDate);
                }
                case "正文语种" -> book.setLanguage(entry.getValue());
            }
        }
        book.setBookDescription(extractContentSummary(doc));
        book.setAuthorDescription(extractAuthorIntroduction(doc));
        book.setEditorRecommendation(extractEditorRecommendation(doc));
        book.setContents(extractContents(doc));

        return book;
    }

    /**
     * 从 url 列表爬取一系列图书信息
     * @param urlList 要爬取的图书页面链接列表
     * @return 图书结构体列表
     */
    public static List<Book> getBooks(List<String> urlList) {
        List<Book> books = new ArrayList<>();
        int total = urlList.size();
        int barLength = 50;

        for (int i = 0; i < total; i++) {
            String url = urlList.get(i);
            Book book = getBook(url);
            books.add(book);

            int completed = i + 1;
            double progress = (double) completed / total;
            int percent = (int) (progress * 100);
            int filledLength = (int) (progress * barLength);

            // 打印爬取进度
            String bar = "#".repeat(filledLength)
                    + "-".repeat(barLength - filledLength);
            PrintStream out = System.out;
            out.print("\r[" + bar + "] " + percent + "% (" + completed + "/" + total + ")");
            out.flush();
        }
        System.out.println();
        return books;
    }

    /**
     * 从分类页面提取出图书 url，并控制爬取的页数
     * @param url 分类页面的基础 url
     * @param maxPages 最大爬取页数
     * @return 图书 url 列表
     */
    public static List<String> getURLs(String url, int maxPages) {
        List<String> urls = new ArrayList<>();
        if (maxPages < 1) {
            return urls;
        }

        for (int pageNo = 1; pageNo <= maxPages; pageNo++) {
            String currentUrl;
            if (pageNo == 1) {
                currentUrl = url;
            } else {
                currentUrl = url + "&pageNo=" + pageNo;
            }

            Document doc = getDoc(currentUrl);
            if (doc == null) {
                continue;
            }

            Elements productElements = doc.select("ul.shop-search-items-img-type li.product");
            if (productElements.isEmpty()) {
                return urls;
            }
            for (Element productElement : productElements) {
                Element link = productElement.selectFirst(".product-image a");
                if (link != null) {
                    String relativeUrl = link.attr("href");
                    if (relativeUrl.startsWith("//")) {
                        relativeUrl = BASE_URL + relativeUrl;
                    }
                    urls.add(relativeUrl);
                }
            }
        }
        return urls;
    }

    public static void main(String[] args) {
        List<String> totalUrlList = new ArrayList<>();
        for (Map.Entry<String, String> entry : CATEGORIES.entrySet()) {
            String categoryName = entry.getKey();
            String categoryId = entry.getValue();
            String currentUrl = "https://search.xhsd.com/search?frontCategoryId=" + categoryId;

            System.out.println("\n正在爬取 " + categoryName + " 类图书 url...");
            List<String> currentUrlList = getURLs(currentUrl, MAX_PAGES);
            System.out.println("爬取到 " + categoryName + " 类图书 url 共 " + currentUrlList.size() + " 条");
            totalUrlList.addAll(currentUrlList);
        }

        System.out.println("\n正在爬取图书信息...");
        List<Book> books = getBooks(totalUrlList);
        System.out.println("爬取到图书信息共 " + books.size() + " 本");

        try {
            System.out.println("\n正在写入文件 " + FILE_PATH + " ...");
            serializeAll(books, new File(FILE_PATH));
            System.out.println("共写入 " + books.size() + " 本图书信息到 " + FILE_PATH);
        } catch (IOException e) {
            System.out.println("文件写入异常" + e);
        }
    }
}
