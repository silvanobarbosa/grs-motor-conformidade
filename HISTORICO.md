# Histórico de evolução — Motor GRC

Diário do produto: o que mudou, por quê, e o que ficou pendente. Mais recente em cima.
As **regras** do repo estão no `AGENTS.md` e no `CLAUDE.md`; a especificação em `docs/`.

Quem abre este app lê este arquivo antes de propor trabalho.

O que é: core único de risco, compliance e governança (ISO 31000 / ISO 37301), versão de
demonstração com dados mock, padrão Senior. Backend Java/Spring Boot (monólito modular),
frontend Angular/PrimeNG (a entrar), Postgres/Neon, servidor MCP (a entrar).

---

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
