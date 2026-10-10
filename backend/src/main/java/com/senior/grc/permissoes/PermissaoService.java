package com.senior.grc.permissoes;

import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Permissoes do CORE (SPEC.md secao 12): escopo de dados (papel + local), categoria do dado,
 * precedencia (core e a base; heranca da origem SO RESTRINGE) e segregacao de funcoes.
 *
 * Processar != exibir: o motor processa tudo; esta camada vale so na exibicao (telas, API, MCP,
 * exportacoes) — CLAUDE.md regra 7.
 */
@Service
public class PermissaoService {

    public static final String KEY_USER = "KEY_USER";
    public static final String GESTOR_RISCOS = "GESTOR_RISCOS";
    public static final String GESTOR_PLANTA = "GESTOR_PLANTA";
    public static final String AUDITOR_EXTERNO = "AUDITOR_EXTERNO";

    private static final Set<String> PAPEIS_SENSIVEL = Set.of(KEY_USER, GESTOR_RISCOS);

    /** Tipos de evidencia que revelam saude -> SENSIVEL. */
    private static final Set<String> TIPOS_SENSIVEIS = Set.of("ASO", "EXAME", "SAUDE", "ATESTADO");

    public CategoriaDado classificar(String tipoEvidencia, String documento) {
        if (tipoEvidencia != null && TIPOS_SENSIVEIS.contains(tipoEvidencia)) {
            return CategoriaDado.SENSIVEL;
        }
        if (documento != null && TIPOS_SENSIVEIS.contains(documento)) {
            return CategoriaDado.SENSIVEL;
        }
        return CategoriaDado.NORMAL;
    }

    /**
     * Pode VER um recurso de um dado local e categoria?
     * Precedencia: decide o core; a origem so restringe (Permite+Nega = nao ve).
     */
    public boolean podeVer(ContextoAcesso ctx, String localDoRecurso, CategoriaDado categoria) {
        boolean corePermite = coreDecide(ctx, localDoRecurso, categoria);
        if (!corePermite) {
            return false;
        }
        // Heranca da origem: so restringe. Se a origem NEGA aquele local, nao ve.
        if (localDoRecurso != null && ctx.origemNega().contains(localDoRecurso)) {
            return false;
        }
        // Falha segura: sem sincronizacao com a origem, categoria sensivel bloqueada.
        if (categoria == CategoriaDado.SENSIVEL && !ctx.origemSincronizada()) {
            return false;
        }
        return true;
    }

    private boolean coreDecide(ContextoAcesso ctx, String localDoRecurso, CategoriaDado categoria) {
        // Categoria sensivel: so papeis autorizados; auditor externo nunca.
        if (categoria == CategoriaDado.SENSIVEL) {
            if (ctx.temPapel(AUDITOR_EXTERNO)) {
                return false;
            }
            if (ctx.papeis().stream().noneMatch(PAPEIS_SENSIVEL::contains)) {
                return false;
            }
        }
        // Escopo de local: key user, gestor de riscos e auditor externo veem todos os locais;
        // gestor de planta ve so os locais do seu escopo.
        if (ctx.temPapel(KEY_USER) || ctx.temPapel(GESTOR_RISCOS) || ctx.temPapel(AUDITOR_EXTERNO)) {
            return true;
        }
        if (ctx.temPapel(GESTOR_PLANTA)) {
            return localDoRecurso == null || ctx.locaisPermitidos().contains(localDoRecurso);
        }
        return false;
    }

    /** Segregacao de funcoes (SPEC.md 12): ninguem aprova a propria decisao. */
    public void garantirSegregacao(String autorDaDecisao, String aprovador) {
        if (autorDaDecisao != null && autorDaDecisao.equals(aprovador)) {
            throw new IllegalArgumentException("segregacao de funcoes: ninguem aprova a propria decisao");
        }
    }
}
