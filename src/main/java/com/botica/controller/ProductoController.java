package com.botica.controller;

import com.botica.model.Producto;
import com.botica.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService service;

    @GetMapping
    public String listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String buscar,
            Model model) {

        Page<Producto> paginaProductos = service.listarPaginado(page, 10, buscar);

        model.addAttribute("paginaProductos", paginaProductos);
        model.addAttribute("productos", paginaProductos.getContent());
        model.addAttribute("paginaActual", page);
        model.addAttribute("totalPaginas", paginaProductos.getTotalPages());
        model.addAttribute("totalElementos", paginaProductos.getTotalElements());
        model.addAttribute("buscar", buscar);

        return "productos/lista";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        return "productos/formulario";
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id, Model model) {
        Producto producto = service.buscarPorId(id);
        model.addAttribute("producto", producto);
        return "productos/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Producto producto) {
        service.guardar(producto);
        return "redirect:/productos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return "redirect:/productos";
    }

    @GetMapping("/entrada/{id}")
    public String formularioEntrada(@PathVariable Long id, Model model) {
        model.addAttribute("producto", service.buscarPorId(id));
        return "productos/entrada";
    }

    @PostMapping("/entrada/guardar")
    public String guardarEntrada(
            @RequestParam Long productoId,
            @RequestParam Integer cantidad,
            Authentication authentication) {

        String usuario = authentication.getName();
        service.registrarEntrada(productoId, cantidad, usuario);
        return "redirect:/inventario";
    }
}