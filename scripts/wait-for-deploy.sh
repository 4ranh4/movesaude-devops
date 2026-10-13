#!/usr/bin/env bash
# Aguarda o Render colocar no ar o commit que o pipeline acabou de enviar.
# Compara o campo "commit" de /api/info com o SHA esperado.
#
# Uso: scripts/wait-for-deploy.sh <url-base> <sha-esperado> [timeout-segundos]
set -uo pipefail

BASE="${1:?informe a URL base}"
BASE="${BASE%/}"
SHA="${2:?informe o SHA esperado}"
TIMEOUT="${3:-1200}"
INTERVALO=20
INICIO=$(date +%s)

echo "Aguardando $BASE servir o commit ${SHA:0:12} (timeout ${TIMEOUT}s)..."
while true; do
  INFO="$(curl -fsS --max-time 60 "$BASE/api/info" 2>/dev/null || true)"
  ATUAL="$(echo "$INFO" | grep -o '"commit":"[^"]*"' | cut -d'"' -f4)"
  DECORRIDO=$(( $(date +%s) - INICIO ))
  if [ "$ATUAL" = "$SHA" ]; then
    echo "✔ Deploy concluído em ${DECORRIDO}s: $INFO"
    exit 0
  fi
  if [ "$DECORRIDO" -ge "$TIMEOUT" ]; then
    echo "✖ Timeout: o serviço ainda responde com o commit '${ATUAL:-sem resposta}'" >&2
    exit 1
  fi
  echo "  [${DECORRIDO}s] commit atual: ${ATUAL:-sem resposta (build/deploy em andamento)}"
  sleep "$INTERVALO"
done
