-- Test seed data for local development and UI integration.
-- This script is idempotent. Test accounts use password: password

SET NAMES utf8mb4;

USE dingdong_user;

INSERT IGNORE INTO mall_user(username, password_hash, nickname, phone, email, avatar_url, role, status) VALUES
('demo_admin', '$2a$10$H0NC/3CaUJ0DDTkCLZSYGeJAHp1VQLErU3WfjV4LjrxNJMYU82fP2', '演示管理员', '13900000001', 'demo_admin@example.test', 'https://faramita.online/i/2026/07/07/4817229336bd48639830286ac148a095.png', 'ADMIN', 1),
('demo_buyer_01', '$2a$10$H0NC/3CaUJ0DDTkCLZSYGeJAHp1VQLErU3WfjV4LjrxNJMYU82fP2', '林小满', '13900000011', 'buyer01@example.test', 'https://faramita.online/i/2026/07/07/4817229336bd48639830286ac148a095.png', 'USER', 1),
('demo_buyer_02', '$2a$10$H0NC/3CaUJ0DDTkCLZSYGeJAHp1VQLErU3WfjV4LjrxNJMYU82fP2', '周可可', '13900000012', 'buyer02@example.test', 'https://faramita.online/i/2026/07/07/4817229336bd48639830286ac148a095.png', 'USER', 1),
('demo_buyer_03', '$2a$10$H0NC/3CaUJ0DDTkCLZSYGeJAHp1VQLErU3WfjV4LjrxNJMYU82fP2', '陈星河', '13900000013', 'buyer03@example.test', 'https://faramita.online/i/2026/07/07/4817229336bd48639830286ac148a095.png', 'USER', 1),
('demo_buyer_04', '$2a$10$H0NC/3CaUJ0DDTkCLZSYGeJAHp1VQLErU3WfjV4LjrxNJMYU82fP2', '叶知秋', '13900000014', 'buyer04@example.test', 'https://faramita.online/i/2026/07/07/4817229336bd48639830286ac148a095.png', 'USER', 1),
('demo_buyer_05', '$2a$10$H0NC/3CaUJ0DDTkCLZSYGeJAHp1VQLErU3WfjV4LjrxNJMYU82fP2', '苏小北', '13900000015', 'buyer05@example.test', 'https://faramita.online/i/2026/07/07/4817229336bd48639830286ac148a095.png', 'USER', 1),
('demo_buyer_06', '$2a$10$H0NC/3CaUJ0DDTkCLZSYGeJAHp1VQLErU3WfjV4LjrxNJMYU82fP2', '顾南枝', '13900000016', 'buyer06@example.test', 'https://faramita.online/i/2026/07/07/4817229336bd48639830286ac148a095.png', 'USER', 1),
('demo_buyer_07', '$2a$10$H0NC/3CaUJ0DDTkCLZSYGeJAHp1VQLErU3WfjV4LjrxNJMYU82fP2', '沈一诺', '13900000017', 'buyer07@example.test', 'https://faramita.online/i/2026/07/07/4817229336bd48639830286ac148a095.png', 'USER', 1),
('demo_buyer_disabled', '$2a$10$H0NC/3CaUJ0DDTkCLZSYGeJAHp1VQLErU3WfjV4LjrxNJMYU82fP2', '禁用演示账号', '13900000018', 'buyer-disabled@example.test', 'https://faramita.online/i/2026/07/07/4817229336bd48639830286ac148a095.png', 'USER', 0);

INSERT INTO user_address(user_id, receiver_name, receiver_phone, province, city, district, detail_address, default_address)
SELECT u.id, CONCAT(u.nickname, '（演示）'), u.phone, '陕西省', '西安市', '雁塔区', CONCAT('科技路 ', 100 + u.id, ' 号叮咚公寓'), 1
FROM mall_user u
WHERE u.username LIKE 'demo_buyer_%' AND u.status = 1
  AND NOT EXISTS (SELECT 1 FROM user_address a WHERE a.user_id = u.id AND a.default_address = 1 AND a.deleted = 0);

