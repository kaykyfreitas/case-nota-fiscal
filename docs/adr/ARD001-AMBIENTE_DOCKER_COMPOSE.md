# ADR001 — Ambiente reproduzível com Docker e Compose

## Contexto

O case pede diagnóstico de performance (latência, concorrência, integrações com `sleep` simulado). Rodar a JVM no host, sem limite de CPU/RAM, produz números que não se repetem na máquina de outro avaliador.

## Decisão

Padronizar o runtime da aplicação com `Dockerfile` + `docker-compose.yaml`:

- publicação da API em `8080`;
- `cpus: "0.50"` e `mem_limit: 512m`;
- `JAVA_OPTS` com `-XX:MaxRAMPercentage=75.0`, para a JVM respeitar o `mem_limit` do Compose;
- fuso `America/Sao_Paulo` (`TZ` e `-Duser.timezone`), para `LocalDateTime.now()` da nota não nascer em UTC.

Bruno (GUI) usa `localhost`; clientes em container (Bruno CLI, k6) usam `host.docker.internal`.

## Consequências

- Números de latência passam a ser comparáveis entre commits.
