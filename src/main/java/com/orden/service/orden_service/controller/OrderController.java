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
        log.info("Recibiendo petición de creación de orden");
        // Ahora JwtService ya tiene este método
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

        // Validación de seguridad: El usuario solo puede ver sus propias órdenes
        // Verificamos que 'getUser()' no sea null antes de sacar el ID
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<?> getAllOrders(@RequestHeader("Authorization") String token) {
        // CORRECCIÓN 1: Usar jwtService (que ya tienes inyectado) en lugar de jwtUtils
        Long userId = jwtService.extractUserId(token);

        // CORRECCIÓN 2: Llamar al método del servicio que filtra por ID
        return ResponseEntity.ok(orderService.findAllOrdersByUserId(userId));
    }

    @PatchMapping("/{id}/payment")
    public ResponseEntity<Order> processPayment(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id
    ) {
        log.info("Procesando pago para la orden ID: {}", id);

        // Opcional: Podrías validar que la orden pertenece al usuario del token
        // Long userId = jwtService.extractUserId(authHeader);

        Order updatedOrder = orderService.payment(id);
        return ResponseEntity.ok(updatedOrder);
    }
}