USE dingdong_product;

INSERT IGNORE INTO product_category(name, parent_id, sort_order, status) VALUES
('测试数码', 0, 10, 1), ('测试家居', 0, 20, 1), ('测试服饰', 0, 30, 1);
INSERT IGNORE INTO product_brand(name, logo_url, sort_order, status) VALUES
('星云精选', 'https://faramita.online/i/2026/07/17/d271fb014ac34da3ae884dccbfaf7436.png', 10, 1),
('远山制造', 'https://faramita.online/i/2026/07/17/d271fb014ac34da3ae884dccbfaf7436.png', 20, 1),
('微光生活', 'https://faramita.online/i/2026/07/17/d271fb014ac34da3ae884dccbfaf7436.png', 30, 1);

DELIMITER $$
DROP PROCEDURE IF EXISTS seed_test_products$$
CREATE PROCEDURE seed_test_products()
BEGIN
    DECLARE item_no INT DEFAULT 1;
    DECLARE category_id_value BIGINT;
    DECLARE brand_id_value BIGINT;
    DECLARE spu_id_value BIGINT;
    WHILE item_no <= 30 DO
        SELECT id INTO category_id_value FROM product_category
        WHERE name = ELT(MOD(item_no - 1, 3) + 1, '测试数码', '测试家居', '测试服饰') AND deleted = 0 LIMIT 1;
        SELECT id INTO brand_id_value FROM product_brand
        WHERE name = ELT(MOD(item_no - 1, 3) + 1, '星云精选', '远山制造', '微光生活') AND deleted = 0 LIMIT 1;
        IF NOT EXISTS (SELECT 1 FROM product_spu WHERE title = CONCAT('演示商品 ', LPAD(item_no, 2, '0')) AND deleted = 0) THEN
            INSERT INTO product_spu(title, subtitle, description, main_image_url, category_id, brand_id, status)
            VALUES (
                CONCAT('演示商品 ', LPAD(item_no, 2, '0')),
                CONCAT('用于商品列表、详情和下单联调的第 ', item_no, ' 件商品'),
                CONCAT('测试数据自动生成。商品编号：', item_no, '；可用于搜索、分类、购物车与订单流程。'),
                'https://faramita.online/i/2026/07/17/d271fb014ac34da3ae884dccbfaf7436.png',
                category_id_value, brand_id_value, 1
            );
        END IF;
        SELECT id INTO spu_id_value FROM product_spu WHERE title = CONCAT('演示商品 ', LPAD(item_no, 2, '0')) AND deleted = 0 LIMIT 1;
        IF NOT EXISTS (SELECT 1 FROM product_sku WHERE sku_code = CONCAT('TEST-SKU-', LPAD(item_no, 3, '0'))) THEN
            INSERT INTO product_sku(spu_id, sku_code, spec_json, price, available_stock, locked_stock, sales, status)
            VALUES (
                spu_id_value, CONCAT('TEST-SKU-', LPAD(item_no, 3, '0')),
                JSON_OBJECT('颜色', ELT(MOD(item_no - 1, 3) + 1, '蓝色', '黑色', '白色'), '版本', CONCAT('测试款-', LPAD(item_no, 2, '0'))),
                59.00 + item_no * 10, 100 + item_no * 5, 0, MOD(item_no, 20), 1
            );
        END IF;
        SET item_no = item_no + 1;
    END WHILE;
END$$
CALL seed_test_products()$$
DROP PROCEDURE seed_test_products$$
DELIMITER ;

USE dingdong_order;

INSERT INTO mall_order(order_no, user_id, receiver_name, receiver_phone, receiver_address, total_amount, status, carrier, tracking_no, shipped_at)
SELECT seed.order_no, u.id, CONCAT(u.nickname, '（演示）'), u.phone, '陕西省 西安市 雁塔区 科技路演示地址', s.price * seed.quantity, seed.status,
       seed.carrier, seed.tracking_no, CASE WHEN seed.status IN ('SHIPPED', 'COMPLETED') THEN NOW() - INTERVAL seed.days_ago DAY ELSE NULL END
