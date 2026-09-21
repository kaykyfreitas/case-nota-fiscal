# ADR003 — Interfaces e injeção de dependência nas integrações

## Contexto

O orquestrador legado instanciava estoque, registro, entrega, financeiro e a calculadora com `new`. Isso impedia teste isolado, escondia I/O e travava qualquer paralelismo futuro.

## Decisão

Expor cada integração como interface (`EstoqueService`, `RegistroService`, `EntregaService`, `FinanceiroService`) e implementar beans Spring injetados no `GeradorNotaFiscalServiceImpl`.

A entrega, que combina espera de domínio (150 ms) e I/O simulado (200 ms), ganha `EntregaIntegrationPort` + `EntregaIntegrationAdapter`. As outras integrações, hoje só `sleep`, permanecem no `*ServiceImpl` até existir sistema externo distinto.

O caso de uso (`GeradorNotaFiscalService`) é a porta de entrada: o adapter HTTP **usa** a interface, não a implementa.

## Consequências

- Testes do gerador mockam as integrações e não disparam `sleep`.
- O orquestrador deixa de conhecer implementação de integração.
- Há assimetria consciente: só entrega tem Port/Adapter, mas isso pode facilmente extendido para os demais.
