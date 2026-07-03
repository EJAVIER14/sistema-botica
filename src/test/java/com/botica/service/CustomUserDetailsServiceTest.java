package com.botica.service;

import com.botica.model.Usuario;
import com.botica.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("Un usuario inactivo debe cargarse como deshabilitado (disabled=true)")
    void deberiaCargarUsuarioComoDeshabilitadoSiEstaInactivo() {
        Usuario usuario = new Usuario();
        usuario.setUsername("jperez");
        usuario.setPassword("$2a$10$hashedPasswordExample");
        usuario.setRol("CAJERO");
        usuario.setActivo(false);

        when(usuarioRepository.findByUsername("jperez")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("jperez");

        assertFalse(userDetails.isEnabled(), "El usuario inactivo no debe estar habilitado para autenticarse");
    }

    @Test
    @DisplayName("Un usuario activo debe cargarse como habilitado (enabled=true)")
    void deberiaCargarUsuarioComoHabilitadoSiEstaActivo() {
        Usuario usuario = new Usuario();
        usuario.setUsername("mgarcia");
        usuario.setPassword("$2a$10$hashedPasswordExample");
        usuario.setRol("ADMIN");
        usuario.setActivo(true);

        when(usuarioRepository.findByUsername("mgarcia")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("mgarcia");

        assertTrue(userDetails.isEnabled(), "El usuario activo debe poder autenticarse");
        assertEquals("mgarcia", userDetails.getUsername());
    }
}