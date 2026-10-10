package com.senior.grc.ia;

import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.fundacao.trilha.TrilhaService;
import com.senior.grc.mocks.SaraPort;
import com.senior.grc.risco.RiscoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Funcoes de IA do produto (Sara), com DUAS garantias inegociaveis:
 *  - a IA NUNCA decide: so sugere (triagem), extrai e explica — nao altera risco, nao aprova nada
 *    (CLAUDE.md regra 8);
 *  - o produto FUNCIONA com a IA desligada: ha uma flag por tenant; desligada, as funcoes de IA
 *    retornam vazio e o resto do produto segue igual (regra 9).
 *
 * Toda saida de IA registra modelo/prompt/confianca na trilha (SPEC.md secao 15).
 */
@Service
public class SaraService {

    private final SaraPort sara;
    private final RiscoService risco;
    private final TrilhaService trilha;

    /** Tenants com a IA DESLIGADA (default: ligada). */
    private final Set<String> desligada = ConcurrentHashMap.newKeySet();

    public SaraService(SaraPort sara, RiscoService risco, TrilhaService trilha) {
        this.sara = sara;
        this.risco = risco;
        this.trilha = trilha;
    }

    public void desligarIa() {
        desligada.add(TenantContext.obrigatorio());
    }

    public void ligarIa() {
        desligada.remove(TenantContext.obrigatorio());
    }

    public boolean iaLigada() {
        return !desligada.contains(TenantContext.obrigatorio());
    }

    /** Triagem de um relato (sugestao). Vazio quando a IA esta desligada — o produto segue funcionando. */
    @Transactional
    public Optional<SaraPort.Triagem> triarOcorrencia(String textoLivre) {
        if (!iaLigada()) {
            return Optional.empty();
        }
        SaraPort.Triagem t = sara.triar(textoLivre);
        trilha.registrar("IA_TRIAGEM",
                "{\"modelo\":\"" + t.modelo() + "\",\"confianca\":" + t.confianca()
                        + ",\"local\":\"" + t.local() + "\",\"pessoas\":" + t.pessoas().size() + "}");
        return Optional.of(t);
    }

    /** Explica, em linguagem natural, por que o risco de um sujeito esta no nivel atual (grounded). */
    @Transactional
    public Optional<String> explicarRisco(String sujeitoId) {
        if (!iaLigada()) {
            return Optional.empty();
        }
        String base = risco.doSujeito(sujeitoId).map(a -> a.getExplicacao()).orElse("Sem avaliacao de risco.");
        String texto = sara.explicar(base);
        trilha.registrar("IA_EXPLICACAO", "{\"modelo\":\"" + sara.modelo() + "\",\"sujeito\":\"" + sujeitoId + "\"}");
        return Optional.of(texto);
    }
}
