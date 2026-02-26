package com.unir.catalogue.search;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Document(indexName = "books")
@Data
public class BookSearchDocument {


	@Id
	private Long id;

	private String title;
	private String author;
	private LocalDate publishedDate;
	private String category;
	private String isbn;
	private int rating;
	private boolean visible;
	private int stock;
	private BigDecimal price;

	public BookSearchDocument() {}

	public BookSearchDocument(Long id, String title, String author, LocalDate publishedDate, String category,
			String isbn, int rating, boolean visible, int stock, BigDecimal price) {
		this.id = id;
		this.title = title;
		this.author = author;
		this.publishedDate = publishedDate;
		this.category = category;
		this.isbn = isbn;
		this.rating = rating;
		this.visible = visible;
		this.stock = stock;
		this.price = price;
	}


}