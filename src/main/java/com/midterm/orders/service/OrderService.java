package com.midterm.orders.service;

import com.midterm.orders.model.Order;
import com.midterm.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository ordenRepository;

    public Order createOrder(Order order) {
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        if (order.getItems() != null) {
            double total = order.getItems().stream()
                    .mapToDouble(item -> item.getPrice() * item.getCantidad())
                    .sum();
            order.setTotal(total);
        }
        Order saved = ordenRepository.save(order);
        log.info("Order creada con id: {}", saved.getId());
        return saved;
    }

    public Optional<Order> getOrderById(String id) {
        log.info("Buscando order con id: {}", id);
        return ordenRepository.findById(id);
    }

    public List<Order> getOrdersByUser(String userId) {
        log.info("Buscando órdenes del user: {}", userId);
        return ordenRepository.findByUserId(userId);
    }

    public Optional<Order> updateOrderStatus(String id, String status) {
        return ordenRepository.findById(id).map(order -> {
            order.setStatus(status);
            order.setUpdatedAt(LocalDateTime.now());
            Order updated = ordenRepository.save(order);
            log.info("Order {} actualizada a status: {}", id, status);
            return updated;
        });
    }

    public Optional<Order> updateOrder(String id, Order updatedOrderData) {
        return ordenRepository.findById(id).map(order -> {
            order.setUserId(updatedOrderData.getUserId());
            order.setItems(updatedOrderData.getItems());
            order.setStatus(updatedOrderData.getStatus());
            order.setUpdatedAt(LocalDateTime.now());
            
            if (order.getItems() != null) {
                double total = order.getItems().stream()
                        .mapToDouble(item -> item.getPrice() * item.getCantidad())
                        .sum();
                order.setTotal(total);
            }
            
            Order updated = ordenRepository.save(order);
            log.info("Order {} actualizada completamente", id);
            return updated;
        });
    }
}
