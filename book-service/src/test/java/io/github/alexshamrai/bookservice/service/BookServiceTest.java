package io.github.alexshamrai.bookservice.service;

import io.github.alexshamrai.bookservice.model.Book;
import io.github.alexshamrai.bookservice.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
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
    void getAllBooks_ShouldReturnListOfBooks() {
        // Given
        Book book2 = new Book();
        book2.setId(2L);
        book2.setTitle("Another Book");
        book2.setAuthor("Another Author");
        book2.setPrice(19.99);
        book2.setStockQuantity(5);

        List<Book> expectedBooks = Arrays.asList(testBook, book2);
        when(bookRepository.findAll()).thenReturn(expectedBooks);

        // When
        List<Book> actualBooks = bookService.getAllBooks();

        // Then
        assertThat(actualBooks).hasSize(2);
        assertThat(actualBooks).containsExactly(testBook, book2);
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void getAllBooks_WhenEmpty_ShouldReturnEmptyList() {
        // Given
        when(bookRepository.findAll()).thenReturn(List.of());

        // When
        List<Book> actualBooks = bookService.getAllBooks();

        // Then
        assertThat(actualBooks).isEmpty();
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void getBook_WhenBookExists_ShouldReturnBook() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));

        // When
        Optional<Book> result = bookService.getBook(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(testBook);
        assertThat(result.get().getTitle()).isEqualTo("Test Book");
        verify(bookRepository, times(1)).findById(1L);
    }

    @Test
    void getBook_WhenBookDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        Optional<Book> result = bookService.getBook(999L);

        // Then
        assertThat(result).isEmpty();
        verify(bookRepository, times(1)).findById(999L);
    }

    @Test
    void createBook_ShouldSaveAndReturnBook() {
        // Given
        Book newBook = new Book();
        newBook.setTitle("New Book");
        newBook.setAuthor("New Author");
        newBook.setPrice(39.99);
        newBook.setStockQuantity(15);

        Book savedBook = new Book();
        savedBook.setId(3L);
        savedBook.setTitle(newBook.getTitle());
        savedBook.setAuthor(newBook.getAuthor());
        savedBook.setPrice(newBook.getPrice());
        savedBook.setStockQuantity(newBook.getStockQuantity());

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        // When
        Book result = bookService.createBook(newBook);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getTitle()).isEqualTo("New Book");
        assertThat(result.getAuthor()).isEqualTo("New Author");
        verify(bookRepository, times(1)).save(newBook);
    }

    @Test
    void updateStock_WhenBookExists_ShouldUpdateAndReturnBook() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Book result = bookService.updateStock(1L, 20);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStockQuantity()).isEqualTo(20);
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(testBook);
    }

    @Test
    void updateStock_WhenBookDoesNotExist_ShouldThrowException() {
        // Given
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.updateStock(999L, 20))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Book not found");

        verify(bookRepository, times(1)).findById(999L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteBook_WhenBookExists_ShouldDeleteBook() {
        // Given
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        doNothing().when(bookRepository).delete(testBook);

        // When
        bookService.deleteBook(1L);

        // Then
        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).delete(testBook);
    }

    @Test
    void deleteBook_WhenBookDoesNotExist_ShouldThrowException() {
        // Given
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.deleteBook(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book not found: 999");

        verify(bookRepository, times(1)).findById(999L);
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    void deleteAllBooks_ShouldDeleteAllBooks() {
        // Given
        when(bookRepository.count()).thenReturn(3L);
        doNothing().when(bookRepository).deleteAll();

        // When
        bookService.deleteAllBooks();

        // Then
        verify(bookRepository, times(1)).count();
        verify(bookRepository, times(1)).deleteAll();
    }
}
