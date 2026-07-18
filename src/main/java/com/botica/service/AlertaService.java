package com.botica.service;

import com.botica.model.Producto;
import com.botica.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class AlertaService {

    @Autowired
    private ProductoRepository repo;

    // Días de anticipación para alertar vencimiento
    private static final int DIAS_ANTICIPACION = 30;

    // ═══ ACTUALIZADO: ahora compara el stock de cada producto contra SU PROPIO stockMinimo ═══
    public List<Producto> productosStockBajo() {
        return repo.findProductosConStockBajoSuMinimo();
    }

    public List<Producto> productosPorVencer() {
        LocalDate fechaLimite = LocalDate.now()
                .plusDays(DIAS_ANTICIPACION);
        return repo.findProductosPorVencer(fechaLimite);
    }

    public List<Producto> productosVencidos() {
        return repo.findProductosVencidos(LocalDate.now());
    }

    public int totalAlertas() {
        return productosStockBajo().size() +
                productosPorVencer().size() +
                productosVencidos().size();
    }
}