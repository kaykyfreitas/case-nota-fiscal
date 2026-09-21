# ADR006 — Model compartilhado na raiz do pacote21

## Contexto

O case congela o payload de entrada. `Pedido`, `NotaFiscal` e aninhados são ao mesmo tempo contrato Jackson, vocabulário do domínio (alíquota, frete) e payload dos services.

Separar DTO HTTP + mapper 1:1 duplicaria o código sem tradução de regra e aumentaria o risco de os dois modelos se "desalinharem".

## Decisão

Manter as classes em `br.com.itau.geradornotafiscal.model`, fora de `domain` e de `adapter`. HTTP e domínio importam o mesmo pacote. Não há `PedidoRequest` / `NotaFiscalResponse`.

Jackson (`@JsonProperty`) permanece no model. `domain.aliquota` e `domain.frete` não dependem de anotações HTTP, só dos tipos.

## Consequências

- Cumpre o contrato sem camada extra.
- O model é anêmico e compartilhado: não é entidade DDD pura.
- Validação Bean Validation pode viver nessas classes (ver ADR007) sem virar código duplicado.
- Se JSON e domínio divergirem no futuro, aí sim justifica DTO + mapper.
