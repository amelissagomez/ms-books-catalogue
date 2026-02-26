package com.unir.catalogue.service;

import com.unir.catalogue.dto.CreateBookRequest;
import com.unir.catalogue.dto.UpdateBookRequest;
import com.unir.catalogue.dto.BookResponse;
import com.unir.catalogue.dto.BookValidationResponse;

import java.time.LocalDate;
import java.util.List;

public interface BookService {

    // ADMIN
    BookResponse create(CreateBookRequest req);

    BookResponse getById(Long id);

    BookResponse update(Long id, UpdateBookRequest req);

    void delete(Long id);

    BookResponse getVisibleById(Long id);

    BookValidationResponse validateForPurchase(Long id);

    void decreaseStock(Long id, int qty);

	List<BookResponse> searchVisible(String query);

	int reindexAll();
}