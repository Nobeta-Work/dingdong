<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ShoppingCart, Search, UserFilled, Location } from '@element-plus/icons-vue'
import { useSession } from '@/composables/session'
import { useCartSummary } from '@/composables/cart'

const router = useRouter()
const route = useRoute()
const keyword = ref('')
const submitSearch = () => router.push({ path: '/products', query: { q: keyword.value } })
const active = computed(() => router.currentRoute.value.path)
const { user, clear } = useSession()
const { cartCount, refreshCartCount } = useCartSummary()
const logout = () => { clear(); cartCount.value = 0; router.push('/auth') }
onMounted(refreshCartCount)
watch(() => route.fullPath, refreshCartCount)
</script>

<template>
  <header class="topbar"><div class="shell topbar-inner"><span>欢迎来到叮咚商城</span><span class="topbar-links">北京 <el-icon><Location /></el-icon>　<RouterLink to="/orders">我的订单</RouterLink>　 <RouterLink v-if="user?.role === 'ADMIN'" to="/admin">商家后台</RouterLink><RouterLink v-else-if="!user" to="/auth">登录 / 注册</RouterLink><el-button v-else text size="small" @click="logout">退出登录</el-button></span></div></header>
  <header class="store-head"><div class="shell head-inner">
    <RouterLink class="brand" to="/"><span class="brand-mark"><img src="/DingDongLogo.png" alt="叮咚商城" /></span><span>叮咚商城<small>好物准时到</small></span></RouterLink>
    <el-input v-model="keyword" class="search-input" placeholder="搜索商品、品牌或品类" @keyup.enter="submitSearch"><template #append><el-button :icon="Search" @click="submitSearch">搜索</el-button></template></el-input>
    <RouterLink class="cart-shortcut" to="/cart"><el-badge :value="cartCount"><el-icon><ShoppingCart /></el-icon></el-badge><span>我的购物车</span></RouterLink>
  </div></header>
  <nav class="primary-nav"><div class="shell nav-inner"><RouterLink to="/" :class="{ active: active === '/' }">首页</RouterLink><RouterLink to="/products" :class="{ active: active.includes('/product') }">全部商品</RouterLink><RouterLink to="/seckill">限时秒杀</RouterLink><RouterLink to="/orders">我的订单</RouterLink><RouterLink to="/profile"><el-icon><UserFilled /></el-icon>个人中心</RouterLink></div></nav>
  <main><RouterView /></main>
  <footer class="store-footer"><div class="shell footer-grid"><div><div class="brand footer-brand"><span class="brand-mark"><img src="/DingDongLogo.png" alt="叮咚商城" /></span><span>叮咚商城<small>好物准时到</small></span></div><p>严选品质，安心购物。让每一次下单都有回应。</p></div><div><strong>购物指南</strong><span>选购商品</span><span>加入购物车</span><span>提交并支付</span></div><div><strong>配送与售后</strong><span>订单状态实时可查</span><span>支持确认收货</span><span>售后请联系客服</span></div><div><strong>联系我们</strong><b>400-888-0000</b><p>周一至周日 9:00-21:00</p></div></div></footer>
</template>
