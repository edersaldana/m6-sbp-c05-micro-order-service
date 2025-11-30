package com.orden.service.orden_service.mapper;

import com.orden.service.orden_service.dto.Order;
import com.orden.service.orden_service.entity.OrderEntity;
import com.orden.service.orden_service.client.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    Order toDomain(OrderEntity entity);
    OrderEntity toEntity(Order domain);

    default Order toDomainWithUser(OrderEntity entity, User user) {
        Order order = toDomain(entity);
        order.setUser(user);
        return order;
    }

    Order toDto(OrderEntity entity);
}
