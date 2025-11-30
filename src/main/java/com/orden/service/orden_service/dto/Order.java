package com.orden.service.orden_service.dto;

import com.orden.service.orden_service.client.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long id;
    private String orderNumber;
    private User user;                     // DTO puede contener User completo
    private Set<OrderItem> items;          // DTO de los items
    private Double totalAmount;            // Puede ser Double en DTO
    private String status;
    private LocalDateTime createdAt;
}
