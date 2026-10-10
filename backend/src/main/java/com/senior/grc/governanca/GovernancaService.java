package com.senior.grc.governanca;

import com.senior.grc.cadastro.CadastroService;
import com.senior.grc.cadastro.Pessoa;
import com.senior.grc.demo.Relogio;
import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.fundacao.trilha.TrilhaService;
import com.senior.grc.mocks.BpmPort;
import com.senior.grc.mocks.GedPort;
import com.senior.grc.mocks.SignPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Governanca (SPEC.md secao 10): politica -> aprovacao (BPM) -> publicacao versionada (GED) ->
 * ciencia por cargo+local (Sign) -> pendencias. Decisoes de BPM (aprovar politica, aceitar risco)
 * deixam rastro na trilha. Aceitar risco = excecao humana que trava a abertura de plano (acoes).
 */
@Service
public class GovernancaService {

    private final PoliticaRepository politicas;
    private final CienciaRepository ciencias;
    private final ExcecaoRiscoRepository excecoes;
    private final GedPort ged;
    private final SignPort sign;
    private final BpmPort bpm;
    private final CadastroService cadastro;
    private final TrilhaService trilha;
    private final Relogio relogio;

    public GovernancaService(PoliticaRepository politicas, CienciaRepository ciencias, ExcecaoRiscoRepository excecoes,
                             GedPort ged, SignPort sign, BpmPort bpm, CadastroService cadastro,
                             TrilhaService trilha, Relogio relogio) {
        this.politicas = politicas;
        this.ciencias = ciencias;
        this.excecoes = excecoes;
        this.ged = ged;
        this.sign = sign;
        this.bpm = bpm;
        this.cadastro = cadastro;
        this.trilha = trilha;
        this.relogio = relogio;
    }

    @Transactional
    public Politica criarPolitica(String codigo, String titulo, String cargoAlvo, String localAlvo) {
        String tenant = TenantContext.obrigatorio();
        return politicas.save(new Politica(UUID.randomUUID(), tenant, codigo, titulo, 1, cargoAlvo, localAlvo));
    }

    @Transactional
    public Politica publicarPolitica(String codigo, String aprovadoPor) {
        String tenant = TenantContext.obrigatorio();
        Politica p = politicas.findByTenantIdAndCodigo(tenant, codigo)
                .orElseThrow(() -> new IllegalArgumentException("politica nao encontrada: " + codigo));
        bpm.decidir(BpmPort.TipoDecisao.APROVAR_POLITICA, aprovadoPor, "aprovacao da politica", codigo);
        String hash = ged.hashDaVersao(codigo, p.getVersao());
        p.publicar(hash, relogio.agora());
        politicas.save(p);
        trilha.registrar("POLITICA_PUBLICADA",
                "{\"politica\":\"" + codigo + "\",\"versao\":" + p.getVersao() + ",\"por\":\"" + aprovadoPor + "\"}");
        return p;
    }

    @Transactional
    public void registrarCiencia(String codigo, String pseudonimo) {
        String tenant = TenantContext.obrigatorio();
        Politica p = politicas.findByTenantIdAndCodigo(tenant, codigo)
                .filter(x -> x.getStatus() == Politica.Status.PUBLICADA)
                .orElseThrow(() -> new IllegalArgumentException("politica nao publicada: " + codigo));
        sign.assinarCiencia(pseudonimo, codigo, p.getVersao());
        ciencias.save(new CienciaRegistro(UUID.randomUUID(), tenant, codigo, p.getVersao(), pseudonimo, relogio.agora()));
        trilha.registrar("CIENCIA_REGISTRADA",
                "{\"politica\":\"" + codigo + "\",\"versao\":" + p.getVersao() + ",\"pseudonimo\":\"" + pseudonimo + "\"}");
    }

    /** Pessoas alvo (cargo+local) que ainda nao deram ciencia da versao publicada. */
    @Transactional(readOnly = true)
    public List<String> pendenciasCiencia(String codigo) {
        String tenant = TenantContext.obrigatorio();
        Politica p = politicas.findByTenantIdAndCodigo(tenant, codigo)
                .filter(x -> x.getStatus() == Politica.Status.PUBLICADA)
                .orElseThrow(() -> new IllegalArgumentException("politica nao publicada: " + codigo));
        Set<String> cientes = ciencias.findByTenantIdAndPoliticaCodigoAndVersao(tenant, codigo, p.getVersao())
                .stream().map(CienciaRegistro::getPseudonimo).collect(Collectors.toSet());
        return cadastro.ativosPorCargoLocal(p.getCargoAlvo(), p.getLocalAlvo()).stream()
                .map(Pessoa::getPseudonimo)
                .filter(ps -> !cientes.contains(ps))
                .toList();
    }

    @Transactional
    public void aceitarRisco(String sujeitoId, String causa, String justificativa, String aprovadoPor) {
        if (justificativa == null || justificativa.isBlank()) {
            throw new IllegalArgumentException("aceitar risco exige justificativa (ISO 31000 6.4.4)");
        }
        String tenant = TenantContext.obrigatorio();
        bpm.decidir(BpmPort.TipoDecisao.ACEITAR_RISCO, aprovadoPor, justificativa, sujeitoId + "/" + causa);
        excecoes.save(new ExcecaoRisco(UUID.randomUUID(), tenant, sujeitoId, causa, justificativa, aprovadoPor, relogio.agora()));
        trilha.registrar("RISCO_ACEITO",
                "{\"sujeito\":\"" + sujeitoId + "\",\"causa\":\"" + causa + "\",\"por\":\"" + aprovadoPor + "\"}");
    }

    @Transactional(readOnly = true)
    public boolean riscoAceito(String tenantId, String sujeitoId, String causa) {
        return excecoes.findByTenantIdAndSujeitoIdAndCausa(tenantId, sujeitoId, causa).isPresent();
    }

    @Transactional(readOnly = true)
    public List<Politica> politicas() {
        return politicas.findByTenantId(TenantContext.obrigatorio());
    }
}
