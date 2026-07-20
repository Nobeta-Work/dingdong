package cn.nobeta.dingdong.common.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单超时事件
 * 下单时由订单服务发布到 RocketMQ，消费方（OrderTimeoutListener）在收到后执行未支付订单的自动关闭逻辑。
 * createdAt 用于消费端计算是否到达超时阈值。
 * @param orderNo 订单号
 * @param createdAt 订单创建时间
 */
public record OrderTimeoutEvent(String orderNo, LocalDateTime createdAt) implements Serializable { }
