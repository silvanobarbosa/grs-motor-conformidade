package com.senior.grc.fundacao.api;

import com.senior.grc.fundacao.evidencia.Evidencia;
import com.senior.grc.fundacao.evidencia.EvidenciaInvalidaException;
import com.senior.grc.fundacao.evidencia.PortaDeEntrada;
import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.fundacao.trilha.TrilhaEvento;
import com.senior.grc.fundacao.trilha.TrilhaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * API da fundacao: saude, porta de entrada de evidencia e consulta da trilha. Exibicao e consulta;
 * o tenant vem do cabecalho X-Tenant-Id (TenantFilter). Permissoes finas chegam em modulo posterior.
 */
@RestController
@RequestMapping("/api")
public class ApiFundacaoController {

    private final PortaDeEntrada portaDeEntrada;
    private final TrilhaService trilha;

    public ApiFundacaoController(PortaDeEntrada portaDeEntrada, TrilhaService trilha) {
        this.portaDeEntrada = portaDeEntrada;
        this.trilha = trilha;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "produto", "Motor GRC", "versao", "0.1.0");
    }

    @PostMapping("/evidencias")
    public ResponseEntity<Map<String, Object>> receberEvidencia(@Valid @RequestBody Evidencia evidencia) {
        UUID eventoId = portaDeEntrada.receber(evidencia);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("eventoId", eventoId));
    }

    @GetMapping("/trilha")
    public List<TrilhaEvento> trilha() {
        return trilha.listar(TenantContext.obrigatorio());
    }

    @GetMapping("/trilha/integridade")
    public TrilhaService.ResultadoIntegridade integridade() {
        return trilha.verificarIntegridade(TenantContext.obrigatorio());
    }

    @ExceptionHandler(EvidenciaInvalidaException.class)
    public ResponseEntity<Map<String, Object>> evidenciaInvalida(EvidenciaInvalidaException e) {
        // RFC 7807-ish: erro explicavel, sem vazar interno.
        return ResponseEntity.badRequest().body(Map.of("tipo", "evidencia-invalida", "detalhe", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> semTenant(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("tipo", "sem-tenant", "detalhe", e.getMessage()));
    }
}
