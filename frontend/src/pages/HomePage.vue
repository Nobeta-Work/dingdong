<script setup lang="ts">
import { ref } from 'vue'
import { ArrowRight, Lightning, Service, Van, CircleCheck } from '@element-plus/icons-vue'
import { categories, products, type Product } from '@/mock'
import ProductCard from '@/components/ProductCard.vue'
const notice = ref('')
const add = (product: Product) => { notice.value = `已将「${product.name}」加入购物车` }
</script>
<template><div>
  <section class="shell hero-wrap"><aside class="category-panel"><h3>全部商品分类</h3><RouterLink v-for="category in categories" :key="category" :to="{ path: '/products', query: { category } }">{{ category }}<el-icon><ArrowRight /></el-icon></RouterLink></aside><div class="hero"><div class="hero-copy"><span>夏日清凉季</span><h1>精选好物，<br />准时叮咚到家</h1><p>甄选品质商品，享受清爽夏日价。</p><RouterLink to="/products"><el-button type="primary" size="large">立即选购 <el-icon><ArrowRight /></el-icon></el-button></RouterLink></div><div class="hero-art"><div class="hero-orb orb-one"></div><div class="hero-orb orb-two"></div><img src="https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=1000&q=85" alt="夏日精选商品" /></div></div></section>
  <section class="shell benefits"><span><el-icon><Lightning /></el-icon>限时好价</span><span><el-icon><CircleCheck /></el-icon>正品保障</span><span><el-icon><Van /></el-icon>极速配送</span><span><el-icon><Service /></el-icon>贴心售后</span></section>
  <section id="hot" class="shell section"><div class="section-title"><div><span>限时抢购</span><h2>热门好物，抢先享</h2></div><RouterLink to="/products">查看全部 <el-icon><ArrowRight /></el-icon></RouterLink></div><div class="flash-grid"><RouterLink v-for="item in products.slice(0, 4)" :key="item.id" :to="`/product/${item.id}`"><img :src="item.image" :alt="item.name" /><p>{{ item.name }}</p><b>¥{{ item.price }}</b><del v-if="item.oldPrice">¥{{ item.oldPrice }}</del></RouterLink></div></section>
  <section id="recommend" class="shell section"><div class="section-title"><div><span>每日精选</span><h2>为你推荐</h2></div><RouterLink to="/products">更多商品 <el-icon><ArrowRight /></el-icon></RouterLink></div><div class="product-grid"><ProductCard v-for="product in products" :key="product.id" :product="product" @add="add" /></div></section>
  <el-notification v-if="false" />
  <el-alert v-if="notice" class="floating-notice" type="success" :closable="true" @close="notice = ''" show-icon>{{ notice }}</el-alert>
</div></template>
