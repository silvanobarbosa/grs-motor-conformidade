# Motor GRC — Demonstração

Versão completa com dados mock, sem conexão a produtos externos. Serve para demonstrações comerciais e para validar o produto com clientes parceiros.

## 1. Escopo

Entra tudo o que está em `SPEC.md`, com os sistemas externos substituídos por adaptadores mock:

| Sistema externo | Adaptador mock | Comportamento |
| --- | --- | --- |
| RH | `MockRhAdapter` | Pessoas, cargos, estrutura, treinamentos, admissões, desligamentos |
| Controle de acesso | `MockAcessoAdapter` | Crachás, áreas, registros de entrada |
| Gestão de terceiros | `MockTerceirosAdapter` | Empresas terceiras, documentos, status |
| OCR | `MockOcrAdapter` | Retorna campos extraídos com confiança por campo (inclui casos de baixa confiança) |
| Gestor de ocorrências | `MockOcorrenciasAdapter` | Recebe planos de ação, devolve conclusão; registra ocorrências |
| GED / Sign / BPM | `MockGedAdapter`, `MockSignAdapter`, `MockBpmAdapter` | Versões de política, ciências, aprovações |
| License | `MockLicenseAdapter` | Usuários, papéis e login simulado |
| Sara | `MockSaraAdapter` | Respostas de IA determinísticas, com confiança e registro de modelo/prompt |

Todos os mocks enviam evidência pela porta de entrada (contrato único). Nenhum grava direto no core.

**Simulador de tempo:** uma tela de administração da demo permite avançar o relógio (dias) e disparar eventos dos mocks, para mostrar vencimentos e mudanças de risco ao vivo.

## 2. Cliente fictício

**Metalúrgica Exemplo S.A.** (fictícia)

- Duas plantas: **Planta Norte** e **Planta Sul**
- Áreas por planta: portaria, almoxarifado, produção, subestação elétrica, espaço confinado (só Planta Norte), telhado/manutenção predial
- 120 colaboradores fictícios distribuídos por cargo: vigilante, operador de produção, eletricista, técnico de manutenção, técnico de segurança do trabalho, gestor de segurança patrimonial, gestor de planta, gestor de riscos, analista de RH
- 4 empresas terceiras fictícias: vigilância, limpeza, manutenção elétrica, obras civis
- Key user: gestor de riscos da matriz
- Apetite a risco definido no onboarding: residual máximo "médio" por planta

Todos os nomes, CPFs e empresas são fictícios e marcados como dados de teste.

## 3. Conteúdo regulatório de demonstração

Marcado como "conteúdo de demonstração, não validado". Requisitos resumidos com palavras próprias, com referência ao item.

- NR-1 (gerais): gerenciamento de riscos ocupacionais
- NR-6: EPI
- NR-10: eletricidade (aplicável a eletricistas e subestação)
- NR-33: espaço confinado (aplicável à Planta Norte)
- NR-35: trabalho em altura (aplicável a manutenção predial)
- 2 políticas internas: "Visitante só circula acompanhado" e "Crachá de desligado bloqueado em até 24h"

Cada requisito com versão e vigência; incluir **uma norma com duas versões** para demonstrar consulta por data.

## 4. Cenários de risco de segurança patrimonial (biblioteca inicial)

1. Crachá ativo de colaborador desligado
2. Acesso a área restrita sem treinamento válido
3. Terceiro com documentação vencida acessando a planta
4. Visitante sem acompanhante registrado
5. Acesso fora do horário do turno
6. Área crítica sem vigilante escalado
7. Ocorrências repetidas no mesmo local em 30 dias
8. Treinamento obrigatório vencendo em até 15 dias

## 5. Roteiros de demonstração

Cada roteiro deve rodar do início ao fim em menos de 5 minutos.

**R1. Onboarding e aplicabilidade**
Key user descreve as atividades → sistema sugere normas (obrigatório, requer confirmação, não aplicável) → key user confirma → marcar NR-33 como não aplicável na Planta Norte exige aprovação de responsável técnico.

