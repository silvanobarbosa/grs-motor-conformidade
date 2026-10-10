package com.senior.grc.admin;

import com.senior.grc.acoes.PlanoDeAcao;
import com.senior.grc.acoes.PlanoDeAcaoRepository;
import com.senior.grc.cadastro.CadastroService;
import com.senior.grc.demo.Relogio;
import com.senior.grc.fundacao.evidencia.EvidenciaRepository;
import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.fundacao.trilha.Hashing;
import com.senior.grc.fundacao.trilha.TrilhaEventoRepository;
import com.senior.grc.fundacao.trilha.TrilhaService;
import com.senior.grc.ia.SaraService;
import com.senior.grc.indicadores.IndicadoresService;
import com.senior.grc.mcp.McpService;
import com.senior.grc.risco.AvaliacaoRisco;
import com.senior.grc.risco.AvaliacaoRiscoRepository;
import com.senior.grc.risco.NivelRisco;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Administracao do sistema: admin master, gestao de usuarios e observabilidade.
 *
 * - O admin master e semeado por tenant (login {@code admin.master}); so ele cria outros usuarios.
 * - Autenticacao simples da demo: senha com hash+sal; login devolve um TOKEN (em memoria) que as
 *   rotas admin exigem no cabecalho X-Admin-Token. (Em producao, a License/SSO da Senior.)
 * - Toda acao administrativa deixa rastro na trilha.
 */
@Service
public class AdminService {

    public static final String PAPEL_ADMIN_MASTER = "ADMIN_MASTER";
    public static final String LOGIN_ADMIN_MASTER = "admin.master";
    public static final String SENHA_ADMIN_MASTER_DEMO = "AdminMaster@2026";

    private final UsuarioSistemaRepository usuarios;
    private final TrilhaService trilha;
    private final Relogio relogio;

    // Observabilidade
    private final TrilhaEventoRepository eventos;
    private final EvidenciaRepository evidencias;
    private final AvaliacaoRiscoRepository avaliacoes;
    private final PlanoDeAcaoRepository planos;
    private final CadastroService cadastro;
    private final IndicadoresService indicadores;
    private final SaraService sara;
    private final McpService mcp;

    /** token -> sessao (em memoria; some no restart — demo). */
    private final Map<String, Sessao> sessoes = new ConcurrentHashMap<>();

    public AdminService(UsuarioSistemaRepository usuarios, TrilhaService trilha, Relogio relogio,
                        TrilhaEventoRepository eventos, EvidenciaRepository evidencias,
                        AvaliacaoRiscoRepository avaliacoes, PlanoDeAcaoRepository planos,
                        CadastroService cadastro, IndicadoresService indicadores, SaraService sara, McpService mcp) {
        this.usuarios = usuarios;
        this.trilha = trilha;
        this.relogio = relogio;
        this.eventos = eventos;
        this.evidencias = evidencias;
        this.avaliacoes = avaliacoes;
        this.planos = planos;
        this.cadastro = cadastro;
        this.indicadores = indicadores;
        this.sara = sara;
        this.mcp = mcp;
    }

    public record Sessao(String tenantId, String login, String nome, List<String> papeis) {
    }

    private static String hash(String sal, String senha) {
        return Hashing.sha256(sal + "|" + senha);
    }

    /** Semeia o admin master do tenant (idempotente). */
    @Transactional
    public void semearAdminMaster(String tenantId) {
        if (usuarios.findByTenantIdAndLogin(tenantId, LOGIN_ADMIN_MASTER).isPresent()) {
            return;
        }
        String sal = UUID.randomUUID().toString();
        usuarios.save(new UsuarioSistema(UUID.randomUUID(), tenantId, LOGIN_ADMIN_MASTER,
                "Administrador Master", hash(sal, SENHA_ADMIN_MASTER_DEMO), sal, PAPEL_ADMIN_MASTER, relogio.agora()));
    }

    /** Login: devolve token se as credenciais baterem e o usuario estiver ativo. */
    @Transactional
    public Optional<Map<String, Object>> autenticar(String login, String senha) {
        String tenant = TenantContext.obrigatorio();
        semearAdminMaster(tenant); // garante o admin master mesmo sem seed de demo
        return usuarios.findByTenantIdAndLogin(tenant, login)
                .filter(UsuarioSistema::isAtivo)
                .filter(u -> u.getSenhaHash().equals(hash(u.getSal(), senha)))
                .map(u -> {
                    String token = UUID.randomUUID().toString().replace("-", "");
                    sessoes.put(token, new Sessao(tenant, u.getLogin(), u.getNome(), u.papeisLista()));
                    trilha.registrar("ADMIN_LOGIN", "{\"login\":\"" + login + "\"}");
                    return Map.<String, Object>of("token", token, "nome", u.getNome(), "papeis", u.papeisLista());
                });
    }

