package com.senior.grc.governanca;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** API de governanca: politicas, ciencia, pendencias e aceitar risco (exibicao/comandos da demo). */
@RestController
@RequestMapping("/api/governanca")
public class GovernancaController {

    private final GovernancaService gov;

    public GovernancaController(GovernancaService gov) {
        this.gov = gov;
    }

    @GetMapping("/politicas")
    public List<Politica> politicas() {
        return gov.politicas();
    }

    @PostMapping("/politicas")
    public Politica criar(@RequestBody Map<String, String> body) {
        return gov.criarPolitica(body.get("codigo"), body.get("titulo"), body.get("cargoAlvo"), body.get("localAlvo"));
    }

    @PostMapping("/politicas/{codigo}/publicar")
    public Politica publicar(@PathVariable String codigo, @RequestBody Map<String, String> body) {
        return gov.publicarPolitica(codigo, body.getOrDefault("por", "key-user"));
    }

    @PostMapping("/politicas/{codigo}/ciencia")
    public Map<String, Object> ciencia(@PathVariable String codigo, @RequestBody Map<String, String> body) {
        gov.registrarCiencia(codigo, body.get("pseudonimo"));
        return Map.of("status", "ciente");
    }

    @GetMapping("/politicas/{codigo}/pendencias")
    public List<String> pendencias(@PathVariable String codigo) {
        return gov.pendenciasCiencia(codigo);
    }

    @PostMapping("/aceitar-risco")
    public Map<String, Object> aceitarRisco(@RequestBody Map<String, String> body) {
        gov.aceitarRisco(body.get("sujeito"), body.get("causa"), body.get("justificativa"),
                body.getOrDefault("por", "key-user"));
        return Map.of("status", "aceito");
    }
}
