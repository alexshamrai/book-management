package io.github.alexshamrai.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrdersResponse {
    private List<OrderDto> orders;
}