package com.unir.catalogue.exception;

public class DuplicateIsbnException extends RuntimeException {
    public DuplicateIsbnException(String isbn) {
        super("ISBN ya existe: " + isbn);
    }
}