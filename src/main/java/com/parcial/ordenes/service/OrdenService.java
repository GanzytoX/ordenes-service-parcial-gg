package com.parcial.ordenes.service;

import com.parcial.ordenes.model.Orden;
import com.parcial.ordenes.repository.OrdenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdenService {

    private final OrdenRepository ordenRepository;

    public Orden createOrden(Orden orden) {
        orden.setStatus("PENDIENTE");
        orden.setCreatedAt(LocalDateTime.now());
        if (orden.getItems() != null) {
            double total = orden.getItems().stream()
                    .mapToDouble(item -> item.getPrecio() * item.getCantidad())
                    .sum();
            orden.setTotal(total);
        }
        Orden saved = ordenRepository.save(orden);
        log.info("Orden creada con id: {}", saved.getId());
        return saved;
    }

    public Optional<Orden> getOrdenById(String id) {
        log.info("Buscando orden con id: {}", id);
        return ordenRepository.findById(id);
    }

    public List<Orden> getOrdenesByUsuario(String usuarioId) {
        log.info("Buscando órdenes del usuario: {}", usuarioId);
        return ordenRepository.findByUsuarioId(usuarioId);
    }

    public Optional<Orden> updateOrdenStatus(String id, String status) {
        return ordenRepository.findById(id).map(orden -> {
            orden.setStatus(status);
            orden.setUpdatedAt(LocalDateTime.now());
            Orden updated = ordenRepository.save(orden);
            log.info("Orden {} actualizada a status: {}", id, status);
            return updated;
        });
    }
}
