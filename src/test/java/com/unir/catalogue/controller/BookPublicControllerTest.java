package com.unir.catalogue.controller;

import com.unir.catalogue.dto.BookResponse;
import com.unir.catalogue.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookPublicControllerTest {

    @Mock
    BookService bookService;

    private BookPublicController controller;

    @BeforeEach
    void setUp() {
        controller = new BookPublicController(bookService);
    }

    @Test
    void getVisibleById_returnsBook() {
        // given
        var resp = new BookResponse(
                1L,
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

        when(bookService.getVisibleById(1L)).thenReturn(resp);

        // when
        BookResponse result = controller.getVisibleById(1L);

        // then
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertTrue(result.visible());
        assertEquals("LB-001", result.isbn());
        verify(bookService).getVisibleById(1L);
    }

    @Test
    void getVisibleById_whenHidden_throws_andWeCaptureIt() {
        // given
        when(bookService.getVisibleById(9L))
                .thenThrow(new IllegalArgumentException("Book is hidden: 9"));

        // when
        var ex = assertThrows(IllegalArgumentException.class,
                () -> controller.getVisibleById(9L));

        // then
        assertEquals("Book is hidden: 9", ex.getMessage());
        verify(bookService).getVisibleById(9L);
    }

    @Test
    void search_returnsList() {
        // given
        var b1 = new BookResponse(
                1L, "Clean Code", "Robert C. Martin",
                LocalDate.parse("2008-08-01"),
                "Programming", "9780132350884",
                5, true, 10,new BigDecimal("120000.00")
        );

        when(bookService.searchVisible(
                "clean",
                null,
                null,
                null,
                null,
                null
        )).thenReturn(List.of(b1));

        // when
        List<BookResponse> result = controller.search(
                "clean",
                null,
                null,
                null,
                null,
                null
        );

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Clean Code", result.get(0).title());
        verify(bookService).searchVisible("clean", null, null, null, null, null);
    }
}