package com.botica.config;

import com.botica.model.EstadoAuditoria;
import com.botica.service.AuditoriaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuditoriaAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private AuditoriaService auditoriaService;

    public AuditoriaAuthenticationSuccessHandler() {
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String username = authentication.getName();
        String rol = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", "").toLowerCase())
                .orElse("desconocido");

        auditoriaService.registrar(
                "auth",
                "LOGIN_EXITOSO",
                EstadoAuditoria.EXITO,
                username,
                "Inicio de sesión exitoso: '" + username + "' (" + rol + ")"
        );

        super.onAuthenticationSuccess(request, response, authentication);
    }
}