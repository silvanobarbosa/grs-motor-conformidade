package com.senior.grc.ocr;

import com.senior.grc.fundacao.evidencia.EvidenciaRegistro;
import com.senior.grc.fundacao.evidencia.EvidenciaRepository;
import com.senior.grc.fundacao.tenant.TenantContext;
import com.senior.grc.fundacao.trilha.TrilhaService;
import com.senior.grc.risco.RiscoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Conferencia de evidencias de baixa confianca (SPEC.md secoes 6 e 15; roteiro R4). Enquanto nao
 * conferidas, nao reduzem/validam o risco; apos a conferencia humana, valem e o risco e recalculado.
 */
@Service
public class ConferenciaService {

    private final EvidenciaRepository evidencias;
    private final RiscoService risco;
    private final TrilhaService trilha;

    public ConferenciaService(EvidenciaRepository evidencias, RiscoService risco, TrilhaService trilha) {
        this.evidencias = evidencias;
        this.risco = risco;
        this.trilha = trilha;
    }

    @Transactional(readOnly = true)
    public List<EvidenciaRegistro> pendentes() {
        return evidencias.findByTenantIdAndConferidaFalse(TenantContext.obrigatorio());
    }

    @Transactional
    public void conferir(UUID evidenciaId, String conferidoPor) {
        EvidenciaRegistro e = evidencias.findById(evidenciaId)
                .orElseThrow(() -> new IllegalArgumentException("evidencia nao encontrada"));
        e.conferir();
        evidencias.save(e);
        trilha.registrar("EVIDENCIA_CONFERIDA",
                "{\"evidencia\":\"" + evidenciaId + "\",\"por\":\"" + conferidoPor + "\"}");
        // Recalcula o risco do sujeito afetado.
        if (e.getPseudonimoPessoa() != null) {
            risco.reavaliarPessoa(e.getPseudonimoPessoa());
        } else {
            String empresa = e.atributo("empresa");
            if (empresa != null) {
                risco.reavaliarEmpresa(empresa);
            }
        }
    }
}
