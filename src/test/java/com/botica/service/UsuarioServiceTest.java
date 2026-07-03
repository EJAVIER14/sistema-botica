package com.botica.service;

import com.botica.exception.PasswordInvalidaException;
import com.botica.model.Usuario;
import com.botica.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository repo;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Debe lanzar excepción al crear un usuario con contraseña muy corta")
    void deberiaLanzarExcepcionSiLaPasswordEsMuyCorta() {
        Usuario usuario = new Usuario();
        usuario.setUsername("jperez");
        usuario.setNombre("Juan Perez");
        usuario.setRol("CAJERO");
        usuario.setPassword("abc123");

        assertThrows(PasswordInvalidaException.class, () -> usuarioService.guardar(usuario));

        verify(repo, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción al crear un usuario con contraseña sin números")
    void deberiaLanzarExcepcionSiLaPasswordNoTieneNumeros() {
        Usuario usuario = new Usuario();
        usuario.setUsername("mgarcia");
        usuario.setNombre("Maria Garcia");
        usuario.setRol("ADMIN");
        usuario.setPassword("solamentelettras");

        assertThrows(PasswordInvalidaException.class, () -> usuarioService.guardar(usuario));

        verify(repo, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Debe guardar el usuario correctamente si la contraseña cumple la política")
    void deberiaGuardarUsuarioSiLaPasswordEsValida() {
        Usuario usuario = new Usuario();
        usuario.setUsername("acastro");
        usuario.setNombre("Ana Castro");
        usuario.setRol("ALMACENERO");
        usuario.setPassword("Segura123");

        when(repo.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.guardar(usuario);

        assertTrue(resultado.getActivo());
        verify(repo, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("cambiarPassword debe lanzar excepción si la nueva contraseña es débil")
    void deberiaLanzarExcepcionAlCambiarAUnaPasswordDebil() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setPassword("$2a$10$hashedExistente");

        when(repo.findById(1L)).thenReturn(java.util.Optional.of(usuario));

        // No necesitamos mockear el match de la contraseña actual porque
        // la validación de la nueva contraseña debe ocurrir antes.
        assertThrows(PasswordInvalidaException.class,
                () -> usuarioService.cambiarPassword(1L, "cualquiera", "1234"));

        verify(repo, never()).save(any(Usuario.class));
    }
}