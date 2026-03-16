#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/dev-common.sh"

BACKEND_PID_FILE="$RUN_DIR/backend.pid"
FRONTEND_PID_FILE="$RUN_DIR/frontend.pid"

find_pids_by_pattern() {
  local pattern="$1"
  pgrep -f "$pattern" 2>/dev/null || true
}

stop_pid_list() {
  local name="$1"
  shift
  local pids=("$@")
  local pid

  if [[ "${#pids[@]}" -eq 0 ]]; then
    return 0
  fi

  log "停止 $name，PID=${pids[*]}"
  kill "${pids[@]}" 2>/dev/null || true

  for pid in "${pids[@]}"; do
    if ! wait_for_pid_exit "$pid" 20; then
      log "$name 进程 $pid 未在 20 秒内退出，执行强制结束"
      kill -9 "$pid" 2>/dev/null || true
      wait_for_pid_exit "$pid" 5 || true
    fi
  done
}

stop_service_group() {
  local name="$1"
  local group_id="$2"

  log "停止 $name，进程组=$group_id"
  kill -- "-$group_id" 2>/dev/null || true

  if ! wait_for_pid_exit "$group_id" 20; then
    log "$name 进程组 $group_id 未在 20 秒内退出，执行强制结束"
    kill -9 -- "-$group_id" 2>/dev/null || true
    wait_for_pid_exit "$group_id" 5 || true
  fi
}

stop_pid_service() {
  local name="$1"
  local pid_file="$2"
  local fallback_pattern="$3"
  local pid
  local fallback_pids=()

  if pid="$(service_pid "$pid_file")"; then
    stop_service_group "$name" "$pid"
    rm -f "$pid_file"
    return 0
  fi

  while IFS= read -r pid; do
    [[ -n "$pid" ]] && fallback_pids+=("$pid")
  done < <(find_pids_by_pattern "$fallback_pattern")

  if [[ "${#fallback_pids[@]}" -eq 0 ]]; then
    rm -f "$pid_file"
    log "$name 未运行"
    return 0
  fi

  stop_pid_list "$name" "${fallback_pids[@]}"
  rm -f "$pid_file"
}

stop_infra() {
  require_cmd docker
  log "停止 MySQL 和 Redis 容器"
  docker_compose stop mysql redis >/dev/null 2>&1 || true
}

main() {
  require_cmd pgrep
  stop_pid_service "前端" "$FRONTEND_PID_FILE" "/home/huge/dev/CH/frontend/.*/vite --host 0.0.0.0|node_modules/.bin/vite --host 0.0.0.0"
  stop_pid_service "后端" "$BACKEND_PID_FILE" "spring-boot:run|StudyRoomApplication"
  stop_infra
  log "全部服务已停止"
}

main "$@"
