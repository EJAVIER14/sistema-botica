package com.botica.service;

import com.botica.model.Usuario;
import com.botica.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository repo;

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

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
}