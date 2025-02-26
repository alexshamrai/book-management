package io.github.alexshamrai.orderservice.dto;

import lombok.Data;

@Data
public class BookDto {
    private Long id;
    private String title;
    private String author;
    private Double price;
    private Integer stockQuantity;
}