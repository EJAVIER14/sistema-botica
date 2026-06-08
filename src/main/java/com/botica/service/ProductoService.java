package com.botica.service;

import com.botica.model.Producto;
import com.botica.repository.ProductoRepository;
import com.botica.service.MovimientoInventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class ProductoService {

    @Autowired
    private ProductoRepository repo;

    @Autowired
    private MovimientoInventarioService movimientoService;

    public List<Producto> listarTodos() {
        return repo.findAll();
    }

    public Producto guardar(Producto p) {
        return repo.save(p);
    }

    public Producto buscarPorId(Long id) {
        return repo.findById(id).orElse(null);
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    public List<Producto> buscarPorNombre(String nombre) {
        return repo.findByNombreContaining(nombre);
    }

    public List<Producto> productosConStockBajo(Integer minimo) {
        return repo.findByStockLessThan(minimo);
    }

    // Registrar entrada de stock
    public void registrarEntrada(Long productoId, Integer cantidad, String usuario) {
        Producto producto = repo.findById(productoId).orElse(null);
        if (producto == null) return;

        int stockAnterior = producto.getStock();
        int stockNuevo = stockAnterior + cantidad;

        producto.setStock(stockNuevo);
        repo.save(producto);

        movimientoService.registrarMovimiento(
                producto,
                "ENTRADA",
                cantidad,
                stockAnterior,
                stockNuevo,
                "REABASTECIMIENTO",
                usuario
        );
    }
}