package com.midterm.orders.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    private String id;

    private String userId;

    private List<ItemOrden> items;

    private Double total;

    /** Estados posibles: PENDING, PROCESADA, CANCELADA */
    private String status = "PENDING";

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemOrden {
        private String productoId;
        private String name;
        private Integer cantidad;
        private Double price;
    }
}
