<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Bell, Collection, DataAnalysis, Goods, List, Search, SwitchButton, Timer, User } from '@element-plus/icons-vue'
import { useSession } from '@/composables/session'

const route = useRoute()
const { user } = useSession()
const collapsed = ref(false)
const activeMenu = computed(() => route.path)
const pageName = computed(() => ({
  '/admin': '经营概览',
  '/admin/products': '商品管理',
  '/admin/catalog': '分类与品牌',
  '/admin/orders': '订单管理',
  '/admin/users': '用户管理',
  '/admin/seckill': '秒杀活动',
}[route.path] || '管理中心'))
</script>

<template>
  <div class="admin-shell" :class="{ 'is-collapsed': collapsed }">
    <aside class="admin-aside">
      <RouterLink to="/admin" class="admin-brand">
        <span class="brand-mark"><img src="/DingDongLogo.png" alt="" /></span>
        <span>叮咚商城<small>管理中心</small></span>
      </RouterLink>
      <el-menu router :default-active="activeMenu">
        <el-menu-item index="/admin"><el-icon><DataAnalysis /></el-icon><span>经营概览</span></el-menu-item>
        <el-menu-item index="/admin/products"><el-icon><Goods /></el-icon><span>商品管理</span></el-menu-item>
        <el-menu-item index="/admin/catalog"><el-icon><Collection /></el-icon><span>分类与品牌</span></el-menu-item>
        <el-menu-item index="/admin/orders"><el-icon><List /></el-icon><span>订单管理</span></el-menu-item>
        <el-menu-item index="/admin/users"><el-icon><User /></el-icon><span>用户管理</span></el-menu-item>
        <el-menu-item index="/admin/seckill"><el-icon><Timer /></el-icon><span>秒杀活动</span></el-menu-item>
      </el-menu>
      <RouterLink class="admin-exit" to="/"><el-icon><SwitchButton /></el-icon><span>返回商城</span></RouterLink>
    </aside>
    <section class="admin-main">
      <header class="admin-head">
        <div><button type="button" aria-label="收起侧边栏" @click="collapsed = !collapsed"><span></span><span></span><span></span></button><b>{{ pageName }}</b></div>
        <div class="admin-head-actions"><el-button text circle :icon="Search" aria-label="搜索" /><el-button text circle :icon="Bell" aria-label="通知" /><el-avatar :size="34" :src="user?.avatarUrl">{{ user?.nickname?.slice(0, 1) || '管' }}</el-avatar><span>{{ user?.nickname || user?.username || '管理员' }}</span></div>
      </header>
      <div class="admin-content"><RouterView /></div>
    </section>
  </div>
</template>
