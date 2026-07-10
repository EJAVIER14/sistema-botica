package com.botica.controller;

import com.botica.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @GetMapping("/olvide-password")
    public String mostrarFormularioSolicitud() {
        return "olvide-password";
    }

    @PostMapping("/olvide-password")
    public String procesarSolicitud(@RequestParam String email, Model model) {
        try {
            passwordResetService.solicitarCodigo(email);
        } catch (Exception e) {
            // Mensaje genérico a propósito: no revelamos si el email existe o no (seguridad)
        }
        model.addAttribute("email", email);
        model.addAttribute("mensaje", "Si el correo está registrado, te enviamos un código de 6 dígitos.");
        return "restablecer-password";
    }

    @PostMapping("/restablecer-password")
    public String procesarReset(@RequestParam String email,
                                @RequestParam String codigo,
                                @RequestParam String nuevaPassword,
                                Model model) {
        try {
            passwordResetService.restablecerConCodigo(email, codigo, nuevaPassword);
            model.addAttribute("exito", true);
            model.addAttribute("mensaje", "Tu contraseña fue actualizada. Ya puedes iniciar sesión.");
        } catch (Exception e) {
            model.addAttribute("email", email);
            model.addAttribute("error", e.getMessage());
        }
        return "restablecer-password";
    }
}