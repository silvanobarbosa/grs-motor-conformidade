package com.senior.grc.demo;

import com.senior.grc.mocks.MockAcessoAdapter;
import com.senior.grc.mocks.MockRhAdapter;
import com.senior.grc.mocks.MockTerceirosAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Popula o cliente ficticio "Metalurgica Exemplo S.A." (DEMO.md secao 2): duas plantas, areas,
 * colaboradores por cargo, treinamentos (alguns proximos do vencimento), crachas e 4 terceiras com
 * documentos (um vencido). Tudo entra pela porta de entrada, via mocks — nada direto no core.
 *
 * Todos os dados sao FICTICIOS e marcados como dados de teste. CPFs gerados nao sao reais.
 */
@Component
public class SeedDemo {

    public static final String TENANT = "metalurgica-exemplo";

    private final MockRhAdapter rh;
    private final MockAcessoAdapter acesso;
    private final MockTerceirosAdapter terceiros;
    private final RelogioDemo relogio;

    public SeedDemo(MockRhAdapter rh, MockAcessoAdapter acesso, MockTerceirosAdapter terceiros, RelogioDemo relogio) {
        this.rh = rh;
        this.acesso = acesso;
        this.terceiros = terceiros;
        this.relogio = relogio;
    }

    private record Perfil(String cargo, List<String> normas) {
    }

    private static final List<Perfil> PERFIS = List.of(
            new Perfil("VIGILANTE", List.of("NR-6")),
            new Perfil("OPERADOR_PRODUCAO", List.of("NR-6")),
            new Perfil("ELETRICISTA", List.of("NR-10", "NR-6")),
            new Perfil("TECNICO_MANUTENCAO", List.of("NR-35", "NR-6")),
            new Perfil("TST", List.of("NR-6")),
            new Perfil("GESTOR_SEGURANCA_PATRIMONIAL", List.of()),
            new Perfil("GESTOR_PLANTA", List.of()),
            new Perfil("GESTOR_RISCOS", List.of()),
            new Perfil("ANALISTA_RH", List.of())
    );

    private static final List<String> PLANTAS = List.of("PLANTA_NORTE", "PLANTA_SUL");

    /** Semeia o cliente ficticio. {@code colaboradores} controla o volume (demo ~120). */
    @Transactional
    public Resumo semear(int colaboradores) {
        Instant base = relogio.agora();
        int pessoas = 0;
        for (int i = 0; i < colaboradores; i++) {
            Perfil perfil = PERFIS.get(i % PERFIS.size());
            String planta = PLANTAS.get(i % PLANTAS.size());
            String cpf = String.format("%011d", 90000000000L + i);
            String nome = "Colaborador Ficticio " + String.format("%03d", i + 1);

            // Treinamentos: a maioria valida por ~1 ano; alguns de NR-35 vencem em 10 dias (para R2).
            Instant validadePadrao = base.plus(Duration.ofDays(365));
            boolean venceLogo = perfil.normas().contains("NR-35") && (i % 7 == 0);
            Map<String, Instant> treinos = new java.util.LinkedHashMap<>();
            for (String norma : perfil.normas()) {
                Instant ate = "NR-35".equals(norma) && venceLogo ? base.plus(Duration.ofDays(10)) : validadePadrao;
                treinos.put(norma, ate);
            }
            // Espaco confinado so na Planta Norte: alguns operadores com NR-33.
            if ("PLANTA_NORTE".equals(planta) && "OPERADOR_PRODUCAO".equals(perfil.cargo()) && i % 5 == 0) {
                treinos.put("NR-33", validadePadrao);
            }

            String pseudonimo = rh.admitir(cpf, nome, perfil.cargo(), planta, treinos);
            acesso.emitirCracha(pseudonimo);
            pessoas++;
        }

        // 4 empresas terceiras; a de obras civis entra com ASO VENCIDO (cenario 3 / R4).
        terceiros.cadastrarEmpresa("TERC-VIG", "Vigilancia Ficticia Ltda", "vigilancia");
        terceiros.cadastrarEmpresa("TERC-LIMP", "Limpeza Ficticia Ltda", "limpeza");
        terceiros.cadastrarEmpresa("TERC-ELET", "Manutencao Eletrica Ficticia Ltda", "manutencao_eletrica");
        terceiros.cadastrarEmpresa("TERC-OBRA", "Obras Civis Ficticia Ltda", "obras_civis");
        terceiros.registrarDocumento("TERC-VIG", "CND", base.plus(Duration.ofDays(180)));
        terceiros.registrarDocumento("TERC-LIMP", "CND", base.plus(Duration.ofDays(180)));
        terceiros.registrarDocumento("TERC-ELET", "ASO", base.plus(Duration.ofDays(90)));
        terceiros.registrarDocumento("TERC-OBRA", "ASO", base.minus(Duration.ofDays(5))); // VENCIDO

        return new Resumo(pessoas, 4);
    }

    public record Resumo(int colaboradores, int terceiras) {
    }
}
