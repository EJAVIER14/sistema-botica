package com.botica.controller;

import com.botica.model.Usuario;
import com.botica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/crear-admin")
    @ResponseBody
    public String crearAdmin() {
        Usuario u = new Usuario();
        u.setNombre("Administrador");
        u.setUsername("admin2");
        u.setPassword("admin123");
        u.setRol("ADMIN");
        u.setActivo(true);
        usuarioService.guardar(u);
        return "Admin creado exitosamente!";
    }
}