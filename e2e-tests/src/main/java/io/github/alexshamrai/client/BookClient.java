package io.github.alexshamrai.client;

import io.github.alexshamrai.dto.BookDto;
import io.restassured.response.Response;
import java.util.List;

public class BookClient extends BaseClient {

    public BookClient(String baseUrl) {
        super(baseUrl);
    }

    public Response createBook(BookDto request) {
        return baseClient()
            .body(request)
            .post("/api/books");
    }

    public Response getBook(Long id) {
        return baseClient()
            .get("/api/books/" + id);
    }

    public Response getAllBooks() {
        return baseClient()
            .get("/api/books");
    }

    public Response updateStock(Long id, Integer quantity) {
        return baseClient()
            .queryParam("quantity", quantity)
            .put("/api/books/" + id + "/stock");
    }

    public Response deleteBook(Long id) {
        return baseClient()
            .delete("/api/books/" + id);
    }

    public Response deleteAllBooks() {
        return baseClient()
            .delete("/api/books");
    }
}