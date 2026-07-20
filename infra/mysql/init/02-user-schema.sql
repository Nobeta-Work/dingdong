USE dingdong_user;

-- 商城用户表 —— 用户资料查询与修改功能的数据存储
-- 用户注册/登录时创建记录，profile() 通过 id 查询，updateProfile() 更新 nickname/phone/email/avatar_url
-- phone 和 email 设有唯一索引，修改资料时需校验全局唯一性；deleted 字段实现逻辑删除
CREATE TABLE IF NOT EXISTS mall_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(32) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    nickname VARCHAR(32) NOT NULL,            -- 用户昵称（资料修改可更新）
    phone VARCHAR(16) NULL,                   -- 手机号，全局唯一（资料修改可更新）
    email VARCHAR(128) NULL,                  -- 邮箱，全局唯一（资料修改可更新）
    avatar_url VARCHAR(512) NULL,             -- 头像 URL（资料修改可更新）
    role VARCHAR(16) NOT NULL DEFAULT 'USER', -- 用户角色（USER/ADMIN，资料查询返回但不可修改）
    status TINYINT NOT NULL DEFAULT 1,        -- 账号状态：1-正常 0-禁用（资料查询时校验）
    deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_mall_user_username (username),
    UNIQUE KEY uk_mall_user_phone (phone),
    UNIQUE KEY uk_mall_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户收货地址表 —— 收货地址增删改查功能的数据存储
-- CRUD: list() 按 user_id 查询 --> create() INSERT --> update() UPDATE --> delete() 逻辑删除 deleted=1
-- default_address 用于标记默认地址，新增/修改时通过 clearDefault() 维护互斥（一个用户仅一个默认地址）
-- user_id + deleted 组合索引加速归属查询
CREATE TABLE IF NOT EXISTS user_address (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,                      -- 用户 ID，标识地址归属（所有操作必须带此条件防越权）
    receiver_name VARCHAR(32) NOT NULL,           -- 收件人姓名
    receiver_phone VARCHAR(16) NOT NULL,          -- 收件人手机号
    province VARCHAR(32) NOT NULL,                -- 省份
    city VARCHAR(32) NOT NULL,                    -- 城市
    district VARCHAR(32) NOT NULL,                -- 区/县
    detail_address VARCHAR(128) NOT NULL,         -- 详细地址（街道/门牌号）
    default_address TINYINT NOT NULL DEFAULT 0,   -- 默认地址标记：1-默认 0-非默认
    deleted TINYINT NOT NULL DEFAULT 0,           -- 逻辑删除标记：1-已删除 0-正常
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_user_address_user (user_id, deleted)  -- 用户 ID + 删除标记复合索引，加速列表查询
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO mall_user(username, password_hash, nickname, role, status)
SELECT 'admin', '$2a$10$H0NC/3CaUJ0DDTkCLZSYGeJAHp1VQLErU3WfjV4LjrxNJMYU82fP2', '商城管理员', 'ADMIN', 1
WHERE NOT EXISTS (SELECT 1 FROM mall_user WHERE username = 'admin');
