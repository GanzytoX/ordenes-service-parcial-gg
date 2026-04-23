package com.parcial.ordenes.controller;

import com.parcial.ordenes.model.Orden;
import com.parcial.ordenes.service.OrdenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ordenes")
@RequiredArgsConstructor
@Slf4j
public class OrdenController {

    private final OrdenService ordenService;

    // POST /ordenes
    @PostMapping
    public ResponseEntity<Orden> createOrden(@RequestBody Orden orden) {
        try {
            log.info("POST /ordenes - usuarioId: {}", orden.getUsuarioId());
            Orden created = ordenService.createOrden(orden);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            log.error("Error en POST /ordenes - enviando al tópico: {}", orden, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /ordenes/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Orden> getOrdenById(@PathVariable String id) {
        log.info("GET /ordenes/{}", id);
        return ordenService.getOrdenById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /ordenes/usuario/{id}
    @GetMapping("/usuario/{id}")
    public ResponseEntity<List<Orden>> getOrdenesByUsuario(@PathVariable String id) {
        log.info("GET /ordenes/usuario/{}", id);
        return ResponseEntity.ok(ordenService.getOrdenesByUsuario(id));
    }

    // PUT /ordenes/{id}/status
    @PutMapping("/{id}/status")
    public ResponseEntity<Orden> updateOrdenStatus(@PathVariable String id,
                                                    @RequestBody Map<String, String> body) {
        String status = body.get("status");
        log.info("PUT /ordenes/{}/status - status: {}", id, status);
        return ordenService.updateOrdenStatus(id, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
