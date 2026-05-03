package com.midterm.orders.controller;

import com.midterm.orders.model.Order;
import com.midterm.orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import org.springframework.kafka.core.KafkaTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    // POST /orders
    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order, @RequestHeader(value = "X-Retry", required = false) String isRetry) {
        try {
            log.info("POST /orders - userId: {}", order.getUserId());
            Order created = orderService.createOrder(order);
            try {
                kafkaTemplate.send("inventory_update_events", objectMapper.writeValueAsString(created));
            } catch (Exception kafkaEx) {
                log.error("Error sending to inventory_update_events", kafkaEx);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error in POST /orders - sending to topic: {}", order, e);
            if (!"true".equals(isRetry)) {
                try {
                    kafkaTemplate.send("order_retry_jobs", objectMapper.writeValueAsString(order));
                } catch (Exception kafkaEx) {
                    log.error("Error sending to Kafka", kafkaEx);
                }
            } else {
                log.warn("This is a failed retry, NOT sending to Kafka again to avoid infinite loop.");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /orders/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable String id) {
        log.info("GET /orders/{}", id);
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /orders/user/{id}
    @GetMapping("/user/{id}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable String id) {
        log.info("GET /orders/user/{}", id);
        return ResponseEntity.ok(orderService.getOrdersByUser(id));
    }

    // PUT /orders/{id}/status
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable String id,
                                                    @RequestBody Map<String, String> body) {
        String status = body.get("status");
        log.info("PUT /orders/{}/status - status: {}", id, status);
        return orderService.updateOrderStatus(id, status)
                .map(updated -> {
                    try {
                        kafkaTemplate.send("order_status_changed_events", objectMapper.writeValueAsString(updated));
                    } catch (Exception e) {
                        log.error("Error sending to order_status_changed_events", e);
                    }
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // PUT /orders/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable String id, @RequestBody Order order) {
        log.info("PUT /orders/{} - updating entire order", id);
        
        // Before updating, get current to check if products changed
        return orderService.getOrderById(id).flatMap(currentOrder -> {
            boolean productsChanged = false;
            // Simplified check: if items are provided, assume products changed
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                productsChanged = true;
            }
            
            final boolean finalProductsChanged = productsChanged;
            
            return orderService.updateOrder(id, order).map(updated -> {
                if (finalProductsChanged) {
                    try {
                        kafkaTemplate.send("inventory_update_events", objectMapper.writeValueAsString(updated));
                    } catch (Exception e) {
                        log.error("Error sending to inventory_update_events", e);
                    }
                }
                return ResponseEntity.ok(updated);
            });
        }).orElse(ResponseEntity.notFound().build());
    }
}
