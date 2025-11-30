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

    default OrderItem toDomainWithProduct(OrderItemEntity entity, Product product) {
        OrderItem orderItem = toDomain(entity);
        orderItem.setProduct(product);
        return orderItem;
    }

    OrderItem toDto(OrderItemEntity entity);
    OrderItemEntity toEntity(OrderItem dto);
}
