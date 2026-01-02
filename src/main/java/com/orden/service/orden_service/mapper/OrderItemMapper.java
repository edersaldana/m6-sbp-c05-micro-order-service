package com.orden.service.orden_service.mapper;

import com.orden.service.orden_service.client.Product;
import com.orden.service.orden_service.dto.OrderItem;
import com.orden.service.orden_service.entity.OrderItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    OrderItemMapper INSTANCE = Mappers.getMapper(OrderItemMapper.class);

    OrderItem toDomain(OrderItemEntity entity);
    //OrderItemEntity toEntity(OrderItem domain);

    public default OrderItem toDomainWithProduct(OrderItemEntity entity, Product product) {
        return OrderItem.builder()
                .productId(entity.getProductId())
                .productName(product.getName())
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .imageUrl(product.getImageUrl())
                .subtotal(entity.getSubtotal())
                .build();
    }

    OrderItem toDto(OrderItemEntity entity);
    OrderItemEntity toEntity(OrderItem dto);
}
