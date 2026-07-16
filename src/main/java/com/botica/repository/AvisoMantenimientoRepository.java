package com.botica.repository;

import com.botica.model.AvisoMantenimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AvisoMantenimientoRepository extends JpaRepository<AvisoMantenimiento, Long> {
    Optional<AvisoMantenimiento> findFirstByActivoTrueOrderByIdDesc();
}