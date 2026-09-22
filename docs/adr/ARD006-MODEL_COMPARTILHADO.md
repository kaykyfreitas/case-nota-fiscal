# ADR006 — Model compartilhado no domínio

## Contexto

O case congela o payload de entrada. `Pedido`, `NotaFiscal` e aninhados são ao mesmo tempo contrato Jackson, vocabulário do domínio (alíquota, frete) e payload dos services.

Separar DTO HTTP + mapper 1:1 duplicaria o código sem tradução de regra e aumentaria o risco de os dois modelos se "desalinharem".

## Decisão

Manter `Pedido`, `NotaFiscal` e aninhados em `core.domain.model`, fora de `adapter`. HTTP e domínio importam o mesmo pacote. Não há `PedidoRequest` / `NotaFiscalResponse`.

Enums do contrato (`TipoPessoa`, `RegimeTributacaoPJ`, `Regiao`, `Finalidade`, `TipoDocumento`) ficam em `core.domain.enums`; o model só os referencia.

Jackson (`@JsonProperty`) permanece no model. `core.domain.aliquota` e `core.domain.frete` não dependem de anotações HTTP, só dos tipos.

## Consequências

- Cumpre o contrato sem camada extra.
- O model é anêmico e compartilhado: não é entidade DDD pura.
- Validação Bean Validation pode viver nessas classes (ver ADR007) sem virar código duplicado.
- Se JSON e domínio divergirem no futuro, aí sim justifica DTO + mapper.
