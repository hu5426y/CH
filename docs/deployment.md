# 部署手册

## 1. 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8.0
- Redis 6.2
- Docker / Docker Compose（可选）

## 2. 数据库初始化

执行：

```bash
mysql -uroot -proot < backend/src/main/resources/db/schema.sql
mysql -uroot -proot < backend/src/main/resources/db/seed.sql
```

## 3. 后端配置

`backend/src/main/resources/application.yml` 支持环境变量覆盖：

- `DB_HOST / DB_PORT / DB_NAME / DB_USER / DB_PASSWORD`
- `REDIS_HOST / REDIS_PORT`
- `JWT_SECRET`

## 4. 启动方式

### 本地开发

使用本机 MySQL/Redis 时：

```bash
mysql -uroot -p < backend/src/main/resources/db/schema.sql
mysql -uroot -p < backend/src/main/resources/db/seed.sql

cd backend
DB_HOST=127.0.0.1 DB_PORT=3306 DB_NAME=study_room_db DB_USER=你的MySQL用户 DB_PASSWORD=你的MySQL密码 \
REDIS_HOST=127.0.0.1 REDIS_PORT=6379 \
mvn spring-boot:run

cd frontend && npm install && npm run dev
```

### WSL 自动初始化

适用于 Ubuntu on WSL：

```bash
chmod +x ./scripts/setup-wsl.sh ./scripts/dev-up.sh ./scripts/dev-down.sh
./scripts/setup-wsl.sh
./scripts/dev-up.sh
./scripts/dev-down.sh
```

说明：
- `setup-wsl.sh` 会自动检查并安装 JDK 17、Maven、Node 18+、MySQL、Redis、MySQL client。
- 脚本会启动 MySQL/Redis、创建 `study_room_db`、导入 `schema.sql` 和 `seed.sql`，并写入本地 `.env.local`。
- 默认会创建项目账号 `studyroom / studyroom`；如需自定义，可在执行前设置 `DB_USER`、`DB_PASSWORD`、`DB_NAME`、`JWT_SECRET`。
- 如果只想装环境不执行 `npm install`，可用 `SKIP_NPM_INSTALL=1 ./scripts/setup-wsl.sh`。

### Windows PowerShell

适用于没有 WSL 的 Windows 环境：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\setup.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\dev-up.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\dev-down.ps1
```

说明：
- `setup.ps1` 会检查 JDK、Maven、Node、MySQL，并初始化数据库和前端依赖。
- `dev-up.ps1` 默认复用本机 `3306/6379` 的 MySQL/Redis；端口未监听时才尝试用 Docker 容器补齐。
- 如果本机 MySQL 账号密码不是默认 `root/root`，可在执行前设置 `$env:DB_USER`、`$env:DB_PASSWORD`、`$env:DB_NAME`。

### Docker Compose

```bash
docker compose up --build
```

说明：
- 容器 MySQL 使用 `3306:3306` 端口映射。
- 容器 Redis 使用 `6379:6379` 端口映射。
- 容器内部 backend 仍通过服务名 `mysql:3306` 访问数据库，不受主机映射端口影响。
- 本地使用 `./scripts/dev-up.sh` 时，会优先复用本机已运行的 MySQL/Redis；端口未监听时才拉起容器。

## 5. 生产建议

- 将 JWT 密钥改为高强度随机值。
- MySQL/Redis 使用独立持久化卷。
- 开启 HTTPS 与网关层限流。
- 按需接入审计告警和备份策略。
