package com.botica.config;

import com.botica.model.EstadoAuditoria;
import com.botica.repository.UsuarioRepository;
import com.botica.service.AuditoriaService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuditoriaAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private AuditoriaService auditoriaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public AuditoriaAuthenticationFailureHandler() {
        setDefaultFailureUrl("/login?error=true");
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        String username = request.getParameter("username");
        if (username == null || username.isBlank()) {
            username = "desconocido";
        }

        boolean existeElUsuario = usuarioRepository.existsByUsername(username);

        String descripcion = existeElUsuario
                ? "Contraseña incorrecta para '" + username + "'"
                : "Intento de login con usuario inexistente: '" + username + "'";

        auditoriaService.registrar(
                "auth",
                "LOGIN_FALLIDO",
                EstadoAuditoria.ERROR,
                username,
                descripcion
        );

        super.onAuthenticationFailure(request, response, exception);
    }
}