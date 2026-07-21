<script setup lang="ts">
import { BellFilled, Collection, DataAnalysis, Goods, List, SwitchButton } from '@element-plus/icons-vue'
import { useSession } from '@/composables/session'
const { user } = useSession()
</script>
<template><div class="admin-shell"><aside class="admin-aside"><RouterLink to="/admin" class="admin-brand"><el-icon><BellFilled /></el-icon> 叮咚运营</RouterLink><el-menu router default-active="/admin" background-color="#0b1f3a" text-color="#c8d5e8" active-text-color="#ffffff"><el-menu-item index="/admin"><el-icon><DataAnalysis /></el-icon>经营概览</el-menu-item><el-menu-item index="/admin/products"><el-icon><Goods /></el-icon>商品管理</el-menu-item><el-menu-item index="/admin/catalog"><el-icon><Collection /></el-icon>分类与品牌</el-menu-item><el-menu-item index="/admin/orders"><el-icon><List /></el-icon>订单履约</el-menu-item></el-menu><div class="admin-exit"><el-icon><SwitchButton /></el-icon><RouterLink to="/">返回商城</RouterLink></div></aside><section class="admin-main"><header class="admin-head"><span>运营管理后台</span><span>管理员 · {{ user?.nickname || user?.username || '未登录' }}</span></header><div class="admin-content"><RouterView /></div></section></div></template>
