package com.botica.repository;

import com.botica.model.OrdenEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrdenEntradaRepository
        extends JpaRepository<OrdenEntrada, Long> {
    List<OrdenEntrada> findAllByOrderByFechaDesc();
}