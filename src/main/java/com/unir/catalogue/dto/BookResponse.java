package com.unir.catalogue.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookResponse(
    Long id,
    String title,
    String author,
    LocalDate publishedDate,
    String category,
    String isbn,
    int rating,
    boolean visible,
    int stock,
    BigDecimal price
) {}