package com.senior.grc.catalogo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** API do catalogo regulatorio (global, somente leitura) e da aplicabilidade. */
@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    private final CatalogoService catalogo;
    private final AplicabilidadeService aplicabilidade;

    public CatalogoController(CatalogoService catalogo, AplicabilidadeService aplicabilidade) {
        this.catalogo = catalogo;
        this.aplicabilidade = aplicabilidade;
    }

    @GetMapping("/normas")
    public List<Map<String, Object>> normas() {
        return catalogo.normas().stream().map(n -> Map.<String, Object>of("codigo", n.getCodigo(), "titulo", n.getTitulo())).toList();
    }

    /** Requisito vigente de um item; {@code data} (ISO) opcional para a consulta por data. */
    @GetMapping("/requisito")
    public Map<String, Object> requisito(@RequestParam String norma, @RequestParam String item,
                                         @RequestParam(required = false) String data) {
        var req = data != null && !data.isBlank()
                ? catalogo.vigenteEm(norma, item, Instant.parse(data))
                : catalogo.vigenteAgora(norma, item);
        return req.<Map<String, Object>>map(r -> Map.of("norma", r.getNormaCodigo(), "item", r.getItem(),
                        "versao", r.getVersao(), "descricao", r.getDescricao(), "naoValidado", r.isNaoValidado(),
                        "vigenciaInicio", r.getVigenciaInicio()))
                .orElse(Map.of("encontrado", false));
    }

    @GetMapping("/aplicabilidade")
    public List<AplicabilidadeService.Sugestao> aplicabilidade(@RequestParam String cargo,
                                                               @RequestParam(required = false) String local) {
        return aplicabilidade.sugerirPara(cargo, local);
    }
}
