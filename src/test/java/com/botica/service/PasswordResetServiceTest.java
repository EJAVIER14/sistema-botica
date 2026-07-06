package com.botica.service;

import com.botica.exception.EmailNoEncontradoException;
import com.botica.exception.PasswordInvalidaException;
import com.botica.exception.TokenExpiradoException;
import com.botica.exception.TokenInvalidoException;
import com.botica.model.PasswordResetToken;
import com.botica.model.Usuario;
import com.botica.repository.PasswordResetTokenRepository;
import com.botica.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock
    private UsuarioRepository usuarioRepo;

    @Mock
    private PasswordResetTokenRepository tokenRepo;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @Test
    @DisplayName("Debe lanzar excepción si el email no existe al solicitar recuperación")
    void deberiaLanzarExcepcionSiElEmailNoExiste() {
        when(usuarioRepo.findByEmail("noexiste@correo.com")).thenReturn(Optional.empty());

        assertThrows(EmailNoEncontradoException.class,
                () -> passwordResetService.solicitarReset("noexiste@correo.com"));

        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("Debe generar y guardar un token válido si el email existe")
    void deberiaGenerarTokenSiElEmailExiste() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("jperez@correo.com");

        when(usuarioRepo.findByEmail("jperez@correo.com")).thenReturn(Optional.of(usuario));
        when(tokenRepo.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        passwordResetService.solicitarReset("jperez@correo.com");

        verify(tokenRepo, times(1)).save(any(PasswordResetToken.class));
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el token no existe")
    void deberiaLanzarExcepcionSiElTokenNoExiste() {
        when(tokenRepo.findByToken("token-invalido")).thenReturn(Optional.empty());

        assertThrows(TokenInvalidoException.class,
                () -> passwordResetService.restablecerPassword("token-invalido", "Nueva123"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el token ya expiró")
    void deberiaLanzarExcepcionSiElTokenExpiro() {
        PasswordResetToken token = new PasswordResetToken(
                "token-expirado", 1L, LocalDateTime.now().minusHours(1));

        when(tokenRepo.findByToken("token-expirado")).thenReturn(Optional.of(token));

        assertThrows(TokenExpiradoException.class,
                () -> passwordResetService.restablecerPassword("token-expirado", "Nueva123"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el token ya fue usado")
    void deberiaLanzarExcepcionSiElTokenYaFueUsado() {
        PasswordResetToken token = new PasswordResetToken(
                "token-usado", 1L, LocalDateTime.now().plusHours(1));
        token.setUsado(true);

        when(tokenRepo.findByToken("token-usado")).thenReturn(Optional.of(token));

        assertThrows(TokenInvalidoException.class,
                () -> passwordResetService.restablecerPassword("token-usado", "Nueva123"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si la nueva contraseña no cumple la política")
    void deberiaLanzarExcepcionSiLaPasswordEsDebil() {
        PasswordResetToken token = new PasswordResetToken(
                "token-valido", 1L, LocalDateTime.now().plusHours(1));

        when(tokenRepo.findByToken("token-valido")).thenReturn(Optional.of(token));

        assertThrows(PasswordInvalidaException.class,
                () -> passwordResetService.restablecerPassword("token-valido", "123"));

        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe restablecer la contraseña y marcar el token como usado si todo es válido")
    void deberiaRestablecerPasswordSiElTokenEsValido() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setPassword("$2a$10$viejaHash");

        PasswordResetToken token = new PasswordResetToken(
                "token-valido", 1L, LocalDateTime.now().plusHours(1));

        when(tokenRepo.findByToken("token-valido")).thenReturn(Optional.of(token));
        when(usuarioRepo.findById(1L)).thenReturn(Optional.of(usuario));

        passwordResetService.restablecerPassword("token-valido", "NuevaSegura123");

        assertTrue(token.isUsado());
        verify(usuarioRepo, times(1)).save(any(Usuario.class));
        verify(tokenRepo, times(1)).save(token);
    }
}