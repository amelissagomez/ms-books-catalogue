package com.unir.catalogue.search;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface BookSearchRepository extends CrudRepository<BookSearchDocument, Long> {


	List<BookSearchDocument> findByVisibleTrueAndTitleContainingIgnoreCase(String title);

	List<BookSearchDocument> findByVisibleTrueAndAuthorContainingIgnoreCase(String author);

	List<BookSearchDocument> findByVisibleTrueAndCategoryContainingIgnoreCase(String category);

	List<BookSearchDocument> findByVisibleTrueAndIsbnContainingIgnoreCase(String isbn);
}