package com.botica.service;

import com.botica.exception.PasswordInvalidaException;
import com.botica.model.Usuario;
import com.botica.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional
public class UsuarioService {

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

    // Buscar usuario por su username (para el perfil)
    public Usuario buscarPorUsername(String username) {
        return repo.findByUsername(username).orElse(null);
    }

    public Usuario guardar(Usuario u) {
        validarPassword(u.getPassword());
        u.setPassword(encoder.encode(u.getPassword()));
        u.setActivo(true);
        return repo.save(u);
    }

    public Usuario actualizar(Usuario u) {
        Usuario existente = repo.findById(u.getId()).orElse(null);
        if (existente != null) {
            existente.setNombre(u.getNombre());
            existente.setUsername(u.getUsername());
            existente.setRol(u.getRol());
            existente.setActivo(u.getActivo());
            return repo.save(existente);
        }
        return null;
    }

    public boolean cambiarPassword(Long id, String passwordActual, String passwordNueva) {
        Usuario usuario = repo.findById(id).orElse(null);
        if (usuario == null) return false;

        validarPassword(passwordNueva);

        if (!encoder.matches(passwordActual, usuario.getPassword())) {
            return false;
        }

        usuario.setPassword(encoder.encode(passwordNueva));
        repo.save(usuario);
        return true;
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }

    public boolean existeUsername(String username) {
        return repo.existsByUsername(username);
    }

    // ═══ NUEVO: activar/desactivar usuario ═══
    public void toggleActivo(Long id) {
        Usuario usuario = repo.findById(id).orElse(null);
        if (usuario != null) {
            usuario.setActivo(!Boolean.TRUE.equals(usuario.getActivo()));
            repo.save(usuario);
        }
    }

    private void validarPassword(String password) {
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