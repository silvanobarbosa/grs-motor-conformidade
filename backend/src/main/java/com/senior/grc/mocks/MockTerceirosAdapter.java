package com.senior.grc.mocks;

import com.senior.grc.demo.Relogio;
import com.senior.grc.fundacao.evidencia.ConfiancaFonte;
import com.senior.grc.fundacao.evidencia.Evidencia;
import com.senior.grc.fundacao.evidencia.PortaDeEntrada;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Mock da gestao de terceiros. Empresas e documentos entram como evidencia (pseudonimoPessoa nulo). */
@Component
public class MockTerceirosAdapter implements TerceirosPort {

    public static final String SISTEMA = "TERCEIROS";

    private final PortaDeEntrada porta;
    private final Relogio relogio;

    public MockTerceirosAdapter(PortaDeEntrada porta, Relogio relogio) {
        this.porta = porta;
        this.relogio = relogio;
    }

    @Override
    public void cadastrarEmpresa(String codigo, String nome, String servico) {
        porta.receber(new Evidencia(null, "EMPRESA_TERCEIRA",
                Map.of("empresa", codigo, "servico", servico),
                "empresa terceira " + nome + " (" + servico + ")", SISTEMA,
                ConfiancaFonte.ALTA, relogio.agora(), null, null));
    }

    @Override
    public void registrarDocumento(String empresaCodigo, String tipoDocumento, Instant validoAte) {
        Map<String, String> attrs = new LinkedHashMap<>();
        attrs.put("empresa", empresaCodigo);
        attrs.put("documento", tipoDocumento);
        attrs.put("status", "VALIDO");
        porta.receber(new Evidencia(null, "DOCUMENTO_TERCEIRO", attrs,
                "documento " + tipoDocumento + " da empresa " + empresaCodigo + " valido ate " + validoAte,
                SISTEMA, ConfiancaFonte.ALTA, relogio.agora(), validoAte, null));
    }

    @Override
    public void registrarAcessoTerceiro(String empresaCodigo, String area) {
        porta.receber(new Evidencia(null, "ACESSO_TERCEIRO",
                Map.of("empresa", empresaCodigo, "area", area),
                "acesso de terceiro da empresa " + empresaCodigo + " na area " + area, SISTEMA,
                ConfiancaFonte.ALTA, relogio.agora(), null, null));
    }
}
