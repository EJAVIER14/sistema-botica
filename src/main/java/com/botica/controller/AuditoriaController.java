package com.botica.controller;

import com.botica.model.AuditoriaLog;
import com.botica.service.AuditoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuditoriaController {

    @Autowired
    private AuditoriaService service;

    @GetMapping("/auditoria")
    public String listar(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<AuditoriaLog> pagina = service.listarPaginado(page, 20);

        model.addAttribute("logs", pagina.getContent());
        model.addAttribute("paginaActual", page);
        model.addAttribute("totalPaginas", pagina.getTotalPages());
        model.addAttribute("totalElementos", pagina.getTotalElements());

        return "auditoria/lista";
    }
}