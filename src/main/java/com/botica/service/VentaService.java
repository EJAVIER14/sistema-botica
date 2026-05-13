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

    public List<Venta> listarTodas() {
        return ventaRepository.findAll();
    }

    public Venta buscarPorId(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    public Venta registrarVenta(Venta venta,
                                List<Long> productoIds,
                                List<Integer> cantidades) {

        venta.setFecha(LocalDateTime.now());
        venta.setDetalles(new ArrayList<>());
        double total = 0.0;

        for (int i = 0; i < productoIds.size(); i++) {
            Producto producto = productoRepository
                    .findById(productoIds.get(i)).orElse(null);

            if (producto == null) continue;

            int cantidad = cantidades.get(i);

            // Descontar stock automáticamente
            producto.setStock(producto.getStock() - cantidad);
            productoRepository.save(producto);

            // Crear detalle
            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setSubtotal(producto.getPrecio() * cantidad);
            detalle.setVenta(venta);

            venta.getDetalles().add(detalle);
            total += detalle.getSubtotal();
        }

        venta.setTotal(total);
        return ventaRepository.save(venta);
    }
}