<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Goods, Money, ShoppingCart, Van } from '@element-plus/icons-vue'
import { adminApi, type DashboardOverview } from '@/api/services'

const loading = ref(true)
const overview = ref<DashboardOverview>({ todayOrderCount: 0, todayPaidAmount: 0, pendingShipmentCount: 0, topProducts: [] })
const money = (value: number) => Number(value || 0).toFixed(2)
const load = async () => { loading.value = true; try { overview.value = await adminApi.dashboard() } finally { loading.value = false } }
onMounted(load)
</script>

<template><div><div class="admin-page-title"><div><span>数据概览</span><h1>运营概览</h1><p>基于订单实时聚合的今日经营数据。</p></div><el-button :loading="loading" @click="load">刷新数据</el-button></div><el-skeleton :loading="loading" animated :rows="8"><template #default><section class="metric-grid dashboard-metrics"><article class="metric-card"><div class="metric-icon blue"><el-icon><ShoppingCart /></el-icon></div><span>今日订单</span><h2>{{ overview.todayOrderCount }}</h2><small>今日创建订单总数</small></article><article class="metric-card"><div class="metric-icon green"><el-icon><Money /></el-icon></div><span>今日成交额</span><h2>¥{{ money(overview.todayPaidAmount) }}</h2><small>已支付及履约订单</small></article><article class="metric-card"><div class="metric-icon orange"><el-icon><Van /></el-icon></div><span>待发货</span><h2>{{ overview.pendingShipmentCount }}</h2><small>状态为已支付的订单</small></article><article class="metric-card"><div class="metric-icon purple"><el-icon><Goods /></el-icon></div><span>热销 SKU</span><h2>{{ overview.topProducts.length }}</h2><small>当前销量排行条目</small></article></section><section class="admin-card ranking-card"><div class="card-heading"><div><h2>商品销量排行</h2><p>统计已支付、已发货和已完成订单</p></div><RouterLink to="/admin/products">查看商品管理</RouterLink></div><el-table :data="overview.topProducts" empty-text="暂无成交商品"><el-table-column type="index" label="#" width="56" /><el-table-column label="商品" min-width="280"><template #default="scope"><div class="table-product"><img :src="scope.row.productImageUrl || 'https://placehold.co/80x80/eaf3ff/1677ff?text=DD'" /><div><b>{{ scope.row.productTitle }}</b><small>SKU #{{ scope.row.skuId }}</small></div></div></template></el-table-column><el-table-column prop="quantity" label="销量" width="120" /><el-table-column label="销售额" width="160"><template #default="scope">¥{{ money(scope.row.salesAmount) }}</template></el-table-column></el-table></section></template></el-skeleton></div></template>
