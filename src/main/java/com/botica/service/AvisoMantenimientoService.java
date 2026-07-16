package com.botica.service;

import com.botica.model.AvisoMantenimiento;
import com.botica.repository.AvisoMantenimientoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AvisoMantenimientoService {

    @Autowired
    private AvisoMantenimientoRepository repo;

    // Devuelve el aviso activo, siempre que su fecha de fin no haya pasado ya
    public Optional<AvisoMantenimiento> obtenerAvisoVigente() {
        return repo.findFirstByActivoTrueOrderByIdDesc()
                .filter(a -> a.getFechaFin() == null || a.getFechaFin().isAfter(LocalDateTime.now()));
    }

    public AvisoMantenimiento guardar(AvisoMantenimiento aviso) {
        aviso.setActivo(true);
        return repo.save(aviso);
    }

    public void desactivar(Long id) {
        repo.findById(id).ifPresent(a -> {
            a.setActivo(false);
            repo.save(a);
        });
    }
}