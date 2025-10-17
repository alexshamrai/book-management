package io.github.alexshamrai.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.alexshamrai.orderservice.dto.OrderItemRequest;
import io.github.alexshamrai.orderservice.dto.OrderRequest;
import io.github.alexshamrai.orderservice.exception.InsufficientStockException;
import io.github.alexshamrai.orderservice.model.Order;
import io.github.alexshamrai.orderservice.model.OrderItem;
import io.github.alexshamrai.orderservice.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private Order testOrder;
    private OrderRequest testOrderRequest;

    @BeforeEach
    void setUp() {
        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUserId(100L);
        testOrder.setOrderDate(LocalDateTime.now());
        testOrder.setStatus("PENDING");
        testOrder.setTotalAmount(59.98);
        testOrder.setOrderItems(new ArrayList<>());

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setBookId(1L);
        itemRequest.setQuantity(2);

        testOrderRequest = new OrderRequest();
        testOrderRequest.setUserId(100L);
        testOrderRequest.setItems(List.of(itemRequest));
    }

    @Test
    void getAllOrders_ShouldReturnListOfOrders() throws Exception {
        // Given
        Order order2 = new Order();
        order2.setId(2L);
        order2.setUserId(200L);
        order2.setStatus("COMPLETED");
        order2.setTotalAmount(99.99);
        order2.setOrderDate(LocalDateTime.now());
        order2.setOrderItems(new ArrayList<>());

        List<Order> orders = Arrays.asList(testOrder, order2);
        when(orderService.getAllOrders()).thenReturn(orders);

        // When & Then
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].userId", is(100)))
                .andExpect(jsonPath("$[0].status", is("PENDING")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].userId", is(200)))
                .andExpect(jsonPath("$[1].status", is("COMPLETED")));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void getAllOrders_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        // Given
        when(orderService.getAllOrders()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(orderService, times(1)).getAllOrders();
    }

    @Test
    void getUserOrders_ShouldReturnOrdersForUser() throws Exception {
        // Given
        List<Order> orders = List.of(testOrder);
        when(orderService.getUserOrders(100L)).thenReturn(orders);

        // When & Then
        mockMvc.perform(get("/api/orders/user/100"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].userId", is(100)));

        verify(orderService, times(1)).getUserOrders(100L);
    }

    @Test
    void getOrder_WhenOrderExists_ShouldReturnOrder() throws Exception {
        // Given
        when(orderService.getOrder(1L)).thenReturn(testOrder);

        // When & Then
        mockMvc.perform(get("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.userId", is(100)))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.totalAmount", is(59.98)));

        verify(orderService, times(1)).getOrder(1L);
    }

    @Test
    void getOrder_WhenOrderDoesNotExist_ShouldReturn404() throws Exception {
        // Given
        when(orderService.getOrder(anyLong()))
                .thenThrow(new EntityNotFoundException("Order not found"));

        // When & Then
        mockMvc.perform(get("/api/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Order not found")));

        verify(orderService, times(1)).getOrder(999L);
    }

    @Test
    void createOrder_WithValidData_ShouldReturnCreatedOrder() throws Exception {
        // Given
        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(testOrder);

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.userId", is(100)))
                .andExpect(jsonPath("$.status", is("PENDING")))
                .andExpect(jsonPath("$.totalAmount", is(59.98)));

        verify(orderService, times(1)).createOrder(any(OrderRequest.class));
    }

    @Test
    void createOrder_WithInsufficientStock_ShouldReturn400() throws Exception {
        // Given
        when(orderService.createOrder(any(OrderRequest.class)))
                .thenThrow(new InsufficientStockException("Insufficient stock for book: Test Book"));

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.error", is("Insufficient Stock")))
                .andExpect(jsonPath("$.message", is("Insufficient stock for book: Test Book")));

        verify(orderService, times(1)).createOrder(any(OrderRequest.class));
    }

    @Test
    void createOrder_WhenBookNotFound_ShouldReturn404() throws Exception {
        // Given
        when(orderService.createOrder(any(OrderRequest.class)))
                .thenThrow(new EntityNotFoundException("Book not found: 1"));

        // When & Then
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testOrderRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Book not found: 1")));

        verify(orderService, times(1)).createOrder(any(OrderRequest.class));
    }

    @Test
    void updateOrderStatus_WhenOrderExists_ShouldReturnUpdatedOrder() throws Exception {
        // Given
        testOrder.setStatus("COMPLETED");
        when(orderService.updateOrderStatus(1L, "COMPLETED")).thenReturn(testOrder);

        // When & Then
        mockMvc.perform(put("/api/orders/1/status")
                        .param("status", "COMPLETED"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("COMPLETED")));

        verify(orderService, times(1)).updateOrderStatus(1L, "COMPLETED");
    }

    @Test
    void updateOrderStatus_WhenOrderDoesNotExist_ShouldReturn404() throws Exception {
        // Given
        when(orderService.updateOrderStatus(anyLong(), anyString()))
                .thenThrow(new EntityNotFoundException("Order not found"));

        // When & Then
        mockMvc.perform(put("/api/orders/999/status")
                        .param("status", "COMPLETED"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Order not found")));

        verify(orderService, times(1)).updateOrderStatus(999L, "COMPLETED");
    }

    @Test
    void deleteOrder_WhenOrderExists_ShouldReturn204() throws Exception {
        // Given
        doNothing().when(orderService).deleteOrder(1L);

        // When & Then
        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isNoContent());

        verify(orderService, times(1)).deleteOrder(1L);
    }

    @Test
    void deleteOrder_WhenOrderDoesNotExist_ShouldReturn404() throws Exception {
        // Given
        doThrow(new EntityNotFoundException("Order not found"))
                .when(orderService).deleteOrder(999L);

        // When & Then
        mockMvc.perform(delete("/api/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Order not found")));

        verify(orderService, times(1)).deleteOrder(999L);
    }

    @Test
    void deleteAllOrders_ShouldReturn204() throws Exception {
        // Given
        doNothing().when(orderService).deleteAllOrders();

        // When & Then
        mockMvc.perform(delete("/api/orders"))
                .andExpect(status().isNoContent());

        verify(orderService, times(1)).deleteAllOrders();
    }
}
