package com.senior.grc.mocks;

import java.time.Instant;

/**
 * Porta da gestao de terceiros (empresas terceiras, documentos, status). Mock na demo.
 * Alimenta o cenario "terceiro com documentacao vencida acessando a planta".
 */
public interface TerceirosPort {

    void cadastrarEmpresa(String codigo, String nome, String servico);

    void registrarDocumento(String empresaCodigo, String tipoDocumento, Instant validoAte);

    void registrarAcessoTerceiro(String empresaCodigo, String area);
}
