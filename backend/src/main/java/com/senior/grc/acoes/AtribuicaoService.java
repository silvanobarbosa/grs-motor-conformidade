package com.senior.grc.acoes;

import com.senior.grc.cadastro.CadastroService;
import com.senior.grc.risco.Cenario;
import org.springframework.stereotype.Service;

/**
 * Atribuicao do dono do risco POR REGRA (cargo + local), nunca por nome (SPEC.md 7; ISO 31000
 * 5.4.3). Guardar o papel (e nao a pessoa) faz a reatribuicao ser automatica: quem ocupa o cargo+
 * local naquele momento e o dono. Se ninguem ocupa, o risco vai ao gestor de riscos, marcado
 * {@code semDono} para destaque no dashboard.
 */
@Service
public class AtribuicaoService {

    public static final String GESTOR_RISCOS = "GESTOR_RISCOS";
    public static final String GESTOR_PLANTA = "GESTOR_PLANTA";
    public static final String GESTOR_SEGURANCA = "GESTOR_SEGURANCA_PATRIMONIAL";

    private final CadastroService cadastro;

    public AtribuicaoService(CadastroService cadastro) {
        this.cadastro = cadastro;
    }

    public record Dono(String cargo, String local, boolean semDono) {
    }

    /** Dono do risco de uma pessoa: gestor da planta dela; sem ele, gestor de riscos (destacado). */
    public Dono donoDePessoa(String pseudonimo) {
        String local = cadastro.porPseudonimo(pseudonimo).map(p -> p.getLocalCodigo()).orElse(null);
        if (local != null && cadastro.existeAtivoComCargoLocal(GESTOR_PLANTA, local)) {
            return new Dono(GESTOR_PLANTA, local, false);
        }
        boolean temGestorRiscos = cadastro.existeAtivoComCargo(GESTOR_RISCOS);
        return new Dono(GESTOR_RISCOS, null, !temGestorRiscos);
    }

    /** Dono do risco de um terceiro: gestor de seguranca patrimonial; sem ele, gestor de riscos. */
    public Dono donoDeEmpresa() {
        if (cadastro.existeAtivoComCargo(GESTOR_SEGURANCA)) {
            return new Dono(GESTOR_SEGURANCA, null, false);
        }
        boolean temGestorRiscos = cadastro.existeAtivoComCargo(GESTOR_RISCOS);
        return new Dono(GESTOR_RISCOS, null, !temGestorRiscos);
    }

    public Dono donoDe(Cenario.Escopo escopo, String sujeitoId) {
        return escopo == Cenario.Escopo.PESSOA ? donoDePessoa(sujeitoId) : donoDeEmpresa();
    }
}
