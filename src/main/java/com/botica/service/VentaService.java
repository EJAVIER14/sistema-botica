package com.botica.service;

import com.botica.model.DetalleVenta;
import com.botica.model.Presentacion;
import com.botica.model.Producto;
import com.botica.model.Venta;
import com.botica.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private ProductoService productoService;

    @Autowired
    private MovimientoInventarioService movimientoService;

    private List<String> stockBajoProductos = new ArrayList<>();

    public List<String> getStockBajoProductos() {
        return stockBajoProductos;
    }

    public List<Venta> listarTodas() {
        return ventaRepository.findAll();
    }

    public Page<Venta> listarPaginado(int page, int size, String buscar) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("fecha").descending());
        if (buscar != null && !buscar.trim().isEmpty()) {
            return ventaRepository.findByClienteContainingIgnoreCase(buscar, pageRequest);
        }
        return ventaRepository.findAll(pageRequest);
    }

    public Venta buscarPorId(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    // ═══ ACTUALIZADO: ahora recibe la presentación (UNIDAD / BLISTER / CAJA) por cada línea ═══
    public Venta registrarVenta(
            Venta venta,
            List<Long> productoIds,
            List<Integer> cantidades,
            List<Presentacion> presentaciones) {

        venta.setFecha(LocalDateTime.now());
        venta.setDetalles(new ArrayList<>());
        stockBajoProductos.clear();

        double subtotal = 0.0;

        for (int i = 0; i < productoIds.size(); i++) {
            Long productoId = productoIds.get(i);
            int cantidad = cantidades.get(i);
            Presentacion presentacion = presentaciones.get(i);

            Producto productoAntes = productoService.buscarPorId(productoId);
            if (productoAntes == null) continue;
            int stockAnterior = productoAntes.getStock();

            // Descuenta el stock en unidades reales, valida stock suficiente,
            // y lanza StockInsuficienteException si no alcanza
            Producto productoActualizado = productoService.venderPorPresentacion(
                    productoId, presentacion, cantidad);

            int unidadesDescontadas = stockAnterior - productoActualizado.getStock();

            movimientoService.registrarMovimiento(
                    productoActualizado, "SALIDA", unidadesDescontadas,
                    stockAnterior, productoActualizado.getStock(),
                    "VENTA", venta.getCliente()
            );

            if (productoActualizado.getStock() <= 10) {
                stockBajoProductos.add(
                        productoActualizado.getNombre() + " (stock: " + productoActualizado.getStock() + ")"
                );
            }

            double precioLinea = productoService.calcularPrecioTotal(productoActualizado, presentacion, cantidad);

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(productoActualizado);
            detalle.setCantidad(cantidad);
            detalle.setPresentacion(presentacion);
            detalle.setPrecioUnitario(precioLinea / cantidad);
            detalle.setSubtotal(precioLinea);
            detalle.setVenta(venta);

            venta.getDetalles().add(detalle);
            subtotal += precioLinea;
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