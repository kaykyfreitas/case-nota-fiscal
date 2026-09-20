#!/usr/bin/env bash
set -euo pipefail

CARGA="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
IMAGE="${K6_IMAGE:-grafana/k6:latest}"
BASE_URL="${BASE_URL:-http://host.docker.internal:8080}"
STAMP="$(date +%Y%m%d-%H%M%S)"
REPORT_DIR="$CARGA/reports/$STAMP"
SCENARIO="${1:-all}"

wait_for_api() {
  echo "Aguardando API em http://127.0.0.1:8080 ..."
  local ready=0
  for _ in $(seq 1 30); do
    if curl -s -o /dev/null --max-time 1 "http://127.0.0.1:8080"; then
      ready=1
      break
    fi
    sleep 1
  done
  if [[ "$ready" -ne 1 ]]; then
    echo "API nao esta escutando na porta 8080. Suba com: docker compose up --build" >&2
    exit 1
  fi
}

run_k6() {
  local name="$1"
  echo
  echo "=== k6 ${name} ==="
  docker run --rm \
    --user "$(id -u):$(id -g)" \
    --add-host=host.docker.internal:host-gateway \
    -e BASE_URL="$BASE_URL" \
    -v "$CARGA:/scripts" \
    -v "$REPORT_DIR:/reports" \
    "$IMAGE" \
    run "/scripts/${name}.js" \
    --summary-export "/reports/${name}.json"
}

wait_for_api
mkdir -p "$REPORT_DIR"
echo "Relatorios em $REPORT_DIR"

failed=0
if [[ "$SCENARIO" == "all" ]]; then
  for name in smoke sequencial pedido-grande carga; do
    if ! run_k6 "$name"; then
      failed=1
    fi
  done
else
  if ! run_k6 "$SCENARIO"; then
    failed=1
  fi
fi

echo
echo "Resumo salvo em $REPORT_DIR"
echo "Anote no caderno: http_req_duration p50/p95/p99, checks de itens, http_req_failed."
exit "$failed"
