package cn.nobeta.dingdong.common.event;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Delayed event requesting closure of a still-unpaid order. */
public record OrderTimeoutEvent(String orderNo, LocalDateTime createdAt) implements Serializable { }
