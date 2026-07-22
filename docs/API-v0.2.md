---
title: 叮咚商城 v0.2 接口契约
aliases:
  - API v0.2
tags:
  - project/ding-dong
  - api/contract
status: implemented
updated: 2026-07-22
related:
  - "[[PRD|产品需求文档]]"
  - "[[TEAM|开发迭代说明]]"
---

# 叮咚商城 v0.2 接口契约

> [!important] 使用约定
> 本文对应 `user-service`（8081）和 `product-service`（8082）的 v0.2 实现。统一响应为 `{"code":"OK","message":"success","data":...,"traceId":null}`；除登录、注册和商品浏览外，请携带 `Authorization: Bearer <JWT>`。

## 1. 用户与鉴权

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/auth/register` | 注册普通用户 |
| POST | `/api/auth/login` | 用户名密码登录，返回 JWT |
| POST | `/api/auth/sms/code` | 获取 Mock 短信验证码；支持 `login`、`register`、`change-phone` 场景 |
| POST | `/api/auth/sms/login` | 手机号和验证码登录，首次登录自动创建用户 |
| GET | `/api/users/me` | 获取当前用户资料 |
| PUT | `/api/users/me` | 更新昵称、手机号、邮箱、头像；手机号变化时校验验证码 |
| PUT | `/api/users/me/password` | 校验当前密码后修改密码 |

### 注册

```json
POST /api/auth/register
{
  "username": "buyer_001",
  "password": "Passw0rd!",
  "nickname": "叮咚用户",
  "phone": "13800138000",
  "email": "buyer@example.com"
}
```

### 登录

```json
POST /api/auth/login
{"username":"admin","password":"password"}
```

```json
{
  "code": "OK",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 7200,
    "user": {"id": 1, "username": "admin", "nickname": "商城管理员", "role": "ADMIN"}
  }
}
```

> [!warning] 演示账号
> `admin / password` 只存在于首次本地数据库初始化。公网或共享环境必须在部署时替换。

### Mock 短信验证码

```json
POST /api/auth/sms/code
{"phone":"13800138000","scene":"login"}
```

```json
{
  "code": "OK",
  "data": {
    "mock": true,
    "debugCode": "381927",
    "expireSeconds": 300,
    "retryAfterSeconds": 60
  }
}
```

第三方短信签名与模板资质尚未通过，当前验证码仍写入 Redis 并执行过期、频控和一次性消费，但会在 Mock 响应中直接返回给前端。真实短信渠道接入后必须移除 `debugCode`。

手机号换绑时，先以 `scene=change-phone` 获取验证码，再提交：

```json
PUT /api/users/me
{"nickname":"叮咚用户","phone":"13900139000","email":"buyer@example.com","avatarUrl":null,"smsCode":"381927"}
```

### 修改密码

```json
PUT /api/users/me/password
{
  "currentPassword": "Passw0rd!",
  "newPassword": "NewPassw0rd!"
}
```

- 新密码长度为 8–72 位；
- 当前密码不正确时拒绝修改；
- 新密码不得与当前密码相同；
- 修改成功后客户端应清除 JWT，并要求用户重新登录。

## 2. 收货地址

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/addresses` | 查询当前用户地址 |
| POST | `/api/addresses` | 新增地址 |
| PUT | `/api/addresses/{id}` | 修改自己的地址 |
| DELETE | `/api/addresses/{id}` | 删除自己的地址 |

```json
{
  "receiverName": "张三",
  "receiverPhone": "13800138000",
  "province": "陕西省",
  "city": "西安市",
  "district": "雁塔区",
  "detailAddress": "科技路 1 号",
  "defaultAddress": true
}
```

设置 `defaultAddress=true` 会自动取消该用户其他地址的默认标记。

## 3. 公开商品接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/categories` | 启用的分类 |
| GET | `/api/brands` | 启用的品牌 |
| GET | `/api/products` | 上架商品分页检索 |
| GET | `/api/products/{id}` | 上架商品详情和可售 SKU |

`GET /api/products` 支持：`keyword`、`categoryId`、`brandId`、`minPrice`、`maxPrice`、`sort`、`page`、`size`。

- `sort`：`newest`（默认）、`price-asc`、`price-desc`、`sales`
- `page`：从 1 开始；`size`：1-100，默认 20

## 4. 管理端商品接口

> [!danger] 权限
> 所有 `/api/admin/**` 请求必须携带 `ADMIN` 角色 JWT。商品服务会独立校验 JWT 签名、过期时间和角色，不能仅依赖前端隐藏按钮。

| 方法 | 路径 | 说明 |
|---|---|---|
| GET/POST/PUT | `/api/admin/categories`、`/api/admin/categories/{id}` | 分类查询、新建、修改 |
| GET/POST/PUT | `/api/admin/brands`、`/api/admin/brands/{id}` | 品牌查询、新建、修改 |
| POST/GET/PUT | `/api/admin/products`、`/api/admin/products/{id}` | SPU 新建、查看、修改 |
| POST/PUT | `/api/admin/products/{spuId}/skus`、`/api/admin/products/{spuId}/skus/{id}` | SKU 新建、修改 |
| POST | `/api/files` | ADMIN 上传不超过 5MB 的图片到 GitHub 图床，返回公开 URL |

### 分类与品牌请求

```json
{"name":"手机通讯","parentId":0,"sortOrder":10,"status":1}
```

```json
{"name":"叮咚精选","logoUrl":"https://example.com/brand.png","sortOrder":10,"status":1}
```

### SPU 与 SKU 请求

```json
{
  "title": "叮咚手机 Pro",
  "subtitle": "演示商品",
  "description": "用于 v0.2 商品浏览演示",
  "mainImageUrl": "https://example.com/product.png",
  "categoryId": 1,
  "brandId": 1,
  "status": 1
}
```

```json
{
  "skuCode": "DD-PRO-BLUE-128",
  "specJson": "{\"颜色\":\"蓝色\",\"存储\":\"128G\"}",
  "price": 2999.00,
  "availableStock": 100,
  "status": 1
}
```

## 5. 主要错误码

| 错误码 | 含义 |
|---|---|
| `AUTH_LOGIN_FAILED` | 用户名或密码错误 |
| `AUTH_TOKEN_INVALID` / `AUTH_TOKEN_EXPIRED` | 令牌无效或过期 |
| `AUTH_FORBIDDEN` | 非管理员访问管理端 |
| `USER_USERNAME_EXISTS` / `USER_PHONE_EXISTS` / `USER_EMAIL_EXISTS` | 注册或资料唯一性冲突 |
| `USER_PASSWORD_INCORRECT` | 修改密码时当前密码不正确 |
| `USER_PASSWORD_UNCHANGED` | 新密码与当前密码相同 |
| `SMS_CODE_INVALID` / `SMS_RATE_LIMITED` / `SMS_SCENE_INVALID` | 验证码错误、发送过频或场景不受支持 |
| `FILE_UPLOAD_CONFIG` / `FILE_UPLOAD_FAILED` | GitHub 图床未配置或上传失败 |
| `USER_ADDRESS_NOT_FOUND` | 地址不属于当前用户或已删除 |
| `PRODUCT_CATEGORY_NOT_FOUND` / `PRODUCT_BRAND_NOT_FOUND` | 商品关联分类或品牌不存在 |
| `PRODUCT_SPU_NOT_FOUND` / `PRODUCT_SKU_NOT_FOUND` | 商品或 SKU 不存在、不可售 |
