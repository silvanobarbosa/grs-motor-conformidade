# Motor GRC — Documentação do sistema

Documentação técnica e funcional do **Motor GRC** (GRS Motor de Conformidade): core único de
**risco, compliance e governança**, versão de demonstração com dados fictícios, padrão Senior.

- **Especificação** (fonte da verdade): [`docs/SPEC.md`](docs/SPEC.md)
- **Demonstração / backlog / roteiros**: [`docs/DEMO.md`](docs/DEMO.md)
- **Regras do agente**: [`CLAUDE.md`](CLAUDE.md) · **Regras do repo**: [`AGENTS.md`](AGENTS.md)
- **Diário de evolução**: [`HISTORICO.md`](HISTORICO.md)

---

## 1. Visão geral

O Motor GRC puxa **evidências** dos sistemas da empresa (RH, controle de acesso, gestão de
terceiros, OCR, GED/Sign/BPM) e vira a **fonte de verdade de risco**: avalia, explica, abre ações e
mede indicadores. Os sistemas operacionais continuam sendo os sistemas de registro; o Motor GRC é a
camada de **inteligência de risco** sobre eles.

- Base metodológica: **ISO 31000** (risco) e **ISO 37301** (compliance).
- Primeira frente: **segurança patrimonial**; depois compliance trabalhista (NRs) e governança, no
  mesmo core.
- Multi-tenant desde o início; catálogo regulatório global e somente leitura.

### URLs (demonstração no ar)

| Peça | URL |
|---|---|
| Aplicação (frontend) | **https://grc.reverblabs.com.br** |
| API (backend) | **https://grc-api.reverblabs.com.br** |
| Repositório | `github.com/silvanobarbosa/grs-motor-conformidade` |

### Credenciais da demonstração

| Item | Valor |
|---|---|
| Tenant | `metalurgica-exemplo` (cabeçalho `X-Tenant-Id`) |
| Admin master (login) | `admin.master` / `AdminMaster@2026` |
| Usuários do cliente (License mock) | senha `Demo@2026` |

---

## 2. Arquitetura

**Monólito modular** (um pacote por componente da arquitetura). **Event-sourcing seletivo**: a
trilha de auditoria é a fonte da verdade (só-inclusão, encadeada por hash); os demais módulos mantêm
modelos de leitura reconstruíveis.

```
Sistemas de origem            Porta de entrada            Core de inteligência             Saídas
(RH, Acesso, Terceiros,  ->   Contrato único de     ->    Trilha imutável (hash)      ->   Risco residual explicável
 OCR, GED, Sign, BPM)         evidência (1 porta)         Motor de avaliação + travas      Planos de ação (donos)
                                                          Catálogo regulatório             Indicadores + dossiê
                                                          Cofre de identidade (LGPD)       Servidor MCP (IA do cliente)
```

Fluxo de domínio (SPEC §2):

```
Risco:       Ativo -> Ameaça -> Risco ------+
                                            +-> Controle <- Evidência <- Sistemas de origem
Compliance:  Lei/Norma/Política -> Requisito +            |
                                                          v
                                   Plano de ação <- Risco residual
                                        |
                                        +-> conclusão volta como evidência
```

### Regras inegociáveis (CLAUDE.md)

1. **Contrato único de evidência** — toda evidência entra pela porta; nenhum mock grava direto no core.
2. **Mocks são adaptadores** — `<Sistema>Port` + `Mock<Sistema>Adapter`, trocáveis sem mudar o core.
3. **Trilha só-inclusão, encadeada por hash** — correção é um novo evento.
4. **Dado pessoal só no cofre** — CPF nunca fora; trilha/evidência usam pseudônimo.
5. **Risco residual nunca é editado** — sempre derivado.
6. **Toda mudança de risco é explicável** — evidência, fonte, regra.
7. **Processar ≠ exibir** — permissões valem só na exibição.
8. **IA nunca decide** — sugere, extrai, explica.
9. **Funciona com a IA desligada** — flag por cliente.
10. **Multi-tenant desde o início.**

---

## 3. Stack e como rodar

- **Backend:** Java 17 + Spring Boot 3 (Maven). Banco PostgreSQL (Neon em produção; H2 nos testes).
- **Frontend:** Angular 18 + PrimeNG, Design System Senior (Public Sans, verde `#00968C`, tokens).
- **Servidor MCP:** `McpService` (lógica e guardas); o transporte JSON-RPC/OAuth 2.1 pluga por cima.

