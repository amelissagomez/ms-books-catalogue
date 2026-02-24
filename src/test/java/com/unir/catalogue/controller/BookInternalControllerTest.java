package com.unir.catalogue.controller;

import com.unir.catalogue.dto.BookValidationResponse;
import com.unir.catalogue.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookInternalControllerTest {

    @Mock
    BookService bookService;

    private BookInternalController controller;

    @BeforeEach
    void setUp() {
        controller = new BookInternalController(bookService);
    }

    @Test
    void validate_returnsResponse() {
        // given
        var resp = new BookValidationResponse(
                2L,
                "9780132350884",
                "Clean Code",
                true,
                10,
                new BigDecimal("120000.00")
        );

        when(bookService.validateForPurchase(2L)).thenReturn(resp);

        // when
        BookValidationResponse result = controller.validate(2L);

        // then
        assertNotNull(result);
        assertEquals(2L, result.id());
        assertEquals("9780132350884", result.isbn());
        assertTrue(result.visible());
        assertEquals(10, result.stock());
        assertEquals(new BigDecimal("120000.00"), result.price());

        verify(bookService).validateForPurchase(2L);
    }

    @Test
    void validate_whenBookNotFound_throwsException_andWeCaptureIt() {
        // given
        when(bookService.validateForPurchase(999L))
                .thenThrow(new IllegalArgumentException("Book not found: 999"));

        // when
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.validate(999L)
        );

        // then
        assertEquals("Book not found: 999", ex.getMessage());
        verify(bookService).validateForPurchase(999L);
    }

    @Test
    void decreaseStock_callsService() {
        // given
        doNothing().when(bookService).decreaseStock(2L, 3);

        // when
        controller.decreaseStock(2L, 3);

        // then
        verify(bookService).decreaseStock(2L, 3);
    }

    @Test
    void decreaseStock_whenQtyInvalid_throwsException_andWeCaptureIt() {
        // given
        doThrow(new IllegalArgumentException("qty must be > 0"))
                .when(bookService).decreaseStock(2L, 0);

        // when
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.decreaseStock(2L, 0)
        );

        // then
        assertEquals("qty must be > 0", ex.getMessage());
        verify(bookService).decreaseStock(2L, 0);
    }
}