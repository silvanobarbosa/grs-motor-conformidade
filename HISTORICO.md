# Histórico de evolução — Motor GRC

Diário do produto: o que mudou, por quê, e o que ficou pendente. Mais recente em cima.
As **regras** do repo estão no `AGENTS.md` e no `CLAUDE.md`; a especificação em `docs/`.

Quem abre este app lê este arquivo antes de propor trabalho.

O que é: core único de risco, compliance e governança (ISO 31000 / ISO 37301), versão de
demonstração com dados mock, padrão Senior. Backend Java/Spring Boot (monólito modular),
frontend Angular/PrimeNG (a entrar), Postgres/Neon, servidor MCP (a entrar).

---

## 2026-10-10 — Backlog completo: módulos 2–14 (demo funcional de ponta a ponta)

**Entregue:** o backend da demo COMPLETO, os 14 módulos do backlog (`docs/DEMO.md`), com **73 testes
verdes** (BUILD SUCCESS) e os **12 roteiros R1–R12 automatizados de ponta a ponta**.

- **2. Cadastro/cofre de identidade** — CPF só como hash; pseudônimo estável; eliminação LGPD mantém
  a trilha íntegra.
- **3. Mocks (RH/Acesso/Terceiros) + simulador de tempo + seed** — tudo entra pela porta de entrada;
  read-model de evidência com tipo+atributos estruturados.
- **4. Catálogo regulatório versionado + aplicabilidade** — consulta por data; obrigatório/requer
  confirmação/não aplicável (justificativa + responsável técnico).
- **5. Motor de avaliação** — `RuleEnginePort` + avaliador simples; cenários declarativos; risco
  residual derivado; travas de confiança/oscilação/explicação.
- **6. Ações e donos** — plano por risco+causa (não duplica), dono por regra cargo+local; ciclo
  fechado; evento `RiscoMudou` desacopla risco de ações.
- **7. Governança** — política→aprovação BPM→publicação versionada→ciência por cargo+local;
  aceitar risco = exceção humana que trava o plano. Mocks GED/Sign/BPM.
- **8. Permissões** — escopo+categoria, precedência (origem só restringe), falha segura, segregação.
- **9. Indicadores** — operacional/gestão/executivo + cobertura + atualidade; drill-down.
- **10. Acesso externo + dossiê** — temporário, só leitura, pseudonimizado, com prova de integridade.
- **11. OCR mock + conferência** — baixa confiança não reduz risco até conferência (R4).
- **12. Sara (IA)** — triagem/explicação; IA nunca decide; funciona desligada.
- **13. Servidor MCP** — resources + tools (leitura, escrita com confirmação), proibidos negados,
  desligado por padrão, tudo na trilha; o transporte JSON-RPC/OAuth pluga por cima.
- **14. Roteiros R1–R12** — acceptance e2e + critérios de pronto.

**Decisões que ficam valendo:**
- Toda evidência (inclusive mocks) entra pela `PortaDeEntrada`; o read-model `EvidenciaRegistro` é
  reconstruível a partir da trilha.
- Risco residual é SEMPRE derivado (`RiscoService`); mudança publica `RiscoMudou` (ApplicationEvent),
  consumido por ações — sem dependência circular.
- "0 confiança" das fontes vira trava; baixa confiança / não conferida não reduz risco.
- MCP e IA são desligáveis por tenant; o produto funciona sem eles.

**Armadilhas:** `reavaliarEmpresa` precisa olhar evidência de QUALQUER fonte (TERCEIROS e OCR), não
só TERCEIROS. Estado de flags (IA/MCP) vive no singleton — resetar entre testes.

**Pendente:** frontend Angular/PrimeNG com o DS Senior; provisionamento (Neon + Vercel/frontend +
deploy do backend na VPS, estilo Ennor); transporte MCP oficial (JSON-RPC/OAuth 2.1) por cima do
`McpService`.

## 2026-10-10 — Fundação (módulo 1 do backlog)

**Entregue:** o esqueleto do backend (Spring Boot 3, Java 17, Maven) e o **módulo 1 — Fundação**:
- **Multi-tenant** desde o início (`TenantContext` + `TenantFilter` por `X-Tenant-Id`); nenhuma
  operação do core roda sem tenant.
- **Trilha de auditoria só-inclusão, encadeada por hash** (`TrilhaEvento` imutável — só getters;
  `TrilhaService.registrar` é a única porta de escrita): cada evento carrega `hashAnterior` + `hash`
  (SHA-256 sobre tenant|seq|tipo|payload|elo). `verificarIntegridade` recalcula a cadeia e aponta,
  de forma explicável, onde ela quebra. Sequência por tenant.
- **Porta de entrada com contrato único** (`PortaDeEntrada.receber(Evidencia)`): a ÚNICA porta por
  onde evidência entra; valida e grava como evento. Recusa dado pessoal cru (CPF) — só pseudônimo
  (regra 4 / LGPD).
- **Mock da License** (`LicensePort` + `MockLicenseAdapter`): usuários/papéis fictícios do cliente
  demo "Metalúrgica Exemplo S.A.", login simulado.
- **API da fundação:** `/api/health`, `POST /api/evidencias`, `GET /api/trilha`,
  `GET /api/trilha/integridade`.
- **10 testes** (BUILD SUCCESS) provando as regras inegociáveis: encadeamento por hash, sequência
  por tenant, **detecção de adulteração**, isolamento entre tenants, contrato único e recusa de CPF.

**Decisões que ficam valendo:**
- A trilha é imutável por construção: entidade só com getters, serviço sem caminho de edição/deleção;
  a adulteração só é possível por SQL cru — e o teste prova que ela é detectada.
- Testes rodam em H2 (schema pelo Hibernate `create-drop`); a migração Flyway (`V1__fundacao.sql`)
  roda contra o Postgres em produção. Flyway 10 separou os módulos por banco — por isso não se usou
  H2+Flyway nos testes.
- Stack confirmada pelo dono: **Java/Spring (VPS) + Angular (Vercel) + Neon + GitHub** — fiel ao
  padrão Senior; Spring Boot não roda na Vercel.

**Pendente:** módulos 2–14 do backlog (`docs/DEMO.md`), a começar por: cadastro canônico/cofre de
identidade (2), mocks de RH/Acesso/Terceiros + simulador de tempo + dados do cliente fictício (3).
Frontend Angular/PrimeNG e provisionamento (Neon + Vercel + deploy VPS) ainda não feitos.
