package com.booksearch.crawler;

import com.booksearch.model.Book;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class BookCrawler {

    /**
     * 抓取网页 html 文档
     * @param url 要抓取的网页 url
     * @return 抓取得到的 html 文档
     */
    public static Document getDoc(String url) {
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
    public static String extractCategory(Document doc) {
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
    public static String extractTitle(Document doc) {
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
    public static double extractSalePrice(Document doc) {
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
    public static double extractOriginalPrice(Document doc) {
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
    public static Map<String, String> extractTableData(Document doc) {
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
                    LocalDate localDate = LocalDate.parse(entry.getValue());
                    book.setPublicationDate(localDate);
                }
                case "正文语种" -> book.setLanguage(entry.getValue());
            }
        }

        return book;
    }

    public static void main(String[] args) {
        String url = "https://item.xhsd.com/items/1010000103423030";
        Document doc = getDoc(url);
        Map<String, String> tableData = extractTableData(doc);
        System.out.println(tableData);
        double originalPrice = extractOriginalPrice(doc);
        System.out.println(originalPrice);
        double price = extractSalePrice(doc);
        System.out.println(price);
        System.out.println(extractTitle(doc));
        System.out.println(extractCategory(doc));
    }

}
