#!/usr/bin/env bash
set -euo pipefail

TAAC="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COLLECTION="$TAAC/collection"
IMAGE="${BRUNO_IMAGE:-usebruno/cli:latest}"
BASE_URL="${BASE_URL:-http://host.docker.internal:8080}"
ENV_NAME="${BRUNO_ENV:-local}"
STAMP="$(date +%Y%m%d-%H%M%S)"
REPORT_DIR="$TAAC/reports/$STAMP"

if [[ ! -f "$COLLECTION/bruno.json" ]]; then
  echo "Collection Bruno nao encontrada em $COLLECTION" >&2
  exit 1
fi

echo "Aguardando API em http://127.0.0.1:8080 ..."
ready=0
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

mkdir -p "$REPORT_DIR"

echo "Rodando collection Bruno ($IMAGE) contra $BASE_URL"
echo "Relatorios em $REPORT_DIR"

docker run --rm \
  --user "$(id -u):$(id -g)" \
  --add-host=host.docker.internal:host-gateway \
  -v "$COLLECTION:/bruno" \
  -v "$REPORT_DIR:/reports" \
  "$IMAGE" \
  run . \
  --env "$ENV_NAME" \
  --env-var "baseUrl=$BASE_URL" \
  --reporter-html /reports/results.html \
  --reporter-json /reports/results.json \
  --reporter-junit /reports/results.xml

echo
echo "Registro salvo:"
echo "  $REPORT_DIR/results.html"
echo "  $REPORT_DIR/results.json"
echo "  $REPORT_DIR/results.xml"
