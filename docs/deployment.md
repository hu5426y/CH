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
