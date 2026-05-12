package com.botica.controller;

import com.botica.model.Usuario;
import com.botica.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/setup")
    @ResponseBody
    public String setup() {
        usuarioRepository.deleteAll();

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();
        String password = encoder.encode("admin123");

        Usuario u = new Usuario();
        u.setNombre("Administrador");
        u.setUsername("admin");
        u.setPassword(password);
        u.setRol("ADMIN");
        u.setActivo(true);

        usuarioRepository.save(u);

        return "Usuario creado! Password length: " +
                password.length() + " - Pass: " + password;
    }
}