FROM (
    SELECT 'DDTEST202607170001' AS order_no, 'demo_buyer_01' AS username, 'TEST-SKU-005' AS sku_code, 1 AS quantity, 'PENDING_PAYMENT' AS status, NULL AS carrier, NULL AS tracking_no, 0 AS days_ago
    UNION ALL SELECT 'DDTEST202607170002', 'demo_buyer_01', 'TEST-SKU-006', 2, 'PAID', NULL, NULL, 1
    UNION ALL SELECT 'DDTEST202607170003', 'demo_buyer_02', 'TEST-SKU-007', 1, 'SHIPPED', '叮咚快递', 'DDX202607170003', 2
    UNION ALL SELECT 'DDTEST202607170004', 'demo_buyer_02', 'TEST-SKU-008', 3, 'COMPLETED', '叮咚快递', 'DDX202607170004', 4
    UNION ALL SELECT 'DDTEST202607170005', 'demo_buyer_03', 'TEST-SKU-009', 1, 'CANCELED', NULL, NULL, 5
    UNION ALL SELECT 'DDTEST202607170006', 'demo_buyer_04', 'TEST-SKU-010', 2, 'PAID', NULL, NULL, 1
    UNION ALL SELECT 'DDTEST202607170007', 'demo_buyer_05', 'TEST-SKU-011', 1, 'SHIPPED', '叮咚快递', 'DDX202607170007', 3
    UNION ALL SELECT 'DDTEST202607170008', 'demo_buyer_06', 'TEST-SKU-012', 2, 'PENDING_PAYMENT', NULL, NULL, 0
) seed
JOIN dingdong_user.mall_user u ON u.username = seed.username
JOIN dingdong_product.product_sku s ON s.sku_code = seed.sku_code
WHERE NOT EXISTS (SELECT 1 FROM mall_order o WHERE o.order_no = seed.order_no);

INSERT INTO order_item(order_id, sku_id, sku_code, product_title, product_image_url, spec_json, unit_price, quantity, total_amount)
SELECT o.id, s.id, s.sku_code, p.title, p.main_image_url, s.spec_json, s.price, seed.quantity, s.price * seed.quantity
FROM (
    SELECT 'DDTEST202607170001' AS order_no, 'TEST-SKU-005' AS sku_code, 1 AS quantity
    UNION ALL SELECT 'DDTEST202607170002', 'TEST-SKU-006', 2
    UNION ALL SELECT 'DDTEST202607170003', 'TEST-SKU-007', 1
    UNION ALL SELECT 'DDTEST202607170004', 'TEST-SKU-008', 3
    UNION ALL SELECT 'DDTEST202607170005', 'TEST-SKU-009', 1
    UNION ALL SELECT 'DDTEST202607170006', 'TEST-SKU-010', 2
    UNION ALL SELECT 'DDTEST202607170007', 'TEST-SKU-011', 1
    UNION ALL SELECT 'DDTEST202607170008', 'TEST-SKU-012', 2
) seed
JOIN mall_order o ON o.order_no = seed.order_no
JOIN dingdong_product.product_sku s ON s.sku_code = seed.sku_code
JOIN dingdong_product.product_spu p ON p.id = s.spu_id
WHERE NOT EXISTS (SELECT 1 FROM order_item i WHERE i.order_id = o.id AND i.sku_id = s.id);

INSERT INTO order_status_log(order_id, from_status, to_status, operator_type, operator_id, remark)
SELECT o.id, NULL, o.status, 'SYSTEM', NULL, '06-test-data 初始化订单状态'
FROM mall_order o
WHERE o.order_no LIKE 'DDTEST%' AND NOT EXISTS (SELECT 1 FROM order_status_log l WHERE l.order_id = o.id);

INSERT INTO order_outbox(event_type, order_no, status, retry_count, created_at, sent_at)
SELECT 'ORDER_TIMEOUT', o.order_no, CASE WHEN o.status = 'PENDING_PAYMENT' THEN 'PENDING' ELSE 'SENT' END, 0, o.created_at,
       CASE WHEN o.status = 'PENDING_PAYMENT' THEN NULL ELSE NOW() END
