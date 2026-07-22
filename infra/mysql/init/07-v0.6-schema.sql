-- v0.6 增量结构；可对保留数据卷的环境手动执行，所有语句均可重复运行。
USE dingdong_product;
CREATE TABLE IF NOT EXISTS inventory_change_log (
 id BIGINT NOT NULL AUTO_INCREMENT,sku_id BIGINT NOT NULL,business_key VARCHAR(128) NOT NULL,business_type VARCHAR(32) NOT NULL,reference_no VARCHAR(64) NULL,
 change_available INT NOT NULL DEFAULT 0,change_locked INT NOT NULL DEFAULT 0,change_sales INT NOT NULL DEFAULT 0,
 before_available INT NOT NULL,after_available INT NOT NULL,before_locked INT NOT NULL,after_locked INT NOT NULL,before_sales INT NOT NULL,after_sales INT NOT NULL,remark VARCHAR(255) NULL,
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,PRIMARY KEY(id),UNIQUE KEY uk_inventory_change_business(business_key),KEY idx_inventory_change_sku(sku_id,id),KEY idx_inventory_change_reference(reference_no,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS seckill_activity (
 id BIGINT NOT NULL AUTO_INCREMENT,name VARCHAR(128) NOT NULL,sku_id BIGINT NOT NULL,seckill_price DECIMAL(12,2) NOT NULL,total_stock INT NOT NULL,available_stock INT NOT NULL,
 status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',start_time DATETIME NOT NULL,end_time DATETIME NOT NULL,version INT NOT NULL DEFAULT 0,
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 PRIMARY KEY(id),KEY idx_seckill_activity_status_time(status,start_time,end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS seckill_order (
 id BIGINT NOT NULL AUTO_INCREMENT,request_id VARCHAR(64) NOT NULL,activity_id BIGINT NOT NULL,user_id BIGINT NOT NULL,sku_id BIGINT NOT NULL,quantity INT NOT NULL DEFAULT 1,
 seckill_price DECIMAL(12,2) NOT NULL,status VARCHAR(16) NOT NULL DEFAULT 'SUCCESS',created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(id),UNIQUE KEY uk_seckill_order_request(request_id),UNIQUE KEY uk_seckill_order_user(activity_id,user_id),KEY idx_seckill_order_activity(activity_id,id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

USE dingdong_order;
CREATE TABLE IF NOT EXISTS order_request (
 id BIGINT NOT NULL AUTO_INCREMENT,user_id BIGINT NOT NULL,request_id VARCHAR(64) NOT NULL,order_no VARCHAR(32) NOT NULL,created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 PRIMARY KEY(id),UNIQUE KEY uk_order_request_user(user_id,request_id),UNIQUE KEY uk_order_request_no(order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
