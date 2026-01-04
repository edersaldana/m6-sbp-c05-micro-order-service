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
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OrderServiceTest {

    @Mock
    private UserClient userClient;

    @Mock
    private ProductClient productClient;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderService orderService;

    private OrderEntity orderEntity;
    private User user;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Crear usuario de prueba
        user = new User();
        user.setId(1L);
        user.setName("Juan Pérez");
        user.setEmail("juan.perez@example.com");

        // Crear orden de prueba
        orderEntity = new OrderEntity();
        orderEntity.setId(1L);
        orderEntity.setOrderNumber("ORD-2025-001");
        orderEntity.setStatus("PENDING");
        orderEntity.setUserId(1L);
        orderEntity.setCreatedAt(LocalDateTime.now());

        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(100L);
        item.setQuantity(2);
        item.setUnitPrice(BigDecimal.valueOf(50));
        item.setSubtotal(BigDecimal.valueOf(100));
        item.setOrderEntity(orderEntity);
        orderEntity.setItems((java.util.List<OrderItemEntity>) Set.of(item));
    }

    @Test
    public void testFindById_OrderExists() {
        when(orderRepository.findByIdWithItems(1L)).thenReturn(Optional.of(orderEntity));
        when(userClient.getUserById(1L)).thenReturn(user);
        when(orderMapper.toDomainWithUser(orderEntity, user)).thenAnswer(invocation -> {
            Order order = new Order();
            order.setId(orderEntity.getId());
            order.setOrderNumber(orderEntity.getOrderNumber());
            order.setStatus(orderEntity.getStatus());
            order.setUser(user);
            return order;
        });
        when(orderItemMapper.toDomainWithProduct(any(OrderItemEntity.class), any(Product.class)))
                .thenAnswer(invocation -> new OrderItem());

        Order result = orderService.findById(1L); // <- ahora pasamos userId también

        assertNotNull(result);
        assertEquals("ORD-2025-001", result.getOrderNumber());
        assertEquals("PENDING", result.getStatus());
        assertEquals(user, result.getUser());
    }

    @Test
    public void testFindById_OrderNotFound() {
        when(orderRepository.findByIdWithItems(2L)).thenReturn(Optional.empty());

        Order result = orderService.findById(2L); // <- userId ficticio
        assertNull(result);
    }

    @Test
    public void testRegisterOrder() {
        CreateOrderRequest.Item itemRequest = new CreateOrderRequest.Item();
        itemRequest.setProductId(100L);
        itemRequest.setQuantity(2);

        CreateOrderRequest request = new CreateOrderRequest();
        request.setUserId(1L);
        request.setItems(Set.of(itemRequest));

        // Mock último pedido
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

        Order result = orderService.registerOrder(request);

        assertNotNull(result);
        assertEquals(user, result.getUser());
        assertEquals("ORD-2025-001", result.getOrderNumber());
    }
}

