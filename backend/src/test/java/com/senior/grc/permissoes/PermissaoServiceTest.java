package com.senior.grc.permissoes;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Modulo 8: permissoes do core — escopo, categoria, precedencia (origem so restringe), falha
 * segura e segregacao. Criterio da DEMO.md 7: "permissoes do core nao sao ampliadas pela origem".
 */
class PermissaoServiceTest {

    private final PermissaoService perm = new PermissaoService();

    private ContextoAcesso gestorPlanta(String... locais) {
        return new ContextoAcesso("u", Set.of(PermissaoService.GESTOR_PLANTA), List.of(locais), Set.of(), true);
    }

    @Test
    void gestor_de_planta_ve_seu_local_e_nao_ve_outro() {
        ContextoAcesso ctx = gestorPlanta("PLANTA_NORTE");
        assertThat(perm.podeVer(ctx, "PLANTA_NORTE", CategoriaDado.NORMAL)).isTrue();
        assertThat(perm.podeVer(ctx, "PLANTA_SUL", CategoriaDado.NORMAL)).isFalse();
    }

    @Test
    void auditor_externo_nunca_ve_sensivel() {
        ContextoAcesso auditor = new ContextoAcesso("a", Set.of(PermissaoService.AUDITOR_EXTERNO), List.of(), Set.of(), true);
        assertThat(perm.podeVer(auditor, "PLANTA_NORTE", CategoriaDado.NORMAL)).isTrue();
        assertThat(perm.podeVer(auditor, "PLANTA_NORTE", CategoriaDado.SENSIVEL)).isFalse();
    }

    @Test
    void origem_so_restringe_nunca_amplia() {
        // Core permite (gestor da planta no seu local), mas a origem NEGA aquele local -> nao ve.
        ContextoAcesso negadoNaOrigem = new ContextoAcesso("u", Set.of(PermissaoService.GESTOR_PLANTA),
                List.of("PLANTA_NORTE"), Set.of("PLANTA_NORTE"), true);
        assertThat(perm.podeVer(negadoNaOrigem, "PLANTA_NORTE", CategoriaDado.NORMAL)).isFalse();

        // Core NEGA (fora do escopo); origem "permitir" nao amplia (nem ha como; origem so nega).
        ContextoAcesso foraDoEscopo = gestorPlanta("PLANTA_NORTE");
        assertThat(perm.podeVer(foraDoEscopo, "PLANTA_SUL", CategoriaDado.NORMAL)).isFalse();
    }

    @Test
    void falha_segura_bloqueia_sensivel_sem_sincronizacao() {
        ContextoAcesso keyUserSemSync = new ContextoAcesso("k", Set.of(PermissaoService.KEY_USER), List.of(), Set.of(), false);
        assertThat(perm.podeVer(keyUserSemSync, "PLANTA_NORTE", CategoriaDado.NORMAL)).isTrue();
        assertThat(perm.podeVer(keyUserSemSync, "PLANTA_NORTE", CategoriaDado.SENSIVEL)).isFalse();
    }

    @Test
    void classifica_saude_como_sensivel() {
        assertThat(perm.classificar("DOCUMENTO_TERCEIRO", "ASO")).isEqualTo(CategoriaDado.SENSIVEL);
        assertThat(perm.classificar("TREINAMENTO", null)).isEqualTo(CategoriaDado.NORMAL);
    }

    @Test
    void segregacao_impede_aprovar_a_propria_decisao() {
        assertThrows(IllegalArgumentException.class, () -> perm.garantirSegregacao("u1", "u1"));
        assertDoesNotThrow(() -> perm.garantirSegregacao("u1", "u2"));
    }
}