```bash
# Testes (H2, sem Docker)
cd backend && JAVA_HOME=<jdk17> PATH=<maven>/bin:$PATH mvn test     # 78 testes

# Rodar local (Postgres via env)
SPRING_DATASOURCE_URL=jdbc:postgresql://host/db?sslmode=require \
SPRING_DATASOURCE_USERNAME=... SPRING_DATASOURCE_PASSWORD=... \
mvn spring-boot:run      # http://localhost:8080

# Frontend
cd frontend && npm install && npx ng build   # dist/grc-frontend/browser
```

As migrações Flyway (`backend/src/main/resources/db/migration/V1..V9`) criam o schema no Postgres.
Nos testes o schema vem do Hibernate (`create-drop`) e o Flyway fica desligado.

---

## 4. Módulos

| # | Pacote | O que faz |
|---|---|---|
| 1 | `fundacao` | Multi-tenant (`TenantContext`/`TenantFilter`), **trilha** só-inclusão por hash (`TrilhaEvento`/`TrilhaService`), **porta de entrada** (`PortaDeEntrada`), read-model de evidência (`EvidenciaRegistro`). |
| 2 | `cadastro` | **Cofre de identidade**: `Pessoa` com CPF só como hash, pseudônimo estável, resolução de entidades, eliminação LGPD. |
| 3 | `demo` + `mocks` | `MockRh/Acesso/Terceiros/Ocorrencias/Ged/Sign/Bpm/Ocr/Sara`, `RelogioDemo` (simulador de tempo), `SeedDemo` (cliente fictício). |
| 4 | `catalogo` | Catálogo regulatório global versionado (consulta por data) + **aplicabilidade** (obrigatório/requer confirmação/não aplicável). |
| 5 | `risco` | **Motor de avaliação** (`RuleEnginePort`/`AvaliadorSimples`), cenários declarativos, risco residual derivado, travas (confiança/oscilação/explicação). |
| 6 | `acoes` | **Planos de ação** (um por risco+causa), dono por regra (cargo+local), ciclo fechado; evento `RiscoMudou`. |
| 7 | `governanca` | Política → aprovação (BPM) → publicação versionada (GED) → ciência (Sign); aceitar risco = exceção humana. |
| 8 | `permissoes` | Escopo (papel+local), categoria sensível, precedência (origem só restringe), falha segura, segregação. |
| 9 | `indicadores` | Operacional / gestão / executivo + cobertura + atualidade; drill-down à evidência. |
| 10 | `externo` | **Acesso externo** temporário/só-leitura + **dossiê** pseudonimizado com prova de integridade. |
| 11 | `ocr` | Conferência de evidência de baixa confiança (não reduz risco até conferir). |
| 12 | `ia` | **Sara**: triagem e explicação; nunca decide; funciona desligada. |
| 13 | `mcp` | **Servidor MCP**: resources, tools (leitura; escrita com confirmação), ações proibidas negadas, desligado por padrão. |
| — | `admin` | **Admin master**, gestão de usuários do sistema e **observabilidade** (ver §6). |

---

## 5. Referência de API

Todas as rotas exigem o cabeçalho `X-Tenant-Id` (ex.: `metalurgica-exemplo`). Rotas admin exigem
também `X-Admin-Token` (do login).

### Fundação / demo
- `GET /api/health`
- `POST /api/evidencias` — recebe uma evidência pela porta.
- `GET /api/trilha` · `GET /api/trilha/integridade`
- `POST /api/demo/seed?colaboradores=N` · `POST /api/demo/relogio/avancar?dias=N` · `GET/POST /api/demo/relogio`

### Risco
- `GET /api/risco` (painel) · `GET /api/risco/{sujeito}` · `GET /api/risco/{sujeito}/porque`
- `POST /api/risco/reavaliar`

### Ações
- `GET /api/acoes/planos` · `POST /api/acoes/planos/{id}/concluir`

### Governança
- `GET/POST /api/governanca/politicas` · `POST /api/governanca/politicas/{codigo}/publicar`
- `POST /api/governanca/politicas/{codigo}/ciencia` · `GET /api/governanca/politicas/{codigo}/pendencias`
- `POST /api/governanca/aceitar-risco`

