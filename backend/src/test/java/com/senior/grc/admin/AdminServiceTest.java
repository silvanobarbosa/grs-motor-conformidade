package com.senior.grc.admin;

import com.senior.grc.fundacao.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Admin master: autenticacao, criacao de usuarios (so admin) e observabilidade. */
@SpringBootTest
@ActiveProfiles("test")
class AdminServiceTest {

    @Autowired AdminService admin;
    @Autowired JdbcTemplate jdbc;

    @AfterEach
    void limpa() {
        TenantContext.limpar();
        jdbc.update("DELETE FROM usuario_sistema");
        jdbc.update("DELETE FROM trilha_evento");
    }

    private String loginMaster() {
        Optional<Map<String, Object>> r = admin.autenticar(AdminService.LOGIN_ADMIN_MASTER, AdminService.SENHA_ADMIN_MASTER_DEMO);
        return (String) r.orElseThrow().get("token");
    }

    @Test
    void admin_master_autentica_e_senha_errada_falha() {
        TenantContext.definir("t1");
        assertThat(admin.autenticar(AdminService.LOGIN_ADMIN_MASTER, AdminService.SENHA_ADMIN_MASTER_DEMO)).isPresent();
        assertThat(admin.autenticar(AdminService.LOGIN_ADMIN_MASTER, "errada")).isEmpty();
    }

    @Test
    void admin_master_cria_usuario_que_depois_loga() {
        TenantContext.definir("t1");
        String token = loginMaster();
        admin.criarUsuario(token, "ana.tst", "Ana TST", "SenhaForte1", List.of("TST"));
        assertThat(admin.autenticar("ana.tst", "SenhaForte1")).isPresent();
        assertThat(admin.listarUsuarios(token)).extracting(UsuarioSistema::getLogin).contains("admin.master", "ana.tst");
    }

    @Test
    void criar_usuario_sem_token_de_admin_e_negado() {
        TenantContext.definir("t1");
        admin.semearAdminMaster("t1");
        assertThrows(IllegalStateException.class, () -> admin.criarUsuario("token-invalido", "x", "X", "SenhaForte1", List.of("TST")));
    }

    @Test
    void observabilidade_traz_metricas_do_sistema() {
        TenantContext.definir("t1");
        String token = loginMaster();
        Map<String, Object> o = admin.observabilidade(token);
        assertThat(o).containsKeys("eventosTrilha", "trilhaIntegra", "evidencias", "pessoas",
                "usuariosSistema", "riscoPorNivel", "iaLigada", "mcpLigado");
        assertThat(o.get("usuariosSistema")).isEqualTo(1L);
    }

    @Test
    void admin_master_nao_pode_ser_desativado() {
        TenantContext.definir("t1");
        String token = loginMaster();
        var master = admin.listarUsuarios(token).stream().filter(u -> u.getLogin().equals("admin.master")).findFirst().orElseThrow();
        assertThrows(IllegalArgumentException.class, () -> admin.definirAtivo(token, master.getId(), false));
    }
}
