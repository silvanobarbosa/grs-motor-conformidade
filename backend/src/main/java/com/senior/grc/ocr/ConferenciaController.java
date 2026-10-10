package com.senior.grc.ocr;

import com.senior.grc.fundacao.evidencia.EvidenciaRegistro;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** API de conferencia de evidencias de baixa confianca (OCR). */
@RestController
@RequestMapping("/api/conferencia")
public class ConferenciaController {

    private final ConferenciaService conferencia;

    public ConferenciaController(ConferenciaService conferencia) {
        this.conferencia = conferencia;
    }

    @GetMapping("/pendentes")
    public List<Map<String, Object>> pendentes() {
        return conferencia.pendentes().stream().map(ConferenciaController::resumo).toList();
    }

    @PostMapping("/{id}/conferir")
    public Map<String, Object> conferir(@PathVariable UUID id, @RequestParam(defaultValue = "key-user") String por) {
        conferencia.conferir(id, por);
        return Map.of("conferida", id);
    }

    private static Map<String, Object> resumo(EvidenciaRegistro e) {
        return Map.of("id", e.getId(), "tipo", e.getTipo(), "fato", e.getFato(),
                "sistemaOrigem", e.getSistemaOrigem(), "confianca", e.getConfianca(),
                "empresa", String.valueOf(e.atributo("empresa")), "documento", String.valueOf(e.atributo("documento")));
    }
}
