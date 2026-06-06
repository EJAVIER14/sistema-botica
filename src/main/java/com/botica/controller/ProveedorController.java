package com.botica.controller;

import com.botica.model.Proveedor;
import com.botica.service.ProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService service;

    // Ver lista de proveedores
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("proveedores",
                service.listarTodos());
        return "proveedores/lista";
    }

    // Abrir formulario nuevo proveedor
    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "proveedores/formulario";
    }

    // Abrir formulario editar proveedor
    @GetMapping("/editar/{id}")
    public String formularioEditar(
            @PathVariable Long id, Model model) {
        model.addAttribute("proveedor",
                service.buscarPorId(id));
        return "proveedores/formulario";
    }

    // Guardar proveedor nuevo o editado
    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Proveedor proveedor) {
        service.guardar(proveedor);
        return "redirect:/proveedores";
    }

    // Eliminar proveedor
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return "redirect:/proveedores";
    }
}
