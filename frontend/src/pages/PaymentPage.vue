<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { CircleCheckFilled, CreditCard } from '@element-plus/icons-vue'
const router = useRouter(); const paid = ref(false); const pay = () => { paid.value = true; setTimeout(() => router.push('/orders'), 1400) }
</script>
<template><div class="payment-page"><section v-if="!paid" class="payment-card"><div class="payment-status"><el-icon><CreditCard /></el-icon><div><span>订单提交成功，请完成模拟支付</span><h1>¥ 5,399.00</h1><p>订单号：DD202607160001　请在 30 分钟内完成支付</p></div></div><el-divider /><h2>选择模拟支付方式</h2><div class="pay-options"><article class="selected"><span class="pay-logo wechat">微</span><b>模拟微信支付</b><i>已选择</i></article><article><span class="pay-logo alipay">支</span><b>模拟支付宝支付</b></article></div><el-alert type="info" :closable="false" show-icon title="开发环境模拟支付，不会发生真实资金扣款。" /><el-button type="primary" size="large" class="pay-submit" @click="pay">确认模拟支付</el-button></section><section v-else class="payment-card payment-result"><el-icon><CircleCheckFilled /></el-icon><h1>支付成功</h1><p>订单状态已更新，可在我的订单中查看。</p><el-button type="primary" @click="router.push('/orders')">查看订单</el-button></section></div></template>
