package com.orden.service.orden_service.dto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;


@Data
public class CreateOrderRequest {

    private Long userId;
    private Set<Item> items = new HashSet<>();

    @Data
    public static class Item {
        private Long productId;
        private Integer quantity;
    }
}
