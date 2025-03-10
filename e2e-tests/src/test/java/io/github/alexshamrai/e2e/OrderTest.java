package io.github.alexshamrai.e2e;

import io.github.alexshamrai.client.BookClient;
import io.github.alexshamrai.client.OrderClient;
import io.github.alexshamrai.dto.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.alexshamrai.e2e.BaseTest.BOOK_SERVICE_URL;
import static io.github.alexshamrai.e2e.BaseTest.ORDER_SERVICE_URL;
import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    private final BookClient bookClient = new BookClient(BOOK_SERVICE_URL);
    private final OrderClient orderClient = new OrderClient(ORDER_SERVICE_URL);
    private BookDto testBook;
    private OrderDto createdOrder;
    private final Long userId = 1L;

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
    void createAndRetrieveOneOrder(){

        OrderItemRequest orderItemRequest = OrderItemRequest.builder()
                .bookId(testBook.getId())
                .quantity(1)
                .build();

        List<OrderItemRequest> orderItemsRequestList = List.of(orderItemRequest);

        OrderRequest orderRequest = OrderRequest.builder()
                .userId(userId)
                .items(orderItemsRequestList)
                .build();

        Response createResponse = orderClient.createOrder(orderRequest);
        createResponse.then().statusCode(200);

        OrderDto createdOrder = createResponse.as(OrderDto.class);
        assertThat(createdOrder.getId()).isNotNull();
        assertThat(createdOrder.getOrderItems()).hasSize(1);
        assertThat(createdOrder.getUserId()).isEqualTo(orderRequest.getUserId());

        List<OrderItemDto> allOrderItems = createdOrder.getOrderItems();
        assertThat(allOrderItems.get(0))
                .usingRecursiveComparison()
                .ignoringFields("id", "price")
                .isEqualTo(orderItemRequest);

    }

    @Test
    void getOrderById() {

        // preconditions: create Order
        OrderItemRequest orderItemRequest = OrderItemRequest.builder()
                .bookId(testBook.getId())
                .quantity(1)
                .build();

        List<OrderItemRequest> orderItemsRequestList = List.of(orderItemRequest);

        OrderRequest orderRequest = OrderRequest.builder()
                .userId(userId)
                .items(orderItemsRequestList)
                .build();

        Response createResponse = orderClient.createOrder(orderRequest);
        createResponse.then().statusCode(200);
        createdOrder = createResponse.as(OrderDto.class);

        // test
        Response getOrderResponse = orderClient.getOrder(createdOrder.getId());
        getOrderResponse.then().statusCode(200);

        OrderDto order = getOrderResponse.as(OrderDto.class);
        assertThat(order.getId()).isNotNull();
        assertThat(order.getOrderItems()).hasSize(1);
        assertThat(order.getUserId()).isEqualTo(orderRequest.getUserId());

        List<OrderItemDto> allOrderItems = order.getOrderItems();
        assertThat(allOrderItems.get(0))
                .usingRecursiveComparison()
                .ignoringFields("id", "price")
                .isEqualTo(orderItemRequest);

    }

    @Test
    void deleteOrderById() {

        // preconditions: create Order
        OrderItemRequest orderItemRequest = OrderItemRequest.builder()
                .bookId(testBook.getId())
                .quantity(1)
                .build();

        List<OrderItemRequest> orderItemsRequestList = List.of(orderItemRequest);

        OrderRequest orderRequest = OrderRequest.builder()
                .userId(userId)
                .items(orderItemsRequestList)
                .build();

        Response createResponse = orderClient.createOrder(orderRequest);
        createResponse.then().statusCode(200);
        createdOrder = createResponse.as(OrderDto.class);

        // test
        Response deleteResponse = orderClient.deleteOrder(createdOrder.getId());
        deleteResponse.then().statusCode(204);
    }

}

/*
Заметки касательно тестового покрытия:

1. В задании сделала пару тестов - чтоб показать умение работать с разными типами запросов. В идеале - нужны тесты на все виды http методов.
Писала по аналогии с тестами из BookTest класса - чтобы показать умение работать в команде) хорошо, когда есть код стайл и команда старется писать в одном стиле
2. Можно сделать контрактные тесты с использованием json-scheme validation
3. Тесты на обязательные/необязательные поля в боди риквестах, валидацию полей
4. Тесты на проверки разных статус кодов и ошибок (например, при ГЕТ несуществующего ордера - проверять наличие корректного кода и текста ошибки)
*/
