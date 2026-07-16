
## 叮咚商城

面向实训交付与 Java 后端求职展示的前后端分离微服务商城。详细范围见 [PRD](docs/PRD.md)，开发迭代说明见 [TEAM](docs/TEAM.md)。

## 技术栈

**前端：** *Vue3、TypeScript、Vite、Element-Plus*
**后端：**
- *Java 21*
- *Spring Boot v3.5.0*
- *Spring Cloud v2025.0.0*
- ***Spring Cloud Alibaba | v2025.0.0.0***
- - *Nacos |v3.0.3* : 服务注册配置中心
- - *Dubbo* : 远程过程调用 (RPC) 中间件
- - *RocketMQ | v5.3.1* : 消息队列中间件
- *Redis | v7.4* : 缓存中间件
- *MyBatis* : 数据库连接层
- *MySQL* : 业务数据持久化

## v0.1 本地基础设施

### 前置条件

- Docker Desktop 已启动；
- Java 21 与 Maven 3.9+；
- 本机端口 `3306`、`6379`、`8848`、`9876`、`8080-8084` 未被占用。

### 启动中间件

```powershell
docker compose up -d
docker compose ps
```

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

### 构建与运行服务

```powershell
cd backend
mvn clean verify

# 依次启动，示例：Gateway
mvn -pl gateway-service spring-boot:run -Dspring-boot.run.arguments="--NACOS_ENABLED=true"
```

服务端口：Gateway `8080`、用户 `8081`、商品 `8082`、订单 `8083`、支付 `8084`。服务健康检查为 `/actuator/health`。

> [!NOTE]
> Docker Compose 当前只编排中间件。Java 服务先以本机进程启动，便于开发与断点调试；业务稳定后再补充服务镜像与完整应用编排。

## 文档说明
[Git 流程说明](docs/GIT.md)
[需求文档](docs/PRD.md)
[开发迭代说明](docs/TEAM.md)
[v0.2 接口契约](docs/API-v0.2.md)
[v0.3 交易接口契约](docs/API-v0.3.md)
[v0.4 支付与履约接口契约](docs/API-v0.4.md)
