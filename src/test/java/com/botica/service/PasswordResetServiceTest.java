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
    @DisplayName("Debe lanzar excepción si el email no existe al solicitar código")
    void deberiaLanzarExcepcionSiElEmailNoExiste() {
        when(usuarioRepo.findByEmail("noexiste@correo.com")).thenReturn(Optional.empty());

        assertThrows(EmailNoEncontradoException.class,
                () -> passwordResetService.solicitarCodigo("noexiste@correo.com"));

        verify(restTemplate, never()).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("Debe generar y guardar un código válido si el email existe")
    void deberiaGenerarCodigoSiElEmailExiste() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("jperez@correo.com");

        when(usuarioRepo.findByEmail("jperez@correo.com")).thenReturn(Optional.of(usuario));
        when(tokenRepo.save(any(PasswordResetToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        passwordResetService.solicitarCodigo("jperez@correo.com");

        verify(tokenRepo, times(1)).save(any(PasswordResetToken.class));
        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el correo no existe al restablecer")
    void deberiaLanzarExcepcionSiElCorreoNoExisteAlRestablecer() {
        when(usuarioRepo.findByEmail("noexiste@correo.com")).thenReturn(Optional.empty());

        assertThrows(TokenInvalidoException.class,
                () -> passwordResetService.restablecerConCodigo("noexiste@correo.com", "123456", "Nueva123"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el código no existe")
    void deberiaLanzarExcepcionSiElCodigoNoExiste() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("jperez@correo.com");

        when(usuarioRepo.findByEmail("jperez@correo.com")).thenReturn(Optional.of(usuario));
        when(tokenRepo.findByToken("000000")).thenReturn(Optional.empty());

        assertThrows(TokenInvalidoException.class,
                () -> passwordResetService.restablecerConCodigo("jperez@correo.com", "000000", "Nueva123"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el código ya expiró")
    void deberiaLanzarExcepcionSiElCodigoExpiro() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("jperez@correo.com");

        PasswordResetToken token = new PasswordResetToken(
                "123456", 1L, LocalDateTime.now().minusMinutes(1));

        when(usuarioRepo.findByEmail("jperez@correo.com")).thenReturn(Optional.of(usuario));
        when(tokenRepo.findByToken("123456")).thenReturn(Optional.of(token));

        assertThrows(TokenExpiradoException.class,
                () -> passwordResetService.restablecerConCodigo("jperez@correo.com", "123456", "Nueva123"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el código ya fue usado")
    void deberiaLanzarExcepcionSiElCodigoYaFueUsado() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("jperez@correo.com");

        PasswordResetToken token = new PasswordResetToken(
                "123456", 1L, LocalDateTime.now().plusMinutes(5));
        token.setUsado(true);

        when(usuarioRepo.findByEmail("jperez@correo.com")).thenReturn(Optional.of(usuario));
        when(tokenRepo.findByToken("123456")).thenReturn(Optional.of(token));

        assertThrows(TokenInvalidoException.class,
                () -> passwordResetService.restablecerConCodigo("jperez@correo.com", "123456", "Nueva123"));
    }

    @Test
    @DisplayName("Debe lanzar excepción si la nueva contraseña no cumple la política")
    void deberiaLanzarExcepcionSiLaPasswordEsDebil() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("jperez@correo.com");

        PasswordResetToken token = new PasswordResetToken(
                "123456", 1L, LocalDateTime.now().plusMinutes(5));

        when(usuarioRepo.findByEmail("jperez@correo.com")).thenReturn(Optional.of(usuario));
        when(tokenRepo.findByToken("123456")).thenReturn(Optional.of(token));

        assertThrows(PasswordInvalidaException.class,
                () -> passwordResetService.restablecerConCodigo("jperez@correo.com", "123456", "123"));

        verify(usuarioRepo, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe restablecer la contraseña y marcar el código como usado si todo es válido")
    void deberiaRestablecerPasswordSiElCodigoEsValido() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("jperez@correo.com");
        usuario.setPassword("$2a$10$viejaHash");

        PasswordResetToken token = new PasswordResetToken(
                "123456", 1L, LocalDateTime.now().plusMinutes(5));

        when(usuarioRepo.findByEmail("jperez@correo.com")).thenReturn(Optional.of(usuario));
        when(tokenRepo.findByToken("123456")).thenReturn(Optional.of(token));

        passwordResetService.restablecerConCodigo("jperez@correo.com", "123456", "NuevaSegura123");

        assertTrue(token.isUsado());
        verify(usuarioRepo, times(1)).save(any(Usuario.class));
        verify(tokenRepo, times(1)).save(token);
    }
}