package com.senior.grc.catalogo;

import com.senior.grc.demo.Relogio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Catalogo regulatorio GLOBAL e somente leitura (SPEC.md secao 9; CLAUDE.md regra 10). Requisitos
 * no nivel do item, versionados por vigencia — a consulta por data responde "cumpria a regra vigente
 * naquela data?". O conteudo e de DEMONSTRACAO (nao validado).
 */
@Service
public class CatalogoService {

    private final NormaRepository normas;
    private final RequisitoRepository requisitos;
    private final Relogio relogio;

    public CatalogoService(NormaRepository normas, RequisitoRepository requisitos, Relogio relogio) {
        this.normas = normas;
        this.requisitos = requisitos;
        this.relogio = relogio;
    }

    public List<Norma> normas() {
        return normas.findAll();
    }

    public List<RequisitoRegulatorio> requisitosDa(String normaCodigo) {
        return requisitos.findByNormaCodigo(normaCodigo);
    }

    /** Requisito vigente de um item numa data (consulta por data). */
    public Optional<RequisitoRegulatorio> vigenteEm(String normaCodigo, String item, Instant data) {
        return requisitos.findByNormaCodigoAndItem(normaCodigo, item).stream()
                .filter(r -> r.vigenteEm(data))
                .findFirst();
    }

    public Optional<RequisitoRegulatorio> vigenteAgora(String normaCodigo, String item) {
        return vigenteEm(normaCodigo, item, relogio.agora());
    }

    /**
     * Semeia o catalogo de demonstracao (idempotente): NR-1, NR-6, NR-10, NR-33, NR-35 + 2 politicas,
     * e UMA norma (NR-35) com DUAS versoes do mesmo item, para demonstrar consulta por data.
     * Datas de vigencia sao ficticias. Requisitos resumidos com palavras proprias.
     */
    @Transactional
    public void semearGlobal() {
        if (normas.count() > 0) {
            return; // ja semeado
        }
        salvarNorma("NR-1", "Disposicoes gerais e gerenciamento de riscos ocupacionais");
        salvarNorma("NR-6", "Equipamento de Protecao Individual (EPI)");
        salvarNorma("NR-10", "Seguranca em instalacoes e servicos em eletricidade");
        salvarNorma("NR-33", "Seguranca e saude nos trabalhos em espacos confinados");
        salvarNorma("NR-35", "Trabalho em altura");
        salvarNorma("POL-VISITANTE", "Politica interna: visitante so circula acompanhado");
        salvarNorma("POL-CRACHA", "Politica interna: cracha de desligado bloqueado em ate 24h");

        Instant epoca2020 = Instant.parse("2020-01-01T00:00:00Z");
        req("NR-1", "1.5", "Gerenciar riscos ocupacionais com inventario e plano de acao.", 1, epoca2020, null);
        req("NR-6", "6.3", "Fornecer e exigir o uso de EPI adequado ao risco.", 1, epoca2020, null);
        req("NR-10", "10.8", "Habilitacao e treinamento valido para servicos em eletricidade.", 1, epoca2020, null);
        req("NR-33", "33.3", "Capacitacao e autorizacao para trabalho em espaco confinado.", 1, epoca2020, null);
        // NR-35 com DUAS versoes do item 35.1 (treinamento de altura): v1 ate 2026-06, v2 a partir de 2026-06.
        Instant corte = Instant.parse("2026-06-01T00:00:00Z");
        req("NR-35", "35.1", "Treinamento de trabalho em altura valido por 2 anos.", 1, epoca2020, corte);
        req("NR-35", "35.1", "Treinamento de trabalho em altura com reciclagem periodica e exame.", 2, corte, null);
        req("POL-VISITANTE", "1", "Visitante so circula acompanhado por responsavel.", 1, epoca2020, null);
        req("POL-CRACHA", "1", "Cracha de colaborador desligado bloqueado em ate 24h.", 1, epoca2020, null);
    }

    private void salvarNorma(String codigo, String titulo) {
        normas.save(new Norma(codigo, titulo));
    }

    private void req(String norma, String item, String descricao, int versao, Instant inicio, Instant fim) {
        requisitos.save(new RequisitoRegulatorio(UUID.randomUUID(), norma, item, descricao, versao, inicio, fim));
    }
}
