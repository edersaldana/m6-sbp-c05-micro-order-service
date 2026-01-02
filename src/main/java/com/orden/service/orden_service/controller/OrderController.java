package com.orden.service.orden_service.controller;

import com.orden.service.orden_service.dto.CreateOrderRequest;
import com.orden.service.orden_service.dto.Order;
import com.orden.service.orden_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping()
    public ResponseEntity<Order> register(
            @RequestBody CreateOrderRequest createOrderRequest
    )
    {
        Order order = orderService.registerOrder(createOrderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        log.info("Request  Get  order by  id :{}",id);
        return ResponseEntity.ok(orderService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.findAllOrders();
        return ResponseEntity.ok(orders);
    }
}
