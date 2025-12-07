package com.booksearch.util;

import com.booksearch.model.Book;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class BookSerializer {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.findAndRegisterModules();
    }

    /**
     * 序列化 Book 到文件
     * @param book
     * @param file 文件路径
     * @throws IOException
     */
    public static void serialize(Book book, File file) throws IOException {
        OBJECT_MAPPER.writeValue(file, book);
    }

    /**
     * 从文件反序列化 Book 对象
     * @param file 文件路径
     * @return 反序列化后的 Book
     * @throws IOException
     */
    public static Book deserialize(File file) throws IOException {
        return OBJECT_MAPPER.readValue(file, Book.class);
    }

    /**
     * 序列化 Book 列表到文件
     * @param books
     * @param file
     * @throws IOException
     */
    public static void serializeAll(List<Book> books, File file) throws IOException {
        OBJECT_MAPPER.writeValue(file, books); // 将 List<Book> 序列化为一个 JSON 数组
    }

    /**
     * 从文件反序列化整个列表
     * @param file
     * @return 反序列化后的 Book 列表
     * @throws IOException
     */
    public static List<Book> deserializeAll(File file) throws IOException {
        return OBJECT_MAPPER.readValue(file, new TypeReference<List<Book>>() {});
    }
}
