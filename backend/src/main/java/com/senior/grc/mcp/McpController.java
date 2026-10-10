package com.senior.grc.mcp;

import com.senior.grc.risco.Cenario;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * API do servidor MCP (lado de controle da demo). Expoe as tools do {@link McpService} com as mesmas
 * guardas (desligado por padrao, escrita com confirmacao, acoes proibidas negadas). O transporte MCP
 * oficial (JSON-RPC/OAuth) pluga por cima.
 */
@RestController
@RequestMapping("/api/mcp")
public class McpController {

    private final McpService mcp;

    public McpController(McpService mcp) {
        this.mcp = mcp;
    }

    @GetMapping("/estado")
    public Map<String, Object> estado() {
        return Map.of("ligado", mcp.estaLigado());
    }

    @PostMapping("/ligar")
    public Map<String, Object> ligar() {
        mcp.ligar();
        return Map.of("ligado", true);
    }

    @PostMapping("/desligar")
    public Map<String, Object> desligar() {
        mcp.desligar();
        return Map.of("ligado", false);
    }

    @GetMapping("/recursos")
    public McpService.Resultado recursos() {
        return mcp.recursos();
    }

    @PostMapping("/consultar-risco")
    public McpService.Resultado consultarRisco(@RequestBody Map<String, String> b) {
        return mcp.consultarRisco(b.get("sujeito"));
    }

    @PostMapping("/listar-pendencias")
    public McpService.Resultado listarPendencias() {
        return mcp.listarPendencias();
    }

    @PostMapping("/buscar-requisito")
    public McpService.Resultado buscarRequisito(@RequestBody Map<String, String> b) {
        return mcp.buscarRequisito(b.get("norma"), b.get("item"), null);
    }

    @PostMapping("/abrir-plano")
    public McpService.Resultado abrirPlano(@RequestBody Map<String, Object> b) {
        Cenario.Escopo escopo = Cenario.Escopo.valueOf((String) b.getOrDefault("escopo", "PESSOA"));
        return mcp.abrirPlano((String) b.get("sujeito"), escopo, (String) b.get("causa"),
                Boolean.TRUE.equals(b.get("confirmado")));
    }

    @PostMapping("/aceitar-risco")
    public McpService.Resultado aceitarRisco() {
        return mcp.aceitarRisco(); // sempre negado
    }
}
