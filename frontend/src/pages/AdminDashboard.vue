<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ArrowRight, Goods, Refresh, ShoppingCart, Van } from '@element-plus/icons-vue'
import { adminApi, type DashboardOverview } from '@/api/services'

const loading = ref(true)
const overview = ref<DashboardOverview>({ todayOrderCount: 0, todayPaidAmount: 0, pendingShipmentCount: 0, topProducts: [] })
const money = (value: number) => Number(value || 0).toFixed(2)
const totalSales = computed(() => overview.value.topProducts.reduce((sum, item) => sum + Number(item.salesAmount || 0), 0))
const load = async () => { loading.value = true; try { overview.value = await adminApi.dashboard() } finally { loading.value = false } }
onMounted(load)
</script>

<template>
  <div>
    <div class="admin-page-title dashboard-title">
      <div><h1>经营概览</h1><p>掌握今日交易与待办，快速处理商城运营。</p></div>
      <el-button :icon="Refresh" :loading="loading" @click="load">刷新数据</el-button>
    </div>
    <el-skeleton :loading="loading" animated :rows="8">
      <template #default>
        <section class="metric-band">
          <article><span>今日成交额</span><h2>¥{{ money(overview.todayPaidAmount) }}</h2><small>已支付及履约订单</small></article>
          <article><span>今日订单</span><h2>{{ overview.todayOrderCount }}</h2><small>今日创建订单总数</small></article>
          <article><span>待发货</span><h2>{{ overview.pendingShipmentCount }}</h2><small>需要优先处理</small></article>
          <article><span>热销商品</span><h2>{{ overview.topProducts.length }}</h2><small>当前销量排行条目</small></article>
        </section>

        <div class="dashboard-grid">
          <section class="admin-card ranking-card">
            <div class="card-heading"><div><h2>热销商品</h2><p>统计已支付、已发货和已完成订单</p></div><RouterLink to="/admin/products">查看商品 <el-icon><ArrowRight /></el-icon></RouterLink></div>
            <el-table :data="overview.topProducts" empty-text="暂无成交商品">
              <el-table-column type="index" label="排名" width="70" />
              <el-table-column label="商品" min-width="280"><template #default="scope"><div class="table-product"><img :src="scope.row.productImageUrl || `https://picsum.photos/seed/admin-${scope.row.skuId}/100/100`" :alt="scope.row.productTitle" /><div><b>{{ scope.row.productTitle }}</b><small>SKU {{ scope.row.skuId }}</small></div></div></template></el-table-column>
              <el-table-column prop="quantity" label="销量" width="100" />
              <el-table-column label="成交额" width="140"><template #default="scope">¥{{ money(scope.row.salesAmount) }}</template></el-table-column>
            </el-table>
          </section>
          <aside class="dashboard-side">
            <section class="admin-card todo-card">
              <div class="card-heading"><div><h2>今日待办</h2><p>按优先级完成运营工作</p></div></div>
              <RouterLink to="/admin/orders"><el-icon><Van /></el-icon><div><b>待发货订单</b><small>已支付订单等待发货</small></div><strong>{{ overview.pendingShipmentCount }}</strong><el-icon><ArrowRight /></el-icon></RouterLink>
              <RouterLink to="/admin/products"><el-icon><Goods /></el-icon><div><b>商品与库存</b><small>检查商品上架与库存</small></div><strong>{{ overview.topProducts.length }}</strong><el-icon><ArrowRight /></el-icon></RouterLink>
              <RouterLink to="/admin/orders"><el-icon><ShoppingCart /></el-icon><div><b>今日订单</b><small>查看全部交易状态</small></div><strong>{{ overview.todayOrderCount }}</strong><el-icon><ArrowRight /></el-icon></RouterLink>
            </section>
            <section class="admin-card sales-summary"><span>热销商品成交额</span><h2>¥{{ money(totalSales) }}</h2><p>当前销量排行商品累计成交数据</p></section>
          </aside>
        </div>
      </template>
    </el-skeleton>
  </div>
</template>
