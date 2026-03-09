package com.parcial.ordenes.repository;

import com.parcial.ordenes.model.Orden;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenRepository extends MongoRepository<Orden, String> {
    List<Orden> findByUsuarioId(String usuarioId);
    List<Orden> findByStatus(String status);
}
