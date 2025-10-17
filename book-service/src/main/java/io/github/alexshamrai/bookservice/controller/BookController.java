package io.github.alexshamrai.bookservice.controller;

import io.github.alexshamrai.bookservice.dto.CreateBookRequest;
import io.github.alexshamrai.bookservice.model.Book;
import io.github.alexshamrai.bookservice.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        log.debug("GET /api/books - Retrieving all books");
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable Long id) {
        log.debug("GET /api/books/{} - Retrieving book", id);
        return bookService.getBook(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Book> createBook(@RequestBody CreateBookRequest request) {
        log.debug("POST /api/books - Creating new book");
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPrice(request.getPrice());
        book.setStockQuantity(request.getStockQuantity());
        return ResponseEntity.ok(bookService.createBook(book));
    }

    @PutMapping("/{id}/stock")
    public ResponseEntity<Book> updateStock(@PathVariable Long id, @RequestParam Integer quantity) {
        log.debug("PUT /api/books/{}/stock - Updating stock to {}", id, quantity);
        return ResponseEntity.ok(bookService.updateStock(id, quantity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        log.debug("DELETE /api/books/{} - Deleting book", id);
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllBooks() {
        log.warn("DELETE /api/books - Deleting all books");
        bookService.deleteAllBooks();
        return ResponseEntity.noContent().build();
    }
}