    public Optional<Sessao> sessao(String token) {
        return Optional.ofNullable(token).map(sessoes::get);
    }

    /** Exige token valido de ADMIN_MASTER no tenant corrente. */
    private Sessao exigirAdminMaster(String token) {
        Sessao s = sessao(token).orElseThrow(() -> new IllegalStateException("sessao invalida"));
        if (!s.tenantId().equals(TenantContext.obrigatorio()) || !s.papeis().contains(PAPEL_ADMIN_MASTER)) {
            throw new IllegalStateException("acesso restrito ao admin master");
        }
        return s;
    }

    @Transactional
    public UsuarioSistema criarUsuario(String token, String login, String nome, String senha, List<String> papeis) {
        exigirAdminMaster(token);
        String tenant = TenantContext.obrigatorio();
        if (usuarios.findByTenantIdAndLogin(tenant, login).isPresent()) {
            throw new IllegalArgumentException("login ja existe: " + login);
        }
        if (senha == null || senha.length() < 8) {
            throw new IllegalArgumentException("senha minima de 8 caracteres");
        }
        String sal = UUID.randomUUID().toString();
        UsuarioSistema u = new UsuarioSistema(UUID.randomUUID(), tenant, login, nome, hash(sal, senha), sal,
                String.join(",", papeis == null || papeis.isEmpty() ? List.of("GESTOR_RISCOS") : papeis), relogio.agora());
        usuarios.save(u);
        trilha.registrar("USUARIO_CRIADO", "{\"login\":\"" + login + "\",\"papeis\":\"" + u.getPapeis() + "\"}");
        return u;
    }

    @Transactional(readOnly = true)
    public List<UsuarioSistema> listarUsuarios(String token) {
        exigirAdminMaster(token);
        return usuarios.findByTenantId(TenantContext.obrigatorio());
    }

    @Transactional
    public void definirAtivo(String token, UUID usuarioId, boolean ativo) {
        exigirAdminMaster(token);
        UsuarioSistema u = usuarios.findById(usuarioId).orElseThrow(() -> new IllegalArgumentException("usuario nao encontrado"));
        if (u.getLogin().equals(LOGIN_ADMIN_MASTER)) {
            throw new IllegalArgumentException("o admin master nao pode ser desativado");
        }
        u.definirAtivo(ativo);
        usuarios.save(u);
        trilha.registrar("USUARIO_" + (ativo ? "ATIVADO" : "DESATIVADO"), "{\"login\":\"" + u.getLogin() + "\"}");
    }

    /** Painel de observabilidade do sistema inteiro (so admin). */
    @Transactional(readOnly = true)
    public Map<String, Object> observabilidade(String token) {
        exigirAdminMaster(token);
        String tenant = TenantContext.obrigatorio();

        Map<NivelRisco, Long> porNivel = new LinkedHashMap<>();
        for (AvaliacaoRisco a : avaliacoes.findByTenantId(tenant)) {
            porNivel.merge(a.getNivelResidual(), 1L, Long::sum);
        }
        List<PlanoDeAcao> ps = planos.findByTenantId(tenant);
        long abertos = ps.stream().filter(p -> p.getStatus() == PlanoDeAcao.Status.ABERTO).count();

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("relogio", relogio.agora());
        out.put("eventosTrilha", eventos.countByTenantId(tenant));
        out.put("trilhaIntegra", trilha.verificarIntegridade(tenant).integra());
        out.put("evidencias", evidencias.countByTenantId(tenant));
        out.put("evidenciasNaoConferidas", evidencias.findByTenantIdAndConferidaFalse(tenant).size());
        out.put("pessoas", cadastro.listar().size());
        out.put("usuariosSistema", usuarios.countByTenantId(tenant));
        out.put("planosAbertos", abertos);
        out.put("planosConcluidos", ps.size() - abertos);
        out.put("riscoPorNivel", porNivel);
        out.put("atualidadePorFonte", indicadores.atualidadePorFonte());
        out.put("cobertura", indicadores.cobertura());
        out.put("iaLigada", sara.iaLigada());
        out.put("mcpLigado", mcp.estaLigado());
        return out;
    }
}
