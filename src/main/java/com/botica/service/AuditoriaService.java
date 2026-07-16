package com.botica.service;

import com.botica.model.AuditoriaLog;
import com.botica.model.EstadoAuditoria;
import com.botica.repository.AuditoriaLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaLogRepository repo;

    public void registrar(String modulo, String accion, EstadoAuditoria estado, String usuario, String descripcion) {
        AuditoriaLog log = new AuditoriaLog(modulo, accion, estado, usuario, descripcion);
        repo.save(log);
    }

    public Page<AuditoriaLog> listarPaginado(int page, int size) {
        return repo.findAllByOrderByFechaDesc(PageRequest.of(page, size));
    }
}