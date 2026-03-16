#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/dev-common.sh"

BACKEND_PID_FILE="$RUN_DIR/backend.pid"
FRONTEND_PID_FILE="$RUN_DIR/frontend.pid"
BACKEND_LOG_FILE="$LOG_DIR/backend.log"
FRONTEND_LOG_FILE="$LOG_DIR/frontend.log"

load_local_env() {
  local env_file="$ROOT_DIR/.env.local"
  local key
  local value

  [[ -f "$env_file" ]] || return 0

  while IFS='=' read -r key value; do
    [[ -n "$key" ]] || continue
    [[ "$key" =~ ^[[:space:]]*# ]] && continue
    [[ -n "${!key:-}" ]] && continue
    export "$key=$value"
  done < "$env_file"
}

load_local_env

APP_DB_HOST="${DB_HOST:-127.0.0.1}"
APP_DB_PORT="${DB_PORT:-3306}"
APP_DB_NAME="${DB_NAME:-study_room_db}"
APP_DB_USER="${DB_USER:-root}"
APP_DB_PASSWORD="${DB_PASSWORD:-root}"
APP_REDIS_HOST="${REDIS_HOST:-127.0.0.1}"
APP_REDIS_PORT="${REDIS_PORT:-6379}"
APP_JWT_SECRET="${JWT_SECRET:-ChangeMeToAtLeast32CharsForJwtSecretKey!}"

ensure_prerequisites() {
  require_cmd mvn
  require_cmd npm
  require_cmd pgrep
}

ensure_docker() {
  require_cmd docker
}

start_mysql() {
  if port_is_open "$APP_DB_HOST" "$APP_DB_PORT"; then
    log "检测到 MySQL 已在运行，复用 $APP_DB_HOST:$APP_DB_PORT"
    return 0
  fi

  ensure_docker
  log "MySQL 未运行，启动容器 mysql"
  docker_compose up -d mysql

  wait_for_port "$APP_DB_HOST" "$APP_DB_PORT" "MySQL 端口" 60 || fail "MySQL $APP_DB_PORT 端口未就绪"
  wait_for_mysql 120 || fail "MySQL 容器未在预期时间内完成初始化"
}

start_redis() {
  if port_is_open "$APP_REDIS_HOST" "$APP_REDIS_PORT"; then
    log "检测到 Redis 已在运行，复用 $APP_REDIS_HOST:$APP_REDIS_PORT"
    return 0
  fi

  ensure_docker
  log "Redis 未运行，启动容器 redis"
  docker_compose up -d redis

  wait_for_port "$APP_REDIS_HOST" "$APP_REDIS_PORT" "Redis 端口" 30 || fail "Redis $APP_REDIS_PORT 端口未就绪"
  wait_for_redis 60 || fail "Redis 容器未在预期时间内完成初始化"
}

validate_mysql_access() {
  if command -v mysql >/dev/null 2>&1; then
    if MYSQL_PWD="$APP_DB_PASSWORD" mysql \
      -h"$APP_DB_HOST" \
      -P"$APP_DB_PORT" \
      -u"$APP_DB_USER" \
      -D"$APP_DB_NAME" \
      -e "SELECT 1" >/dev/null 2>&1; then
      return 0
    fi

    fail "无法使用当前配置连接 MySQL $APP_DB_HOST:$APP_DB_PORT/$APP_DB_NAME，请设置 DB_USER/DB_PASSWORD/DB_NAME，或停止本机 MySQL 后重试"
  fi

  if command -v mysqladmin >/dev/null 2>&1; then
    if MYSQL_PWD="$APP_DB_PASSWORD" mysqladmin ping -h"$APP_DB_HOST" -P"$APP_DB_PORT" -u"$APP_DB_USER" --silent >/dev/null 2>&1; then
      return 0
    fi
  fi

  log "未检测到 mysql 客户端，跳过 MySQL 鉴权预检查"
}

start_backend() {
  local pid

  if pid="$(service_pid "$BACKEND_PID_FILE")"; then
    log "后端已在运行，PID=$pid"
    return 0
  fi

  if port_is_open 127.0.0.1 8080; then
    fail "8080 端口已被占用，无法自动启动后端"
  fi

  log "启动后端服务"
  (
    cd "$ROOT_DIR/backend"
    setsid env \
      DB_HOST="$APP_DB_HOST" \
      DB_PORT="$APP_DB_PORT" \
      DB_NAME="$APP_DB_NAME" \
      DB_USER="$APP_DB_USER" \
      DB_PASSWORD="$APP_DB_PASSWORD" \
      REDIS_HOST="$APP_REDIS_HOST" \
      REDIS_PORT="$APP_REDIS_PORT" \
      JWT_SECRET="$APP_JWT_SECRET" \
      mvn spring-boot:run >"$BACKEND_LOG_FILE" 2>&1 < /dev/null &
    echo $! >"$BACKEND_PID_FILE"
  )

  pid="$(read_pid "$BACKEND_PID_FILE")"
  wait_for_process_port "$pid" 127.0.0.1 8080 "后端" 180 || {
    tail -n 40 "$BACKEND_LOG_FILE" >&2 || true
    fail "后端启动失败，详见 $BACKEND_LOG_FILE"
  }
}

ensure_frontend_dependencies() {
  if [[ -d "$ROOT_DIR/frontend/node_modules" ]]; then
    return 0
  fi

  log "安装前端依赖"
  (
    cd "$ROOT_DIR/frontend"
    npm install
  )
}

start_frontend() {
  local pid

  if pid="$(service_pid "$FRONTEND_PID_FILE")"; then
    log "前端已在运行，PID=$pid"
    return 0
  fi

  if port_is_open 127.0.0.1 5173; then
    fail "5173 端口已被占用，无法自动启动前端"
  fi

  ensure_frontend_dependencies

  log "启动前端服务"
  (
    cd "$ROOT_DIR/frontend"
    setsid bash -lc 'exec ./node_modules/.bin/vite --host 0.0.0.0' >"$FRONTEND_LOG_FILE" 2>&1 < /dev/null &
    echo $! >"$FRONTEND_PID_FILE"
  )

  pid="$(read_pid "$FRONTEND_PID_FILE")"
  wait_for_process_port "$pid" 127.0.0.1 5173 "前端" 120 || {
    tail -n 40 "$FRONTEND_LOG_FILE" >&2 || true
    fail "前端启动失败，详见 $FRONTEND_LOG_FILE"
  }
}

print_summary() {
  log "全部服务已启动"
  printf '\n'
  printf '前端: http://127.0.0.1:5173\n'
  printf '后端: http://127.0.0.1:8080\n'
  printf '后端日志: %s\n' "$BACKEND_LOG_FILE"
  printf '前端日志: %s\n' "$FRONTEND_LOG_FILE"
}

main() {
  ensure_prerequisites
  start_mysql
  start_redis
  validate_mysql_access
  start_backend
  start_frontend
  print_summary
}

main "$@"
