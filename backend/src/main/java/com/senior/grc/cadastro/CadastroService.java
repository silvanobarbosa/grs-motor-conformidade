package com.senior.grc.cadastro;

import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.fundacao.trilha.Hashing;
import com.senior.grc.fundacao.trilha.TrilhaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cadastro canonico e cofre de identidade (SPEC.md secao 4).
 *
 * - Resolucao de entidades: casa um CPF (so aqui, no cofre) com a pessoa canonica pelo hash.
 * - Pseudonimo estavel derivado do hash do CPF — trilha e evidencias so veem o pseudonimo.
 * - Eliminacao LGPD: quebra a ligacao pseudonimo -> identidade; a trilha continua integra.
 *
 * Toda operacao de identidade deixa rastro na trilha (so pseudonimo, nunca CPF).
 */
@Service
public class CadastroService {

    private final PessoaRepository repo;
    private final TrilhaService trilha;

    public CadastroService(PessoaRepository repo, TrilhaService trilha) {
        this.repo = repo;
        this.trilha = trilha;
    }

    public static String hashCpf(String tenantId, String cpf) {
        return Hashing.sha256("cpf|" + tenantId + "|" + cpf);
    }

    /** Pseudonimo estavel: nunca revela o CPF, mas o mesmo CPF sempre cai no mesmo pseudonimo. */
    public static String pseudonimoDe(String cpfHash) {
        return "P-" + cpfHash.substring(0, 12).toUpperCase();
    }

    /**
     * Registra (ou reaproveita) a pessoa canonica a partir do CPF. Idempotente por CPF: o mesmo CPF
     * resolve sempre para a mesma pessoa. O CPF e usado so para derivar o hash e e descartado aqui.
     */
    @Transactional
    public Pessoa resolverOuCriar(String cpf, String nome, String cargoCodigo, String localCodigo) {
        String tenant = TenantContext.obrigatorio();
        String cpfHash = hashCpf(tenant, cpf);
        Optional<Pessoa> existente = repo.findByTenantIdAndCpfHash(tenant, cpfHash);
        if (existente.isPresent()) {
            Pessoa p = existente.get();
            p.moverPara(cargoCodigo, localCodigo);
            trilha.registrar("PESSOA_ATUALIZADA", "{\"pseudonimo\":\"" + p.getPseudonimo() + "\",\"cargo\":\"" + cargoCodigo + "\",\"local\":\"" + localCodigo + "\"}");
            return repo.save(p);
        }
        String pseudonimo = pseudonimoDe(cpfHash);
        Pessoa nova = new Pessoa(UUID.randomUUID(), tenant, pseudonimo, cpfHash, nome, cargoCodigo, localCodigo);
        Pessoa salva = repo.save(nova);
        trilha.registrar("PESSOA_REGISTRADA", "{\"pseudonimo\":\"" + pseudonimo + "\",\"cargo\":\"" + cargoCodigo + "\",\"local\":\"" + localCodigo + "\"}");
        return salva;
    }

    /** Resolucao de entidades: do CPF de um sistema de origem para o pseudonimo canonico. */
    @Transactional(readOnly = true)
    public Optional<String> pseudonimoPorCpf(String cpf) {
        String tenant = TenantContext.obrigatorio();
        return repo.findByTenantIdAndCpfHash(tenant, hashCpf(tenant, cpf)).map(Pessoa::getPseudonimo);
    }

    /** Eliminacao LGPD (art. 18, VI): identidade irrecuperavel; trilha e indicadores intactos. */
    @Transactional
    public void eliminar(String pseudonimo) {
        String tenant = TenantContext.obrigatorio();
        Pessoa p = repo.findByTenantIdAndPseudonimo(tenant, pseudonimo)
                .orElseThrow(() -> new IllegalArgumentException("pessoa nao encontrada: " + pseudonimo));
        p.eliminar();
        repo.save(p);
        trilha.registrar("PESSOA_ELIMINADA", "{\"pseudonimo\":\"" + pseudonimo + "\"}");
    }

    @Transactional(readOnly = true)
    public List<Pessoa> listar() {
        return repo.findByTenantId(TenantContext.obrigatorio());
    }

    @Transactional(readOnly = true)
    public Optional<Pessoa> porPseudonimo(String pseudonimo) {
        return repo.findByTenantIdAndPseudonimo(TenantContext.obrigatorio(), pseudonimo);
    }

    @Transactional(readOnly = true)
    public boolean existeAtivoComCargoLocal(String cargo, String local) {
        return repo.existsByTenantIdAndCargoCodigoAndLocalCodigoAndEliminadaFalse(TenantContext.obrigatorio(), cargo, local);
    }

    @Transactional(readOnly = true)
    public boolean existeAtivoComCargo(String cargo) {
        return repo.existsByTenantIdAndCargoCodigoAndEliminadaFalse(TenantContext.obrigatorio(), cargo);
    }
}
