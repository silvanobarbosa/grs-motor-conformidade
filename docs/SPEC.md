# Motor GRC — Especificação

Fonte da verdade para funcionalidades e arquitetura. Consolidada a partir dos documentos "Motor GRC — Arquitetura do Core de Risco e Compliance" e "Motor GRC — MCP, IA e comparativo de mercado".

## 1. Visão

Core único de risco e compliance. Puxa evidências dos sistemas do cliente e vira a fonte de verdade para avaliação, ação e indicadores: o sistema nervoso da gestão de riscos.

- Base metodológica: ISO 31000 (risco) e ISO 37301 (compliance)
- Primeira frente: segurança patrimonial; depois compliance trabalhista (NRs) e governança, no mesmo core
- Os sistemas operacionais continuam como sistemas de registro; o core é o sistema de inteligência de risco
- Produto simples, autônomo, com custo mínimo de implantação
- Produto completo (sem MVP). Recomenda-se validar com 2 ou 3 clientes parceiros de design ao longo do desenvolvimento

## 2. Modelo de domínio

Duas cadeias convergem no mesmo controle:

```
Risco:       Ativo → Ameaça → Risco ─────┐
                                         ├→ Controle ← Evidência ← Sistemas de origem
Compliance:  Lei/Norma/Política → Requisito ┘                │
                                                             ↓
                                    Plano de ação ← Risco residual
                                         │
                                         └→ conclusão volta como evidência
```

Entidades principais:
- **Entidades canônicas:** Pessoa, Local/Área, Ativo, EmpresaTerceira, Cargo, EstruturaOrganizacional
- **Risco:** Ativo, Ameaça, Risco (inerente e residual), Controle
- **Compliance:** Fonte (lei, norma, política interna), Requisito (no nível do item), Aplicabilidade
- **Evidência:** fato, origem, confiança da fonte, data de coleta, validade, hash
- **Ação:** Plano de ação (tratamento), Ocorrência
- **Governança:** Política, Versão de política, Ciência, Decisão
- **Auditoria:** Evento da trilha

Relações-chave:
- Requisito ↔ Controle: N:N
- Risco ↔ Controle: N:N
- Não conformidade de requisito alimenta o risco residual (ISO 37301)

## 3. Evidência

Caminho do meio: o core guarda o fato relevante; o dado bruto fica na origem.

Cada evidência: fato (ex.: "treinamento NR-35 da pessoa P-123 válido até 2026-11-30"), origem (sistema, fonte, nível de confiança), data de coleta, validade, hash.

- Trilha imutável e linhagem rastreável
- O conector traz fato observável; quem avalia risco é o core
- Minimização (LGPD art. 6º, III)

## 4. Cadastro canônico

- ID próprio por entidade, ligado aos IDs de cada sistema de origem
- Chave de pessoa: CPF; chave alternativa: passaporte ou RNM
- CPF real só no cofre; casamento de registros por hash (LGPD art. 46)
- O cadastro é o **cofre de identidade**: trilha e evidências guardam só pseudônimos
- Eliminação de pessoa (LGPD art. 18, VI) = apagar a ligação pseudônimo → pessoa; trilha e indicadores continuam íntegros

## 5. Integração

- **Contrato único de evidência** para todas as fontes, inclusive Senior. Nenhum atalho ao banco do core
- Esquema: quem, o quê, quando, de onde, válido até quando, confiança da fonte
- Entrada por API e por importação de arquivo
- Configuração por conector: nível de confiança, herança de permissões

## 6. Cálculo de risco

| Nível | Como muda |
| --- | --- |
| Inerente | Avaliação humana periódica |
| Residual | Automático, pela eficácia dos controles a partir das evidências |

Travas obrigatórias:
1. Confiança da fonte: evidência de baixa confiança não leva o risco a crítico sozinha
2. Proteção contra oscilação: limites e janelas de tempo antes de mudar o nível
3. Explicação de toda mudança: evidência, fonte, regra
4. Exceção humana com justificativa registrada (ISO 31000, 6.4.4)

## 7. Ações e donos

