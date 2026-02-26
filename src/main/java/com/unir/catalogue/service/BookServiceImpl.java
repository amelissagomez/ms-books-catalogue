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
import com.unir.catalogue.search.BookSearchService;

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

	// ✅ Nuevo: servicio para indexar/buscar en OpenSearch
	private final BookSearchService bookSearchService;

	@Override
	public BookResponse create(CreateBookRequest req) {

		// Regla: ISBN único
		if (bookRepository.existsByIsbn(req.isbn())) {
			throw new IllegalArgumentException("ISBN already exists: " + req.isbn());
		}

		BookEntity entity = new BookEntity();
		entity.setTitle(req.title());
		entity.setAuthor(req.author());
		entity.setPublishedDate(req.publishedDate());
		entity.setCategory(req.category());
		entity.setIsbn(req.isbn());
		entity.setPrice(req.price());
		entity.setRating(req.rating());
		entity.setVisible(req.visible() != null ? req.visible() : true);
		entity.setStock(req.stock() != null ? req.stock() : 0);

		BookEntity saved = bookRepository.save(entity);

		// ✅ Punto 3: después de persistir → indexar en OpenSearch
		// Nota: el indexado NO debe romper la transacción si falla (best effort)
		try {
			bookSearchService.index(saved);
		} catch (Exception e) {
			// por entrega: log simple y seguimos (BD es la fuente de verdad)
			// si quieres, lo cambiamos a logger
			System.err.println("OpenSearch index failed for book id=" + saved.getId() + " -> " + e.getMessage());
		}

		return toResponse(saved);
	}

	@Override
	@Transactional(readOnly = true)
	public BookResponse getById(Long id) {
		BookEntity book = bookRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Book not found: " + id));
		return toResponse(book);
	}

	@Override
	@Transactional(readOnly = true)
	public BookResponse getVisibleById(Long id) {
		BookEntity book = bookRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Book not found: " + id));

		if (!book.isVisible()) {
			throw new IllegalArgumentException("Book is hidden: " + id);
		}
		return toResponse(book);
	}

	@Override
	public BookResponse update(Long id, UpdateBookRequest req) {
		BookEntity book = bookRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Book not found: " + id));

		// Si cambia ISBN: validar que no exista en otro registro
		if (req.isbn() != null && !req.isbn().equalsIgnoreCase(book.getIsbn())) {
			if (bookRepository.existsByIsbn(req.isbn())) {
				throw new IllegalArgumentException("ISBN already exists: " + req.isbn());
			}
			book.setIsbn(req.isbn());
		}

		if (req.title() != null) book.setTitle(req.title());
		if (req.author() != null) book.setAuthor(req.author());
		if (req.publishedDate() != null) book.setPublishedDate(req.publishedDate());
		if (req.category() != null) book.setCategory(req.category());
		if (req.price() != null) book.setPrice(req.price());
		if (req.rating() != null) book.setRating(req.rating());
		if (req.visible() != null) book.setVisible(req.visible());
		if (req.stock() != null) book.setStock(req.stock());

		BookEntity saved = bookRepository.save(book);

		// ✅ Punto 3: después de update → re-indexar
		try {
			bookSearchService.index(saved);
		} catch (Exception e) {
			System.err.println("OpenSearch re-index failed for book id=" + saved.getId() + " -> " + e.getMessage());
		}

		return toResponse(saved);
	}

	@Override
	public void delete(Long id) {
		if (!bookRepository.existsById(id)) {
			throw new IllegalArgumentException("Book not found: " + id);
		}

		bookRepository.deleteById(id);

		// ✅ Punto 3: después de delete → borrar del índice
		try {
			bookSearchService.deleteById(id);
		} catch (Exception e) {
			System.err.println("OpenSearch delete failed for book id=" + id + " -> " + e.getMessage());
		}
	}

	// ==========================
	// INTERNAL - Para payments
	// ==========================

	@Override
	@Transactional(readOnly = true)
	public BookValidationResponse validateForPurchase(Long id) {
		BookEntity book = bookRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Book not found: " + id));

		// Regla: no se compra oculto
		if (!book.isVisible()) {
			throw new IllegalArgumentException("Book is hidden: " + id);
		}

		return new BookValidationResponse(
				book.getId(),
				book.getIsbn(),
				book.getTitle(),
				book.isVisible(),
				book.getStock(),
				book.getPrice()
				);
	}

	@Override
	public void decreaseStock(Long id, int qty) {
		if (qty <= 0) {
			throw new IllegalArgumentException("qty must be > 0");
		}

		BookEntity book = bookRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Book not found: " + id));

		if (!book.isVisible()) {
			throw new IllegalArgumentException("Book is hidden: " + id);
		}

		if (book.getStock() < qty) {
			throw new IllegalArgumentException("Insufficient stock for bookId=" + id);
		}

		book.setStock(book.getStock() - qty);
		BookEntity saved = bookRepository.save(book);

		// ✅ opcional pero recomendado: re-indexar para que búsquedas reflejen stock
		try {
			bookSearchService.index(saved);
		} catch (Exception e) {
			System.err.println("OpenSearch re-index (stock) failed for book id=" + id + " -> " + e.getMessage());
		}
	}

	// ==========================
	// PUBLIC SEARCH - OpenSearch
	// ==========================
	@Override
	@Transactional(readOnly = true)
	public List<BookResponse> searchVisible(String query) {
		// Nota: tu requisito principal era búsquedas combinadas.
		// Para entrega, lo dejamos como "query" full-text y devolvemos solo visibles.
		try {
			return bookSearchService.searchVisible(query);
		} catch (Exception e) {
			// fallback: si OpenSearch cae, no tumbamos el sistema
			System.err.println("OpenSearch search failed -> fallback DB. " + e.getMessage());
			return bookRepository.findAll().stream()
					.filter(BookEntity::isVisible)
					.filter(b -> query == null || query.isBlank()
					|| containsIgnoreCase(b.getTitle(), query)
					|| containsIgnoreCase(b.getAuthor(), query)
					|| (b.getCategory() != null && containsIgnoreCase(b.getCategory(), query))
					|| containsIgnoreCase(b.getIsbn(), query))
					.map(BookServiceImpl::toResponse)
					.toList();
		}
	}

	private static boolean containsIgnoreCase(String s, String q) {
		return s != null && q != null && s.toLowerCase().contains(q.toLowerCase());
	}

	private static BookResponse toResponse(BookEntity b) {
		return new BookResponse(
				b.getId(),
				b.getTitle(),
				b.getAuthor(),
				b.getPublishedDate(),
				b.getCategory(),
				b.getIsbn(),
				b.getRating(),
				b.isVisible(),
				b.getStock(),
				b.getPrice()
				);
	}

}
