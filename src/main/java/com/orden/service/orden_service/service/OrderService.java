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

    private final UserClient userClient;
    private final ProductClient productClient;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public Order findById(Long id) {
        OrderEntity entity = orderRepository.findByIdWithItems(id).orElse(null);
        if (entity == null) return null;

        User user = userClient.getUserById(entity.getUserId());

        List<OrderItem> orderItems = entity.getItems().stream()
                .map(item -> {
                    Product product = productClient.getProductById(item.getProductId());
                    return orderItemMapper.toDomainWithProduct(item, product);
                }).collect(Collectors.toList());

        Order order = orderMapper.toDomainWithUser(entity, user);
        order.setItems(orderItems);
        return order;
    }

    @Transactional(readOnly = true)
    public List<Order> findAllOrders() {
        List<OrderEntity> entities = orderRepository.findAllWithItems();
        return entities.stream().map(entity -> {
            User user = userClient.getUserById(entity.getUserId());
            List<OrderItem> items = entity.getItems().stream()
                    .map(i -> {
                        Product product = productClient.getProductById(i.getProductId());
                        return orderItemMapper.toDomainWithProduct(i, product);
                    }).collect(Collectors.toList());

            Order order = orderMapper.toDomainWithUser(entity, user);
            order.setItems(items);
            return order;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Order> findAllOrdersByUserId(Long userId) {
        // Asegúrate que este nombre coincida con el paso anterior en el Repository
        List<OrderEntity> entities = orderRepository.findByUserIdWithItems(userId);

        return entities.stream().map(entity -> {
            User user = userClient.getUserById(entity.getUserId());
            List<OrderItem> items = entity.getItems().stream()
                    .map(i -> {
                        Product product = productClient.getProductById(i.getProductId());
                        return orderItemMapper.toDomainWithProduct(i, product);
                    }).collect(Collectors.toList());

            Order order = orderMapper.toDomainWithUser(entity, user);
            order.setItems(items);
            return order; // Correcto
        }).collect(Collectors.toList());
    }

    @Transactional
    public Order registerOrder(CreateOrderRequest request) {
        OrderEntity entity = new OrderEntity();
        entity.setUserId(request.getUserId());
        entity.setOrderNumber(nextOrderNumber());
        entity.setStatus("PENDING");
        entity.setCreatedAt(LocalDateTime.now());

        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderRequest.Item item : request.getItems()) {
            Product product = productClient.getProductById(item.getProductId());
            OrderItemEntity orderItem = new OrderItemEntity();
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItem.setOrderEntity(entity);

            entity.getItems().add(orderItem);
            total = total.add(orderItem.getSubtotal());
        }

        entity.setTotalAmount(total);
        OrderEntity saved = orderRepository.save(entity);

        User user = userClient.getUserById(saved.getUserId());
        List<OrderItem> items = saved.getItems().stream()
                .map(i -> orderItemMapper.toDomainWithProduct(i, productClient.getProductById(i.getProductId())))
                .collect(Collectors.toList());

        Order order = orderMapper.toDomain(saved);
        order.setUser(user);
        order.setItems(items);

        return order;
    }

    public String nextOrderNumber() {
        int numberId = 0;
        int year = LocalDate.now().getYear();
        Optional<OrderEntity> last = orderRepository.findTopByOrderByIdDesc();
        if (last.isPresent()) {
            String[] parts = last.get().getOrderNumber().split("-");
            if (parts.length == 3 && parts[1].equals(String.valueOf(year))) {
                numberId = Integer.parseInt(parts[2]) + 1;
            }
        }
        return String.format("ORD-%d-%03d", year, numberId);
    }

    @Transactional
    public Order payment(Long orderId) {

        OrderEntity orderEntity = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Solo permitir pagar si está pendiente
        if (!"PENDING".equals(orderEntity.getStatus())) {
            throw new IllegalStateException("Order cannot be paid");
        }

        // Simulación de pago exitoso
        orderEntity.setStatus("CONFIRMED");
        orderEntity.setUpdatedAt(LocalDateTime.now());

        orderRepository.save(orderEntity);

        // Obtener usuario (NO debe romper el flujo si falla)
        User user = null;
        try {
            user = userClient.getUserById(orderEntity.getUserId());
        } catch (Exception e) {
            log.warn("User service not available, continuing payment");
        }

        return orderMapper.toDomainWithUser(orderEntity, user);
    }

}
