package io.github.alexshamrai.bookservice.service;

import io.github.alexshamrai.bookservice.model.Book;
import io.github.alexshamrai.bookservice.repository.BookRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBook(Long id) {
        return bookRepository.findById(id);
    }

    public Book createBook(Book book) {
        return bookRepository.save(book);
    }

    @Transactional
    public Book updateStock(Long id, Integer quantity) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Book not found"));
        book.setStockQuantity(quantity);
        return bookRepository.save(book);
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Book not found: " + id));
        bookRepository.delete(book);
    }

    @Transactional
    public void deleteAllBooks() {
        bookRepository.deleteAll();
    }
}