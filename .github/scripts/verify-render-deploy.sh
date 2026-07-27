#!/usr/bin/env bash
set -euo pipefail

require_environment_variable() {
  local variable_name="$1"

  if [ -z "${!variable_name:-}" ]; then
    echo "::error::${variable_name} is required for tracked Render deployment."
    exit 1
  fi
}

require_environment_variable "RENDER_DEPLOY_HOOK_URL"
require_environment_variable "RENDER_API_KEY"
require_environment_variable "RENDER_SERVICE_ID"
require_environment_variable "RENDER_HEALTH_URL"
require_environment_variable "GITHUB_SHA"

DEPLOY_RESPONSE_FILE="$(mktemp)"
DEPLOY_STATUS_FILE="$(mktemp)"
HEALTH_RESPONSE_FILE="$(mktemp)"

cleanup() {
  rm -f "${DEPLOY_RESPONSE_FILE}" "${DEPLOY_STATUS_FILE}" "${HEALTH_RESPONSE_FILE}"
}
trap cleanup EXIT

if [[ "${RENDER_DEPLOY_HOOK_URL}" == *"?"* ]]; then
  DEPLOY_URL="${RENDER_DEPLOY_HOOK_URL}&ref=${GITHUB_SHA}"
else
  DEPLOY_URL="${RENDER_DEPLOY_HOOK_URL}?ref=${GITHUB_SHA}"
fi

HTTP_STATUS="$(
  curl \
    --silent \
    --show-error \
    --request POST \
    --output "${DEPLOY_RESPONSE_FILE}" \
    --write-out "%{http_code}" \
    "${DEPLOY_URL}"
)"

if [ "${HTTP_STATUS}" = "202" ]; then
  echo "::error::Render queued the deploy behind another deployment and did not return a deploy ID."
  exit 1
fi

if [ "${HTTP_STATUS}" != "200" ]; then
  echo "::error::Render rejected the deploy hook request with HTTP ${HTTP_STATUS}."
  exit 1
fi

DEPLOY_ID="$(jq --raw-output '.deploy.id // .id // empty' "${DEPLOY_RESPONSE_FILE}")"

if [ -z "${DEPLOY_ID}" ]; then
  echo "::error::Render accepted the deploy but did not return a trackable deploy ID."
  exit 1
fi

echo "Tracking Render deploy ${DEPLOY_ID}."

DEPLOY_LIVE=false

for attempt in $(seq 1 100); do
  API_STATUS="$(
    curl \
      --silent \
      --show-error \
      --header "Accept: application/json" \
      --header "Authorization: Bearer ${RENDER_API_KEY}" \
      --output "${DEPLOY_STATUS_FILE}" \
      --write-out "%{http_code}" \
      "https://api.render.com/v1/services/${RENDER_SERVICE_ID}/deploys/${DEPLOY_ID}"
  )"

  if [ "${API_STATUS}" = "429" ]; then
    sleep 15
    continue
  fi

  if [ "${API_STATUS}" != "200" ]; then
    echo "::error::Render deploy-status request failed with HTTP ${API_STATUS}."
    exit 1
  fi

  DEPLOY_STATUS="$(jq --raw-output '.status // .deploy.status // empty' "${DEPLOY_STATUS_FILE}")"

  if [ -z "${DEPLOY_STATUS}" ]; then
    echo "::error::Render deploy-status response did not include a status."
    exit 1
  fi

  echo "Render deploy status: ${DEPLOY_STATUS}"

  case "${DEPLOY_STATUS}" in
    live)
      DEPLOY_LIVE=true
      break
      ;;
    build_failed|update_failed|pre_deploy_failed|canceled|deactivated)
      echo "::error::Render deployment failed with status ${DEPLOY_STATUS}. Review the Render deploy logs."
      exit 1
      ;;
    created|build_in_progress|pre_deploy_in_progress|update_in_progress)
      sleep 10
      ;;
    *)
      echo "::error::Render returned an unsupported deploy status: ${DEPLOY_STATUS}."
      exit 1
      ;;
  esac
done

if [ "${DEPLOY_LIVE}" != "true" ]; then
  echo "::error::Render deployment did not become live before the CI timeout."
  exit 1
fi

for attempt in $(seq 1 18); do
  if curl \
    --fail \
    --silent \
    --show-error \
    --max-time 10 \
    --output "${HEALTH_RESPONSE_FILE}" \
    "${RENDER_HEALTH_URL}" \
    && jq --exit-status '.status == "UP"' "${HEALTH_RESPONSE_FILE}" >/dev/null; then
    echo "Render deployment is live and the production health endpoint is UP."
    exit 0
  fi

  sleep 10
done

echo "::error::Render marked the deployment live, but the production health endpoint did not become healthy."
exit 1
