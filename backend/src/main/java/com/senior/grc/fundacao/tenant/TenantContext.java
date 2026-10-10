package com.senior.grc.fundacao.tenant;

/**
 * Contexto do tenant corrente (multi-tenant desde o inicio — CLAUDE.md, regra 10).
 *
 * Todo dado do core carrega {@code tenantId}; o catalogo regulatorio e global e somente leitura
 * (tratado em outro modulo). Aqui guardamos o tenant da requisicao num ThreadLocal, preenchido
 * pelo {@link TenantFilter} a partir do cabecalho, para que servicos e repositorios saibam a qual
 * cliente a operacao pertence sem passar o id em toda assinatura.
 */
public final class TenantContext {

    private static final ThreadLocal<String> ATUAL = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void definir(String tenantId) {
        ATUAL.set(tenantId);
    }

    /** Tenant corrente. Lanca se nao houver — nenhuma operacao do core roda sem tenant. */
    public static String obrigatorio() {
        String t = ATUAL.get();
        if (t == null || t.isBlank()) {
            throw new IllegalStateException("Sem tenant no contexto: toda operacao do core exige tenant.");
        }
        return t;
    }

    public static String atualOuNulo() {
        return ATUAL.get();
    }

    public static void limpar() {
        ATUAL.remove();
    }
}
