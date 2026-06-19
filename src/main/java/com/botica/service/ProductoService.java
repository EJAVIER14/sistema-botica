package com.botica.service;

import com.botica.model.Producto;
import com.botica.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

    // Listar con paginación y búsqueda opcional
    public Page<Producto> listarPaginado(int page, int size, String buscar) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());

        if (buscar != null && !buscar.trim().isEmpty()) {
            return repo.findByNombreContainingIgnoreCase(buscar, pageRequest);
        }
        return repo.findAll(pageRequest);
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

    public void registrarEntrada(Long productoId, Integer cantidad, String usuario) {
        Producto producto = repo.findById(productoId).orElse(null);
        if (producto == null) return;

        int stockAnterior = producto.getStock();
        int stockNuevo = stockAnterior + cantidad;

        producto.setStock(stockNuevo);
        repo.save(producto);

        movimientoService.registrarMovimiento(
                producto, "ENTRADA", cantidad, stockAnterior, stockNuevo,
                "REABASTECIMIENTO", usuario
        );
    }
}