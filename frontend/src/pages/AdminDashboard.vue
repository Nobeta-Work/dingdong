<script setup lang="ts">
import { Connection, Goods } from '@element-plus/icons-vue'
</script>
<template><div><div class="admin-page-title"><div><span>数据概览</span><h1>运营概览</h1><p>展示由后端经营统计接口驱动的实时数据。</p></div></div><section class="admin-card unavailable-card"><el-icon><Connection /></el-icon><h2>等待经营统计接口接入</h2><p>当前接口契约尚未提供今日订单、成交额、待发货数和商品销量排行的查询入口，因此前端不会展示虚构统计数据。</p><RouterLink to="/admin/products"><el-button type="primary" :icon="Goods">进入商品管理</el-button></RouterLink></section></div></template>
