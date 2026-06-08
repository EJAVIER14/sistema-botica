package com.botica.service;

import com.botica.model.DetalleVenta;
import com.botica.model.Producto;
import com.botica.model.Venta;
import com.botica.repository.ProductoRepository;
import com.botica.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private MovimientoInventarioService movimientoService;

    private List<String> stockBajoProductos = new ArrayList<>();

    public List<String> getStockBajoProductos() {
        return stockBajoProductos;
    }

    public List<Venta> listarTodas() {
        return ventaRepository.findAll();
    }

    public Venta buscarPorId(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    public Venta registrarVenta(
            Venta venta,
            List<Long> productoIds,
            List<Integer> cantidades) {

        venta.setFecha(LocalDateTime.now());
        venta.setDetalles(new ArrayList<>());
        stockBajoProductos.clear();

        double subtotal = 0.0;

        for (int i = 0; i < productoIds.size(); i++) {
            Producto producto = productoRepository
                    .findById(productoIds.get(i)).orElse(null);

            if (producto == null) continue;

            int cantidad = cantidades.get(i);
            int stockAnterior = producto.getStock();
            int nuevoStock = stockAnterior - cantidad;

            producto.setStock(nuevoStock);
            productoRepository.save(producto);

            // Registrar movimiento de SALIDA por VENTA
            movimientoService.registrarMovimiento(
                    producto,
                    "SALIDA",
                    cantidad,
                    stockAnterior,
                    nuevoStock,
                    "VENTA",
                    venta.getCliente()
            );

            if (nuevoStock <= 10) {
                stockBajoProductos.add(
                        producto.getNombre() + " (stock: " + nuevoStock + ")"
                );
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(producto.getPrecio() * cantidad);
            detalle.setVenta(venta);

            venta.getDetalles().add(detalle);
            subtotal += detalle.getSubtotal();
        }

        double descuento = venta.getDescuento() != null ? venta.getDescuento() : 0.0;
        double subtotalConDescuento = subtotal - descuento;
        double subtotalSinIgv = subtotalConDescuento / 1.18;
        double igv = Math.round((subtotalConDescuento - subtotalSinIgv) * 100.0) / 100.0;
        double total = Math.round(subtotalConDescuento * 100.0) / 100.0;
        double montoRecibido = venta.getMontoRecibido() != null ? venta.getMontoRecibido() : 0.0;
        double vuelto = montoRecibido - total;

        venta.setSubtotal(Math.round(subtotalSinIgv * 100.0) / 100.0);
        venta.setIgv(igv);
        venta.setDescuento(descuento);
        venta.setTotal(total);
        venta.setMontoRecibido(montoRecibido);
        venta.setVuelto(Math.round(vuelto * 100.0) / 100.0);

        return ventaRepository.save(venta);
    }
}