#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$ROOT_DIR/logs"
RUN_DIR="$ROOT_DIR/run"
COMPOSE_FILE="$ROOT_DIR/docker-compose.yml"

mkdir -p "$LOG_DIR" "$RUN_DIR"

log() {
  printf '[%s] %s\n' "$(date '+%H:%M:%S')" "$*"
}

fail() {
  log "ERROR: $*" >&2
  exit 1
}

require_cmd() {
  local cmd="$1"
  command -v "$cmd" >/dev/null 2>&1 || fail "缺少命令: $cmd"
}

docker_compose() {
  docker compose -f "$COMPOSE_FILE" "$@"
}

read_pid() {
  local pid_file="$1"
  [[ -f "$pid_file" ]] || return 1
  tr -d '[:space:]' < "$pid_file"
}

pid_is_running() {
  local pid="$1"
  [[ -n "$pid" ]] && kill -0 "$pid" 2>/dev/null
}

group_has_processes() {
  local pgid="$1"
  [[ -n "$pgid" ]] && pgrep -g "$pgid" >/dev/null 2>&1
}

tracked_process_is_running() {
  local pid_or_group="$1"
  group_has_processes "$pid_or_group" || pid_is_running "$pid_or_group"
}

cleanup_pidfile_if_stale() {
  local pid_file="$1"
  local pid

  pid="$(read_pid "$pid_file" 2>/dev/null || true)"
  if [[ -n "$pid" ]] && ! tracked_process_is_running "$pid"; then
    rm -f "$pid_file"
  fi
}

service_pid() {
  local pid_file="$1"
  local pid

  cleanup_pidfile_if_stale "$pid_file"
  pid="$(read_pid "$pid_file" 2>/dev/null || true)"
  if [[ -n "$pid" ]] && tracked_process_is_running "$pid"; then
    printf '%s' "$pid"
    return 0
  fi
  return 1
}

port_is_open() {
  local host="$1"
  local port="$2"
  (echo >"/dev/tcp/$host/$port") >/dev/null 2>&1
}

wait_for_port() {
  local host="$1"
  local port="$2"
  local name="$3"
  local timeout="${4:-60}"
  local elapsed

  for ((elapsed = 0; elapsed < timeout; elapsed++)); do
    if port_is_open "$host" "$port"; then
      log "$name 已就绪: $host:$port"
      return 0
    fi
    sleep 1
  done

  return 1
}

wait_for_pid_exit() {
  local pid="$1"
  local timeout="${2:-20}"
  local elapsed

  for ((elapsed = 0; elapsed < timeout; elapsed++)); do
    if ! tracked_process_is_running "$pid"; then
      return 0
    fi
    sleep 1
  done

  return 1
}

wait_for_mysql() {
  local timeout="${1:-120}"
  local elapsed

  for ((elapsed = 0; elapsed < timeout; elapsed++)); do
    if docker_compose exec -T mysql mysqladmin ping -uroot -proot --silent >/dev/null 2>&1; then
      log "MySQL 已就绪"
      return 0
    fi
    sleep 1
  done

  return 1
}

wait_for_redis() {
  local timeout="${1:-60}"
  local elapsed

  for ((elapsed = 0; elapsed < timeout; elapsed++)); do
    if [[ "$(docker_compose exec -T redis redis-cli ping 2>/dev/null || true)" == "PONG" ]]; then
      log "Redis 已就绪"
      return 0
    fi
    sleep 1
  done

  return 1
}

wait_for_process_port() {
  local pid="$1"
  local host="$2"
  local port="$3"
  local name="$4"
  local timeout="${5:-120}"
  local elapsed

  for ((elapsed = 0; elapsed < timeout; elapsed++)); do
    if port_is_open "$host" "$port"; then
      log "$name 已启动: $host:$port"
      return 0
    fi
    if ! tracked_process_is_running "$pid"; then
      return 1
    fi
    sleep 1
  done

  return 1
}
