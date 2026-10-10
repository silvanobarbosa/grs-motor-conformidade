package com.senior.grc.fundacao.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Preenche o {@link TenantContext} a partir do cabecalho {@code X-Tenant-Id} e limpa ao fim da
 * requisicao. Na demo a identidade/autenticacao vem do mock da License; aqui so amarramos o tenant
 * ao thread. Rotas publicas (health) seguem sem tenant.
 */
@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Tenant-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String tenant = request.getHeader(HEADER);
        try {
            if (tenant != null && !tenant.isBlank()) {
                TenantContext.definir(tenant.trim());
            }
            chain.doFilter(request, response);
        } finally {
            TenantContext.limpar();
        }
    }
}
