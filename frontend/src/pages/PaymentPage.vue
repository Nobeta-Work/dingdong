<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { CircleCheckFilled, CreditCard } from '@element-plus/icons-vue'
import { paymentApi } from '@/api/services'
const route = useRoute(); const router = useRouter(); const paymentNo = ref(''); const paid = ref(false); const loading = ref(false); const orderNo = String(route.query.orderNo || ''); const amount = Number(route.query.amount || 0).toFixed(2)
const pay = async () => { loading.value = true; try { await paymentApi.simulate(paymentNo.value, true); paid.value = true; setTimeout(() => router.push('/orders'), 900) } finally { loading.value = false } }
onMounted(async () => { if (!orderNo) return router.replace('/orders'); const payment = await paymentApi.create(orderNo); paymentNo.value = payment.paymentNo })
</script>
<template><div class="payment-page"><section v-if="!paid" class="payment-card"><div class="payment-status"><el-icon><CreditCard /></el-icon><div><span>订单提交成功，请完成模拟支付</span><h1>¥ {{ amount }}</h1><p>订单号：{{ orderNo }}　请在 30 分钟内完成支付</p></div></div><el-divider /><h2>选择模拟支付方式</h2><div class="pay-options"><article class="selected"><span class="pay-logo wechat">微</span><b>模拟微信支付</b><i>已选择</i></article><article><span class="pay-logo alipay">支</span><b>模拟支付宝支付</b></article></div><el-alert type="info" :closable="false" show-icon title="开发环境模拟支付，不会发生真实资金扣款。" /><el-button type="primary" size="large" class="pay-submit" :loading="loading" :disabled="!paymentNo" @click="pay">确认模拟支付</el-button></section><section v-else class="payment-card payment-result"><el-icon><CircleCheckFilled /></el-icon><h1>支付成功</h1><p>订单状态已更新，可在我的订单中查看。</p></section></div></template>
