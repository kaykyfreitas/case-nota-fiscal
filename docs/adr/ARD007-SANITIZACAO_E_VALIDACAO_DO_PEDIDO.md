# ADR007 — Sanitização e validação do payload de pedido

## Contexto

O domínio passou a tratar o erro (`PedidoInvalidoException`, regime não suportado), mas o adapter HTTP ainda aceitava payload incompleto ou strings só com espaço.

Já que ficou decidido seguir sem uma camada de DTOs e mapeamento nesse momento, foi adicionada a sanitização dos dados na camada de models, na ponta do HTTP.

## Decisão

Na entrada HTTP, nesta ordem:

1. Jackson desserializa o JSON no `Pedido` compartilhado;
2. `PedidoSanitizacaoAdvice` (`RequestBodyAdvice`) faz `trim` nas strings; branco vira `null` (higiene do modelo; `@NotBlank` já recusaria `""`);
3. Bean Validation (`@Valid`): destinatário, itens, endereço de entrega, regime obrigatório para PJ, valores não negativos.

Erros de contrato → **400** Problem Details (RFC 9457) com `errors[]`. O gerador **não** é chamado.

Falha de regra no domínio → **422**. O payload já passou no `@Valid`; o controller chama `gerarNotaFiscal` e a exception de domínio (`RegimeTributacaoNaoSuportadoException`, `PedidoInvalidoException`) é traduzida pelo Advice. Exemplo típico: PJ `OUTROS` (a validação HTTP exige regime, mas aceita `OUTROS`; quem recusa é o cálculo de alíquota).

Constraints estruturais ficam no model; o advice fica em `adapter.in.web`.

## Consequências

- 400 vs 422 fica explícito para o cliente e para a entrevista.
- Há sobreposição parcial com exceptions de domínio (rede de segurança se alguém chamar o serviço sem HTTP).
- O model carrega `jakarta.validation`; Lombok precisa estar em `annotationProcessorPaths` no Maven para conviver com o starter de validation.
