# ADR005 — Cálculo de frete e fator no enum `Regiao`

## Contexto

O gerador também escolhia o endereço (`ENTREGA` / `COBRANCA_ENTREGA`) e aplicava um multiplicador por região. Copiar o molde de cinco strategies de alíquota seria o padrão errado: cada região só tem um fator, não uma regra diferente.

## Decisão

- `CalculoFrete` / `CalculoFreteService` no domínio: resolve a região de entrega e aplica `valorFrete * fator`.
- O fator fica no enum `Regiao` (conjunto fechado do contrato JSON: Norte 1.08, Nordeste 1.085, Centro-Oeste 1.07, Sudeste 1.048, Sul 1.06).
- Sem endereço de entrega com região: `PedidoInvalidoException` (não mais frete 0 silencioso).

O nome do enum no JSON não muda; o construtor extra não quebra desserialização por nome.

## Consequências

- Gerador só chama `calculoFrete.calcular(pedido)`.
- Alterar fator é um valor no enum, não um `if` no orquestrador.
- O enum mistura vocabulário da API e parâmetro de cálculo — aceitável enquanto o conjunto for fechado; parametrização em banco reusaria o serviço e trocaria a fonte do fator.