- Mudança de nível do risco residual → notifica o dono e abre plano de ação no gestor de ocorrências
- Tipos distintos: "ocorrência" (fato ocorrido) e "ação de tratamento" (preventiva), com indicadores separados
- Agrupamento: um plano por risco e causa; plano aberto é atualizado, não duplicado
- Ciclo fechado: ação concluída volta como evidência; ocorrências reais também são evidência de falha de controle

Papéis: dono do risco, dono do controle, executor da ação.
- Atribuição por regra (cargo + local), nunca por nome (ISO 31000, 5.4.3)
- Reatribuição automática quando o RH informa desligamento ou mudança de função
- Risco sem dono → gestor de riscos do cliente, destacado no dashboard

## 8. Configuração e self-service

- Risco como configuração: cenário = regra declarativa (evidências cruzadas, condição, criticidade)
- Biblioteca inicial de cenários de segurança patrimonial, ativados pelo cliente
- Configuração em camadas: cliente ajusta parâmetros; consultor edita a regra completa
- Onboarding guiado com perguntas de contexto e critérios (ISO 31000, 6.3)
- Meta: tempo até o primeiro risco ativo sem consultor

## 9. Compliance

- Requisito no nível do item da norma
- Conteúdo regulatório mantido pela Senior: versão e vigência por requisito; responde "cumpria a regra vigente naquela data?"
- Atualização com análise de impacto; cliente aceita ou ajusta
- Camadas: conteúdo base Senior + ajustes do cliente
- Conteúdo publicado como dado, sem depender de release
- Escopo: normas trabalhistas e NRs, em ondas (gerais, especiais, setoriais)

Aplicabilidade:
- Regras de aplicabilidade são conteúdo versionado (atividade, grau de risco, nº de empregados, tipo de local)
- Resultados: obrigatório · requer confirmação · não aplicável (sempre com justificativa)
- Na dúvida, "requer confirmação"
- Aplicabilidade contínua: conexões geram sugestões e alertas de divergência (ex.: RH cadastra eletricista → sugere NR-10)
- Key user confirma; qualificação registrada; não aplicável de alta criticidade exige responsável técnico; revisão anual

## 10. Governança

- Política interna é fonte de requisito (ISO 37301, 5.2)
- Ciclo: rascunho → aprovação → publicação com versão e vigência → ciência dos colaboradores
- Ciência por regra (cargo + local), ligada à versão exata lida

Componentes Senior empacotados:

| Ferramenta | Papel | O que chega ao core |
| --- | --- | --- |
| GED | Texto e versões das políticas | ID e hash da versão |
| Sign | Ciência e aprovação | Quem, qual versão, quando |
| BPM | Fluxos de aprovação | Decisão, aprovador, justificativa |

- BPM: decisões e aprovações (aprovar política, aceitar risco, marcar não aplicável)
- Gestor de ocorrências: execução de planos

Trilha de auditoria:
- Registro só de inclusão, encadeado por hash
- Acesso externo (auditor, fiscal): concedido pelo key user, com escopo, temporário, só leitura, auditado
- Dossiê de conformidade exportável com verificação de integridade
- Pseudonimização por padrão no acesso externo
- Retenção definida pelo conteúdo regulatório, cobrindo no mínimo o prazo de ação trabalhista (CF art. 7º, XXIX)

## 11. Indicadores

| Público | Pergunta | Exemplo |
| --- | --- | --- |
| Operacional | O que resolvo hoje? | Vencimentos nos próximos 15 dias |
| Gestão | Onde a exposição cresce? | Tendência do residual por unidade |
| Executivo | Dentro do apetite? | Residual vs apetite |

- Antecedentes (evidência) antes de consequentes (ocorrências)
- Obrigatórios: cobertura (% de requisitos aplicáveis com evidência automática) e atualidade (último envio de cada fonte)
- Todo número abre até a evidência
- BI externo: indicadores calculados no core, saída agregada, base analítica separada, API aberta + exportação

## 12. Segurança e permissões

