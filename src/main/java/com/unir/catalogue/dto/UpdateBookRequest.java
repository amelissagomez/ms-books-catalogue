package com.unir.catalogue.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateBookRequest(
        @Size(max = 200) 
        String title,
        
        @Size(max = 150) 
        String author,
        
        LocalDate publishedDate,
        
        @Size(max = 80) 
        String category,
        
        @Size(max = 40) 
        String isbn,
        
        BigDecimal price,
        
        @Min(1) @Max(5) 
        Integer rating,
        
        Boolean visible,
        
        @Min(0) 
        Integer stock
) {}