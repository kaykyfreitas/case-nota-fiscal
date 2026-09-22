#!/usr/bin/env bash
set -euo pipefail

CARGA="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
IMAGE="${K6_IMAGE:-grafana/k6:latest}"
BASE_URL="${BASE_URL:-http://host.docker.internal:8080}"
STAMP="$(date +%Y%m%d-%H%M%S)"
REPORT_DIR="$CARGA/reports/$STAMP"
SCENARIO="${1:-all}"

wait_for_api() {
  echo "Aguardando liveness em http://127.0.0.1:8080/actuator/health/liveness ..."
  local ready=0
  for _ in $(seq 1 30); do
    if curl -sf -o /dev/null --max-time 1 "http://127.0.0.1:8080/actuator/health/liveness"; then
      ready=1
      break
    fi
    sleep 1
  done
  if [[ "$ready" -ne 1 ]]; then
    echo "Liveness nao respondeu em /actuator/health/liveness. Suba com: docker compose up --build" >&2
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
echo "Cenário $SCENARIO"
echo "Resumo salvo em $REPORT_DIR"
exit "$failed"