FROM mall_order o
WHERE o.order_no IN ('DDTEST202607170001', 'DDTEST202607170002', 'DDTEST202607170008')
  AND NOT EXISTS (SELECT 1 FROM order_outbox x WHERE x.order_no = o.order_no AND x.event_type = 'ORDER_TIMEOUT');

USE dingdong_product;

INSERT INTO inventory_lock(order_no, sku_id, quantity, status, released_at)
SELECT seed.order_no, s.id, seed.quantity, seed.status, CASE WHEN seed.status = 'RELEASED' THEN NOW() - INTERVAL 5 DAY ELSE NULL END
FROM (
    SELECT 'DDTEST202607170001' AS order_no, 'TEST-SKU-005' AS sku_code, 1 AS quantity, 'LOCKED' AS status
    UNION ALL SELECT 'DDTEST202607170002', 'TEST-SKU-006', 2, 'CONFIRMED'
    UNION ALL SELECT 'DDTEST202607170003', 'TEST-SKU-007', 1, 'CONFIRMED'
    UNION ALL SELECT 'DDTEST202607170004', 'TEST-SKU-008', 3, 'CONFIRMED'
    UNION ALL SELECT 'DDTEST202607170005', 'TEST-SKU-009', 1, 'RELEASED'
    UNION ALL SELECT 'DDTEST202607170006', 'TEST-SKU-010', 2, 'CONFIRMED'
    UNION ALL SELECT 'DDTEST202607170007', 'TEST-SKU-011', 1, 'CONFIRMED'
    UNION ALL SELECT 'DDTEST202607170008', 'TEST-SKU-012', 2, 'LOCKED'
) seed
JOIN product_sku s ON s.sku_code = seed.sku_code
WHERE NOT EXISTS (SELECT 1 FROM inventory_lock l WHERE l.order_no = seed.order_no AND l.sku_id = s.id);

-- Keep seed SKU counters consistent with the generated lock records. This only affects TEST-SKU rows.
UPDATE product_sku s
LEFT JOIN (
    SELECT sku_id,
           SUM(CASE WHEN status = 'LOCKED' THEN quantity ELSE 0 END) AS active_locked,
           SUM(CASE WHEN status = 'CONFIRMED' THEN quantity ELSE 0 END) AS confirmed_quantity
    FROM inventory_lock
    WHERE order_no LIKE 'DDTEST%'
    GROUP BY sku_id
) lock_summary ON lock_summary.sku_id = s.id
SET s.available_stock = 100 + CAST(RIGHT(s.sku_code, 3) AS UNSIGNED) * 5 - COALESCE(lock_summary.active_locked, 0),
    s.locked_stock = COALESCE(lock_summary.active_locked, 0),
    s.sales = MOD(CAST(RIGHT(s.sku_code, 3) AS UNSIGNED), 20) + COALESCE(lock_summary.confirmed_quantity, 0)
WHERE s.sku_code LIKE 'TEST-SKU-%';

USE dingdong_pay;

INSERT INTO payment_order(payment_no, order_no, user_id, amount, channel, status, transaction_no, paid_at)
SELECT CONCAT('PAYTEST', RIGHT(o.order_no, 12)), o.order_no, o.user_id, o.total_amount, 'MOCK', 'SUCCESS',
       CONCAT('MOCK-', RIGHT(o.order_no, 12)), NOW() - INTERVAL 1 DAY
FROM dingdong_order.mall_order o
WHERE o.status IN ('PAID', 'SHIPPED', 'COMPLETED')
  AND o.order_no LIKE 'DDTEST%'
  AND NOT EXISTS (SELECT 1 FROM payment_order p WHERE p.order_no = o.order_no);

-- Expected seed volume after a fresh initialization:
-- 1 default admin + 9 demo users, 3 categories, 3 brands, 30 SPUs/SKUs,
-- 4 cart items, 8 orders, 8 order items, 8 inventory locks and 5 successful payments.
