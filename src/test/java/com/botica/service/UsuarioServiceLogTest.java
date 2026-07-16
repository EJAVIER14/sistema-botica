package com.botica.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.botica.model.Usuario;
import com.botica.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceLogTest {

    @Mock
    private UsuarioRepository repo;

    @InjectMocks
    private UsuarioService service;

    // Capturador de eventos de log
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logbackLogger;

    @BeforeEach
    void configurarCapturaDeLogs() {
        // Obtenemos el logger real de UsuarioService (Logback, no la interfaz SLF4J)
        logbackLogger = (Logger) LoggerFactory.getLogger(UsuarioService.class);

        listAppender = new ListAppender<>();
        listAppender.start();

        // Enganchamos el appender al logger para capturar todo lo que emita durante el test
        logbackLogger.addAppender(listAppender);
    }

    @AfterEach
    void limpiarCapturaDeLogs() {
        // Desconectamos el appender para no afectar otros tests
        logbackLogger.detachAppender(listAppender);
    }

    @Test
    @DisplayName("Al crear un usuario exitosamente, debe generar un log INFO con el username y rol")
    void guardarUsuarioDebeGenerarLogInfo() {
        Usuario nuevo = new Usuario();
        nuevo.setUsername("jperez");
        nuevo.setPassword("Clave1234");
        nuevo.setRol("CAJERO");

        when(repo.save(any(Usuario.class))).thenReturn(nuevo);

        service.guardar(nuevo);

        List<ILoggingEvent> eventos = listAppender.list;

        assertThat(eventos)
                .anyMatch(e ->
                        e.getLevel().toString().equals("INFO") &&
                                e.getFormattedMessage().contains("jperez") &&
                                e.getFormattedMessage().contains("CAJERO")
                );
    }

    @Test
    @DisplayName("Al fallar el cambio de contraseña por contraseña actual incorrecta, debe generar un log WARN")
    void cambiarPasswordFallidoDebeGenerarLogWarn() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cajero1");
        usuario.setPassword(new BCryptPasswordEncoder().encode("ClaveReal123"));

        when(repo.findById(1L)).thenReturn(Optional.of(usuario));

        boolean resultado = service.cambiarPassword(1L, "ClaveIncorrecta1", "NuevaClave123");

        assertThat(resultado).isFalse();

        List<ILoggingEvent> eventos = listAppender.list;

        assertThat(eventos)
                .anyMatch(e ->
                        e.getLevel().toString().equals("WARN") &&
                                e.getFormattedMessage().contains("Contraseña actual incorrecta") &&
                                e.getFormattedMessage().contains("cajero1")
                );
    }

    @Test
    @DisplayName("Al eliminar un usuario, debe generar un log WARN con el id eliminado")
    void eliminarUsuarioDebeGenerarLogWarn() {
        service.eliminar(7L);

        List<ILoggingEvent> eventos = listAppender.list;

        assertThat(eventos)
                .anyMatch(e ->
                        e.getLevel().toString().equals("WARN") &&
                                e.getFormattedMessage().contains("Eliminando usuario") &&
                                e.getFormattedMessage().contains("7")
                );
    }

    @Test
    @DisplayName("Al intentar activar/desactivar un usuario inexistente, debe generar un log WARN")
    void toggleActivoUsuarioInexistenteDebeGenerarLogWarn() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        service.toggleActivo(99L);

        List<ILoggingEvent> eventos = listAppender.list;

        assertThat(eventos)
                .anyMatch(e ->
                        e.getLevel().toString().equals("WARN") &&
                                e.getFormattedMessage().contains("Intento de activar/desactivar un usuario inexistente")
                );
    }
}