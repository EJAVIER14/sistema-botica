package com.botica.service;

import com.botica.dto.ProductoDTO;
import com.botica.exception.FechaVencimientoInvalidaException;
import com.botica.exception.NombreInvalidoException;
import com.botica.exception.PrecioInvalidoException;
import com.botica.exception.ProductoDuplicadoException;
import com.botica.exception.StockInsuficienteException;
import com.botica.exception.StockInvalidoException;
import com.botica.model.Presentacion;
import com.botica.model.Producto;
import com.botica.repository.ProductoRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
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

    public Page<Producto> listarPaginado(int page, int size, String buscar, String categoria) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        return repo.buscarConFiltros(buscar, categoria, pageRequest);
    }

    public List<String> listarCategorias() {
        return repo.listarCategoriasDistintas();
    }

    public Producto guardar(Producto p) {
        return repo.save(p);
    }

    // ═══ crear producto validando nombre, duplicado, precio, stock y fecha de vencimiento ═══
    public Producto crear(ProductoDTO dto) {
        if (dto.nombre() == null || dto.nombre().isBlank()) {
            throw new NombreInvalidoException();
        }

        if (repo.existsByNombre(dto.nombre())) {
            throw new ProductoDuplicadoException(dto.nombre());
        }

        if (dto.precio() == null || dto.precio() <= 0) {
            throw new PrecioInvalidoException(dto.precio());
        }

        if (dto.stock() == null || dto.stock() < 0) {
            throw new StockInvalidoException(dto.stock());
        }

        if (dto.fechaVencimiento() != null && dto.fechaVencimiento().isBefore(LocalDate.now())) {
            throw new FechaVencimientoInvalidaException(dto.fechaVencimiento());
        }

        Producto producto = new Producto();
        producto.setNombre(dto.nombre());
        producto.setDescripcion(dto.descripcion());
        producto.setPrecio(dto.precio());
        producto.setStock(dto.stock());
        producto.setFechaVencimiento(dto.fechaVencimiento());
        producto.setCategoria(dto.categoria());
        // ═══ NUEVO ═══
        producto.setLote(dto.lote());
        producto.setCosto(dto.costo());

        // Primer guardado: obtenemos el ID autogenerado
        Producto guardado = repo.save(producto);

        // Generamos el código basado en el ID y guardamos de nuevo
        guardado.setCodigo(generarCodigo(guardado.getId()));
        return repo.save(guardado);
    }

    private String generarCodigo(Long id) {
        return String.format("BOT-%04d", id);
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

    // ═══════════ PRESENTACIONES DE VENTA (unidad / blister / caja) ═══════════

    public int calcularUnidades(Producto producto, Presentacion presentacion, int cantidad) {
        int factor = switch (presentacion) {
            case UNIDAD -> 1;
            case BLISTER -> producto.getUnidadesPorBlister() != null ? producto.getUnidadesPorBlister() : 1;
            case CAJA -> producto.getUnidadesPorCaja() != null ? producto.getUnidadesPorCaja() : 1;
        };
        return cantidad * factor;
    }

    public double calcularPrecioTotal(Producto producto, Presentacion presentacion, int cantidad) {
        int unidades = calcularUnidades(producto, presentacion, cantidad);
        return unidades * producto.getPrecio();
    }

    public Producto venderPorPresentacion(Long productoId, Presentacion presentacion, int cantidad) {
        Producto producto = repo.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        int unidadesADescontar = calcularUnidades(producto, presentacion, cantidad);

        if (producto.getStock() < unidadesADescontar) {
            throw new StockInsuficienteException(
                    producto.getNombre(), producto.getStock(), unidadesADescontar);
        }

        producto.setStock(producto.getStock() - unidadesADescontar);
        return repo.save(producto);
    }

    // ═══ GENERAR PLANTILLA EXCEL ═══
    public byte[] generarPlantillaExcel() throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Productos");

        CellStyle headerStyle = wb.createCellStyle();
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row header = sheet.createRow(0);
        // ═══ ACTUALIZADO: se agregaron columnas Lote y Costo ═══
        String[] cols = {"Nombre", "Descripcion", "Categoria", "Costo", "Precio", "Stock", "Lote", "FechaVencimiento(AAAA-MM-DD)"};
        for (int i = 0; i < cols.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(cols[i]);
            cell.setCellStyle(headerStyle);
            sheet.setColumnWidth(i, 5000);
        }

        // Fila de ejemplo
        Row ejemplo = sheet.createRow(1);
        ejemplo.createCell(0).setCellValue("Paracetamol 500mg");
        ejemplo.createCell(1).setCellValue("Analgésico y antipirético");
        ejemplo.createCell(2).setCellValue("Analgésico");
        ejemplo.createCell(3).setCellValue(1.50);
        ejemplo.createCell(4).setCellValue(2.50);
        ejemplo.createCell(5).setCellValue(50);
        ejemplo.createCell(6).setCellValue("LT-2026-014");
        ejemplo.createCell(7).setCellValue("2027-12-31");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();
        return out.toByteArray();
    }

    // ═══ IMPORTAR DESDE EXCEL ═══
    public ResultadoImportacion importarExcel(MultipartFile archivo) throws IOException {
        ResultadoImportacion resultado = new ResultadoImportacion();
        Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(archivo.getBytes()));
        Sheet sheet = wb.getSheetAt(0);

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            try {
                String nombre = obtenerTexto(row.getCell(0));
                if (nombre == null || nombre.trim().isEmpty()) continue;

                Producto producto = new Producto();
                producto.setNombre(nombre);
                producto.setDescripcion(obtenerTexto(row.getCell(1)));
                producto.setCategoria(obtenerTexto(row.getCell(2)));
                // ═══ ACTUALIZADO: nuevo orden de columnas con Costo, Precio, Stock, Lote ═══
                producto.setCosto(obtenerNumero(row.getCell(3)));
                producto.setPrecio(obtenerNumero(row.getCell(4)));
                producto.setStock((int) obtenerNumero(row.getCell(5)));
                producto.setLote(obtenerTexto(row.getCell(6)));

                String fechaTexto = obtenerTexto(row.getCell(7));
                if (fechaTexto != null && !fechaTexto.trim().isEmpty()) {
                    producto.setFechaVencimiento(LocalDate.parse(fechaTexto.trim()));
                }

                Producto guardado = repo.save(producto);
                guardado.setCodigo(generarCodigo(guardado.getId()));
                repo.save(guardado);

                resultado.exitosos++;

            } catch (Exception e) {
                resultado.errores.add("Fila " + (i + 1) + ": " + e.getMessage());
            }
        }

        wb.close();
        return resultado;
    }

    private String obtenerTexto(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        return null;
    }

    private double obtenerNumero(Cell cell) {
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        if (cell.getCellType() == CellType.STRING) {
            try { return Double.parseDouble(cell.getStringCellValue()); } catch (Exception e) { return 0; }
        }
        return 0;
    }

    public static class ResultadoImportacion {
        public int exitosos = 0;
        public List<String> errores = new ArrayList<>();
    }
}