package com.senior.grc.externo;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** API de acesso externo e dossie de conformidade. */
@RestController
@RequestMapping("/api/externo")
public class ExternoController {

    private final DossieService dossie;

    public ExternoController(DossieService dossie) {
        this.dossie = dossie;
    }

    @PostMapping("/acessos")
    public Map<String, Object> conceder(@RequestBody Map<String, Object> body) {
        AcessoExterno a = dossie.conceder((String) body.get("auditor"), (String) body.get("escopo"),
                ((Number) body.getOrDefault("dias", 5)).intValue(), (String) body.getOrDefault("por", "key-user"));
        return Map.of("auditor", a.getAuditorId(), "escopo", a.getEscopo(), "validoAte", a.getValidoAte());
    }

    @PostMapping("/dossie")
    public DossieService.Dossie gerar(@RequestBody Map<String, String> body) {
        return dossie.gerar(body.get("auditor"), body.get("norma"));
    }
}
