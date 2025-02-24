package io.github.alexshamrai.orderservice.client;

import io.github.alexshamrai.orderservice.dto.BookDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "book-service", url = "http://localhost:9091")
public interface BookClient {
    @GetMapping("/api/books/{id}")
    ResponseEntity<BookDto> getBook(@PathVariable Long id);

    @PutMapping("/api/books/{id}/stock")
    ResponseEntity<BookDto> updateStock(@PathVariable Long id, @RequestParam Integer quantity);
}
