package com.unir.catalogue.repository;

import com.unir.catalogue.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<BookEntity, Long> {
    boolean existsByIsbn(String getIsbn);
}