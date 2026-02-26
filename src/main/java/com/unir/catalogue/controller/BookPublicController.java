package com.unir.catalogue.controller;

import com.unir.catalogue.dto.BookResponse;
import com.unir.catalogue.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookPublicController {

    private final BookService bookService;

    @GetMapping("/{id}")
    public BookResponse getVisibleById(@PathVariable Long id) {
        return bookService.getVisibleById(id);
    }

    @GetMapping("/search")
    public List<BookResponse> search(@RequestParam(required = false) String query) {
        return bookService.searchVisible(query);
    }
}