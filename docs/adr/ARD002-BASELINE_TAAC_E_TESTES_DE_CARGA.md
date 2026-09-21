# ADR002 — Baseline com TaaC e testes de carga

## Contexto

Bugs (lista estática, delay de 5s) e refatorações (DI, regras, depois paralelo) mudam o comportamento e a latência. Sem suíte executável e sem número de carga, a evolução vira opinião.

O contrato HTTP permanece o JSON original; as esperas simuladas das integrações não podem ser removidas.

## Decisão

Registrar um baseline e reexecutá-lo a cada fatia relevante:

- **TaaC** em `tests/taac` (collection Bruno: PF, PJ, segundo request, pedido com 6 itens);
- **carga** em `tests/carga` (k6: smoke, sequencial, carga, pedido grande).

Os reports ficam versionados localmente para comparar p50/p95, taxa de falha e asserts de negócio (ex.: um item no PF).

## Consequências

- Correção funcional e ganho de performance têm evidência, não narrativa.
- A suíte TaaC trava regressão do contrato sem virar teste de unidade do domínio.
