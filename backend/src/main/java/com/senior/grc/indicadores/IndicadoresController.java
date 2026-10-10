package com.senior.grc.indicadores;

import com.senior.grc.risco.NivelRisco;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** API de indicadores (saida agregada; aberta para BI externo). SPEC.md secao 11. */
@RestController
@RequestMapping("/api/indicadores")
public class IndicadoresController {

    private final IndicadoresService ind;

    public IndicadoresController(IndicadoresService ind) {
        this.ind = ind;
    }

    @GetMapping("/operacional")
    public List<IndicadoresService.Vencimento> operacional(@RequestParam(defaultValue = "15") int dias) {
        return ind.operacionalVencimentos(dias);
    }

    @GetMapping("/gestao")
    public Map<String, Map<NivelRisco, Long>> gestao() {
        return ind.gestaoResidualPorUnidade();
    }

    @GetMapping("/executivo")
    public Map<String, Object> executivo(@RequestParam(defaultValue = "MEDIO") NivelRisco apetite) {
        return ind.executivoVsApetite(apetite);
    }

    @GetMapping("/atualidade")
    public Map<String, java.time.Instant> atualidade() {
        return ind.atualidadePorFonte();
    }

    @GetMapping("/cobertura")
    public Map<String, Object> cobertura() {
        return ind.cobertura();
    }
}
