package com.botica.controller;

import com.botica.model.Usuario;
import com.botica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    @Autowired
    private UsuarioService service;

    @GetMapping
    public String verPerfil(Authentication authentication, Model model) {
        String username = authentication.getName();
        Usuario usuario = service.buscarPorUsername(username);
        model.addAttribute("usuario", usuario);
        return "perfil/ver";
    }

    @PostMapping("/cambiar-password")
    public String cambiarPassword(
            Authentication authentication,
            @RequestParam String passwordActual,
            @RequestParam String passwordNueva,
            @RequestParam String passwordConfirmar,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = service.buscarPorUsername(authentication.getName());

        if (!passwordNueva.equals(passwordConfirmar)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas nuevas no coinciden");
            return "redirect:/perfil";
        }

        if (passwordNueva.length() < 6) {
            redirectAttributes.addFlashAttribute("error", "La contraseña debe tener mínimo 6 caracteres");
            return "redirect:/perfil";
        }

        boolean ok = service.cambiarPassword(usuario.getId(), passwordActual, passwordNueva);

        if (!ok) {
            redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta");
            return "redirect:/perfil";
        }

        redirectAttributes.addFlashAttribute("exito", "Contraseña actualizada correctamente");
        return "redirect:/perfil";
    }
}