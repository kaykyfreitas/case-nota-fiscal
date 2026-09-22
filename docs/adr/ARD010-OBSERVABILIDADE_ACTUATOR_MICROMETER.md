# ADR010 — Observabilidade com Actuator, Micrometer e Prometheus

## Contexto

O gerador é um POST síncrono: conferência do pedido, cálculo de alíquota/frete e quatro integrações em virtual threads (ADR008). Sem sinal na aplicação, latência e erro só aparecem no cliente (Bruno/k6): não dá para separar 422 de conferência, falha de lateral e teto do `sleep` do registro (~500 ms).

Precisamos de três perguntas na própria JVM: a instância está viva, quantas notas saíram e quanto tempo cada etapa levou, e como achar *este* `id_pedido` no log.

## Decisão

Instrumentar na aplicação e **exportar por pull**. O backend (Prometheus, Grafana, Datadog, etc.) é consumidor do scrape e do stdout; não entra no código nem no Compose default.

- Actuator: `health` (liveness/readiness) e `prometheus`. Sem `env`/`heapdump`/`beans`.
- Micrometer: `http.server.requests` automático; **Counter `nf.emitida`** só depois das integrações com sucesso; timers `nf.gerar` (depois da conferência até o join) e `nf.integracao` (`integracao=estoque|registro|entrega|financeiro`).
- p95/p99 **por instância** via `publishPercentiles` (`quantile` no scrape). `count`/`sum` acumulam desde o start do processo; o percentil é janela móvel desta JVM. Não somar `quantile` entre réplicas.
- `nf.emitida` não incrementa em 4xx/422 nem em falha de integração. Tags de baixa cardinalidade: `tipo_pessoa`, `regime` (`NA` quando PF). Sem `id_pedido`.
- `X-Request-Id`: lê ou gera UUID, MDC `requestId`, ecoa no response, `MDC.remove` no `finally` do filtro. Actuator não passa no filtro. Log **JSON** (Logstash, `logging.structured.format.console=logstash`): uma linha por evento; `requestId` do MDC vira campo. Mensagem com `id_pedido` / `id_nota_fiscal`, sem CPF/CNPJ. Cópia do MDC no `submit` das virtual threads (`ThreadLocal` não herda sozinho). Debug na worker prova a cópia (`logging.level.br.com.itau.geradornotafiscal.service.impl=DEBUG`).
- 4xx: `warn` no Advice (`codigo` + `detail`, sem payload). Fail-fast: `warn` com ids e causa antes de relançar; 500 do Advice guarda o stack.

No Prometheus: `nf_emitida_total`; taxa entre instâncias `sum(rate(nf_emitida_total[5m]))`.

## Consequências

- Probe e scrape funcionam com `curl` / MockMvc, sem Grafana no Compose.
- Stdout é JSON (Logstash): collector / Grafana Loki / Datadog Logs parseiam linha a linha; `requestId` é campo, não substring do `message`.
- Volume de NF não é proxy de `http.server.requests` (este mistura 422 e `/actuator`).
- Timers das laterais tornam o ADR008 mensurável: p99 de `nf.gerar` ~ máximo das quatro (~500 ms, registro), não a soma (~1480 ms).
- A mesma emissão serve a qualquer raspador OpenMetrics. Trace distribuído (agente na JVM, OTLP) continua fora deste recorte: waterfall de um `requestId` não está no scrape.
