package com.booksearch.model;

import lombok.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class Book {
    private String isbn;
    private String title;
    private String author;
    private String language;
    private String publisher;
    private LocalDate publicationDate;
    private double salePrice;
    private double originalPrice;
    private String category;
    private String contents;
    private String bookDescription;
    private String authorDescription;
    private String editorRecommendation;

    public Book() {
        isbn = "<UNK>";
        title = "<UNK>";
        author = "<UNK>";
        language = "<UNK>";
        publisher = "<UNK>";
        publicationDate = LocalDate.now();
        salePrice = 0.0;
        originalPrice = 0.0;
        category = "<UNK>";
        contents = "<UNK>";
        bookDescription = "<UNK>";
        authorDescription = "<UNK>";
        editorRecommendation = "<UNK>";
    }
}