### Indicadores
- `GET /api/indicadores/operacional?dias=15` · `/gestao` · `/executivo?apetite=MEDIO` · `/atualidade` · `/cobertura`

### Admin
- `POST /api/admin/login` `{login,senha}` → `{token,nome,papeis}`
- `GET /api/admin/me` · `GET /api/admin/usuarios` · `POST /api/admin/usuarios` `{login,nome,senha,papeis[]}`
- `POST /api/admin/usuarios/{id}/ativo?ativo=true|false`
- `GET /api/admin/observabilidade`

---

## 6. Administração e observabilidade

A tela **Administração** (`/admin` no frontend) exige login do **admin master**.

- **Admin master**: único que cria/ativa/desativa usuários do sistema. Semeado no boot por tenant
  (`AdminSeedRunner`), não pode ser desativado. Senha guardada como hash com sal.
- **Gestão de usuários**: criar usuário (login, nome, senha mín. 8, papéis), ativar/desativar. Cada
  ação deixa rastro na trilha (`USUARIO_CRIADO`, `ADMIN_LOGIN`, etc.).
- **Observabilidade do sistema**: eventos na trilha + **integridade**, evidências (e não conferidas),
  pessoas no cofre, usuários, planos (abertos/concluídos), cobertura, risco por nível, atualidade por
  fonte, e o estado das flags **IA (Sara)** e **MCP**.

> Autenticação da demo é por token em memória (some no restart). Em produção, a identidade e a
> autenticação vêm da **License/SSO da Senior** — o cadastro de usuários aqui faz o papel dela.

---

## 7. Roteiros de demonstração (R1–R12)

Automatizados em `backend/src/test/java/com/senior/grc/e2e/RoteirosE2ETest.java`:

R1 onboarding/aplicabilidade · R2 evidência muda o risco · R3 ciclo fechado · R4 OCR/conferência ·
R5 aplicabilidade contínua · R6 governança/ciência · R7 desligamento (crachá ativo) · R8 dashboards ·
R9 auditoria externa + dossiê · R10 LGPD (eliminação) · R11 IA (triagem/explicação) · R12 MCP.

Critérios de pronto provados em teste: trilha imutável (detecta adulteração), risco residual derivado
e explicável, permissões do core não ampliadas pela origem, MCP não executa ação proibida, nenhum
dado real.

---

## 8. Deploy

| Camada | Onde | Como |
|---|---|---|
| **Frontend** | Vercel (projeto `grc-motor-conformidade`, domínio `grc.reverblabs.com.br`) | push no GitHub (git integration) ou `POST /v13/deployments`. `outputDirectory = dist/grc-frontend/browser`. |
| **Backend** | VPS Contabo (systemd `grc`, porta 8097), atrás do Caddy dockerizado | `mvn package -DskipTests` → `scp target/motor-grc-0.1.0.jar` para `/opt/grc/` → `sudo systemctl restart grc`. |
| **Banco** | Neon `grs-motor-conformidade-eu` (aws-eu-central-1) | migrações Flyway no boot. Env em `/opt/grc/grc.env`. |

Reverse proxy: bloco no `/opt/reverblabs/deploy/contabo/Caddyfile`
(`grc-api.reverblabs.com.br { reverse_proxy 172.18.0.1:8097 }`) + regra ufw
(`allow from 172.18.0.0/16 to any port 8097`). CORS: `GRC_CORS_ORIGINS` (default libera
`*.reverblabs.com.br` e `*.vercel.app`).

**Gotchas de deploy:**
- Neon deve ficar **perto do backend** (eu-central): Neon em São Paulo + VPS na Europa inviabiliza o
  seed por latência SSL.
- Vercel/Angular 18: o output fica em `dist/<proj>/browser`; setar `outputDirectory` nas settings.
- `*.vercel.app` fica atrás do SSO da conta → usar domínio custom (bypassa).

---

## 9. Pendências / próximos passos

- Transporte **MCP oficial** (JSON-RPC/OAuth 2.1) sobre o `McpService` (lógica e guardas já prontas).
- Mais telas no frontend: governança, dossiê, conferência, console MCP.
- Troca dos mocks pelas integrações reais da Senior (License, RH, Acesso, Terceiros, OCR, GED/Sign/
  BPM, Sara) — **sem alterar o core** (contrato único + portas).
- Registro na frota/monitor, se o dono quiser.

---

_Versão de demonstração. Todos os dados são fictícios e marcados como dados de teste._
