#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env.local"

DB_HOST="${DB_HOST:-127.0.0.1}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-study_room_db}"
DB_USER="${DB_USER:-studyroom}"
DB_PASSWORD="${DB_PASSWORD:-studyroom}"
REDIS_HOST="${REDIS_HOST:-127.0.0.1}"
REDIS_PORT="${REDIS_PORT:-6379}"
JWT_SECRET="${JWT_SECRET:-ChangeMeToAtLeast32CharsForJwtSecretKey!}"
SKIP_NPM_INSTALL="${SKIP_NPM_INSTALL:-0}"

APT_UPDATED=0
NODE_REPO_CONFIGURED=0

log() {
  printf '[%s] %s\n' "$(date '+%H:%M:%S')" "$*"
}

fail() {
  log "ERROR: $*" >&2
  exit 1
}

run_root() {
  if [[ "${EUID:-$(id -u)}" -eq 0 ]]; then
    "$@"
  else
    sudo "$@"
  fi
}

command_exists() {
  command -v "$1" >/dev/null 2>&1
}

apt_update() {
  if [[ "$APT_UPDATED" -eq 0 ]]; then
    log "更新 apt 索引"
    run_root apt-get update
    APT_UPDATED=1
  fi
}

install_apt_packages() {
  apt_update
  log "安装: $*"
  run_root apt-get install -y "$@"
}

java_major_version() {
  command_exists java || return 1
  java -version 2>&1 | sed -n 's/.*version "\([0-9][0-9]*\).*/\1/p' | head -n 1
}

maven_version() {
  command_exists mvn || return 1
  mvn -v 2>/dev/null | awk '/Apache Maven/ {print $3; exit}'
}

node_major_version() {
  command_exists node || return 1
  node -v 2>/dev/null | sed 's/^v//' | cut -d. -f1
}

mysql_server_major_version() {
  command_exists mysqld || return 1
  if mysqld --version 2>/dev/null | grep -qi 'MariaDB'; then
    return 1
  fi
  mysqld --version 2>/dev/null | sed -n 's/.*Ver \([0-9][0-9]*\)\..*/\1/p' | head -n 1
}

redis_major_version() {
  command_exists redis-server || return 1
  redis-server --version 2>/dev/null | sed -n 's/.*v=\([0-9][0-9]*\)\..*/\1/p' | head -n 1
}

configure_nodesource_repo() {
  [[ "$NODE_REPO_CONFIGURED" -eq 0 ]] || return 0
  install_apt_packages ca-certificates curl gnupg
  log "配置 NodeSource 20.x 仓库"
  curl -fsSL https://deb.nodesource.com/setup_20.x | run_root bash
  NODE_REPO_CONFIGURED=1
}

ensure_java() {
  local version
  version="$(java_major_version || true)"
  if [[ -n "$version" && "$version" -ge 17 ]]; then
    log "JDK 已满足要求: $version"
    return 0
  fi
  install_apt_packages openjdk-17-jdk
}

ensure_maven() {
  local version
  version="$(maven_version || true)"
  if [[ -n "$version" ]] && dpkg --compare-versions "$version" ge "3.8.0"; then
    log "Maven 已满足要求: $version"
    return 0
  fi
  install_apt_packages maven
}

ensure_node() {
  local version
  version="$(node_major_version || true)"
  if [[ -n "$version" && "$version" -ge 18 ]]; then
    log "Node.js 已满足要求: $version"
    return 0
  fi
  configure_nodesource_repo
  install_apt_packages nodejs
}

ensure_mysql() {
  local version
  version="$(mysql_server_major_version || true)"
  if [[ -n "$version" && "$version" -ge 8 ]]; then
    log "MySQL 已满足要求: $version"
    install_apt_packages mysql-client
    return 0
  fi
  install_apt_packages mysql-server mysql-client
}

ensure_redis() {
  local version
  version="$(redis_major_version || true)"
  if [[ -n "$version" && "$version" -ge 6 ]]; then
    log "Redis 已满足要求: $version"
    return 0
  fi
  install_apt_packages redis-server
}

service_uses_systemd() {
  command_exists systemctl && [[ -d /run/systemd/system ]]
}

start_service() {
  local service_name="$1"
  if service_uses_systemd; then
    run_root systemctl enable --now "$service_name"
  else
    run_root service "$service_name" start
  fi
}

wait_for_port() {
  local host="$1"
  local port="$2"
  local name="$3"
  local timeout="${4:-30}"

  for ((i = 0; i < timeout; i++)); do
    if (echo >"/dev/tcp/$host/$port") >/dev/null 2>&1; then
      log "$name 已就绪: $host:$port"
      return 0
    fi
    sleep 1
  done

  fail "$name 未在预期时间内就绪: $host:$port"
}

configure_mysql_for_project() {
  log "配置 MySQL 数据库和账号"
  run_root mysql <<SQL
CREATE DATABASE IF NOT EXISTS ${DB_NAME} DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '${DB_USER}'@'127.0.0.1' IDENTIFIED BY '${DB_PASSWORD}';
CREATE USER IF NOT EXISTS '${DB_USER}'@'localhost' IDENTIFIED BY '${DB_PASSWORD}';
GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'127.0.0.1';
GRANT ALL PRIVILEGES ON ${DB_NAME}.* TO '${DB_USER}'@'localhost';
FLUSH PRIVILEGES;
SQL
}

initialize_database() {
  local schema_file="$ROOT_DIR/backend/src/main/resources/db/schema.sql"
  local seed_file="$ROOT_DIR/backend/src/main/resources/db/seed.sql"

  log "导入数据库结构和种子数据"
  MYSQL_PWD="$DB_PASSWORD" mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" < "$schema_file"
  MYSQL_PWD="$DB_PASSWORD" mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" < "$seed_file"
}

install_frontend_dependencies() {
  if [[ "$SKIP_NPM_INSTALL" == "1" ]]; then
    log "跳过前端依赖安装"
    return 0
  fi

  log "安装前端依赖"
  (
    cd "$ROOT_DIR/frontend"
    npm install
  )
}

write_env_file() {
  log "写入本地环境文件 $ENV_FILE"
  cat > "$ENV_FILE" <<EOF
DB_HOST=$DB_HOST
DB_PORT=$DB_PORT
DB_NAME=$DB_NAME
DB_USER=$DB_USER
DB_PASSWORD=$DB_PASSWORD
REDIS_HOST=$REDIS_HOST
REDIS_PORT=$REDIS_PORT
JWT_SECRET=$JWT_SECRET
EOF
}

main() {
  if [[ ! -f /proc/version ]] || ! grep -qi 'microsoft' /proc/version; then
    log "警告: 当前环境看起来不是 WSL，脚本仍会继续执行"
  fi

  ensure_java
  ensure_maven
  ensure_node
  ensure_mysql
  ensure_redis

  log "启动 MySQL 和 Redis 服务"
  start_service mysql
  start_service redis-server
  wait_for_port "$DB_HOST" "$DB_PORT" "MySQL" 30
  wait_for_port "$REDIS_HOST" "$REDIS_PORT" "Redis" 30

  configure_mysql_for_project
  initialize_database
  install_frontend_dependencies
  write_env_file

  log "WSL 环境初始化完成"
  printf '\n'
  printf '后续可直接执行:\n'
  printf './scripts/dev-up.sh\n'
}

main "$@"
