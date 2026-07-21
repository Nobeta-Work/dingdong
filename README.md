
## 叮咚商城

<p align="center">
  <img src="frontend/public/DingDongLogo.png" width="120" alt="叮咚商城图标" />
</p>

叮咚商城是一个基于 Spring Cloud Alibaba (2025.0.0.0) 开发的前后端分离电商项目。

## 技术栈

**前端：** *Vue3、TypeScript、Vite、Element-Plus*

**后端：**
- *Java 21*
- *Spring Boot v3.5.0*
- *Spring Cloud v2025.0.0*
- ***Spring Cloud Alibaba | v2025.0.0.0***
- - *Nacos |v3.0.3* : 服务注册配置中心
- - *LoadBalancer* : 请求转发负载均衡组件
- - *Dubbo* : 远程过程调用 (RPC) 中间件
- - *RocketMQ | v5.3.1* : 消息队列中间件
- *Redis | v7.4* : 缓存中间件
- *MyBatis* : 数据库连接层
- *MySQL* : 业务数据持久化

## 本地部署

> 后端采用 Docker Compose 编排，包含五个 Java 服务及 MySQL、Redis、Nacos、RocketMQ。前端采用前后端分离部署，独立构建和发布；当前及后续版本均不会把前端纳入本项目 Docker Compose。

### 前置条件

- Docker Desktop 已启动；
- Docker Compose 构建后端镜像时无需本机安装 Java 与 Maven；本地调试 Java 服务时需要 Java 21 与 Maven 3.9+；
- 本机端口 `3306`、`6379`、`8848`、`9876`、`8080-8084` 未被占用。

### 启动后端

```powershell
docker compose up -d --build
docker compose ps
```

该命令会构建并启动 Gateway、用户、商品、订单、支付五个 Java 服务，以及全部后端中间件。首次启动会创建四个逻辑数据库：`dingdong_user`、`dingdong_product`、`dingdong_order`、`dingdong_pay`。

| 组件 | 地址 | 默认凭证 |
|---|---|---|
| MySQL | `127.0.0.1:3306` | `dingdong` / `dingdong_dev` |
| Redis | `127.0.0.1:6379` | 密码：`redis_dev` |
| Nacos 服务端 | `127.0.0.1:8848` | 开发基线关闭鉴权 |
| Nacos 控制台 | `http://127.0.0.1:8085/` | 开发基线关闭鉴权 |
| RocketMQ NameServer | `127.0.0.1:9876` | 无 |
| Gateway | `http://127.0.0.1:8080` | 前端统一 API 入口 |

首次初始化会创建开发管理员：`admin` / `password`。仅用于本地演示，公网环境必须替换密码。

可从 `.env.example` 复制为 `.env` 覆盖默认开发凭证；`.env` 不会提交到 Git。

启动 Java 服务时，`JWT_SECRET` 必须在 Gateway、用户服务与商品服务保持一致；当前默认值仅适用于本地开发。

### 测试数据

`infra/mysql/init/06-test-data.sql` 会在 **首次创建 MySQL 数据卷** 时自动执行。它会生成 30 个上架商品与 SKU、演示用户和地址，以及覆盖待支付、已支付、已发货、已完成、已取消状态的订单、库存锁定和支付数据。购物车存储在 Redis，由登录用户通过接口产生，不再写入 MySQL 初始化数据。

演示账户密码均为 `password`：管理员使用 `demo_admin`，买家可使用 `demo_buyer_01` 至 `demo_buyer_07`；`demo_buyer_disabled` 用于验证禁用账户。商品图片统一使用测试商品图，用户及管理端头像使用测试头像。

已有 MySQL 数据卷不会重复执行初始化脚本。需要手动导入时，在项目根目录执行：

```powershell
Get-Content -Raw infra/mysql/init/06-test-data.sql | docker compose exec -T mysql mysql -udingdong -pdingdong_dev
```

脚本可重复执行；如需完全重置本地数据，请先删除 Compose 数据卷后再启动中间件。

### 前端独立运行与部署

前端不属于 Compose 生命周期。开发环境单独启动 Vite，并把 `VITE_API_BASE_URL` 指向 Gateway；生产环境将 `frontend` 构建产物独立发布到静态站点或 CDN，并把 `/api` 反向代理到 Gateway。Gateway 默认允许本机 `localhost` 与 `127.0.0.1` 开发源跨域，部署环境可通过 `CORS_ALLOWED_ORIGIN_*` 收紧来源。

```powershell
cd frontend
npm install
$env:VITE_API_BASE_URL='http://127.0.0.1:8080/api'
npm run dev

# 独立生产构建
npm run build
```

> 前端与后端镜像解耦，后续也不会增加 frontend Compose service。

### Java 服务本机调试（可选）

```powershell
cd backend
mvn clean verify

# 依次启动，示例：Gateway
mvn -pl gateway-service spring-boot:run -Dspring-boot.run.arguments="--NACOS_ENABLED=true"
```

服务端口：Gateway `8080`、用户 `8081`、商品 `8082`、订单 `8083`、支付 `8084`。服务健康检查为 `/actuator/health`。

## 文档说明
[Git 流程说明](docs/GIT.md)

[需求文档](docs/PRD.md)

[开发迭代说明](docs/TEAM.md)

[v0.2 接口契约](docs/API-v0.2.md)

[v0.3 交易接口契约](docs/API-v0.3.md)

[v0.4 支付与履约接口契约](docs/API-v0.4.md)

[v0.5 可靠性说明](docs/API-v0.5.md)

