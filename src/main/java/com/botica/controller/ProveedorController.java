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

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("proveedores",
                service.listarTodos());
        return "proveedores/lista";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "proveedores/formulario";
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(
            @PathVariable Long id, Model model) {
        model.addAttribute("proveedor",
                service.buscarPorId(id));
        return "proveedores/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Proveedor proveedor) {
        service.guardar(proveedor);
        return "redirect:/proveedores";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return "redirect:/proveedores";
    }
}
