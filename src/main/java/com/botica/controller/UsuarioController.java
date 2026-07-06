package com.botica.controller;

import com.botica.exception.PasswordInvalidaException;
import com.botica.model.Usuario;
import com.botica.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.regex.Pattern;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService service;

    private static final Pattern EMAIL_VALIDO =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("usuarios", service.listarTodos());
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "usuarios/formulario";
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", service.buscarPorId(id));
        return "usuarios/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Usuario usuario, Model model) {

        if (usuario.getEmail() == null || !EMAIL_VALIDO.matcher(usuario.getEmail()).matches()) {
            model.addAttribute("error", "Ingresa un correo electrónico válido");
            return "usuarios/formulario";
        }

        boolean esNuevo = usuario.getId() == null;

        if (esNuevo && service.existeUsername(usuario.getUsername())) {
            model.addAttribute("error", "El nombre de usuario ya existe");
            return "usuarios/formulario";
        }

        boolean emailDuplicado = esNuevo
                ? service.existeEmail(usuario.getEmail())
                : service.existeEmailParaOtroUsuario(usuario.getEmail(), usuario.getId());

        if (emailDuplicado) {
            model.addAttribute("error", "Ese correo ya está registrado por otro usuario");
            return "usuarios/formulario";
        }

        try {
            if (esNuevo) {
                service.guardar(usuario);
            } else {
                service.actualizar(usuario);
            }
        } catch (PasswordInvalidaException e) {
            model.addAttribute("error", e.getMessage()
                    + ". La contraseña debe tener al menos 8 caracteres, incluir letras y números.");
            return "usuarios/formulario";
        }
        return "redirect:/usuarios";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return "redirect:/usuarios";
    }

    // ═══ NUEVO: activar/desactivar usuario ═══
    @GetMapping("/toggle-activo/{id}")
    public String toggleActivo(@PathVariable Long id) {
        service.toggleActivo(id);
        return "redirect:/usuarios";
    }

    // Abrir formulario cambiar contraseña
    @GetMapping("/cambiar-password/{id}")
    public String formularioCambiarPassword(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", service.buscarPorId(id));
        return "usuarios/cambiar-password";
    }

    // Guardar nueva contraseña
    @PostMapping("/cambiar-password")
    public String cambiarPassword(
            @RequestParam Long id,
            @RequestParam String passwordActual,
            @RequestParam String passwordNueva,
            @RequestParam String passwordConfirmar,
            RedirectAttributes redirectAttributes) {

        if (!passwordNueva.equals(passwordConfirmar)) {
            redirectAttributes.addFlashAttribute("error",
                    "Las contraseñas nuevas no coinciden");
            return "redirect:/usuarios/cambiar-password/" + id;
        }

        try {
            boolean ok = service.cambiarPassword(id, passwordActual, passwordNueva);

            if (!ok) {
                redirectAttributes.addFlashAttribute("error",
                        "La contraseña actual es incorrecta");
                return "redirect:/usuarios/cambiar-password/" + id;
            }
        } catch (PasswordInvalidaException e) {
            redirectAttributes.addFlashAttribute("error",
                    e.getMessage() + ". Debe tener al menos 8 caracteres, incluir letras y números.");
            return "redirect:/usuarios/cambiar-password/" + id;
        }

        redirectAttributes.addFlashAttribute("exito",
                "Contraseña cambiada correctamente");
        return "redirect:/usuarios";
    }
}