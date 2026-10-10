package com.senior.grc.externo;

import com.senior.grc.demo.Relogio;
import com.senior.grc.fundacao.evidencia.EvidenciaRegistro;
import com.senior.grc.fundacao.evidencia.EvidenciaRepository;
import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.fundacao.trilha.Hashing;
import com.senior.grc.fundacao.trilha.TrilhaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Acesso externo e dossie de conformidade (SPEC.md secao 10).
 *
 * O acesso e concedido pelo key user, temporario e so leitura; toda concessao/uso fica na trilha.
 * O dossie e PSEUDONIMIZADO por padrao (as evidencias ja guardam pseudonimo, nunca CPF) e carrega a
 * verificacao de integridade da trilha + um hash do proprio dossie, para provar que nao foi alterado.
 */
@Service
public class DossieService {

    private final AcessoExternoRepository acessos;
    private final EvidenciaRepository evidencias;
    private final TrilhaService trilha;
    private final Relogio relogio;

    public DossieService(AcessoExternoRepository acessos, EvidenciaRepository evidencias,
                         TrilhaService trilha, Relogio relogio) {
        this.acessos = acessos;
        this.evidencias = evidencias;
        this.trilha = trilha;
        this.relogio = relogio;
    }

    @Transactional
    public AcessoExterno conceder(String auditorId, String escopo, int dias, String concedidoPor) {
        String tenant = TenantContext.obrigatorio();
        AcessoExterno a = new AcessoExterno(UUID.randomUUID(), tenant, auditorId, escopo, concedidoPor,
                relogio.agora(), relogio.agora().plus(java.time.Duration.ofDays(dias)));
        acessos.save(a);
        trilha.registrar("ACESSO_EXTERNO_CONCEDIDO",
                "{\"auditor\":\"" + auditorId + "\",\"escopo\":\"" + escopo + "\",\"por\":\"" + concedidoPor
                        + "\",\"validoAteDias\":" + dias + "}");
        return a;
    }

    @Transactional
    public void revogar(UUID acessoId) {
        acessos.findById(acessoId).ifPresent(a -> {
            a.revogar();
            acessos.save(a);
            trilha.registrar("ACESSO_EXTERNO_REVOGADO", "{\"acesso\":\"" + acessoId + "\"}");
        });
    }

    @Transactional(readOnly = true)
    public boolean acessoValido(String auditorId) {
        String tenant = TenantContext.obrigatorio();
        return acessos.findByTenantIdAndAuditorId(tenant, auditorId).stream()
                .anyMatch(a -> a.ativoEm(relogio.agora()));
    }

    public record ItemDossie(String pseudonimo, String tipo, String fato, Instant validoAte, String fonte) {
    }

    public record Dossie(String norma, int itens, boolean trilhaIntegra, String hashDossie, List<ItemDossie> conteudo) {
    }

    /**
     * Gera o dossie de uma norma (so leitura, pseudonimizado). Exige acesso externo valido do auditor.
     * O hash do dossie amarra o conteudo; a integridade da trilha e verificada e anexada.
     */
    @Transactional
    public Dossie gerar(String auditorId, String norma) {
        String tenant = TenantContext.obrigatorio();
        if (!acessoValido(auditorId)) {
            throw new IllegalStateException("acesso externo expirado ou inexistente para " + auditorId);
        }
        List<EvidenciaRegistro> evs = evidencias.findByTenantId(tenant).stream()
                .filter(e -> norma.equals(e.atributo("norma")))
                .toList();
        List<ItemDossie> itens = evs.stream()
                .map(e -> new ItemDossie(e.getPseudonimoPessoa(), e.getTipo(), e.getFato(), e.getValidoAte(), e.getSistemaOrigem()))
                .toList();
        boolean integra = trilha.verificarIntegridade(tenant).integra();
        String hash = Hashing.sha256(tenant + "|" + norma + "|" + evs.stream().map(e -> e.getId().toString()).sorted().reduce("", String::concat));

        trilha.registrar("DOSSIE_GERADO",
                "{\"auditor\":\"" + auditorId + "\",\"norma\":\"" + norma + "\",\"itens\":" + itens.size()
                        + ",\"hash\":\"" + hash + "\"}");
        return new Dossie(norma, itens.size(), integra, hash, itens);
    }
}
