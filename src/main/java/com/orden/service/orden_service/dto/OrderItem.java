package com.orden.service.orden_service.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.orden.service.orden_service.client.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"productId", "quantity","unitPrice","subtotal"})
@Builder
public class OrderItem {

    private Long productId;
    private Integer quantity;
    private Double unitPrice;
    private Double subtotal;
    private Product product;

}
