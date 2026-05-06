package com.botica.repository;

import com.botica.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProveedorRepository
        extends JpaRepository<Proveedor, Long> {

    List<Proveedor> findByNombreContaining(String nombre);
}