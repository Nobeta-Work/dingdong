<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { seckillApi, type SeckillActivity } from '@/api/services'
import { useSession } from '@/composables/session'
const {user}=useSession();const rows=ref<SeckillActivity[]>([]);const loading=ref(false);const pending=ref<number>();const resultText=ref('')
const load=async()=>{loading.value=true;try{rows.value=await seckillApi.activities()}finally{loading.value=false}}
const buy=async(a:SeckillActivity)=>{if(!user.value)return ElMessage.warning('请先登录');pending.value=a.id;const requestId=crypto.randomUUID();try{const accepted=await seckillApi.purchase(a.id,requestId);resultText.value=`请求已进入队列，Redis 剩余 ${accepted.remainingStock}`;ElMessage.success(resultText.value);setTimeout(async()=>{const result=await seckillApi.result(requestId);resultText.value=result.status==='SUCCESS'?`秒杀成功，异步订单 #${result.orderId}`:`处理状态：${result.status}`},900)}finally{pending.value=undefined}}
const time=(v:string)=>v.replace('T',' ').slice(0,19)
onMounted(load)
</script>
<template><div class="shell page seckill-page"><div class="section-title modern-section-title"><div><h1>限时秒杀</h1><p>资格由 Redis 原子抢占，消息队列异步创建订单。</p></div><el-button @click="load">刷新库存</el-button></div><el-alert v-if="resultText" :title="resultText" type="success" show-icon style="margin-bottom:20px"/><el-empty v-if="!loading&&!rows.length" description="当前没有进行中的秒杀活动"/><el-row v-loading="loading" :gutter="20"><el-col v-for="item in rows" :key="item.id" :xs="24" :md="12" :lg="8"><el-card shadow="never" class="seckill-card"><template #header><b>{{item.name}}</b></template><p>SKU #{{item.skuId}}</p><h2>¥{{Number(item.seckillPrice).toFixed(2)}}</h2><p>{{time(item.startTime)}} - {{time(item.endTime)}}</p><el-button type="primary" size="large" style="width:100%" :loading="pending===item.id" :disabled="!user" @click="buy(item)">{{user?'立即抢购':'登录后参与'}}</el-button></el-card></el-col></el-row></div></template>
