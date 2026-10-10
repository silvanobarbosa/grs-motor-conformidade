# Motor GRC — regras do repositório

Produto: **core único de risco, compliance e governança** (ISO 31000 / ISO 37301), versão de
demonstração com dados mock. Padrão de desenvolvimento **Senior**.

As instruções do agente estão em `CLAUDE.md` (regras inegociáveis). A especificação é
`docs/SPEC.md` (fonte da verdade) e `docs/DEMO.md` (escopo da demo, dados mock, backlog, Estado).
A evolução do produto vai em `HISTORICO.md`.

## Stack
- **Backend:** Java 17 + Spring Boot 3 (monólito modular, um pacote por componente). Maven.
- **Frontend:** Angular + PrimeNG (a entrar). Design System oficial da **Senior** (Public Sans,
  verde `#00968C`, tokens-only, WCAG AA) — não gerar identidade própria.
- **Banco:** PostgreSQL (Neon em produção). Testes com H2 (schema pelo Hibernate; a migração
  Flyway roda contra o Postgres).
- **MCP:** SDK oficial do Model Context Protocol (a entrar).

## Como rodar
- Testes: `cd backend && mvn test` (JDK 17; H2, sem Docker).
- App: `mvn spring-boot:run` com `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` no ambiente (Neon).

## Regras de engenharia (além das inegociáveis do CLAUDE.md)
- Segredo só em env, nunca no código/git. `.gitignore` cobre `.env*`.
- Cada módulo do backlog (`docs/DEMO.md`) entrega com testes das regras inegociáveis
  (imutabilidade da trilha, risco residual não editável, explicação, permissões, MCP não executa
  ação proibida).
- Adaptadores de sistema externo: `<Sistema>Port` (interface no core) + `Mock<Sistema>Adapter`.
- Trocar um mock por integração real **não pode** mudar o core.
- Autoria dos commits = conta da casa (`silvanobarbosa@gmail.com`), nunca o login de operação.
