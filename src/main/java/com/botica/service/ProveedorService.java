package com.botica.service;

import com.botica.model.Proveedor;
import com.botica.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ProveedorService {

    @Autowired
    private ProveedorRepository repo;

    public List<Proveedor> listarTodos() {
        return repo.findAll();
    }

    public Proveedor buscarPorId(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Proveedor guardar(Proveedor p) {
        return repo.save(p);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    public List<Proveedor> buscarPorNombre(String nombre) {
        return repo.findByNombreContaining(nombre);
    }
}