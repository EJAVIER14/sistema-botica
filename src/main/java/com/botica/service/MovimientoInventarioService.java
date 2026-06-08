package com.botica.service;

import com.botica.model.MovimientoInventario;
import com.botica.model.Producto;
import com.botica.repository.MovimientoInventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimientoInventarioService {

    @Autowired
    private MovimientoInventarioRepository repo;

    public void registrarMovimiento(
            Producto producto,
            String tipo,
            Integer cantidad,
            Integer stockAnterior,
            Integer stockNuevo,
            String motivo,
            String usuario) {

        MovimientoInventario mov = new MovimientoInventario();
        mov.setProducto(producto);
        mov.setTipo(tipo);
        mov.setCantidad(cantidad);
        mov.setStockAnterior(stockAnterior);
        mov.setStockNuevo(stockNuevo);
        mov.setMotivo(motivo);
        mov.setUsuario(usuario);
        mov.setFecha(LocalDateTime.now());
        repo.save(mov);
    }

    public List<MovimientoInventario> listarTodos() {
        return repo.findAllByOrderByFechaDesc();
    }

    public List<MovimientoInventario> listarPorProducto(Long productoId) {
        return repo.findByProductoIdOrderByFechaDesc(productoId);
    }
}