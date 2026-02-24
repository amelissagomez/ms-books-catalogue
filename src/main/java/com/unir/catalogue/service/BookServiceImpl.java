package com.unir.catalogue.service;

import com.unir.catalogue.dto.CreateBookRequest;
import com.unir.catalogue.dto.UpdateBookRequest;
import com.unir.catalogue.dto.BookResponse;
import com.unir.catalogue.dto.BookValidationResponse;
import com.unir.catalogue.entity.BookEntity;
import com.unir.catalogue.exception.BadRequestException;
import com.unir.catalogue.exception.DuplicateIsbnException;
import com.unir.catalogue.exception.ResourceNotFoundException;
import com.unir.catalogue.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;


    @Override
    public BookResponse create(CreateBookRequest req) {
        if (bookRepository.existsByIsbn(req.isbn())) {
            throw new DuplicateIsbnException(req.isbn());
        }

        BookEntity entity = BookEntity.builder()
                .title(req.title())
                .author(req.author())
                .publishedDate(req.publishedDate())
                .category(req.category())
                .isbn(req.isbn())
                .rating(req.rating())
                .visible(req.visible() != null ? req.visible() : true)
                .stock(req.stock() != null ? req.stock() : 0)
                .price(req.price())
                .build();

        return toResponse(bookRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public BookResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public BookResponse patch(Long id, UpdateBookRequest req) {
        BookEntity b = findOrThrow(id);

        if (req.title() != null) b.setTitle(req.title());
        if (req.author() != null) b.setAuthor(req.author());
        if (req.publishedDate() != null) b.setPublishedDate(req.publishedDate());
        if (req.category() != null) b.setCategory(req.category());

        if (req.isbn() != null && !req.isbn().equalsIgnoreCase(b.getIsbn())) {
            if (bookRepository.existsByIsbn(req.isbn())) {
                throw new DuplicateIsbnException(req.isbn());
            }
            b.setIsbn(req.isbn());
        }

        if (req.rating() != null) b.setRating(req.rating());
        if (req.visible() != null) b.setVisible(req.visible());
        if (req.stock() != null) b.setStock(req.stock());
        if (req.price() != null) b.setPrice(req.price());

        return toResponse(bookRepository.save(b));
    }

    @Override
    public void delete(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Libro no encontrado: " + id);
        }
        bookRepository.deleteById(id);
    }


    @Override
    @Transactional(readOnly = true)
    public BookResponse getVisibleById(Long id) {
        BookEntity b = findOrThrow(id);
        if (!b.isVisible()) {
            throw new BadRequestException("Libro oculto: " + id);
        }
        return toResponse(b);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookResponse> searchVisible(String title,
                                           String author,
                                           LocalDate publishedDate,
                                           String category,
                                           String isbn,
                                           Integer rating) {

        return bookRepository.findAll().stream()
                .filter(BookEntity::isVisible)
                .filter(b -> title == null || containsIgnoreCase(b.getTitle(), title))
                .filter(b -> author == null || containsIgnoreCase(b.getAuthor(), author))
                .filter(b -> publishedDate == null || publishedDate.equals(b.getPublishedDate()))
                .filter(b -> category == null || equalsIgnoreCaseSafe(b.getCategory(), category))
                .filter(b -> isbn == null || equalsIgnoreCaseSafe(b.getIsbn(), isbn))
                .filter(b -> rating == null || b.getRating() == rating)
                .map(BookServiceImpl::toResponse)
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public BookValidationResponse validateForPurchase(Long id) {
        BookEntity b = findOrThrow(id);
        return new BookValidationResponse(
                b.getId(),
                b.getIsbn(),
                b.getTitle(),
                b.isVisible(),
                b.getStock(),
                b.getPrice()
        );
    }
    
    
    @Override
    public void decreaseStock(Long id, int quality) {
        if (quality <= 0) throw new BadRequestException("la calificación del libro debe ser mayor a 0");

        BookEntity bookEntity = findOrThrow(id);

        if (!bookEntity.isVisible()) {
            throw new BadRequestException("Libro oculto: " + id);
        }
        if (bookEntity.getStock() < quality) {
            throw new BadRequestException("No hay suficiente stock para este libro: " + id);
        }

        bookEntity.setStock(bookEntity.getStock() - quality);
        bookRepository.save(bookEntity);
    }


    private BookEntity findOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado: " + id));
    }

    private static BookResponse toResponse(BookEntity bookEntity) {
        return new BookResponse(
        		bookEntity.getId(),
        		bookEntity.getTitle(),
        		bookEntity.getAuthor(),
        		bookEntity.getPublishedDate(),
        		bookEntity.getCategory(),
        		bookEntity.getIsbn(),
        		bookEntity.getRating(),
        		bookEntity.isVisible(),
        		bookEntity.getStock(),
        		bookEntity.getPrice()
        );
    }

    private static boolean containsIgnoreCase(String value, String q) {
        return value != null && q != null && value.toLowerCase().contains(q.toLowerCase());
    }

    private static boolean equalsIgnoreCaseSafe(String a, String b) {
        return a != null && b != null && a.equalsIgnoreCase(b);
    }
}