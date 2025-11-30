package com.orden.service.orden_service.service;

import com.orden.service.orden_service.client.Product;
import com.orden.service.orden_service.client.ProductClient;
import com.orden.service.orden_service.client.User;
import com.orden.service.orden_service.client.UserClient;
import com.orden.service.orden_service.dto.CreateOrderRequest;
import com.orden.service.orden_service.dto.Order;
import com.orden.service.orden_service.dto.OrderItem;
import com.orden.service.orden_service.entity.OrderEntity;
import com.orden.service.orden_service.entity.OrderItemEntity;
import com.orden.service.orden_service.mapper.OrderItemMapper;
import com.orden.service.orden_service.mapper.OrderMapper;
import com.orden.service.orden_service.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OrderServiceTest {

    @MockitoBean
    private UserClient userClient;

    @MockitoBean
    private ProductClient productClient;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderMapper orderMapper;

    @MockitoBean
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderService orderService;

    private OrderEntity orderEntity;
    private Order orderDto;
    private User user;

    @Test
    public void testFindById_OrderExists() {
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(orderEntity));
        when(userClient.getUserById(1L)).thenReturn(user);
        when(orderMapper.toDomainWithUser(orderEntity, user)).thenReturn(orderDto);
        when(orderItemMapper.toDomainWithProduct(any(OrderItemEntity.class), any(Product.class)))
                .thenAnswer(invocation -> new OrderItem()); // retorna item dummy

        Order result = orderService.findById(1L);

        assertNotNull(result);
        assertEquals("ORD-2025-001", result.getOrderNumber());
        assertEquals("PENDING", result.getStatus());
        assertEquals(user, result.getUser());
    }

    @Test
    public void testFindById_OrderNotFound() {
        when(orderRepository.findByIdWithItems(2L)).thenReturn(Optional.empty());

        Order result = orderService.findById(2L);
        assertNull(result);
    }

    @Test
    public void testRegisterOrder() {
        // Crear request simulado
        CreateOrderRequest.Item itemRequest = new CreateOrderRequest.Item();
        itemRequest.setProductId(100L); // debe coincidir con el mock
        itemRequest.setQuantity(2);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);
        request.setItems(Set.of(itemRequest));

        // -------------------------------
        // Mocks necesarios
        // -------------------------------

        // Mock último pedido para nextOrderNumber
        OrderEntity lastOrder = new OrderEntity();
        lastOrder.setOrderNumber("ORD-2025-000");
        when(orderRepository.findTopByOrderByIdDesc()).thenReturn(Optional.of(lastOrder));

        // Mock product
        Product product = new Product();
        product.setId(100L);
        product.setName("Product A");
        product.setPrice(BigDecimal.valueOf(50.0));
        when(productClient.getProductById(100L)).thenReturn(product);

        // Mock save
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // Mock mapper y userClient
        when(orderMapper.toDomain(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity e = invocation.getArgument(0);
            Order o = new Order();
            o.setId(e.getId());
            o.setOrderNumber(e.getOrderNumber());
            o.setStatus(e.getStatus());
            o.setTotalAmount(e.getTotalAmount().doubleValue());
            o.setUser(user);
            return o;
        });
        when(userClient.getUserById(1L)).thenReturn(user);
        when(orderItemMapper.toDomainWithProduct(any(OrderItemEntity.class), any(Product.class)))
                .thenAnswer(invocation -> new OrderItem());

        // -------------------------------
        // Llamada al método que queremos testear
        // -------------------------------
        Order result = orderService.registerOrder(request);

        // -------------------------------
        // Asserts
        // -------------------------------
        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals("ORD-2025-001", result.getOrderNumber());
    }
}
