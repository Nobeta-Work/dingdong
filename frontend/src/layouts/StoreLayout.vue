<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { BellFilled, ShoppingCart, Search, UserFilled, Location } from '@element-plus/icons-vue'

const router = useRouter()
const keyword = ref('')
const cartCount = ref(3)
const submitSearch = () => router.push({ path: '/products', query: { q: keyword.value } })
const active = computed(() => router.currentRoute.value.path)
</script>

<template>
  <header class="topbar"><div class="shell topbar-inner"><span>欢迎来到叮咚商城</span><span class="topbar-links">北京 <el-icon><Location /></el-icon>　我的订单　客服中心　 <RouterLink to="/admin">商家后台</RouterLink></span></div></header>
  <header class="store-head"><div class="shell head-inner">
    <RouterLink class="brand" to="/"><span class="brand-mark"><el-icon><BellFilled /></el-icon></span><span>叮咚商城<small>好物准时到</small></span></RouterLink>
    <el-input v-model="keyword" class="search-input" placeholder="搜索商品、品牌或品类" @keyup.enter="submitSearch"><template #append><el-button :icon="Search" @click="submitSearch">搜索</el-button></template></el-input>
    <RouterLink class="cart-shortcut" to="/cart"><el-badge :value="cartCount"><el-icon><ShoppingCart /></el-icon></el-badge><span>我的购物车</span></RouterLink>
  </div></header>
  <nav class="primary-nav"><div class="shell nav-inner"><RouterLink to="/" :class="{ active: active === '/' }">首页</RouterLink><RouterLink to="/products" :class="{ active: active.includes('/product') }">全部商品</RouterLink><a href="#hot">热销好物</a><a href="#recommend">为你推荐</a><RouterLink to="/orders">我的订单</RouterLink><RouterLink to="/profile"><el-icon><UserFilled /></el-icon>个人中心</RouterLink></div></nav>
  <main><RouterView /></main>
  <footer class="store-footer"><div class="shell footer-grid"><div><div class="brand footer-brand"><span class="brand-mark"><el-icon><BellFilled /></el-icon></span><span>叮咚商城<small>好物准时到</small></span></div><p>严选品质，安心购物。让每一次下单都有回应。</p></div><div><strong>购物指南</strong><a>购物流程</a><a>会员介绍</a><a>常见问题</a></div><div><strong>配送服务</strong><a>配送说明</a><a>运费标准</a><a>售后政策</a></div><div><strong>联系我们</strong><b>400-888-0000</b><p>周一至周日 9:00-21:00</p></div></div></footer>
</template>
