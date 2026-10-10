package com.senior.grc.risco;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * API de risco: painel (estado residual por sujeito), reavaliacao e a explicacao "por que mudou".
 * Processar != exibir — aqui e exibicao; o motor processa tudo independentemente (SPEC.md 7/12).
 */
@RestController
@RequestMapping("/api/risco")
public class RiscoController {

    private final RiscoService risco;

    public RiscoController(RiscoService risco) {
        this.risco = risco;
    }

    @GetMapping
    public List<AvaliacaoRisco> painel() {
        return risco.painel();
    }

    @GetMapping("/{sujeitoId}")
    public AvaliacaoRisco doSujeito(@PathVariable String sujeitoId) {
        return risco.doSujeito(sujeitoId).orElse(null);
    }

    /** Explicacao da avaliacao corrente do sujeito ("qual evidencia, de qual fonte, em qual regra"). */
    @GetMapping("/{sujeitoId}/porque")
    public Map<String, Object> porque(@PathVariable String sujeitoId) {
        return risco.doSujeito(sujeitoId)
                .map(a -> Map.<String, Object>of("nivel", a.getNivelResidual(), "cenario",
                        String.valueOf(a.getCenarioDominante()), "explicacao", String.valueOf(a.getExplicacao())))
                .orElse(Map.of("nivel", "SEM_RISCO", "explicacao", "sem avaliacao"));
    }

    @PostMapping("/reavaliar")
    public Map<String, Object> reavaliarTudo() {
        return Map.of("sujeitosReavaliados", risco.reavaliarTudo());
    }
}
