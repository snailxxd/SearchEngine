package com.booksearch.searcher;

import com.booksearch.model.Book;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.booksearch.util.BookSerializer.*;
import static com.booksearch.util.Constants.*;

public class BookSearcher {

    private static final List<Book> books;
    private static final Map<String, Book> isbnToBookMap;

    static {
        try {
            books = deserializeAll(new File(FILE_PATH));
            isbnToBookMap = books.stream()
                    .collect(Collectors.toMap(Book::getIsbn, book -> book, (a, _) -> a));
        } catch (Exception e) {
            throw new RuntimeException("初始化书籍数据失败", e);
        }
    }

    private final IndexReader reader;
    private final IndexSearcher searcher;
    private final Analyzer analyzer;

    private final String[] SEARCH_FIELDS = new String[]{"title", "author", "contents", "bookDescription", "editorRecommendation"};

    /**
     * 初始化搜索器
     */
    public BookSearcher() throws IOException {
        Directory dir = FSDirectory.open(Paths.get(INDEX_FILE_DIR));
        this.reader = DirectoryReader.open(dir);
        this.searcher = new IndexSearcher(reader);
        this.analyzer = new SmartChineseAnalyzer();
    }

    /**
     * 执行书籍搜索
     */
    public List<Book> search(String queryString, int limit) throws ParseException, IOException {
        MultiFieldQueryParser parser = new MultiFieldQueryParser(SEARCH_FIELDS, analyzer);
        Query query = parser.parse(queryString);
        TopDocs results = searcher.search(query, limit);
        ScoreDoc[] hits = results.scoreDocs;
        System.out.printf("找到 %d 个匹配项\n", results.totalHits.value);

        List<Book> resultBooks = new ArrayList<>();
        for (ScoreDoc hit : hits) {
            Document doc = searcher.storedFields().document(hit.doc);
            Book book = documentToBook(doc);
            if (book != null) {
                resultBooks.add(book);
            }
        }

        return resultBooks;
    }

    /**
     * 从 Lucene Document 中提取 ISBN，然后从内存 Map 中获取完整的 Book 对象。
     * @param doc Lucene Document
     * @return 完整的 Book 对象
     */
    private Book documentToBook(Document doc) {
        String isbn = doc.get("isbn");
        if (isbn == null) {
            System.err.println("Lucene Document 缺少 ISBN 字段，无法恢复完整的 Book 对象");
            return null;
        }
        Book fullBook = isbnToBookMap.get(isbn);
        if (fullBook == null) {
            System.err.println("ISBN " + isbn + " 在内存数据集中找不到对应书籍");
            return null;
        }
        return fullBook;
    }

    /**
     * 关闭 IndexReader
     */
    public void close() throws IOException {
        reader.close();
        System.out.println("索引读取器已关闭");
    }
}