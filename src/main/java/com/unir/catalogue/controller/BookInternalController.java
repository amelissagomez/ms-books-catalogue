package com.unir.catalogue.controller;

import com.unir.catalogue.dto.BookValidationResponse;
import com.unir.catalogue.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/internal/books")
@RequiredArgsConstructor
public class BookInternalController {

    private final BookService bookService;

    @GetMapping("/{id}")
    public BookValidationResponse validate(@PathVariable Long id) {
        return bookService.validateForPurchase(id);
    }

    @PostMapping("/{id}/decrease-stock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void decreaseStock(@PathVariable Long id, @RequestParam int qty) {
        bookService.decreaseStock(id, qty);
    }
}