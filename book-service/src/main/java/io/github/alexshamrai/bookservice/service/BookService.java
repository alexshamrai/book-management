package io.github.alexshamrai.bookservice.service;

import io.github.alexshamrai.bookservice.model.Book;
import io.github.alexshamrai.bookservice.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public List<Book> getAllBooks() {
        log.debug("Fetching all books");
        List<Book> books = bookRepository.findAll();
        log.info("Retrieved {} books", books.size());
        return books;
    }

    @Transactional(readOnly = true)
    public Optional<Book> getBook(Long id) {
        log.debug("Fetching book with id: {}", id);
        Optional<Book> book = bookRepository.findById(id);
        if (book.isPresent()) {
            log.info("Found book with id: {}", id);
        } else {
            log.warn("Book not found with id: {}", id);
        }
        return book;
    }

    @Transactional
    public Book createBook(Book book) {
        log.info("Creating new book: title={}, author={}", book.getTitle(), book.getAuthor());
        Book savedBook = bookRepository.save(book);
        log.info("Successfully created book with id: {}", savedBook.getId());
        return savedBook;
    }

    @Transactional
    public Book updateStock(Long id, Integer quantity) {
        log.info("Updating stock for book id: {} to quantity: {}", id, quantity);
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Failed to update stock: Book not found with id: {}", id);
                return new EntityNotFoundException("Book not found");
            });
        Integer oldQuantity = book.getStockQuantity();
        book.setStockQuantity(quantity);
        Book updatedBook = bookRepository.save(book);
        log.info("Successfully updated stock for book id: {} from {} to {}", id, oldQuantity, quantity);
        return updatedBook;
    }

    @Transactional
    public void deleteBook(Long id) {
        log.info("Attempting to delete book with id: {}", id);
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> {
                log.error("Failed to delete: Book not found with id: {}", id);
                return new EntityNotFoundException("Book not found: " + id);
            });
        bookRepository.delete(book);
        log.info("Successfully deleted book with id: {}", id);
    }

    @Transactional
    public void deleteAllBooks() {
        log.warn("Deleting all books from the database");
        long count = bookRepository.count();
        bookRepository.deleteAll();
        log.warn("Successfully deleted {} books", count);
    }
}