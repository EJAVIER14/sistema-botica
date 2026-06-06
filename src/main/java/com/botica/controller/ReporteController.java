package com.botica.controller;

import com.botica.model.Venta;
import com.botica.service.ReporteService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @GetMapping
    public String verReportes(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            Model model) {

        if (inicio == null) inicio = LocalDate.now().withDayOfMonth(1);
        if (fin == null) fin = LocalDate.now();

        model.addAttribute("inicio", inicio);
        model.addAttribute("fin", fin);
        model.addAttribute("ventas", reporteService.ventasEntreFechas(inicio, fin));
        model.addAttribute("totalIngresos", reporteService.totalIngresos(inicio, fin));
        model.addAttribute("cantidadVentas", reporteService.cantidadVentas(inicio, fin));
        model.addAttribute("productosMasVendidos", reporteService.productosMasVendidos());
        model.addAttribute("totalHoy", reporteService.totalHoy());
        model.addAttribute("ventasHoy", reporteService.ventasHoy());
        return "reportes/lista";
    }

    // ─── DESCARGA PDF ────────────────────────────────────────────
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> descargarPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin)
            throws Exception {

        List<Venta> ventas = reporteService.ventasEntreFechas(inicio, fin);
        Double total = reporteService.totalIngresos(inicio, fin);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        Document doc = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, out);
        doc.open();

        // Título
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 18,
                com.itextpdf.text.Font.BOLD,
                new BaseColor(27, 94, 32));
        Paragraph titulo = new Paragraph("Reporte de Ventas - Sistema Botica", titleFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        doc.add(titulo);
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Período: " + inicio.format(fmt) + " al " + fin.format(fmt)));
        doc.add(new Paragraph("Total ingresos: S/ " + total));
        doc.add(new Paragraph("Total ventas: " + ventas.size()));
        doc.add(new Paragraph(" "));

        // Tabla
        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{1, 3, 3, 2});

        com.itextpdf.text.Font headerFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 11,
                com.itextpdf.text.Font.BOLD, BaseColor.WHITE);
        BaseColor headerColor = new BaseColor(27, 94, 32);

        for (String h : new String[]{"ID", "Cliente", "Fecha", "Total"}) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(headerColor);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(cell);
        }

        DateTimeFormatter fmtFecha =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        for (Venta v : ventas) {
            tabla.addCell(String.valueOf(v.getId()));
            tabla.addCell(v.getCliente() != null ? v.getCliente() : "-");
            tabla.addCell(v.getFecha().format(fmtFecha));
            tabla.addCell("S/ " + v.getTotal());
        }

        doc.add(tabla);
        doc.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=reporte_ventas.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(out.toByteArray());
    }

    // ─── DESCARGA EXCEL ──────────────────────────────────────────
    @GetMapping("/excel")
    public ResponseEntity<byte[]> descargarExcel(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin)
            throws IOException {

        List<Venta> ventas = reporteService.ventasEntreFechas(inicio, fin);
        Double total = reporteService.totalIngresos(inicio, fin);

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Ventas");

        // Estilo encabezado
        CellStyle headerStyle = wb.createCellStyle();
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // Fila de encabezados
        Row header = sheet.createRow(0);
        String[] cols = {"ID", "Cliente", "Fecha", "Total (S/)"};
        for (int i = 0; i < cols.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(cols[i]);
            cell.setCellStyle(headerStyle);
        }

        // Filas de datos
        DateTimeFormatter fmtFecha =
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        int rowNum = 1;
        for (Venta v : ventas) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(v.getId());
            row.createCell(1).setCellValue(
                    v.getCliente() != null ? v.getCliente() : "-");
            row.createCell(2).setCellValue(v.getFecha().format(fmtFecha));
            row.createCell(3).setCellValue(v.getTotal());
        }

        // Fila de total
        Row totalRow = sheet.createRow(rowNum + 1);
        CellStyle boldStyle = wb.createCellStyle();
        Font boldFont = wb.createFont();
        boldFont.setBold(true);
        boldStyle.setFont(boldFont);
        Cell labelCell = totalRow.createCell(2);
        labelCell.setCellValue("TOTAL:");
        labelCell.setCellStyle(boldStyle);
        Cell totalCell = totalRow.createCell(3);
        totalCell.setCellValue(total);
        totalCell.setCellStyle(boldStyle);

        // Autoajustar columnas
        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 8000);
        sheet.setColumnWidth(2, 7000);
        sheet.setColumnWidth(3, 5000);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        wb.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=reporte_ventas.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(out.toByteArray());
    }
}
