package com.botica.config;

import com.botica.service.AvisoMantenimientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Se ejecuta antes de CUALQUIER controlador MVC del sistema, agregando el aviso
 * de mantenimiento vigente (si existe) al Model de cada página automáticamente,
 * sin tener que modificar cada controlador uno por uno.
 */
@ControllerAdvice
public class GlobalAvisoMantenimientoAdvice {

    @Autowired
    private AvisoMantenimientoService service;

    @ModelAttribute
    public void agregarAvisoMantenimiento(Model model) {
        service.obtenerAvisoVigente().ifPresent(a -> model.addAttribute("avisoMantenimiento", a));
    }
}