package com.senior.grc.acoes;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** API de acoes: planos abertos/concluidos e conclusao (fecha o ciclo). */
@RestController
@RequestMapping("/api/acoes")
public class AcoesController {

    private final AcoesService acoes;

    public AcoesController(AcoesService acoes) {
        this.acoes = acoes;
    }

    @GetMapping("/planos")
    public List<PlanoDeAcao> planos() {
        return acoes.painel();
    }

    @PostMapping("/planos/{id}/concluir")
    public Map<String, Object> concluir(@PathVariable UUID id) {
        acoes.concluir(id);
        return Map.of("status", "concluido", "plano", id);
    }
}
