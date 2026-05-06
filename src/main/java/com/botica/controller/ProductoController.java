package com.botica.controller;

import com.botica.model.Producto;
import com.botica.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService service;

    // Ver lista de todos los productos
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("productos", service.listarTodos());
        return "productos/lista";
    }

    // Abrir formulario para crear producto nuevo
    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("producto", new Producto());
        return "productos/formulario";
    }

    // Abrir formulario para editar un producto existente
    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id, Model model) {
        Producto producto = service.buscarPorId(id);
        model.addAttribute("producto", producto);
        return "productos/formulario";
    }

    // Guardar producto nuevo o editado
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Producto producto) {
        service.guardar(producto);
        return "redirect:/productos";
    }

    // Eliminar un producto
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return "redirect:/productos";
    }
}