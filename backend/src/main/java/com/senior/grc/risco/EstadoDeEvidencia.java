package com.senior.grc.risco;

import com.senior.grc.fundacao.evidencia.EvidenciaRegistro;

import java.time.Instant;
import java.util.List;

/**
 * Estado atual de evidencia de um sujeito (pessoa ou empresa), montado a partir do read-model.
 * O avaliador consulta isto; nao toca no banco. Cada consulta considera "agora" (do relogio) para
 * validade e so evidencias CONFERIDAS reduzem/criam risco com forca total (baixa confianca = trava).
 */
public class EstadoDeEvidencia {

    private final List<EvidenciaRegistro> evidencias;
    private final Instant agora;

    public EstadoDeEvidencia(List<EvidenciaRegistro> evidencias, Instant agora) {
        this.evidencias = evidencias;
        this.agora = agora;
    }

    public Instant agora() {
        return agora;
    }

    /** A evidencia mais recente de um tipo (por data de coleta), ou null. */
    public EvidenciaRegistro ultimaDoTipo(String tipo) {
        return evidencias.stream()
                .filter(e -> e.getTipo().equals(tipo))
                .max((a, b) -> a.getColetadaEm().compareTo(b.getColetadaEm()))
                .orElse(null);
    }

    /** A evidencia mais recente de um tipo com um atributo de valor dado. */
    public EvidenciaRegistro ultimaComAtributo(String tipo, String chave, String valor) {
        return evidencias.stream()
                .filter(e -> e.getTipo().equals(tipo) && valor.equals(e.atributo(chave)))
                .max((a, b) -> a.getColetadaEm().compareTo(b.getColetadaEm()))
                .orElse(null);
    }

    public boolean existeTipo(String tipo) {
        return evidencias.stream().anyMatch(e -> e.getTipo().equals(tipo));
    }

    public boolean valida(EvidenciaRegistro e) {
        return e != null && (e.getValidoAte() == null || !e.getValidoAte().isBefore(agora));
    }

    public List<EvidenciaRegistro> todas() {
        return evidencias;
    }

    /** A evidencia mais recente de cada GRUPO (valor do atributo {@code chave}) dentro de um tipo. */
    public java.util.Map<String, EvidenciaRegistro> ultimasPorGrupo(String tipo, String chave) {
        java.util.Map<String, EvidenciaRegistro> out = new java.util.LinkedHashMap<>();
        for (EvidenciaRegistro e : evidencias) {
            if (!e.getTipo().equals(tipo)) {
                continue;
            }
            String grupo = e.atributo(chave);
            if (grupo == null) {
                grupo = "";
            }
            EvidenciaRegistro atual = out.get(grupo);
            if (atual == null || e.getColetadaEm().isAfter(atual.getColetadaEm())) {
                out.put(grupo, e);
            }
        }
        return out;
    }
}
