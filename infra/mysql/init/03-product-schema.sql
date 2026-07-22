USE dingdong_product;

CREATE TABLE IF NOT EXISTS product_category (
    id BIGINT NOT NULL AUTO_INCREMENT, name VARCHAR(64) NOT NULL, parent_id BIGINT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0, status TINYINT NOT NULL DEFAULT 1, deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY(id), UNIQUE KEY uk_product_category_name(name,parent_id,deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS product_brand (
    id BIGINT NOT NULL AUTO_INCREMENT, name VARCHAR(64) NOT NULL, logo_url VARCHAR(512) NULL,
    sort_order INT NOT NULL DEFAULT 0, status TINYINT NOT NULL DEFAULT 1, deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY(id), UNIQUE KEY uk_product_brand_name(name,deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS product_spu (
    id BIGINT NOT NULL AUTO_INCREMENT, title VARCHAR(128) NOT NULL, subtitle VARCHAR(255) NULL, description TEXT NULL, main_image_url VARCHAR(512) NULL,
    category_id BIGINT NOT NULL, brand_id BIGINT NOT NULL, status TINYINT NOT NULL DEFAULT 0, deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY(id), KEY idx_product_spu_category(category_id,status), KEY idx_product_spu_brand(brand_id,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS product_sku (
    id BIGINT NOT NULL AUTO_INCREMENT, spu_id BIGINT NOT NULL, sku_code VARCHAR(64) NOT NULL, spec_json VARCHAR(1000) NOT NULL,
    price DECIMAL(12,2) NOT NULL, available_stock INT NOT NULL DEFAULT 0, locked_stock INT NOT NULL DEFAULT 0, sales INT NOT NULL DEFAULT 0,
    status TINYINT NOT NULL DEFAULT 0, version INT NOT NULL DEFAULT 0, deleted TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY(id), UNIQUE KEY uk_product_sku_code(sku_code), KEY idx_product_sku_spu(spu_id,status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inventory_lock (
    id BIGINT NOT NULL AUTO_INCREMENT, order_no VARCHAR(32) NOT NULL, sku_id BIGINT NOT NULL, quantity INT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'LOCKED', created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, released_at DATETIME NULL,
    PRIMARY KEY(id), UNIQUE KEY uk_inventory_lock_order_sku(order_no, sku_id), KEY idx_inventory_lock_order(order_no, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS inventory_change_log (
    id BIGINT NOT NULL AUTO_INCREMENT, sku_id BIGINT NOT NULL, business_key VARCHAR(128) NOT NULL,
    business_type VARCHAR(32) NOT NULL, reference_no VARCHAR(64) NULL,
    change_available INT NOT NULL DEFAULT 0, change_locked INT NOT NULL DEFAULT 0, change_sales INT NOT NULL DEFAULT 0,
    before_available INT NOT NULL, after_available INT NOT NULL, before_locked INT NOT NULL, after_locked INT NOT NULL,
    before_sales INT NOT NULL, after_sales INT NOT NULL, remark VARCHAR(255) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(id), UNIQUE KEY uk_inventory_change_business(business_key),
    KEY idx_inventory_change_sku(sku_id,id), KEY idx_inventory_change_reference(reference_no,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seckill_activity (
    id BIGINT NOT NULL AUTO_INCREMENT, name VARCHAR(128) NOT NULL, sku_id BIGINT NOT NULL,
    seckill_price DECIMAL(12,2) NOT NULL, total_stock INT NOT NULL, available_stock INT NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT', start_time DATETIME NOT NULL, end_time DATETIME NOT NULL,
    version INT NOT NULL DEFAULT 0, created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY(id), KEY idx_seckill_activity_status_time(status,start_time,end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seckill_order (
    id BIGINT NOT NULL AUTO_INCREMENT, request_id VARCHAR(64) NOT NULL, activity_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL, sku_id BIGINT NOT NULL, quantity INT NOT NULL DEFAULT 1,
    seckill_price DECIMAL(12,2) NOT NULL, status VARCHAR(16) NOT NULL DEFAULT 'SUCCESS',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(id), UNIQUE KEY uk_seckill_order_request(request_id),
    UNIQUE KEY uk_seckill_order_user(activity_id,user_id), KEY idx_seckill_order_activity(activity_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
