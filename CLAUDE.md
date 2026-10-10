# Motor GRC — Instruções para o agente

Este repositório contém a **versão de demonstração** do Motor GRC: um core único de risco, compliance e governança. Os dados são mock e o sistema não se conecta a nenhum produto externo.

Antes de qualquer tarefa, leia:
- `docs/SPEC.md` — especificação funcional e de arquitetura (fonte da verdade)
- `docs/DEMO.md` — escopo da demonstração, dados mock e roteiros

## Objetivo desta versão

Demonstrar o produto completo de ponta a ponta com dados fictícios, preservando as fronteiras da arquitetura real. Tudo o que hoje é mock deve poder ser trocado por integração real **sem alterar o core**.

## Regras inegociáveis

1. **Contrato único de evidência.** Toda evidência entra pela porta de entrada, inclusive a dos mocks. Nenhum mock grava direto nas tabelas do core.
2. **Mocks são adaptadores.** Cada sistema externo (RH, controle de acesso, gestão de terceiros, OCR, gestor de ocorrências, GED, Sign, BPM, License, Sara) é uma interface no core com uma implementação mock. Nome padrão: `<Sistema>Port` + `Mock<Sistema>Adapter`.
3. **Registro de eventos só de inclusão** para evidências, avaliações e decisões. Nada é editado nem apagado; correção é um novo evento. Cada evento carrega o hash do anterior.
4. **Dado pessoal só no cadastro canônico.** Trilha, evidências e indicadores guardam apenas o pseudônimo da pessoa. CPF real nunca fora do cofre; use hash para casar registros.
5. **Risco residual nunca é editado.** É sempre resultado do processamento dos eventos.
6. **Toda mudança de risco é explicável:** qual evidência, de qual fonte, em qual regra.
7. **Processar ≠ exibir.** O motor processa tudo; as permissões valem só na exibição (telas, API, MCP, exportações).
8. **IA nunca decide.** Funções de IA (mock da Sara) sugerem, extraem e explicam. Não alteram nível de risco, não marcam não aplicável e não aprovam nada.
9. **O produto funciona com a IA desligada.** Toda função de IA tem flag por cliente.
10. **Multi-tenant desde o início.** Todo dado tem `tenantId`; catálogo regulatório é global e somente leitura.

## Dados

- Somente dados fictícios. Nenhum nome, CPF ou empresa real.
- CPFs gerados devem ser fictícios e marcados como dados de teste.
- O conteúdo regulatório (NRs) é **conteúdo de demonstração**: requisitos resumidos com as próprias palavras, com referência ao item da norma, e marcados como "não validado". Não copiar texto integral de normas.

## Stack

Seguir os padrões de desenvolvimento da Senior. Na ausência de orientação específica para a demo:
- Backend: Java + Spring Boot, monólito modular com um módulo por componente da arquitetura
- Frontend: Angular + PrimeNG
- Banco: PostgreSQL (registro de eventos em tabela só de inclusão + modelos de leitura)
- Servidor MCP: SDK oficial do Model Context Protocol, conforme `docs/SPEC.md`

O motor de regras real será o da Senior. Na demo, implementar um avaliador simples de expressões **atrás de uma interface** (`RuleEnginePort`), para ser trocado depois.

## Forma de trabalho

- Construir por módulos, na ordem do backlog em `docs/DEMO.md`.
- Cada módulo com testes automatizados das regras inegociáveis acima (especialmente imutabilidade, explicação e permissões).
- Ao terminar cada módulo, atualizar a seção "Estado" no fim de `docs/DEMO.md`.
- Em dúvida entre simplificar e quebrar uma regra inegociável, simplificar outra coisa.
