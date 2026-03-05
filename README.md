# 基于 SpringBoot 的校园自习室座位预约系统

本项目依据《基于SpringBoot的校园自习室座位预约系统设计与实现.docx》进行工程化落地，采用前后端分离架构：
- 后端：Spring Boot 2.7 + Spring Security + JWT + MyBatis-Plus + Redis + MySQL
- 前端：Vue 3 + Element Plus + Pinia + Axios + Vue Router + ECharts
- 部署：Docker + Docker Compose

## 目录结构

```text
.
├── backend                 # SpringBoot 后端
│   ├── src/main/java/...   # controller/service/mapper/entity/config
│   └── src/main/resources/db
│       ├── schema.sql      # 建表脚本
│       └── seed.sql        # 初始化数据
├── frontend                # Vue3 前端
├── docs                    # 项目文档
└── docker-compose.yml      # 一键启动
```

## 覆盖的核心需求

- 角色与权限：学生/管理员分权，JWT 认证，接口级权限控制。
- 用户管理：注册登录、个人信息维护、管理员启停账号/重置密码。
- 自习室与座位管理：自习室 CRUD、座位 CRUD、状态维护。
- 座位查询与预约：按条件筛选、预约/取消/签到/签离。
- 违约处理：超时未签到自动判违约、信用分扣减、管理员撤销违约。
- 规则配置：最大预约时长、提前预约、签到超时、信用阈值、扣分规则可配置。
- 数据统计：系统总览、自习室使用统计、预约趋势图。
- 系统日志：AOP 自动记录关键操作日志。
- 非功能：Redis 缓存座位状态、IP 限流、全局异常处理、容器化部署。

## 快速启动（本地）

### 1) 启动 MySQL + Redis

```bash
docker compose up -d mysql redis
```
说明：容器 MySQL 映射为主机 `3307` 端口，避免与本机 MySQL(`3306`)冲突。

### 2) 启动后端

```bash
cd backend
mvn spring-boot:run
```

### 3) 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端访问：`http://127.0.0.1:5173`

## 一键容器启动

```bash
docker compose up --build
```

访问：`http://127.0.0.1:5173`

## 无 Docker 启动

如果你没有配置 Docker，可直接使用本机 MySQL/Redis：

1. 启动本机 MySQL 和 Redis
```bash
sudo systemctl start mysql
sudo systemctl start redis-server
```

2. 准备数据库
```bash
mysql -uroot -p < backend/src/main/resources/db/schema.sql
mysql -uroot -p < backend/src/main/resources/db/seed.sql
```

3. 启动后端（按你的本机账号密码替换）
```bash
cd backend
DB_HOST=127.0.0.1 DB_PORT=3306 DB_NAME=study_room_db DB_USER=你的MySQL用户 DB_PASSWORD=你的MySQL密码 \
REDIS_HOST=127.0.0.1 REDIS_PORT=6379 \
mvn spring-boot:run
```

4. 启动前端
```bash
cd frontend
npm install
npm run dev
```

5. 访问地址
```text
前端: http://127.0.0.1:5173
后端: http://127.0.0.1:8080
```

## 默认账号

- 管理员：`admin / password`
- 学生：`20230001 / password`

## 详细文档

- [架构设计](docs/architecture.md)
- [接口清单](docs/api.md)
- [部署手册](docs/deployment.md)
- [测试方案](docs/test-plan.md)
