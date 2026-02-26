package com.unir.catalogue.search;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.unir.catalogue.dto.BookResponse;
import com.unir.catalogue.entity.BookEntity;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookSearchServiceImpl implements BookSearchService {

    private final BookSearchRepository repository;

    @Override
    public void index(BookEntity e) {
        BookSearchDocument doc = new BookSearchDocument(
                e.getId(),
                e.getTitle(),
                e.getAuthor(),
                e.getPublishedDate(),
                e.getCategory(),
                e.getIsbn(),
                e.getRating(),
                e.isVisible(),
                e.getStock(),
                e.getPrice()
        );
        repository.save(doc);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<BookResponse> searchVisible(String query) {

        if (query == null || query.isBlank()) {
            return ((Collection<BookSearchDocument>) repository.findAll()).stream()
                    .filter(BookSearchDocument::isVisible)
                    .map(BookSearchServiceImpl::toResponse)
                    .toList();
        }

        Map<Long, BookSearchDocument> unique = new LinkedHashMap<>();

        repository.findByVisibleTrueAndTitleContainingIgnoreCase(query)
                .forEach(d -> unique.put(d.getId(), d));

        repository.findByVisibleTrueAndAuthorContainingIgnoreCase(query)
                .forEach(d -> unique.put(d.getId(), d));

        repository.findByVisibleTrueAndCategoryContainingIgnoreCase(query)
                .forEach(d -> unique.put(d.getId(), d));

        repository.findByVisibleTrueAndIsbnContainingIgnoreCase(query)
                .forEach(d -> unique.put(d.getId(), d));

        return unique.values().stream()
                .map(BookSearchServiceImpl::toResponse)
                .toList();
    }

    private static BookResponse toResponse(BookSearchDocument d) {
        return new BookResponse(
                d.getId(),
                d.getTitle(),
                d.getAuthor(),
                d.getPublishedDate(),
                d.getCategory(),
                d.getIsbn(),
                d.getRating(),
                d.isVisible(),
                d.getStock(),
                d.getPrice()
        );
    }
}