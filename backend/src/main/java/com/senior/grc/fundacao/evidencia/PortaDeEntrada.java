package com.senior.grc.fundacao.evidencia;

import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.fundacao.trilha.TrilhaEvento;
import com.senior.grc.fundacao.trilha.TrilhaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Porta de entrada: a UNICA porta por onde evidencia entra no core (CLAUDE.md, regra 1). Valida,
 * recusa dado pessoal cru (regra 4) e grava a evidencia como evento na trilha (so inclusao). Os
 * adaptadores mock de cada sistema chamam exatamente este metodo — nao ha atalho ao banco.
 */
@Service
public class PortaDeEntrada {

    public static final String TIPO_EVENTO = "EVIDENCIA_RECEBIDA";

    /** 11 digitos soltos cheiram a CPF; dado pessoal cru nao entra na evidencia (so pseudonimo). */
    private static final Pattern PARECE_CPF = Pattern.compile(".*\\b\\d{11}\\b.*|.*\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}.*");

    private final TrilhaService trilha;
    private final EvidenciaRepository evidencias;

    public PortaDeEntrada(TrilhaService trilha, EvidenciaRepository evidencias) {
        this.trilha = trilha;
        this.evidencias = evidencias;
    }

    /**
     * Recebe a evidencia: grava o evento na trilha (fonte da verdade) e projeta o read-model
     * consultavel. Evidencia de BAIXA confianca entra NAO conferida — nao reduz risco ate conferencia
     * humana (SPEC.md secoes 6 e 15). Devolve o id do evento da trilha.
     */
    @Transactional
    public UUID receber(Evidencia e) {
        validar(e);
        TrilhaEvento evento = trilha.registrar(TIPO_EVENTO, canonico(e));
        boolean conferida = e.confianca() != ConfiancaFonte.BAIXA;
        evidencias.save(new EvidenciaRegistro(UUID.randomUUID(), TenantContext.obrigatorio(), evento.getId(), e, conferida));
        return evento.getId();
    }

    private void validar(Evidencia e) {
        if (e == null) {
            throw new EvidenciaInvalidaException("evidencia nula");
        }
        if (e.fato() == null || e.fato().isBlank()) {
            throw new EvidenciaInvalidaException("evidencia sem fato");
        }
        if (e.sistemaOrigem() == null || e.sistemaOrigem().isBlank()) {
            throw new EvidenciaInvalidaException("evidencia sem sistema de origem");
        }
        if (e.tipo() == null || e.tipo().isBlank()) {
            throw new EvidenciaInvalidaException("evidencia sem tipo");
        }
        if (e.confianca() == null) {
            throw new EvidenciaInvalidaException("evidencia sem confianca da fonte");
        }
        if (e.coletadaEm() == null) {
            throw new EvidenciaInvalidaException("evidencia sem data de coleta");
        }
        if (e.pseudonimoPessoa() != null && PARECE_CPF.matcher(e.pseudonimoPessoa()).matches()) {
            throw new EvidenciaInvalidaException("dado pessoal cru na evidencia: use o pseudonimo, nunca o CPF (regra 4)");
        }
        if (e.fato() != null && PARECE_CPF.matcher(e.fato()).matches()) {
            throw new EvidenciaInvalidaException("CPF detectado no fato: minimize o dado pessoal (LGPD art. 6, III)");
        }
    }

    /**
     * JSON canonico (campos em ordem fixa) para o hash da trilha ser estavel. Nulos viram "null".
     * Simples de proposito: na demo nao ha aninhamento; o formato e deterministico e testavel.
     */
    public static String canonico(Evidencia e) {
        return "{"
                + "\"pseudonimoPessoa\":" + jstr(e.pseudonimoPessoa()) + ","
                + "\"tipo\":" + jstr(e.tipo()) + ","
                + "\"atributos\":" + atributosJson(e) + ","
                + "\"fato\":" + jstr(e.fato()) + ","
                + "\"sistemaOrigem\":" + jstr(e.sistemaOrigem()) + ","
                + "\"confianca\":" + jstr(e.confianca() == null ? null : e.confianca().name()) + ","
                + "\"coletadaEm\":" + jinstant(e.coletadaEm()) + ","
                + "\"validoAte\":" + jinstant(e.validoAte()) + ","
                + "\"hashDocumento\":" + jstr(e.hashDocumento())
                + "}";
    }

    /** Atributos em ordem de chave, para o canonico ser deterministico. */
    public static String atributosJson(Evidencia e) {
        return AtributosJson.escrever(e.atributos());
    }

    private static String jstr(String v) {
        if (v == null) {
            return "null";
        }
        return "\"" + v.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String jinstant(Instant v) {
        return v == null ? "null" : "\"" + v.toString() + "\"";
    }
}
