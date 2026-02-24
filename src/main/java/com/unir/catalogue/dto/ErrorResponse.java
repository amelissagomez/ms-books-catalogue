package com.unir.catalogue.dto;

import java.time.OffsetDateTime;

public record ErrorResponse (
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {}