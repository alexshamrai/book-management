package io.github.alexshamrai.e2e;

import io.github.alexshamrai.client.BookClient;
import io.github.alexshamrai.client.OrderClient;
import io.github.alexshamrai.dto.BookDto;
import io.github.alexshamrai.dto.OrderDto;
import io.github.alexshamrai.dto.OrderItemRequest;
import io.github.alexshamrai.dto.OrderRequest;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static io.github.alexshamrai.e2e.BaseTest.BOOK_SERVICE_URL;
import static io.github.alexshamrai.e2e.BaseTest.ORDER_SERVICE_URL;
import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {
    private final BookClient bookClient = new BookClient(BOOK_SERVICE_URL);
    private final OrderClient orderClient = new OrderClient(ORDER_SERVICE_URL);
    private BookDto testBook;

    @BeforeEach
    void setUp() {
        orderClient.deleteAllOrders()
            .then()
            .statusCode(204);

        bookClient.deleteAllBooks()
            .then()
            .statusCode(204);

        BookDto bookRequest = BookDto.builder()
            .title("Test Book")
            .author("Test Author")
            .price(29.99)
            .stockQuantity(10)
            .build();

        Response createBookResponse = bookClient.createBook(bookRequest);
        createBookResponse.then().statusCode(200);
        testBook = createBookResponse.as(BookDto.class);
    }

    @Test
    void newTest(){

    }
}