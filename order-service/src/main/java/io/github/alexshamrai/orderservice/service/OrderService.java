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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final BookClient bookClient;

    @Transactional
    public Order createOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setUserId(orderRequest.getUserId());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            ResponseEntity<BookDto> bookResponse = bookClient.getBook(itemRequest.getBookId());
            if (!bookResponse.getStatusCode().is2xxSuccessful()) {
                throw new EntityNotFoundException("Book not found: " + itemRequest.getBookId());
            }

            BookDto book = bookResponse.getBody();
            if (book.getStockQuantity() < itemRequest.getQuantity()) {
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
            bookClient.updateStock(book.getId(),
                book.getStockQuantity() - itemRequest.getQuantity());
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }

    
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }
    
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
    }
    
    @Transactional
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = getOrder(orderId);
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = getOrder(orderId); // Ensure the order exists; throws exception if not found
        orderRepository.delete(order); // Delete the order
    }

    @Transactional
    public void deleteAllOrders() {
        orderRepository.deleteAll(); // Deletes all orders from the repository
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll(); // Returns a list of all orders
    }

}