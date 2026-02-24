package com.unir.catalogue.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateBookRequest(
        @NotBlank @Size(max = 200) 
        String title,
        
        @NotBlank @Size(max = 150) 
        String author,
        
        LocalDate publishedDate,
        
        @Size(max = 80) 
        String category,
        
        @NotBlank @Size(max = 40) 
        String isbn,
        
        @NotNull
        BigDecimal price,
        
        @Min(1) @Max(5) 
        int rating,
        
        Boolean visible,
        
        @Min(0) 
        Integer stock
) {}