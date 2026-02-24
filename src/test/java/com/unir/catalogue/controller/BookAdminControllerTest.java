package com.unir.catalogue.controller;

import com.unir.catalogue.dto.CreateBookRequest;
import com.unir.catalogue.dto.UpdateBookRequest;
import com.unir.catalogue.dto.BookResponse;
import com.unir.catalogue.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookAdminControllerTest {

    @Mock
    BookService bookService;

    BookAdminController controller;

    @BeforeEach
    void setUp() {
        controller = new BookAdminController(bookService);
    }

    @Test
    void create_returns201_andBody() {
        var req = new CreateBookRequest(
                "Clean Code",
                "Robert C. Martin",
                LocalDate.parse("2008-08-01"),
                "Programming",
                "9780132350884",
                new BigDecimal("120000.00"), // price va aquí
                5,                            // rating
                Boolean.TRUE,                 // visible
                Integer.valueOf(10)           // stock
        );

        var resp = new BookResponse(
                1L,
                req.title(),
                req.author(),
                req.publishedDate(),
                req.category(),
                req.isbn(),
                req.rating(),
                true,
                10,
                req.price()
        );

        when(bookService.create(any(CreateBookRequest.class))).thenReturn(resp);

        ResponseEntity<BookResponse> result = controller.create(req);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().id());
        assertEquals("9780132350884", result.getBody().isbn());

        // Verifica que llamó al service con el request
        ArgumentCaptor<CreateBookRequest> cap = ArgumentCaptor.forClass(CreateBookRequest.class);
        verify(bookService).create(cap.capture());
        assertEquals("Clean Code", cap.getValue().title());
    }

    @Test
    void getById_returns200() {
        var resp = new BookResponse(
                10L,
                "Relatos de Papel",
                "Valeria Montes",
                LocalDate.parse("2021-01-15"),
                "Ficción",
                "LB-001",
                5,
                true,
                10,
                new BigDecimal("54000.00")
        );

        when(bookService.getById(10L)).thenReturn(resp);

        ResponseEntity<BookResponse> result = controller.getById(10L);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Relatos de Papel", result.getBody().title());
        verify(bookService).getById(10L);
    }

    @Test
    void update_returns200() {
        var req = new UpdateBookRequest(
                null,
                null, 
                null, 
                null,
                null,
                new BigDecimal("135000.00"),       
                5,
                null,
                null
        );

        var resp = new BookResponse(
                1L,
                "Clean Code",
                "Robert C. Martin",
                LocalDate.parse("2008-08-01"),
                "Programming",
                "9780132350884",
                5,
                true,
                5,
                new BigDecimal("135000.00")
        );

        when(bookService.patch(eq(1L), any(UpdateBookRequest.class))).thenReturn(resp);

        ResponseEntity<BookResponse> result = controller.patch(1L, req);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(5, result.getBody().stock());
        assertEquals(new BigDecimal("135000.00"), result.getBody().price());

        verify(bookService).patch(eq(1L), any(UpdateBookRequest.class));
    }

    @Test
    void delete_returns204() {
        doNothing().when(bookService).delete(1L);

        controller.delete(1L);

        verify(bookService).delete(1L);
    }
}