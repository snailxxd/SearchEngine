package com.booksearch.model;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private String isbn;
    private String title;
    private String author;
    private String publisher;
    private LocalDate publicationDate;
    private double price;
    private String description;
}