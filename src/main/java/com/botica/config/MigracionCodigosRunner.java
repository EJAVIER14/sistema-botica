package com.botica.config;

import com.botica.model.Producto;
import com.botica.repository.ProductoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MigracionCodigosRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MigracionCodigosRunner.class);

    @Autowired
    private ProductoRepository repo;

    @Override
    public void run(String... args) {
        List<Producto> sinCodigo = repo.findByCodigoIsNull();

        if (sinCodigo.isEmpty()) {
            return;
        }

        log.info("Migración: asignando código a {} producto(s) existente(s)...", sinCodigo.size());

        for (Producto p : sinCodigo) {
            p.setCodigo(String.format("BOT-%04d", p.getId()));
            repo.save(p);
        }

        log.info("Migración de códigos completada.");
    }
}