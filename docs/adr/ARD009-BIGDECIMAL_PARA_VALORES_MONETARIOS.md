# ADR009 — `BigDecimal` para valores monetários e alíquotas

## Contexto

O contrato JSON de pedido/nota usa números para dinheiro e taxa (`valor_total_itens`, `valor_frete`, `valor_unitario`, `valor_tributo_item`) e o domínio aplica produto em dois pontos: tributo (`valorUnitario * aliquota`) e frete (`valorFrete * fator`). Hoje tudo isso é `double`.

`double` é IEEE-754 binário: 0.12, 0.03 e 1.085 não cabem de forma exata. Faixas inteiras (`< 500`, `<= 2000`) até passam, mas o centavo da NF nasce do produto e do arredondamento implícito. Em nota fiscal isso é defeito de domínio, não detalhe de estilo.

O model é compartilhado (ADR006): o tipo do campo é ao mesmo tempo Jackson e vocabulário de `CalculoAliquota` / `CalculoFrete` (ADR004, ADR005). Trocar o tipo no model troca o cálculo sem DTO novo. O JSON permanece número; não há quebra de contrato de nomes nem de forma do payload.

`quantidade` não é dinheiro: continua `int`.

## Decisão

Usar `java.math.BigDecimal` para:

- valores monetários do model (`Pedido`, `Item`, `NotaFiscal`, `ItemNotaFiscal`);
- alíquotas das strategies e de `CalculadoraAliquotaProduto`;
- fatores de `Regiao`.

Regras de construção e comparação:

- literal sempre em `String` (`new BigDecimal("0.12")`, `new BigDecimal("1.085")`); nunca `new BigDecimal(0.12)`, que copia o erro do `double`;
- faixa e igualdade numérica com `compareTo`, não `equals` (`1.0` e `1.00` não são `equals`).

Arredondamento (BRL):

- fator e alíquota ficam com a escala do literal (ex.: Nordeste `"1.085"`, PF `"0.12"`) — não se arredonda a taxa;
- resultado monetário (tributo do item, frete da nota) fecha em **2 casas**, `RoundingMode.HALF_UP`.

Jackson desserializa o número do JSON em `BigDecimal`. Serialização usa forma decimal simples (`spring.jackson.write.write-bigdecimal-as-plain`, `StreamWriteFeature.WRITE_BIGDECIMAL_AS_PLAIN`), para não emitir `1E+1`.

Validação (ADR007): `@PositiveOrZero` permanece; como deixa de ser primitivo, dinheiro obrigatório ganha `@NotNull` — campo omitido vira **400**, não `0.0` silencioso.

Fora de escopo: JSR 354 / `MonetaryAmount` (peso sem ganho no case) e `long` em centavos (os fatores `1.085` / `0.12` ainda exigiriam decimal).

## Consequências

- Cálculo de tributo e frete deixa de depender de epsilon em teste e de coincidência de mantissa.
- Código fica mais verboso (`multiply`, `setScale`); a política de 2 casas fica explícita no domínio, não no `double`.
- Payload de exemplo (`100.0`, `10.0`) continua válido; só muda o tipo Java.
- Testes de strategy/frete/calculadora passam a comparar `BigDecimal` com `compareTo` (ou `isEqualByComparingTo`).
