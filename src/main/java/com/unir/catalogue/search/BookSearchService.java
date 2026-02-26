package com.unir.catalogue.search;

import com.unir.catalogue.dto.BookResponse;
import com.unir.catalogue.entity.BookEntity;

import java.util.List;

public interface BookSearchService {

	void index(BookEntity entity);

	void deleteById(Long id);

	List<BookResponse> searchVisible(String query);
}