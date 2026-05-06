package com.botica.controller;

import com.botica.model.Usuario;
import com.botica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    // Ver lista de usuarios
    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios",
                service.listarTodos());
        return "usuarios/lista";
    }

    // Abrir formulario nuevo usuario
    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuarios/formulario";
    }

    // Abrir formulario editar usuario
    @GetMapping("/editar/{id}")
    public String formularioEditar(
            @PathVariable Long id, Model model) {
        model.addAttribute("usuario",
                service.buscarPorId(id));
        return "usuarios/formulario";
    }

    // Guardar usuario nuevo
    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Usuario usuario,
            Model model) {

        // Verificar si el username ya existe
        if (service.existeUsername(usuario.getUsername())
                && usuario.getId() == null) {
            model.addAttribute("error",
                    "El nombre de usuario ya existe");
            return "usuarios/formulario";
        }

        if (usuario.getId() == null) {
            service.guardar(usuario);
        } else {
            service.actualizar(usuario);
        }
        return "redirect:/usuarios";
    }

    // Eliminar usuario
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return "redirect:/usuarios";
    }
}