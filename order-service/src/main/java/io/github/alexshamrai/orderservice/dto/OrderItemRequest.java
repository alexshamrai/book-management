package io.github.alexshamrai.orderservice.dto;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long bookId;
    private Integer quantity;
}

