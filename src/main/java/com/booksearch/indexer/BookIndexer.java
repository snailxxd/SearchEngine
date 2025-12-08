package com.booksearch.indexer;

import com.booksearch.model.Book;
import static com.booksearch.util.Constants.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZoneOffset;

public class BookIndexer {
    private final IndexWriter writer;

    /**
     * 初始化 IndexManager
     */
    public BookIndexer() throws IOException {
        Directory dir = FSDirectory.open(Paths.get(INDEX_FILE_DIR));
        Analyzer analyzer = new SmartChineseAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        this.writer = new IndexWriter(dir, iwc);
    }

    /**
     * 将单个 Book 对象添加到 Lucene 索引
     *
     * @param book 要索引的 Book 对象
     */
    public void indexBook(Book book) throws IOException {
        if (book == null || book.getIsbn() == null || "<UNK>".equals(book.getIsbn())) {
            return;
        }
        Document doc = new Document();

        // ID 字段
        doc.add(new StringField("isbn", book.getIsbn(), Field.Store.YES));
        // 中文文本搜索字段
        // 索引时同时存储，方便搜索结果展示
        if (!"<UNK>".equals(book.getTitle())) {
            doc.add(new TextField("title", book.getTitle(), Field.Store.YES));
        }
        if (!"<UNK>".equals(book.getAuthor())) {
            doc.add(new TextField("author", book.getAuthor(), Field.Store.YES));
        }
        if (!"<UNK>".equals(book.getContents())) {
            doc.add(new TextField("contents", book.getContents(), Field.Store.NO)); // 内容太长，不存储，只索引
        }
        if (!"<UNK>".equals(book.getBookDescription())) {
            doc.add(new TextField("bookDescription", book.getBookDescription(), Field.Store.NO)); // 不存储，只索引
        }
        if (!"<UNK>".equals(book.getEditorRecommendation())) {
            doc.add(new TextField("editorRecommendation", book.getEditorRecommendation(), Field.Store.NO)); // 不存储，只索引
        }

        // 筛选/精确字段 (StringField 不分词，但可索引)
        if (!"<UNK>".equals(book.getCategory())) {
            doc.add(new StringField("category", book.getCategory(), Field.Store.YES));
        }

        // 数值和日期字段，用于范围查询和排序
        // DoublePoint/LongPoint 用于索引数值
        if (book.getSalePrice() > 0.0) {
            doc.add(new DoublePoint("salePrice", book.getSalePrice()));
            doc.add(new StoredField("salePrice", book.getSalePrice())); // 存储原始值
        }
        if (book.getPublicationDate() != null) {
            // 将 LocalDate 转换为 Long 时间戳进行索引
            long dateMillis = book.getPublicationDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
            doc.add(new LongPoint("publicationDate", dateMillis));
            doc.add(new StoredField("publicationDate", dateMillis)); // 存储原始值
        }

        // 添加文档到索引
        writer.addDocument(doc);
    }

    /**
     * 关闭 IndexWriter，将所有变更写入磁盘
     */
    public void close() throws IOException {
        writer.close();
        System.out.println("索引构建完成，已写入磁盘");
    }
}
