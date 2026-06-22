package com.botica.config;

import com.botica.service.AlertaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private AlertaService alertaService;

    @ModelAttribute("totalAlertasNavbar")
    public int totalAlertasNavbar() {
        // Solo calcula si hay un usuario autenticado
        if (SecurityContextHolder.getContext().getAuthentication() == null
                || !SecurityContextHolder.getContext().getAuthentication().isAuthenticated()
                || SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals("anonymousUser")) {
            return 0;
        }
        return alertaService.totalAlertas();
    }
}