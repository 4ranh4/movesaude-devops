#!/usr/bin/env bash
# Smoke test da MoveSaúde API: confirma que a instância está saudável, no ambiente certo e respondendo.
#
# Uso: scripts/smoke-test.sh <url-base> [ambiente-esperado] [--fluxo]
#   --fluxo  também cria uma corrida, busca opções e confirma (não use em produção)
#
# Exemplo: scripts/smoke-test.sh http://localhost:8080 local --fluxo
set -euo pipefail

BASE="${1:?informe a URL base, ex.: http://localhost:8080}"
BASE="${BASE%/}"
AMBIENTE="${2:-}"
FLUXO="${3:-}"

passo() { printf '\n▶ %s\n' "$*"; }
falha() { printf '✖ %s\n' "$*" >&2; exit 1; }
ok() { printf '✔ %s\n' "$*"; }

get() { curl -fsS --retry 3 --retry-delay 5 --max-time 60 "$BASE$1"; }

passo "Health check ($BASE/actuator/health)"
PRONTA="$(get /actuator/health/readiness)"
echo "$PRONTA" | grep -q '"status":"UP"' || falha "aplicação não está pronta: $PRONTA"
BANCO="$(get /actuator/health/db)"
echo "$BANCO" | grep -q '"status":"UP"' || falha "banco de dados não está UP: $BANCO"
ok "aplicação e banco UP"

passo "Informações da instância (/api/info)"
INFO="$(get /api/info)"
echo "$INFO"
if [ -n "$AMBIENTE" ]; then
  echo "$INFO" | grep -q "\"ambiente\":\"$AMBIENTE\"" || falha "ambiente esperado: $AMBIENTE"
  ok "ambiente = $AMBIENTE"
fi
echo "$INFO" | grep -q '"banco":"PostgreSQL' || falha "a instância não está usando PostgreSQL"
ok "banco = PostgreSQL"

passo "Endpoints de leitura"
for rota in /api/prestadores /api/passageiros /api/convenios/planos /api/corridas /api/esg/indicadores /v3/api-docs; do
  get "$rota" > /dev/null || falha "falhou: $rota"
  ok "GET $rota"
done
get /api/prestadores | grep -q '"nomeFantasia"' || falha "nenhum prestador cadastrado"

if [ "$FLUXO" = "--fluxo" ]; then
  passo "Fluxo de corrida: solicitar → opções → confirmar"
  PASSAGEIRO="$(get /api/passageiros | grep -o '"id":[0-9]*,"nome":"Lúcia Ferreira"' | grep -o '[0-9]*' | head -1)"
  [ -n "$PASSAGEIRO" ] || falha "passageira de demonstração não encontrada"
  DATA="$(date -d '+2 days' +%F 2>/dev/null || date -v+2d +%F)"
  CORRIDA="$(curl -fsS -X POST "$BASE/api/corridas" -H 'Content-Type: application/json' \
    -d "{\"passageiroId\":$PASSAGEIRO,\"origem\":\"Smoke test - origem\",\"destino\":\"Smoke test - destino\",\"distanciaKm\":6.5,\"data\":\"$DATA\",\"hora\":\"10:00\",\"idaEVolta\":false,\"finalidade\":\"CONSULTA\"}")"
  ID="$(echo "$CORRIDA" | grep -o '^{"id":[0-9]*' | grep -o '[0-9]*')"
  [ -n "$ID" ] || falha "corrida não criada: $CORRIDA"
  ok "corrida $ID criada"
  OPCOES="$(get "/api/corridas/$ID/opcoes")"
  PRESTADOR="$(echo "$OPCOES" | grep -o '"opcoes":\[{"prestadorId":[0-9]*' | grep -o '[0-9]*$')"
  [ -n "$PRESTADOR" ] || falha "matching sem opções: $OPCOES"
  ok "matching sugeriu o prestador $PRESTADOR"
  curl -fsS -X POST "$BASE/api/corridas/$ID/confirmacao" -H 'Content-Type: application/json' \
    -d "{\"prestadorId\":$PRESTADOR}" | grep -q '"status":"CONFIRMADA"' || falha "confirmação falhou"
  ok "corrida $ID confirmada"
fi

printf '\n✅ Smoke test aprovado em %s\n' "$BASE"
