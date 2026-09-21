# ADR008 — Virtual threads e fail-fast nas integrações

## Contexto

Depois da nota montada, o legado chamava estoque, registro, entrega e financeiro em sequência. Os `sleep` (380, 500, 150+200, 250 ms) somavam ~1480 ms por request. As quatro chamadas não compartilham resultado: são I/O independente, stand-in de HTTP bloqueante.

O endpoint permanece síncrono; os `sleep` do case não podem ser removidos. Precisamos esperar as quatro acabarem e, se uma falhar, interromper as que ainda estão no I/O — `Future.cancel(true)` de task submetida no `ExecutorService` faz isso.

Mesmo com as integrações em paralelo, a thread que atende o HTTP fica bloqueada ~500 ms no `take()`. No Tomcat clássico isso é uma **platform thread do pool** ocupada por request: sob carga o pool esgota e a fila de conexão cresce, embora o trabalho seja só esperar I/O.

## Decisão

Em `GeradorNotaFiscalServiceImpl.integrarNotaFiscal`, depois do cálculo de alíquota e frete:

- `Executors.newVirtualThreadPerTaskExecutor()` por request (`try-with-resources`), uma virtual thread por integração;
- `ExecutorCompletionService` para inscrever as tasks e esperar na **ordem de conclusão** (`take().get()` tantas vezes quanto o número de `Future`s);
- na primeira `ExecutionException`, `cancel(true)` nas demais (interrupt do `sleep` / HTTP in-flight) e desembrulha a causa;
- `InterruptedException` na thread do request restaura a flag, cancela as workers e lança `IllegalStateException`;
- `spring.threads.virtual.enabled=true`: a thread do Tomcat que segura o request (e espera o `take()`) também é virtual. Muitos requests simultâneos bloqueados em I/O deixam de consumir um slot cada um no pool de platform threads.

São dois ganhos distintos: o executor das integrações corta a **latência de um** request (soma → máximo); a flag do Spring aumenta a **concorrência** da API sem inflar o pool do Tomcat.

Alíquota e frete continuam sequenciais: a nota precisa existir antes do I/O. Cancelar não é saga: efeito já confirmado (HTTP 200) não é desfeito.

## Consequências

- Latência das integrações vira o máximo (~500 ms, o registro), não a soma.
- Fail-fast aborta I/O ainda bloqueado; o `close()` do executor não espera o `sleep` restante.
- Carga: mais requests em voo sem um-para-um com platform thread do Tomcat.
- Teste do gerador cobre orquestração (quatro chamadas) e interrupt da irmã quando uma falha.
- Não há timeout no `get()`: o teto hoje é o próprio `sleep` simulado.
