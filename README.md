
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

## 本地启动流程

> 项目采用前后端分离部署。当前 Docker Compose 只负责 MySQL、Redis、Nacos 与 RocketMQ 等后端中间件；Java 服务在本机手动启动，前端独立安装、构建与发布，后续也不得加入 Docker Compose。

### 前置条件

- Docker Desktop 已启动；
- Java 21 与 Maven 3.9+；
- Node.js 20+ 与 npm；
- 本机端口 `3306`、`6379`、`8848`、`9876`、`8080-8085`、`5173` 未被占用。

### 1. 启动中间件

在项目根目录执行：


```powershell
docker compose up -d
docker compose ps
```

等待 MySQL、Redis、Nacos 和 RocketMQ 启动完成后，再启动 Java 服务。Compose 不包含前端，也不负责当前开发流程中的 Java 进程。

首次启动会创建四个逻辑数据库：`dingdong_user`、`dingdong_product`、`dingdong_order`、`dingdong_pay`。

| 组件 | 地址 | 默认凭证 |
|---|---|---|
| MySQL | `127.0.0.1:3306` | `dingdong` / `dingdong_dev` |
| Redis | `127.0.0.1:6379` | 密码：`redis_dev` |
| Nacos 服务端 | `127.0.0.1:8848` | 开发基线关闭鉴权 |
| Nacos 控制台 | `http://127.0.0.1:8085/` | 开发基线关闭鉴权 |
| RocketMQ NameServer | `127.0.0.1:9876` | 无 |

首次初始化会创建开发管理员：`admin` / `password`。仅用于本地演示，公网环境必须替换密码。

可从 `.env.example` 复制为 `.env` 覆盖默认开发凭证；`.env` 不会提交到 Git。

启动 Java 服务时，`JWT_SECRET` 必须在 Gateway、用户服务与商品服务保持一致；当前默认值仅适用于本地开发。

商品图片与品牌 Logo 通过 GitHub Contents API 上传到图床仓库。启动 `product-service` 前，需要在当前终端设置：

```powershell
$env:GITHUB_IMAGE_BED_REPO="owner/image-bed-repository"
$env:GITHUB_IMAGE_BED_TOKEN="github-token-with-contents-write-permission"
$env:GITHUB_IMAGE_BED_BRANCH="main"
$env:GITHUB_IMAGE_BED_FOLDER="product-images"
```

Token 只授予目标仓库的 Contents 读写权限，禁止写入仓库配置或提交到 Git。未配置时，商品与品牌仍可手工填写公开图片 URL，但上传接口会明确返回配置错误。

短信验证码当前采用 Mock 模式：第三方短信签名与模板资质尚未通过，验证码仍写入 Redis 并执行 5 分钟过期、60 秒频控，同时由接口返回给前端用于登录和手机号换绑演示。真实支付与真实快递查询均不接入，支付成功和物流发货使用明确标注的模拟流程。

### 测试数据

`infra/mysql/init/06-test-data.sql` 会在 **首次创建 MySQL 数据卷** 时自动执行。它会生成 30 个上架商品与 SKU、演示用户和地址、购物车，以及覆盖待支付、已支付、已发货、已完成、已取消状态的订单、库存锁定和支付数据。

演示账户密码均为 `password`：管理员使用 `demo_admin`，买家可使用 `demo_buyer_01` 至 `demo_buyer_07`；`demo_buyer_disabled` 用于验证禁用账户。商品图片统一使用测试商品图，用户及管理端头像使用测试头像。

已有 MySQL 数据卷不会重复执行初始化脚本。需要手动导入时，在项目根目录执行：

```powershell
Get-Content -Raw infra/mysql/init/06-test-data.sql | docker compose exec -T mysql mysql -udingdong -pdingdong_dev
```

脚本可重复执行；如需完全重置本地数据，请先删除 Compose 数据卷后再启动中间件。

从 v0.5 保留数据卷升级到 v0.6 时，先执行幂等增量结构：

```powershell
Get-Content -Raw infra/mysql/init/07-v0.6-schema.sql | docker compose exec -T mysql mysql -udingdong -pdingdong_dev
```

### 2. 构建后端

```powershell
cd backend
mvn clean verify
```

构建成功后，各服务的可执行 JAR 位于对应模块的 `target` 目录。

### 3. 手动启动 Java 服务

保持当前目录为 `backend`，分别打开五个终端，按以下顺序启动。每条命令应保持运行：

```powershell
# 终端 1：用户服务
java -jar user-service/target/user-service-0.1.0-SNAPSHOT.jar

# 终端 2：商品服务
java -jar product-service/target/product-service-0.1.0-SNAPSHOT.jar

# 终端 3：订单与购物车服务
java -jar order-service/target/order-service-0.1.0-SNAPSHOT.jar

# 终端 4：支付服务
java -jar pay-service/target/pay-service-0.1.0-SNAPSHOT.jar

# 终端 5：API 网关
java -jar gateway-service/target/gateway-service-0.1.0-SNAPSHOT.jar
```

本地端口如下：

| 服务 | 端口 | 健康检查 |
|---|---:|---|
| API Gateway | `8080` | `http://127.0.0.1:8080/actuator/health` |
| 用户服务 | `8081` | `http://127.0.0.1:8081/actuator/health` |
| 商品服务 | `8082` | `http://127.0.0.1:8082/actuator/health` |
| 订单与购物车服务 | `8083` | `http://127.0.0.1:8083/actuator/health` |
| 支付服务 | `8084` | `http://127.0.0.1:8084/actuator/health` |

启动后可通过网关验证商品接口：

```powershell
Invoke-RestMethod http://127.0.0.1:8080/api/products?page=1`&size=5
```

### 4. 独立启动前端

前端不依赖 Docker Compose。在新的终端中回到项目根目录并执行：

```powershell
cd frontend
npm install
npm run dev
```

开发地址默认为 `http://127.0.0.1:5173/`，开发代理会将 `/api` 请求转发至 `http://127.0.0.1:8080`。

生产发布只生成静态文件，由独立 Web 服务器部署：

```powershell
cd frontend
npm run build
```

构建产物位于 `frontend/dist`，不要将前端服务或构建步骤加入 `compose.yaml`。

### 5. 关闭服务

先在各 Java 和前端终端中按 `Ctrl+C` 停止进程，再在项目根目录关闭中间件：

```powershell
docker compose down
```

该命令保留 MySQL 与 Redis 数据卷。只有明确需要重置全部本地数据时才执行：

```powershell
docker compose down -v
```

### 开发方式补充

如需热更新和断点调试，也可以在后端构建完成后，从 `backend` 目录分别运行对应模块：

```powershell
mvn -pl gateway-service spring-boot:run
```

其他模块将 `gateway-service` 替换为 `user-service`、`product-service`、`order-service` 或 `pay-service`。多个服务仍需分别占用独立终端。

## 文档说明
[Git 流程说明](docs/GIT.md)

[需求文档](docs/PRD.md)

[开发迭代说明](docs/TEAM.md)

[v0.2 接口契约](docs/API-v0.2.md)

[v0.3 交易接口契约](docs/API-v0.3.md)

[v0.4 支付与履约接口契约](docs/API-v0.4.md)

[v0.5 可靠性说明](docs/API-v0.5.md)

[v0.6 并发与秒杀接口](docs/API-v0.6.md)

