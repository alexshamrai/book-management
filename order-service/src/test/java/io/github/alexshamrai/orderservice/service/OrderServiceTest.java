package io.github.alexshamrai.orderservice.service;

import io.github.alexshamrai.orderservice.client.BookClient;
import io.github.alexshamrai.orderservice.dto.BookDto;
import io.github.alexshamrai.orderservice.dto.OrderItemRequest;
import io.github.alexshamrai.orderservice.dto.OrderRequest;
import io.github.alexshamrai.orderservice.exception.InsufficientStockException;
import io.github.alexshamrai.orderservice.model.Order;
import io.github.alexshamrai.orderservice.model.OrderItem;
import io.github.alexshamrai.orderservice.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private BookClient bookClient;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private BookDto testBook;
    private OrderRequest testOrderRequest;

    @BeforeEach
    void setUp() {
        testBook = new BookDto();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setPrice(29.99);
        testBook.setStockQuantity(10);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setBookId(1L);
        itemRequest.setQuantity(2);

        testOrderRequest = new OrderRequest();
        testOrderRequest.setUserId(100L);
        testOrderRequest.setItems(List.of(itemRequest));

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUserId(100L);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setStatus("PENDING");
        testOrder.setTotalAmount(59.98);
        testOrder.setOrderItems(new ArrayList<>());
    }

    @Test
    void createOrder_WithValidData_ShouldCreateOrder() {
        // Given
        when(bookClient.getBook(1L)).thenReturn(ResponseEntity.ok(testBook));
        when(bookClient.updateStock(1L, 8)).thenReturn(ResponseEntity.ok(testBook));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.createOrder(testOrderRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo("PENDING");
        verify(bookClient, times(1)).getBook(1L);
        verify(bookClient, times(1)).updateStock(1L, 8);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void createOrder_WhenBookNotFound_ShouldThrowException() {
        // Given
        when(bookClient.getBook(anyLong()))
                .thenReturn(ResponseEntity.status(HttpStatus.NOT_FOUND).build());

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(testOrderRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Book not found");

        verify(bookClient, times(1)).getBook(1L);
        verify(bookClient, never()).updateStock(anyLong(), anyInt());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WithInsufficientStock_ShouldThrowException() {
        // Given
        testBook.setStockQuantity(1); // Less than requested quantity
        when(bookClient.getBook(1L)).thenReturn(ResponseEntity.ok(testBook));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(testOrderRequest))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");

        verify(bookClient, times(1)).getBook(1L);
        verify(bookClient, never()).updateStock(anyLong(), anyInt());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_WithMultipleItems_ShouldCalculateTotalCorrectly() {
        // Given
        BookDto book2 = new BookDto();
        book2.setId(2L);
        book2.setTitle("Another Book");
        book2.setPrice(15.99);
        book2.setStockQuantity(5);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setBookId(2L);
        item2.setQuantity(3);

        testOrderRequest.getItems().add(item2);

        when(bookClient.getBook(1L)).thenReturn(ResponseEntity.ok(testBook));
        when(bookClient.getBook(2L)).thenReturn(ResponseEntity.ok(book2));
        when(bookClient.updateStock(anyLong(), anyInt())).thenReturn(ResponseEntity.ok(testBook));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        // When
        Order result = orderService.createOrder(testOrderRequest);

        // Then
        assertThat(result).isNotNull();
        // Total = (29.99 * 2) + (15.99 * 3) = 59.98 + 47.97 = 107.95
        assertThat(result.getTotalAmount()).isEqualTo(107.95);
        verify(bookClient, times(1)).getBook(1L);
        verify(bookClient, times(1)).getBook(2L);
        verify(bookClient, times(2)).updateStock(anyLong(), anyInt());
    }

    @Test
    void getUserOrders_ShouldReturnOrdersForUser() {
        // Given
        Order order2 = new Order();
        order2.setId(2L);
        order2.setUserId(100L);

        List<Order> orders = Arrays.asList(testOrder, order2);
        when(orderRepository.findByUserId(100L)).thenReturn(orders);

        // When
        List<Order> result = orderService.getUserOrders(100L);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(testOrder, order2);
        verify(orderRepository, times(1)).findByUserId(100L);
    }

    @Test
    void getUserOrders_WhenNoOrders_ShouldReturnEmptyList() {
        // Given
        when(orderRepository.findByUserId(anyLong())).thenReturn(List.of());

        // When
        List<Order> result = orderService.getUserOrders(999L);

        // Then
        assertThat(result).isEmpty();
        verify(orderRepository, times(1)).findByUserId(999L);
    }

    @Test
    void getOrder_WhenOrderExists_ShouldReturnOrder() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When
        Order result = orderService.getOrder(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(100L);
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void getOrder_WhenOrderDoesNotExist_ShouldThrowException() {
        // Given
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrder(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Order not found");

        verify(orderRepository, times(1)).findById(999L);
    }

    @Test
    void updateOrderStatus_WhenOrderExists_ShouldUpdateStatus() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Order result = orderService.updateOrderStatus(1L, "COMPLETED");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    void updateOrderStatus_WhenOrderDoesNotExist_ShouldThrowException() {
        // Given
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.updateOrderStatus(999L, "COMPLETED"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Order not found");

        verify(orderRepository, times(1)).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void deleteOrder_WhenOrderExists_ShouldDeleteOrder() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        doNothing().when(orderRepository).delete(testOrder);

        // When
        orderService.deleteOrder(1L);

        // Then
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).delete(testOrder);
    }

    @Test
    void deleteOrder_WhenOrderDoesNotExist_ShouldThrowException() {
        // Given
        when(orderRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.deleteOrder(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Order not found");

        verify(orderRepository, times(1)).findById(999L);
        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test
    void deleteAllOrders_ShouldDeleteAllOrders() {
        // Given
        when(orderRepository.count()).thenReturn(5L);
        doNothing().when(orderRepository).deleteAll();

        // When
        orderService.deleteAllOrders();

        // Then
        verify(orderRepository, times(1)).count();
        verify(orderRepository, times(1)).deleteAll();
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Given
        Order order2 = new Order();
        order2.setId(2L);
        order2.setUserId(200L);

        List<Order> orders = Arrays.asList(testOrder, order2);
        when(orderRepository.findAll()).thenReturn(orders);

        // When
        List<Order> result = orderService.getAllOrders();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).containsExactly(testOrder, order2);
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getAllOrders_WhenEmpty_ShouldReturnEmptyList() {
        // Given
        when(orderRepository.findAll()).thenReturn(List.of());

        // When
        List<Order> result = orderService.getAllOrders();

        // Then
        assertThat(result).isEmpty();
        verify(orderRepository, times(1)).findAll();
    }
}
