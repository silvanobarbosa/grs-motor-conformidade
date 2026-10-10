package com.senior.grc.fundacao.trilha;

import com.senior.grc.fundacao.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Append da trilha e verificacao de integridade da cadeia (CLAUDE.md, regra 3).
 *
 * Registrar e a UNICA porta de escrita da trilha: calcula a sequencia por tenant, pega o hash do
 * ultimo evento como elo anterior (ou GENESIS no primeiro) e grava o novo com o seu hash. Nao ha
 * caminho de edicao ou delecao.
 */
@Service
public class TrilhaService {

    public static final String GENESIS = "GENESIS";

    private final TrilhaEventoRepository repo;

    public TrilhaService(TrilhaEventoRepository repo) {
        this.repo = repo;
    }

    /** Registra um evento no tenant corrente. {@code payloadJson} ja deve vir canonico. */
    @Transactional
    public TrilhaEvento registrar(String tipo, String payloadJson) {
        String tenant = TenantContext.obrigatorio();
        TrilhaEvento ultimo = repo.findFirstByTenantIdOrderBySeqDesc(tenant).orElse(null);
        long seq = ultimo == null ? 1 : ultimo.getSeq() + 1;
        String hashAnterior = ultimo == null ? GENESIS : ultimo.getHash();
        String hash = Hashing.sha256(TrilhaEvento.conteudoParaHash(tenant, seq, tipo, payloadJson, hashAnterior));
        TrilhaEvento evento = new TrilhaEvento(UUID.randomUUID(), tenant, seq, tipo, payloadJson, hashAnterior, hash, Instant.now());
        return repo.save(evento);
    }

    @Transactional(readOnly = true)
    public List<TrilhaEvento> listar(String tenantId) {
        return repo.findByTenantIdOrderBySeqAsc(tenantId);
    }

    /**
     * Verifica a cadeia do tenant: cada evento tem o hash recalculado correto, o elo com o anterior
     * bate, e a sequencia nao tem buraco. Devolve o resultado explicavel (nao lanca) para a trilha
     * poder ser auditada e o dossie provar integridade (SPEC.md secao 10).
     */
    @Transactional(readOnly = true)
    public ResultadoIntegridade verificarIntegridade(String tenantId) {
        List<TrilhaEvento> eventos = repo.findByTenantIdOrderBySeqAsc(tenantId);
        String anterior = GENESIS;
        long seqEsperada = 1;
        for (TrilhaEvento e : eventos) {
            if (e.getSeq() != seqEsperada) {
                return ResultadoIntegridade.quebrada(e.getSeq(), "sequencia fora de ordem (esperava " + seqEsperada + ")");
            }
            if (!e.getHashAnterior().equals(anterior)) {
                return ResultadoIntegridade.quebrada(e.getSeq(), "elo com o evento anterior nao confere");
            }
            if (!e.getHash().equals(e.hashRecalculado())) {
                return ResultadoIntegridade.quebrada(e.getSeq(), "conteudo adulterado: hash nao confere");
            }
            anterior = e.getHash();
            seqEsperada++;
        }
        return ResultadoIntegridade.integra(eventos.size());
    }

    public record ResultadoIntegridade(boolean integra, long eventos, Long seqProblema, String motivo) {
        static ResultadoIntegridade integra(long eventos) {
            return new ResultadoIntegridade(true, eventos, null, null);
        }

        static ResultadoIntegridade quebrada(long seq, String motivo) {
            return new ResultadoIntegridade(false, seq, seq, motivo);
        }
    }
}
