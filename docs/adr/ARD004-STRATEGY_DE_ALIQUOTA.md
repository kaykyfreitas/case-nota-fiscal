# ADR004 — Strategy de alíquota no domínio

## Contexto

PF e os regimes PJ (Simples, Lucro Real, Lucro Presumido) têm faixas e percentuais diferentes. Essa árvore de `if` vivia no gerador, que deveria só orquestrar a nota.

`CalculadoraAliquotaProduto` aplica um percentual já decidido nos itens: não é strategy nem porta de I/O.

## Decisão

- `CalculoAliquota` / `CalculoAliquotaService` em `core.domain.aliquota`: fachada que o gerador chama (`calcular(Pedido)`).
- `AliquotaStrategy` por política (PF, Simples Nacional, Lucro Real, Lucro Presumido) em `core.domain.aliquota.strategy`.
- `CalculadoraAliquotaProduto`: tributo da linha `valorUnitario * quantidade * aliquota`, 2 casas `HALF_UP`.
- Tipo de pessoa e regime vêm de `core.domain.enums` (`TipoPessoa`, `RegimeTributacaoPJ`).

Nenhuma strategy para `OUTROS` (e equivalentes sem regra): o seletor lança `RegimeTributacaoNaoSuportadoException` (`core.domain.exception`).

## Consequências

- Nova faixa ou regime vira classe nova, sem reabrir o gerador.
- Testes de faixa ficam nas strategies, o gerador mocka a fachada.
- Percentuais continuam no código; se um dia forem a um catálogo/banco, a fachada permanece e as strategies passam a ler uma porta.
- O legado aplicava alíquota só no unitário; `quantidade` ia para a NF sem entrar na conta. A linha agora fecha `unitário × quantidade × alíquota`. `valor_total_itens` continua subtotal da mercadoria — tributo não entra nesse campo.
