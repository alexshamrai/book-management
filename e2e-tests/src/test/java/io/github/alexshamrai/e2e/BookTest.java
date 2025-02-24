package io.github.alexshamrai.e2e;

import io.github.alexshamrai.client.BookClient;
import io.github.alexshamrai.dto.BookDto;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.alexshamrai.e2e.BaseTest.BOOK_SERVICE_URL;
import static org.assertj.core.api.Assertions.assertThat;

class BookTest {

    private final BookClient bookClient = new BookClient(BOOK_SERVICE_URL);

    @BeforeEach
    void cleanUp() {
        Response response = bookClient.deleteAllBooks();
        response.then().statusCode(204);
    }

    @Test
    void createAndRetrieveBooks() {
        BookDto bookRequest1 = BookDto.builder()
            .title("The Great Gatsby")
            .author("F. Scott Fitzgerald")
            .price(29.99)
            .stockQuantity(10)
            .build();

        Response createResponse1 = bookClient.createBook(bookRequest1);
        createResponse1.then().statusCode(200);
        BookDto createdBook1 = createResponse1.as(BookDto.class);
        assertThat(createdBook1.getId()).isNotNull();
        assertThat(createdBook1)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(bookRequest1);

        BookDto bookRequest2 = BookDto.builder()
            .title("1984")
            .author("George Orwell")
            .price(24.99)
            .stockQuantity(15)
            .build();

        Response createResponse2 = bookClient.createBook(bookRequest2);
        createResponse2.then().statusCode(200);
        BookDto createdBook2 = createResponse2.as(BookDto.class);
        assertThat(createdBook2.getId()).isNotNull();
        assertThat(createdBook2)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(bookRequest2);

        Response getAllResponse = bookClient.getAllBooks();
        getAllResponse.then().statusCode(200);
        List<BookDto> allBooks = getAllResponse.jsonPath().getList(".", BookDto.class);
        assertThat(allBooks).hasSize(2);

        Response getBookResponse = bookClient.getBook(createdBook1.getId());
        getBookResponse.then().statusCode(200);
        BookDto retrievedBook = getBookResponse.as(BookDto.class);
        assertThat(retrievedBook).isEqualTo(createdBook1);
    }

    @Test
    void bookNotFound() {
        Response response = bookClient.getBook(999L);
        response.then().statusCode(404);
    }

    @Test
    void updatesBookStock() {
        BookDto bookRequest = BookDto.builder()
            .title("Test Book")
            .author("Test Author")
            .price(19.99)
            .stockQuantity(10)
            .build();

        Response createResponse = bookClient.createBook(bookRequest);
        createResponse.then().statusCode(200);
        BookDto createdBook = createResponse.as(BookDto.class);

        int newStockQuantity = 5;
        Response updateResponse = bookClient.updateStock(createdBook.getId(), newStockQuantity);
        updateResponse.then().statusCode(200);

        Response getBookResponse = bookClient.getBook(createdBook.getId());
        getBookResponse.then().statusCode(200);
        BookDto updatedBook = getBookResponse.as(BookDto.class);
        assertThat(updatedBook.getStockQuantity()).isEqualTo(newStockQuantity);
    }

    @Test
    void deleteAllBooks() {
        BookDto bookRequest = BookDto.builder()
            .title("Book to Delete")
            .author("Delete Author")
            .price(15.99)
            .stockQuantity(5)
            .build();

        Response createResponse = bookClient.createBook(bookRequest);
        createResponse.then().statusCode(200);
        BookDto createdBook = createResponse.as(BookDto.class);

        Response getResponse = bookClient.getBook(createdBook.getId());
        getResponse.then().statusCode(200);

        Response deleteResponse = bookClient.deleteBook(createdBook.getId());
        deleteResponse.then().statusCode(204);
    }

    @Test
    void shouldDeleteAllBooks() {
        BookDto book1 = BookDto.builder()
            .title("Book 1")
            .author("Author 1")
            .price(10.99)
            .stockQuantity(5)
            .build();

        BookDto book2 = BookDto.builder()
            .title("Book 2")
            .author("Author 2")
            .price(12.99)
            .stockQuantity(3)
            .build();

        bookClient.createBook(book1);
        bookClient.createBook(book2);

        Response getAllBeforeDelete = bookClient.getAllBooks();
        getAllBeforeDelete.then().statusCode(200);
        List<BookDto> booksBeforeDelete = getAllBeforeDelete.as(new TypeRef<List<BookDto>>() {});
        assertThat(booksBeforeDelete).hasSize(2);

        Response deleteAllResponse = bookClient.deleteAllBooks();
        deleteAllResponse.then().statusCode(204);

        Response getAllAfterDelete = bookClient.getAllBooks();
        getAllAfterDelete.then().statusCode(200);
        List<BookDto> booksAfterDelete = getAllAfterDelete.as(new TypeRef<List<BookDto>>() {});
        assertThat(booksAfterDelete).isEmpty();
    }
}