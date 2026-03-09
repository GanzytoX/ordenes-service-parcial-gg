package com.parcial.ordenes.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "ordenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Orden {

    @Id
    private String id;

    private String usuarioId;

    private List<ItemOrden> items;

    private Double total;

    /** Estados posibles: PENDIENTE, PROCESADA, CANCELADA */
    private String status = "PENDIENTE";

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemOrden {
        private String productoId;
        private String nombre;
        private Integer cantidad;
        private Double precio;
    }
}