- Papel + escopo (unidades, locais) + categoria do dado
- Classificação de sensibilidade por evidência; dados que revelam saúde são sensíveis (LGPD arts. 5º, II, e 11)
- Segregação de funções: ninguém aprova a própria decisão
- Permissões por cargo + local

Precedência: regras do core são a base; herança da origem só restringe.

| Core | Origem | Resultado |
| --- | --- | --- |
| Permite | Nega | Não vê |
| Nega | Permite | Não vê |
| Permite | Sem dado | Vê |

- Falha segura: sem sincronização de permissões, categorias sensíveis bloqueadas
- License: identidade, autenticação e permissões funcionais. Core: escopo de dados, herança e segregação

## 13. Arquitetura técnica

| Componente | Responsabilidade |
| --- | --- |
| Porta de entrada | Recebe evidência (API e arquivo), valida, enfileira |
| Resolução de entidades | Casa com o cadastro canônico |
| Repositório de evidências | Fato, origem, validade, hash; só inclusão |
| Motor de avaliação | Regras declarativas + travas; recalcula residual |
| Catálogo regulatório | Normas, requisitos, aplicabilidade versionados |
| Trilha de auditoria | Registro encadeado de decisões |
| Orquestração de ações | Gestor de ocorrências e BPM |
| Camada de indicadores | Base analítica |
| Servidor MCP | Canal de exibição para IA do cliente |

- Eventos só para evidências, avaliações e decisões; demais módulos tradicionais com versão
- SaaS multi-tenant; catálogo global + dados por cliente
- Chave de criptografia por cliente
- Filas separadas por cliente/prioridade; tempo real à frente de lotes
- Texto livre: orientação e detecção de nomes/CPF antes de gravar

Motor de regras: o da Senior, validado por:
1. Condições de tempo
2. Regras com versão e vigência
3. Explicação do resultado
4. Reprocessamento por data passada
5. Editor em camadas

## 14. MCP

Base: especificação MCP 2026-07-28. Canal de exibição com as mesmas permissões, trilha e travas.

| Primitiva | Oferecer |
| --- | --- |
| Resources | Estado de risco por unidade, requisitos aplicáveis, indicadores, catálogo |
| Tools de leitura | Consultar risco, explicar mudança, listar pendências, buscar requisito |
| Tools de escrita (com confirmação) | Abrir plano de ação, comentar, solicitar aprovação |
| Prompts | Resumo para auditoria, análise de unidade, preparação de reunião |
| Tasks | Dossiê de conformidade |

Nunca via MCP: aprovar aceitação de risco, marcar não aplicável, alterar regras, revelar identidade, exportar em massa.

Segurança: OAuth 2.1 (servidor como resource server, Protected Resource Metadata, validação de audiência, sem repasse de tokens), escopos mínimos, step-up para escrita, campos de terceiros marcados como não confiáveis, toda chamada na trilha, cotas por cliente. MCP desligado por padrão; categorias sensíveis desligadas por padrão.

## 15. IA e componentes Senior

| Componente | Papel |
| --- | --- |
| OCR de mercado | Leitura de documentos (o mesmo do gestão de terceiros) |
| Gestão de terceiros | Registro e avaliação de fornecedores; core consome como evidência |
| Sara | Gateway de IA para funções dentro do produto (provedores Senior) |
| MCP | Porta para a IA corporativa do cliente |

- OCR: uma leitura, uma evidência; retorno com confiança por campo, hash do documento e versão do motor; baixa confiança → conferência
- IA (caminho C): cliente usa IA do produto via Sara, ou a própria via MCP, ou desliga
- Capacidades de IA, prioridade 1: leitura de documentos (via OCR), triagem de ocorrências, explicação do risco
- Prioridade 2: assistente de aplicabilidade, agente de mudança regulatória (interno Senior), copiloto de políticas, dossiê
- Prioridade 3: detecção de padrões, agente de acompanhamento
- Toda saída de IA: grau de confiança, revisão humana abaixo do limite, registro de modelo/versão/prompt
- Decisões automatizadas que afetam trabalhadores têm caminho de revisão (LGPD art. 20)
