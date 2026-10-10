package com.senior.grc.risco;

import com.senior.grc.fundacao.evidencia.ConfiancaFonte;
import com.senior.grc.fundacao.evidencia.EvidenciaRegistro;
import com.senior.grc.risco.Cenario.Condicao;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Avaliador de expressoes simples (demo) atras do {@link RuleEnginePort}. Interpreta as condicoes
 * declarativas do cenario sobre o estado de evidencia. Todas as condicoes verdadeiras = dispara.
 * So evidencia CONFERIDA entra como gatilho de peso total; evidencia de baixa confianca / nao
 * conferida marca o gatilho como "apenas baixa confianca" (trava de criticidade no RiscoService).
 */
@Component
public class AvaliadorSimples implements RuleEnginePort {

    @Override
    public Optional<Gatilho> avaliar(Cenario cenario, EstadoDeEvidencia estado) {
        List<EvidenciaRegistro> gatilhos = new ArrayList<>();
        List<String> partes = new ArrayList<>();

        for (Condicao c : cenario.condicoes()) {
            EvidenciaRegistro ev = avaliarCondicao(c, estado, partes);
            if (ev == null) {
                return Optional.empty(); // uma condicao falhou -> cenario nao dispara
            }
            gatilhos.add(ev);
        }

        boolean apenasBaixa = !gatilhos.isEmpty() && gatilhos.stream()
                .allMatch(e -> e.getConfianca() == ConfiancaFonte.BAIXA || !e.isConferida());
        String explicacao = "Cenario \"" + cenario.nome() + "\": " + String.join("; ", partes) + ".";
        return Optional.of(new Gatilho(gatilhos, explicacao, apenasBaixa));
    }

    /** Devolve a evidencia que satisfez a condicao (gatilho), ou null se a condicao nao foi satisfeita. */
    private EvidenciaRegistro avaliarCondicao(Condicao c, EstadoDeEvidencia estado, List<String> partes) {
        switch (c.modo()) {
            case EXISTE -> {
                EvidenciaRegistro e = estado.ultimaDoTipo(c.tipo());
                if (e != null) {
                    partes.add("existe " + c.tipo() + " (fonte " + e.getSistemaOrigem() + ")");
                    return e;
                }
                return null;
            }
            case STATUS_IGUAL -> {
                EvidenciaRegistro e = estado.ultimaDoTipo(c.tipo());
                if (e != null && c.valor().equals(e.atributo(c.chave()))) {
                    partes.add(c.tipo() + " com " + c.chave() + "=" + c.valor() + " (fonte " + e.getSistemaOrigem() + ")");
                    return e;
                }
                return null;
            }
            case VENCIDA -> {
                for (EvidenciaRegistro e : estado.ultimasPorGrupo(c.tipo(), c.chave()).values()) {
                    if (e.getValidoAte() != null && e.getValidoAte().isBefore(estado.agora())) {
                        partes.add(c.tipo() + " " + c.chave() + "=" + e.atributo(c.chave()) + " VENCIDO em "
                                + e.getValidoAte() + " (fonte " + e.getSistemaOrigem() + ")");
                        return e;
                    }
                }
                return null;
            }
            case VENCE_EM_DIAS -> {
                long dias = Long.parseLong(c.valor());
                var limite = estado.agora().plus(Duration.ofDays(dias));
                for (EvidenciaRegistro e : estado.ultimasPorGrupo(c.tipo(), c.chave()).values()) {
                    if (e.getValidoAte() != null
                            && !e.getValidoAte().isBefore(estado.agora())
                            && !e.getValidoAte().isAfter(limite)) {
                        partes.add(c.tipo() + " " + c.chave() + "=" + e.atributo(c.chave()) + " vence em "
                                + e.getValidoAte() + " (fonte " + e.getSistemaOrigem() + ")");
                        return e;
                    }
                }
                return null;
            }
            case NAO_CONFERIDA -> {
                for (EvidenciaRegistro e : estado.ultimasPorGrupo(c.tipo(), c.chave()).values()) {
                    if (!e.isConferida()) {
                        partes.add(c.tipo() + " " + c.chave() + "=" + e.atributo(c.chave())
                                + " NAO CONFERIDO (fonte " + e.getSistemaOrigem() + ", baixa confianca)");
                        return e;
                    }
                }
                return null;
            }
            default -> {
                return null;
            }
        }
    }
}
