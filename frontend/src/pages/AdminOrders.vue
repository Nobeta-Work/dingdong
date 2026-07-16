<script setup lang="ts">
import { Connection } from '@element-plus/icons-vue'
</script>
<template><div><div class="admin-page-title"><div><span>交易履约</span><h1>订单管理</h1><p>订单发货操作将通过管理端订单查询接口定位目标订单。</p></div></div><section class="admin-card unavailable-card"><el-icon><Connection /></el-icon><h2>等待管理端订单查询接口接入</h2><p>当前已定义 <code>POST /api/admin/orders/{orderNo}/shipment</code> 发货接口，但没有管理员查询订单列表或订单详情的接口，无法安全地在前端生成可操作订单表。</p><p>接口提供后，此页面可立即接入筛选、查看详情和发货表单。</p></section></div></template>