**R2. Evidência muda o risco**
Avançar o relógio → treinamento NR-35 de três técnicos vence → risco residual da Planta Sul sobe → tela "por que mudou" mostra evidência, fonte e regra → plano de ação único aberto no gestor de ocorrências, atribuído ao dono por cargo + local.

**R3. Ciclo fechado**
Concluir o plano no mock do gestor de ocorrências → conclusão volta como evidência → risco recalculado.

**R4. Terceiro e OCR**
Terceiro envia ASO e certificado → OCR mock lê → um campo com baixa confiança vai para conferência e não reduz o risco → após conferência, evidência válida.

**R5. Aplicabilidade contínua**
RH mock cadastra um novo eletricista → sistema sugere NR-10 e alerta divergência se a norma estava marcada como não aplicável.

**R6. Governança**
Política publicada (GED mock) → ciência exigida por cargo + local → ciências chegam (Sign mock) → painel de pendências de ciência.

**R7. Desligamento**
RH mock desliga o gestor de segurança da Planta Norte → donos reatribuídos automaticamente → crachá ainda ativo gera risco (cenário 1).

**R8. Dashboards**
Visão operacional, de gestão e executiva; indicadores de cobertura e atualidade; drill-down até a evidência; simular um conector parado e mostrar o alerta de atualidade.

**R9. Auditoria externa e dossiê**
Key user concede acesso temporário a um auditor → auditor vê dados pseudonimizados → gerar dossiê "NR-35, Planta Sul, período" → verificar integridade pelos hashes.

**R10. LGPD: eliminação de pessoa**
Eliminar um ex-colaborador → trilha continua íntegra, indicadores históricos inalterados, identidade irrecuperável.

**R11. IA no produto (Sara mock)**
Triagem de um relato de ocorrência em texto livre → extração de local, data e pessoas → explicação em linguagem natural do aumento de risco. Mostrar o produto funcionando com a IA desligada.

**R12. MCP**
Conectar um cliente MCP (ex.: Claude) ao servidor da demo → perguntar "por que o risco subiu na Planta Sul?" → pedir abertura de plano de ação (com confirmação) → tentar aceitar um risco via MCP e mostrar que é negado.

## 6. Backlog de construção (ordem sugerida)

1. Fundação: multi-tenant, autenticação mock (License), registro de eventos com hash encadeado, porta de entrada com contrato único
2. Cadastro canônico e cofre de identidade
3. Mocks de RH, acesso e terceiros + simulador de tempo + dados do cliente fictício
4. Catálogo regulatório versionado e aplicabilidade
5. Motor de avaliação (`RuleEnginePort` com avaliador simples), cenários, travas e explicação
6. Ações e donos (mock do gestor de ocorrências)
7. Governança (mocks de GED, Sign, BPM) e trilha de auditoria
8. Permissões (papel + escopo + categoria, precedência, segregação)
9. Indicadores e dashboards
10. Acesso externo e dossiê
11. OCR mock e conferência
12. Sara mock (triagem e explicação)
13. Servidor MCP
14. Roteiros R1 a R12 automatizados como testes de ponta a ponta

## 7. Critérios de pronto da demo

- Os 12 roteiros rodam de ponta a ponta sem intervenção técnica
- Testes provam: trilha não pode ser alterada; risco residual não pode ser editado; toda mudança de risco tem explicação; permissões do core não são ampliadas pela origem; MCP não executa ações proibidas
- Nenhum dado real presente

## Estado

- [x] **1. Fundação** — multi-tenant, autenticação mock (License), registro de eventos com hash
  encadeado, porta de entrada com contrato único. Backend Spring Boot 3 / Java 17 / Maven.
  10 testes (imutabilidade, encadeamento, detecção de adulteração, isolamento por tenant, contrato
  único, recusa de CPF). (2026-10-10)
