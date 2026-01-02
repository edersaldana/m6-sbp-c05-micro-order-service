package com.orden.service.orden_service.service;

import com.orden.service.orden_service.client.Product;
import com.orden.service.orden_service.client.ProductClient;
import com.orden.service.orden_service.client.UserClient;
import com.orden.service.orden_service.dto.CreateOrderRequest;
import com.orden.service.orden_service.dto.Order;
import com.orden.service.orden_service.dto.OrderItem;
import com.orden.service.orden_service.entity.OrderEntity;
import com.orden.service.orden_service.entity.OrderItemEntity;
import com.orden.service.orden_service.mapper.OrderItemMapper;
import com.orden.service.orden_service.mapper.OrderMapper;
import com.orden.service.orden_service.repository.OrderItemRepository;
import com.orden.service.orden_service.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.orden.service.orden_service.client.User;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class OrderService {

    private  final UserClient userClient;
    private final ProductClient productClient;

    private final OrderMapper orderMapper;
    private final OrderRepository orderRepository;
    private final OrderItemMapper orderItemMapper;

    private OrderItemRepository orderItemRepository;


    @Transactional(readOnly = true)
    public Order findById(Long id) {
        // Trae la entidad del repositorio
        //OrderEntity entity = orderRepository.findById(id).orElse(null);
        OrderEntity entity = orderRepository.findByIdWithItems(id).orElse(null);
        if (entity == null) {
            return null;
        }

        log.info("Orden encontrada: {}", entity.getOrderNumber());
        log.info("Items de la orden (size={}): {}", entity.getItems().size(), entity.getItems());

        // Trae el usuario
        User user = userClient.getUserById(entity.getUserId());

        //List<OrderItemEntity> items = orderItemRepository.findByOrderId(id);
        // Trabaja sobre una copia de la colección para evitar ConcurrentModificationException
        List<OrderItem> orderItems = entity.getItems().stream()
                .map(item -> {
                    Product product = productClient.getProductById(item.getProductId());
                    return orderItemMapper.toDomainWithProduct(item, product);
                })
                .collect(Collectors.toList());

        // Mapea la orden con usuario
        Order order = orderMapper.toDomainWithUser(entity, user);
        order.setItems(orderItems);

        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> findAllOrders() {
        List<OrderEntity> entities = orderRepository.findAllWithItems();

        return entities.stream().map(entity -> {
            User user = userClient.getUserById(entity.getUserId());

            List<OrderItem> orderItems = entity.getItems().stream()
                    .map(item -> {
                        Product product = productClient.getProductById(item.getProductId());
                        return orderItemMapper.toDomainWithProduct(item, product);
                    })
                    .collect(Collectors.toList());

            Order order = orderMapper.toDomainWithUser(entity, user);
            order.setItems(orderItems);
            return order;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Order registerOrder(CreateOrderRequest request) {

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setUserId(request.getUserId());
        orderEntity.setOrderNumber(nextOrderNumber());
        orderEntity.setStatus("PENDING");
        orderEntity.setCreatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;

        for (CreateOrderRequest.Item item : request.getItems()) {

            Product product = productClient.getProductById(item.getProductId());

            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(product.getPrice());

            BigDecimal subtotal =
                    product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));

            orderItem.setSubtotal(subtotal);
            orderItem.setOrderEntity(orderEntity);

            orderEntity.getItems().add(orderItem);
            total = total.add(subtotal);
        }

        orderEntity.setTotalAmount(total);

        OrderEntity saved = orderRepository.save(orderEntity);

        // Mapear a DTO
        User user = userClient.getUserById(saved.getUserId());

        List<OrderItem> items = saved.getItems().stream()
                .map(i -> {
                    Product product = productClient.getProductById(i.getProductId());
                    OrderItem dto = orderItemMapper.toDomainWithProduct(i, product);
                    dto.setImageUrl(product.getImageUrl());
                    return dto;
                })
                .collect(Collectors.toList());

        Order order = orderMapper.toDomain(saved);
        order.setUser(user);
        order.setItems(items);

        return order;
    }

    public String nextOrderNumber() {
        int numberId = 0;
        int year = LocalDate.now().getYear();
        Optional<OrderEntity> lastOrderEntity = orderRepository.findTopByOrderByIdDesc();

        if (lastOrderEntity.isPresent()) {
            String lastOrderNumber = lastOrderEntity.get().getOrderNumber();
            String[] parts = lastOrderNumber.split("-");
            if (parts.length == 3 && parts[1].equals(String.valueOf(year))) {
                numberId = Integer.parseInt(parts[2]) + 1;
            }
        }

        return String.format("ORD-%d-%03d", year, numberId);
    }
}
