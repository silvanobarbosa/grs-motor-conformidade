package com.senior.grc.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Administracao da demo (DEMO.md secao 1): simulador de tempo e carga do cliente ficticio.
 * Tudo roda no tenant do cabecalho X-Tenant-Id (tipicamente "metalurgica-exemplo").
 */
@RestController
@RequestMapping("/api/demo")
public class DemoAdminController {

    private final SeedDemo seed;
    private final RelogioDemo relogio;

    public DemoAdminController(SeedDemo seed, RelogioDemo relogio) {
        this.seed = seed;
        this.relogio = relogio;
    }

    @PostMapping("/seed")
    public SeedDemo.Resumo semear(@RequestParam(defaultValue = "120") int colaboradores) {
        return seed.semear(colaboradores);
    }

    @GetMapping("/relogio")
    public Map<String, Object> relogio() {
        return Map.of("agora", relogio.agora());
    }

    @PostMapping("/relogio/avancar")
    public Map<String, Object> avancar(@RequestParam long dias) {
        Instant novo = relogio.avancarDias(dias);
        return Map.of("agora", novo);
    }

    @PostMapping("/relogio/reiniciar")
    public Map<String, Object> reiniciar() {
        relogio.reiniciar();
        return Map.of("agora", relogio.agora());
    }
}
