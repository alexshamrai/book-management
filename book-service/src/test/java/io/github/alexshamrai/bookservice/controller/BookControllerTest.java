package io.github.alexshamrai.bookservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alexshamrai.bookservice.dto.CreateBookRequest;
import io.github.alexshamrai.bookservice.model.Book;
import io.github.alexshamrai.bookservice.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    private Book testBook;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setPrice(29.99);
        testBook.setStockQuantity(10);
    }

    @Test
    void getAllBooks_ShouldReturnListOfBooks() throws Exception {
        // Given
        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Another Book");
        book2.setAuthor("Another Author");
        book2.setPrice(19.99);
        book2.setStockQuantity(5);

        List<Book> books = Arrays.asList(testBook, book2);
        when(bookService.getAllBooks()).thenReturn(books);

        // When & Then
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Test Book")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].title", is("Another Book")));

        verify(bookService, times(1)).getAllBooks();
    }

    @Test
    void getAllBooks_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        // Given
        when(bookService.getAllBooks()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(bookService, times(1)).getAllBooks();
    }

    @Test
    void getBook_WhenBookExists_ShouldReturnBook() throws Exception {
        // Given
        when(bookService.getBook(1L)).thenReturn(Optional.of(testBook));

        // When & Then
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Test Book")))
                .andExpect(jsonPath("$.author", is("Test Author")))
                .andExpect(jsonPath("$.price", is(29.99)))
                .andExpect(jsonPath("$.stockQuantity", is(10)));

        verify(bookService, times(1)).getBook(1L);
    }

    @Test
    void getBook_WhenBookDoesNotExist_ShouldReturn404() throws Exception {
        // Given
        when(bookService.getBook(anyLong())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).getBook(999L);
    }

    @Test
    void createBook_WithValidData_ShouldReturnCreatedBook() throws Exception {
        // Given
        CreateBookRequest request = CreateBookRequest.builder()
                .title("New Book")
                .author("New Author")
                .price(39.99)
                .stockQuantity(15)
                .build();

        Book createdBook = new Book();
        createdBook.setId(3L);
        createdBook.setTitle(request.getTitle());
        createdBook.setAuthor(request.getAuthor());
        createdBook.setPrice(request.getPrice());
        createdBook.setStockQuantity(request.getStockQuantity());

        when(bookService.createBook(any(Book.class))).thenReturn(createdBook);

        // When & Then
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.title", is("New Book")))
                .andExpect(jsonPath("$.author", is("New Author")))
                .andExpect(jsonPath("$.price", is(39.99)))
                .andExpect(jsonPath("$.stockQuantity", is(15)));

        verify(bookService, times(1)).createBook(any(Book.class));
    }

    @Test
    void updateStock_WhenBookExists_ShouldReturnUpdatedBook() throws Exception {
        // Given
        testBook.setStockQuantity(25);
        when(bookService.updateStock(1L, 25)).thenReturn(testBook);

        // When & Then
        mockMvc.perform(put("/api/books/1/stock")
                        .param("quantity", "25"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.stockQuantity", is(25)));

        verify(bookService, times(1)).updateStock(1L, 25);
    }

    @Test
    void updateStock_WhenBookDoesNotExist_ShouldReturn404() throws Exception {
        // Given
        when(bookService.updateStock(anyLong(), anyInt()))
                .thenThrow(new EntityNotFoundException("Book not found"));

        // When & Then
        mockMvc.perform(put("/api/books/999/stock")
                        .param("quantity", "25"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Book not found")));

        verify(bookService, times(1)).updateStock(999L, 25);
    }

    @Test
    void deleteBook_WhenBookExists_ShouldReturn204() throws Exception {
        // Given
        doNothing().when(bookService).deleteBook(1L);

        // When & Then
        mockMvc.perform(delete("/api/books/1"))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteBook(1L);
    }

    @Test
    void deleteBook_WhenBookDoesNotExist_ShouldReturn404() throws Exception {
        // Given
        doThrow(new EntityNotFoundException("Book not found: 999"))
                .when(bookService).deleteBook(999L);

        // When & Then
        mockMvc.perform(delete("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Book not found: 999")));

        verify(bookService, times(1)).deleteBook(999L);
    }

    @Test
    void deleteAllBooks_ShouldReturn204() throws Exception {
        // Given
        doNothing().when(bookService).deleteAllBooks();

        // When & Then
        mockMvc.perform(delete("/api/books"))
                .andExpect(status().isNoContent());

        verify(bookService, times(1)).deleteAllBooks();
    }
}
