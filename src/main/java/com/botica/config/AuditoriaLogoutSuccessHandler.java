package com.botica.config;

import com.botica.model.EstadoAuditoria;
import com.botica.service.AuditoriaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuditoriaLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    @Autowired
    private AuditoriaService auditoriaService;

    public AuditoriaLogoutSuccessHandler() {
        setDefaultTargetUrl("/login");
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                Authentication authentication) throws IOException, ServletException {

        if (authentication != null) {
            String username = authentication.getName();
            auditoriaService.registrar(
                    "auth",
                    "LOGOUT",
                    EstadoAuditoria.EXITO,
                    username,
                    "Cierre de sesión: '" + username + "'"
            );
        }

        super.onLogoutSuccess(request, response, authentication);
    }
}