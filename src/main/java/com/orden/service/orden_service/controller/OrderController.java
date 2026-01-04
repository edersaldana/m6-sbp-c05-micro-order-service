package com.orden.service.orden_service.controller;

import com.orden.service.orden_service.dto.CreateOrderRequest;
import com.orden.service.orden_service.dto.Order;
import com.orden.service.orden_service.service.JwtService;
import com.orden.service.orden_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final JwtService jwtService;

    @PostMapping
    public ResponseEntity<Order> register(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody CreateOrderRequest createOrderRequest
    ) {
        Long userId = jwtService.extractUserId(authHeader);
        createOrderRequest.setUserId(userId);
        Order order = orderService.registerOrder(createOrderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id
    ) {
        Long userId = jwtService.extractUserId(authHeader);
        Order order = orderService.findById(id);
        if (order == null) return ResponseEntity.notFound().build();
        if (!order.getUser().getId().equals(userId)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders(@RequestHeader("Authorization") String authHeader) {
        Long userId = jwtService.extractUserId(authHeader);
        List<Order> orders = orderService.findAllOrders().stream()
                .filter(o -> o.getUser().getId().equals(userId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }
}
