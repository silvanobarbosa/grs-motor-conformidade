package com.senior.grc.risco;

import com.senior.grc.fundacao.evidencia.ConfiancaFonte;
import com.senior.grc.fundacao.evidencia.Evidencia;
import com.senior.grc.fundacao.evidencia.PortaDeEntrada;
import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.fundacao.trilha.TrilhaService;
import com.senior.grc.mocks.MockAcessoAdapter;
import com.senior.grc.mocks.MockRhAdapter;
import com.senior.grc.mocks.MockTerceirosAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Modulo 5: motor de avaliacao — cenarios, travas e explicacao (roteiros R2, R4, R7).
 */
@SpringBootTest
@ActiveProfiles("test")
class RiscoServiceTest {

    @Autowired MockRhAdapter rh;
    @Autowired MockAcessoAdapter acesso;
    @Autowired MockTerceirosAdapter terceiros;
    @Autowired PortaDeEntrada porta;
    @Autowired RiscoService risco;
    @Autowired TrilhaService trilha;
    @Autowired com.senior.grc.demo.RelogioDemo relogio;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void limpa() {
        TenantContext.limpar();
        jdbc.update("DELETE FROM avaliacao_risco");
        jdbc.update("DELETE FROM evidencia_registro");
        jdbc.update("DELETE FROM pessoa");
        jdbc.update("DELETE FROM trilha_evento");
        relogio.reiniciar();
    }

    @Test
    void cracha_ativo_de_desligado_e_critico_com_explicacao() {
        TenantContext.definir("t1");
        String p = rh.admitir("90000000010", "Ficticio", "VIGILANTE", "PORTARIA_NORTE", null);
        acesso.emitirCracha(p);
        rh.desligar(p);

        AvaliacaoRisco r = risco.reavaliarPessoa(p);
        assertThat(r.getNivelResidual()).isEqualTo(NivelRisco.CRITICO);
        assertThat(r.getCenarioDominante()).isEqualTo("CRACHA_DESLIGADO");
        assertThat(r.getExplicacao()).contains("CRACHA").contains("DESLIGAMENTO");
    }

    @Test
    void treinamento_vencendo_depois_vencido_sobe_o_risco() {
        TenantContext.definir("t1");
        String p = rh.admitir("90000000011", "Ficticio", "TECNICO_MANUTENCAO", "PLANTA_SUL",
                Map.of("NR-35", relogio.agora().plus(Duration.ofDays(5))));

        // Em 5 dias de validade: cenario "vencendo em ate 15 dias" -> MEDIO.
        assertThat(risco.reavaliarPessoa(p).getNivelResidual()).isEqualTo(NivelRisco.MEDIO);

        // Avanca o relogio: o treinamento vence -> ALTO (roteiro R2).
        relogio.avancarDias(10);
        AvaliacaoRisco depois = risco.reavaliarPessoa(p);
        assertThat(depois.getNivelResidual()).isEqualTo(NivelRisco.ALTO);
        assertThat(depois.getCenarioDominante()).isEqualTo("TREINAMENTO_VENCIDO");
    }

    @Test
    void terceiro_com_documento_vencido_e_alto() {
        TenantContext.definir("t1");
        terceiros.cadastrarEmpresa("TERC-OBRA", "Ficticia", "obras");
        terceiros.registrarDocumento("TERC-OBRA", "ASO", relogio.agora().minus(Duration.ofDays(1)));

        AvaliacaoRisco r = risco.reavaliarEmpresa("TERC-OBRA");
        assertThat(r.getNivelResidual()).isEqualTo(NivelRisco.ALTO);
        assertThat(r.getCenarioDominante()).isEqualTo("DOC_TERCEIRO_VENCIDO");
    }

    @Test
    void trava_de_confianca_nao_deixa_ir_a_critico_so_com_baixa_confianca() {
        TenantContext.definir("t1");
        String pseudo = "P-BAIXA";
        // Cracha ATIVO e DESLIGAMENTO, ambos de BAIXA confianca: cenario critico, mas trava -> ALTO.
        porta.receber(new Evidencia(pseudo, "CRACHA", Map.of("status", "ATIVO"), "cracha", "ACESSO",
                ConfiancaFonte.BAIXA, relogio.agora(), null, null));
        porta.receber(new Evidencia(pseudo, "DESLIGAMENTO", Map.of("status", "DESLIGADO"), "deslig", "RH",
                ConfiancaFonte.BAIXA, relogio.agora(), null, null));

        AvaliacaoRisco r = risco.reavaliarPessoa(pseudo);
        assertThat(r.getNivelResidual()).isEqualTo(NivelRisco.ALTO); // nao CRITICO
    }

    @Test
    void oscilacao_so_registra_evento_quando_o_nivel_muda() {
        TenantContext.definir("t1");
        String p = rh.admitir("90000000012", "Ficticio", "VIGILANTE", "PORTARIA_SUL", null);
        acesso.emitirCracha(p);
        rh.desligar(p);

        long antes = trilha.listar("t1").stream().filter(e -> e.getTipo().equals("RISCO_AVALIADO")).count();
        risco.reavaliarPessoa(p); // 1a mudanca (SEM_RISCO -> CRITICO)
        risco.reavaliarPessoa(p); // sem mudanca
        risco.reavaliarPessoa(p); // sem mudanca
        long depois = trilha.listar("t1").stream().filter(e -> e.getTipo().equals("RISCO_AVALIADO")).count();
        assertThat(depois - antes).isEqualTo(1);
    }
}
