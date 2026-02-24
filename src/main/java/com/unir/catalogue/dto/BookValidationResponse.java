package com.unir.catalogue.dto;

import java.math.BigDecimal;

public record BookValidationResponse(
    Long id,
    String isbn,
    String title,
    boolean visible,
    int stock,
    BigDecimal price
) {}