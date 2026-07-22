<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, ShoppingCart, UserFilled } from '@element-plus/icons-vue'
import { useSession } from '@/composables/session'
import { useCartSummary } from '@/composables/cart'

const router = useRouter()
const route = useRoute()
const keyword = ref(typeof route.query.q === 'string' ? route.query.q : '')
const mobileOpen = ref(false)
const active = computed(() => route.path)
const { user, clear } = useSession()
const { cartCount, refreshCartCount } = useCartSummary()

const submitSearch = () => {
  const q = keyword.value.trim()
  router.push({ path: '/products', query: q ? { q } : {} })
}
const logout = () => {
  clear()
  cartCount.value = 0
  router.push('/auth')
}

onMounted(refreshCartCount)
watch(() => route.fullPath, () => {
  mobileOpen.value = false
  refreshCartCount()
})
</script>

<template>
  <header class="store-header">
    <div class="shell store-header-inner">
      <RouterLink class="brand" to="/" aria-label="叮咚商城首页">
        <span class="brand-mark"><img src="/DingDongLogo.png" alt="" /></span>
        <span class="brand-copy">叮咚商城<small>好物准时到</small></span>
      </RouterLink>

      <nav class="store-nav" :class="{ open: mobileOpen }" aria-label="商城主导航">
        <RouterLink to="/" :class="{ active: active === '/' }">首页</RouterLink>
        <RouterLink to="/products" :class="{ active: active.startsWith('/product') }">全部商品</RouterLink>
        <RouterLink to="/seckill" :class="{ active: active === '/seckill' }">限时秒杀</RouterLink>
      </nav>

      <form class="global-search" role="search" @submit.prevent="submitSearch">
        <el-icon><Search /></el-icon>
        <input v-model="keyword" aria-label="搜索商品" placeholder="搜索商品、品牌或品类" />
        <button type="submit">搜索</button>
      </form>

      <div class="header-actions">
        <RouterLink class="icon-action account-action" :to="user ? '/profile' : '/auth'" :aria-label="user ? '个人中心' : '登录'">
          <el-avatar v-if="user?.avatarUrl" :size="30" :src="user.avatarUrl" />
          <el-icon v-else><UserFilled /></el-icon>
          <span>{{ user?.nickname || (user ? '个人中心' : '登录') }}</span>
        </RouterLink>
        <RouterLink class="icon-action cart-action" to="/cart" aria-label="购物车">
          <el-icon><ShoppingCart /></el-icon>
          <span>购物车</span>
          <b v-if="cartCount">{{ cartCount > 99 ? '99+' : cartCount }}</b>
        </RouterLink>
        <button class="mobile-menu" type="button" :aria-expanded="mobileOpen" aria-label="打开导航" @click="mobileOpen = !mobileOpen">
          <span></span><span></span><span></span>
        </button>
      </div>
    </div>
  </header>

  <main><RouterView /></main>

  <footer class="store-footer">
    <div class="shell footer-grid">
      <div class="footer-intro">
        <div class="brand footer-brand">
          <span class="brand-mark"><img src="/DingDongLogo.png" alt="" /></span>
          <span class="brand-copy">叮咚商城<small>好物准时到</small></span>
        </div>
        <p>严选日常所需，让每一次下单都简单、安心。</p>
      </div>
      <div><strong>购物服务</strong><RouterLink to="/products">全部商品</RouterLink><RouterLink to="/cart">购物车</RouterLink><RouterLink to="/orders">我的订单</RouterLink></div>
      <div><strong>账户管理</strong><RouterLink to="/profile">个人资料</RouterLink><RouterLink to="/addresses">收货地址</RouterLink><RouterLink to="/account">账户安全</RouterLink></div>
      <div><strong>服务时间</strong><b>400-888-0000</b><p>周一至周日 9:00-21:00</p><el-button v-if="user" text class="footer-logout" @click="logout">退出登录</el-button></div>
    </div>
    <div class="shell footer-bottom"><span>叮咚商城</span><span>正品保障 / 极速配送 / 安心售后</span></div>
  </footer>
</template>
