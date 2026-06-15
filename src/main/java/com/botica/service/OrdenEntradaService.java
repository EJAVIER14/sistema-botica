package com.botica.service;

import com.botica.model.*;
import com.botica.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
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

    // Carpeta donde se guardan los archivos
    private final String UPLOAD_DIR = "uploads/ordenes/";

    public List<OrdenEntrada> listarTodas() {
        return ordenRepo.findAllByOrderByFechaDesc();
    }

    public OrdenEntrada buscarPorId(Long id) {
        return ordenRepo.findById(id).orElse(null);
    }

    public OrdenEntrada crearOrden(
            Long proveedorId,
            String observacion,
            List<Long> productoIds,
            List<Integer> cantidades,
            MultipartFile foto,
            MultipartFile documento) throws IOException {

        OrdenEntrada orden = new OrdenEntrada();
        orden.setFecha(LocalDateTime.now());
        orden.setEstado("PENDIENTE");
        orden.setObservacion(observacion);
        orden.setProveedor(proveedorRepo.findById(proveedorId).orElse(null));
        orden.setDetalles(new ArrayList<>());

        // Guardar foto
        if (foto != null && !foto.isEmpty()) {
            String nombreFoto = "foto_" + System.currentTimeMillis() + "_" + foto.getOriginalFilename();
            guardarArchivo(foto, nombreFoto);
            orden.setFotoNombre(nombreFoto);
        }

        // Guardar documento
        if (documento != null && !documento.isEmpty()) {
            String nombreDoc = "doc_" + System.currentTimeMillis() + "_" + documento.getOriginalFilename();
            guardarArchivo(documento, nombreDoc);
            orden.setDocumentoNombre(nombreDoc);
        }

        // Agregar productos
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

    private void guardarArchivo(MultipartFile archivo, String nombre) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(nombre);
        Files.copy(archivo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
    }

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
                    producto, "ENTRADA", detalle.getCantidad(),
                    stockAnterior, stockNuevo,
                    "ORDEN DE ENTRADA #" + ordenId, usuario
            );
        }

        orden.setEstado("RECIBIDO");
        ordenRepo.save(orden);
    }
}