package com.botica.service;

import com.botica.model.*;
import com.botica.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class OrdenEntradaService {

    @Autowired
    private OrdenEntradaRepository ordenRepo;

    @Autowired
    private ProductoRepository productoRepo;

    @Autowired
    private ProveedorRepository proveedorRepo;

    @Autowired
    private MovimientoInventarioService movimientoService;

    public List<OrdenEntrada> listarTodas() {
        return ordenRepo.findAllByOrderByFechaDesc();
    }

    public OrdenEntrada buscarPorId(Long id) {
        return ordenRepo.findById(id).orElse(null);
    }

    // Crear orden en estado PENDIENTE
    public OrdenEntrada crearOrden(
            Long proveedorId,
            String observacion,
            List<Long> productoIds,
            List<Integer> cantidades) {

        OrdenEntrada orden = new OrdenEntrada();
        orden.setFecha(LocalDateTime.now());
        orden.setEstado("PENDIENTE");
        orden.setObservacion(observacion);
        orden.setProveedor(proveedorRepo.findById(proveedorId).orElse(null));
        orden.setDetalles(new ArrayList<>());

        for (int i = 0; i < productoIds.size(); i++) {
            Producto producto = productoRepo.findById(productoIds.get(i)).orElse(null);
            if (producto == null) continue;

            DetalleOrdenEntrada detalle = new DetalleOrdenEntrada();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidades.get(i));
            detalle.setOrden(orden);
            orden.getDetalles().add(detalle);
        }

        return ordenRepo.save(orden);
    }

    // Recibir orden — actualiza stock y registra movimientos
    public void recibirOrden(Long ordenId, String usuario) {
        OrdenEntrada orden = ordenRepo.findById(ordenId).orElse(null);
        if (orden == null || orden.getEstado().equals("RECIBIDO")) return;

        for (DetalleOrdenEntrada detalle : orden.getDetalles()) {
            Producto producto = detalle.getProducto();
            int stockAnterior = producto.getStock();
            int stockNuevo = stockAnterior + detalle.getCantidad();

            producto.setStock(stockNuevo);
            productoRepo.save(producto);

            movimientoService.registrarMovimiento(
                    producto,
                    "ENTRADA",
                    detalle.getCantidad(),
                    stockAnterior,
                    stockNuevo,
                    "ORDEN DE ENTRADA #" + ordenId,
                    usuario
            );
        }

        orden.setEstado("RECIBIDO");
        ordenRepo.save(orden);
    }
}