- [x] **2. Cadastro canônico e cofre de identidade** — `Pessoa` no cofre (CPF só como hash, nunca
  em claro; pseudônimo estável derivado do hash). Resolução de entidades por CPF→pseudônimo.
  Eliminação LGPD quebra a identidade mantendo a trilha íntegra. 4 testes. (2026-10-10)
- [x] **3. Mocks de RH, acesso e terceiros + simulador de tempo + dados do cliente fictício** —
  `MockRhAdapter` / `MockAcessoAdapter` / `MockTerceirosAdapter` (todos emitem pela porta de
  entrada; nada direto no core), `RelogioDemo` (avança em dias), `SeedDemo` (Metalúrgica Exemplo:
  colaboradores por cargo/planta, treinamentos com vencimentos, crachás, 4 terceiras + doc vencido),
  admin da demo (`/api/demo/*`). Read-model de evidência (`EvidenciaRegistro`) com tipo+atributos
  estruturados para o motor de risco. 5 testes. (2026-10-10)
- [x] **4. Catálogo regulatório versionado e aplicabilidade** — catálogo GLOBAL (NR-1/6/10/33/35
  + 2 políticas), requisitos no nível do item com versão/vigência e consulta por data (NR-35 com 2
  versões); conteúdo de demonstração "não validado". Aplicabilidade: regras globais cargo→norma
  (obrigatório / requer confirmação / não aplicável), confirmação por tenant com justificativa
  obrigatória e responsável técnico p/ alta criticidade; na dúvida, requer confirmação. 5 testes. (2026-10-10)
- [x] **5. Motor de avaliação (`RuleEnginePort`), cenários, travas e explicação** — avaliador
  simples atrás do `RuleEnginePort` (trocável pelo motor da Senior); cenários declarativos
  (biblioteca inicial: crachá ativo de desligado=CRÍTICO, treinamento vencido=ALTO, vencendo em
  15d=MÉDIO, doc terceiro vencido=ALTO); risco residual DERIVADO (nunca editado), upsert por sujeito;
  travas de confiança (baixa não vai a CRÍTICO), oscilação (evento só na mudança) e explicação
  (evidência/fonte/regra na trilha). 5 testes (R2/R4/R7). (2026-10-10)
- [x] **6. Ações e donos (mock do gestor de ocorrências)** — mudança de risco publica evento
  (`RiscoMudou`); `AcoesService` abre UM plano por risco+causa (não duplica), dono por REGRA
  (cargo+local; fallback no gestor de riscos marcado sem dono → reatribuição automática); mock do
  gestor de ocorrências; ciclo fechado (concluir → evidência ACAO_CONCLUIDA → reavaliação). 4 testes
  (R3, R7). (2026-10-10)
- [x] **7. Governança (mocks de GED, Sign, BPM) e trilha de auditoria** — política (rascunho →
  aprovação BPM → publicação versionada via GED com hash), ciência por cargo+local (Sign) com painel
  de pendências (R6), decisões BPM (aprovar política, aceitar risco). Aceitar risco = exceção humana
  (justificativa obrigatória) que trava a abertura de plano. Mocks GedPort/SignPort/BpmPort. 4 testes. (2026-10-10)
- [x] **8. Permissões (papel + escopo + categoria, precedência, segregação)** — `PermissaoService`:
  escopo por papel+local (gestor de planta só vê seu local), categoria SENSÍVEL (saúde) classificada
  por evidência, precedência (core é base; herança da origem SÓ restringe — origem nega → não vê),
  falha segura (sem sincronização → sensível bloqueada), segregação de funções (ninguém aprova a
  própria decisão). Processar ≠ exibir. 6 testes. (2026-10-10)
- [ ] 9. Indicadores e dashboards
- [ ] 10. Acesso externo e dossiê
- [ ] 11. OCR mock e conferência
- [ ] 12. Sara mock (triagem e explicação)
- [ ] 13. Servidor MCP
- [ ] 14. Roteiros R1 a R12 automatizados como testes de ponta a ponta
