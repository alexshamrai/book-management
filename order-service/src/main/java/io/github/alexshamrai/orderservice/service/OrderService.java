package io.github.alexshamrai.orderservice.service;

import io.github.alexshamrai.orderservice.client.BookClient;
import io.github.alexshamrai.orderservice.dto.BookDto;
import io.github.alexshamrai.orderservice.exception.InsufficientStockException;
import io.github.alexshamrai.orderservice.model.Order;
import io.github.alexshamrai.orderservice.model.OrderItem;
import io.github.alexshamrai.orderservice.repository.OrderRepository;
import io.github.alexshamrai.orderservice.dto.OrderRequest;
import io.github.alexshamrai.orderservice.dto.OrderItemRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final BookClient bookClient;

    @Transactional
    public Order createOrder(OrderRequest orderRequest) {
        log.info("Creating order for user: {}", orderRequest.getUserId());
        log.debug("Order contains {} items", orderRequest.getItems().size());

        Order order = new Order();
        order.setUserId(orderRequest.getUserId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            log.debug("Processing order item: bookId={}, quantity={}",
                    itemRequest.getBookId(), itemRequest.getQuantity());

            ResponseEntity<BookDto> bookResponse = bookClient.getBook(itemRequest.getBookId());
            if (!bookResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Book not found: {}", itemRequest.getBookId());
                throw new EntityNotFoundException("Book not found: " + itemRequest.getBookId());
            }

            BookDto book = bookResponse.getBody();
            log.debug("Found book: {} by {}, available stock: {}",
                    book.getTitle(), book.getAuthor(), book.getStockQuantity());

            if (book.getStockQuantity() < itemRequest.getQuantity()) {
                log.error("Insufficient stock for book: {} (requested: {}, available: {})",
                        book.getTitle(), itemRequest.getQuantity(), book.getStockQuantity());
                throw new InsufficientStockException("Insufficient stock for book: " + book.getTitle());
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setBookId(book.getId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(book.getPrice());
            orderItem.setOrder(order);
            orderItems.add(orderItem);

            totalAmount += book.getPrice() * itemRequest.getQuantity();

            // Update book stock
            log.debug("Updating stock for book: {} from {} to {}",
                    book.getId(), book.getStockQuantity(),
                    book.getStockQuantity() - itemRequest.getQuantity());
            bookClient.updateStock(book.getId(),
                book.getStockQuantity() - itemRequest.getQuantity());
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        Order savedOrder = orderRepository.save(order);
        log.info("Successfully created order {} for user {} with total amount: {}",
                savedOrder.getId(), savedOrder.getUserId(), savedOrder.getTotalAmount());

        return savedOrder;
    }


    @Transactional(readOnly = true)
    public List<Order> getUserOrders(Long userId) {
        log.debug("Fetching orders for user: {}", userId);
        List<Order> orders = orderRepository.findByUserId(userId);
        log.info("Found {} orders for user: {}", orders.size(), userId);
        return orders;
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        log.debug("Fetching order with id: {}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order not found with id: {}", orderId);
                    return new EntityNotFoundException("Order not found");
                });
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        log.info("Updating order {} status to: {}", orderId, status);
        Order order = getOrder(orderId);
        String oldStatus = order.getStatus();
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        log.info("Successfully updated order {} status from {} to {}", orderId, oldStatus, status);
        return updatedOrder;
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        log.info("Attempting to delete order with id: {}", orderId);
        Order order = getOrder(orderId); // Ensure the order exists; throws exception if not found
        orderRepository.delete(order); // Delete the order
        log.info("Successfully deleted order with id: {}", orderId);
    }

    @Transactional
    public void deleteAllOrders() {
        log.warn("Deleting all orders from the database");
        long count = orderRepository.count();
        orderRepository.deleteAll(); // Deletes all orders from the repository
        log.warn("Successfully deleted {} orders", count);
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        log.debug("Fetching all orders");
        List<Order> orders = orderRepository.findAll(); // Returns a list of all orders
        log.info("Retrieved {} orders", orders.size());
        return orders;
    }

}