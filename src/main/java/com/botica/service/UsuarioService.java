package com.botica.service;

import com.botica.exception.PasswordInvalidaException;
import com.botica.model.Usuario;
import com.botica.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository repo;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    private static final Pattern TIENE_LETRA = Pattern.compile(".*[A-Za-z].*");
    private static final Pattern TIENE_NUMERO = Pattern.compile(".*\\d.*");

    public List<Usuario> listarTodos() {
        return repo.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Usuario buscarPorUsername(String username) {
        return repo.findByUsername(username).orElse(null);
    }

    public Usuario guardar(Usuario u) {
        validarPassword(u.getPassword());
        u.setPassword(encoder.encode(u.getPassword()));
        u.setActivo(true);
        Usuario creado = repo.save(u);
        logger.info("Usuario creado correctamente: username={}, rol={}", creado.getUsername(), creado.getRol());
        return creado;
    }

    public Usuario actualizar(Usuario u) {
        Usuario existente = repo.findById(u.getId()).orElse(null);
        if (existente != null) {
            existente.setNombre(u.getNombre());
            existente.setUsername(u.getUsername());
            existente.setEmail(u.getEmail());
            existente.setRol(u.getRol());
            existente.setActivo(u.getActivo());
            Usuario actualizado = repo.save(existente);
            logger.info("Usuario actualizado: id={}, username={}", actualizado.getId(), actualizado.getUsername());
            return actualizado;
        }
        logger.warn("Intento de actualizar un usuario inexistente: id={}", u.getId());
        return null;
    }

    // ═══ Cambio de contraseña por el propio usuario (Mi Perfil) — sigue pidiendo la actual ═══
    public boolean cambiarPassword(Long id, String passwordActual, String passwordNueva) {
        Usuario usuario = repo.findById(id).orElse(null);
        if (usuario == null) {
            logger.warn("Intento de cambio de contraseña para usuario inexistente: id={}", id);
            return false;
        }

        validarPassword(passwordNueva);

        if (!encoder.matches(passwordActual, usuario.getPassword())) {
            logger.warn("Contraseña actual incorrecta al intentar cambiarla: username={}", usuario.getUsername());
            return false;
        }

        usuario.setPassword(encoder.encode(passwordNueva));
        repo.save(usuario);
        logger.info("Contraseña actualizada correctamente para username={}", usuario.getUsername());
        return true;
    }

    // ═══ NUEVO: reseteo de contraseña por parte del Admin, sin pedir la actual ═══
    public boolean resetearPassword(Long id, String passwordNueva) {
        Usuario usuario = repo.findById(id).orElse(null);
        if (usuario == null) {
            logger.warn("Intento de resetear contraseña de un usuario inexistente: id={}", id);
            return false;
        }

        validarPassword(passwordNueva);

        usuario.setPassword(encoder.encode(passwordNueva));
        repo.save(usuario);
        logger.info("Contraseña reseteada por un administrador para username={}", usuario.getUsername());
        return true;
    }

    public void eliminar(Long id) {
        logger.warn("Eliminando usuario: id={}", id);
        repo.deleteById(id);
    }

    public boolean existeUsername(String username) {
        return repo.existsByUsername(username);
    }

    public boolean existeEmail(String email) {
        return repo.findByEmail(email).isPresent();
    }

    public boolean existeEmailParaOtroUsuario(String email, Long id) {
        return repo.findByEmail(email)
                .map(u -> !u.getId().equals(id))
                .orElse(false);
    }

    public void toggleActivo(Long id) {
        Usuario usuario = repo.findById(id).orElse(null);
        if (usuario != null) {
            boolean nuevoEstado = !Boolean.TRUE.equals(usuario.getActivo());
            usuario.setActivo(nuevoEstado);
            repo.save(usuario);
            logger.info("Estado de usuario cambiado: username={}, activo={}", usuario.getUsername(), nuevoEstado);
        } else {
            logger.warn("Intento de activar/desactivar un usuario inexistente: id={}", id);
        }
    }

    private void validarPassword(String password) {
        logger.debug("Validando politica de contraseña (longitud={})", password == null ? 0 : password.length());
        if (password == null || password.length() < 8) {
            throw new PasswordInvalidaException("debe tener al menos 8 caracteres");
        }
        if (!TIENE_LETRA.matcher(password).matches()) {
            throw new PasswordInvalidaException("debe contener al menos una letra");
        }
        if (!TIENE_NUMERO.matcher(password).matches()) {
            throw new PasswordInvalidaException("debe contener al menos un número");
        }
    }
}