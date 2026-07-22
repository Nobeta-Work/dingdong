<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ArrowRight, CircleCheck, PriceTag, Service, Van } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { catalogApi, cartApi, type Category, type ProductListItem } from '@/api/services'
import ProductCard from '@/components/ProductCard.vue'
import { useCartSummary } from '@/composables/cart'

const categories = ref<Category[]>([])
const products = ref<ProductListItem[]>([])
const loading = ref(true)
const { refreshCartCount } = useCartSummary()
const heroProducts = computed(() => products.value.slice(0, 4))

const price = (value?: number) => Number(value || 0).toFixed(2)
const add = async (product: ProductListItem) => {
  try {
    const detail = await catalogApi.product(product.id)
    const sku = detail.skus.find((item) => item.status === 1 && item.availableStock > 0)
    if (!sku) return ElMessage.warning('商品暂无可售规格')
    await cartApi.add(sku.id, 1)
    await refreshCartCount()
    ElMessage.success('已加入购物车')
  } catch { /* handled centrally */ }
}

onMounted(async () => {
  try {
    const [categoryResult, productResult] = await Promise.all([
      catalogApi.categories(),
      catalogApi.products({ page: 1, size: 12, sort: 'sales' }),
    ])
    categories.value = categoryResult
    products.value = productResult.items
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="home-page">
    <section class="hero-section">
      <div class="shell">
        <el-carousel v-if="heroProducts.length" class="product-hero-carousel" height="500px" :interval="5600" arrow="always" trigger="click" pause-on-hover>
          <el-carousel-item v-for="(product, index) in heroProducts" :key="product.id">
            <article class="product-hero">
              <div class="hero-copy">
                <span v-if="index === 0" class="hero-eyebrow">本周热卖</span>
                <h1>{{ index === 0 ? '好物，准时到家' : product.title }}</h1>
                <p>{{ index === 0 ? '严选日常所需，让每一次下单都简单、安心。' : (product.subtitle || '叮咚严选，品质保障。') }}</p>
                <div class="hero-actions">
                  <RouterLink :to="index === 0 ? '/products' : `/product/${product.id}`"><el-button type="primary" size="large">{{ index === 0 ? '立即选购' : '查看商品' }}</el-button></RouterLink>
                  <RouterLink class="hero-link" :to="`/product/${product.id}`">热门商品 ¥{{ price(product.minPrice ?? product.price) }} <el-icon><ArrowRight /></el-icon></RouterLink>
                </div>
              </div>
              <RouterLink class="hero-media" :to="`/product/${product.id}`" :aria-label="product.title">
                <img :src="product.mainImageUrl || `https://picsum.photos/seed/dingdong-${product.id}/1200/900`" :alt="product.title" />
                <div class="hero-product-caption"><span>{{ product.title }}</span><b>¥{{ price(product.minPrice ?? product.price) }}</b></div>
              </RouterLink>
            </article>
          </el-carousel-item>
        </el-carousel>
        <div v-else class="product-hero hero-placeholder">
          <div class="hero-copy"><span class="hero-eyebrow">叮咚精选</span><h1>好物，准时到家</h1><p>严选日常所需，让每一次下单都简单、安心。</p><RouterLink to="/products"><el-button type="primary" size="large">立即选购</el-button></RouterLink></div>
          <div class="hero-media"><el-skeleton animated><template #template><el-skeleton-item variant="image" style="width:100%;height:100%" /></template></el-skeleton></div>
        </div>
      </div>
    </section>

    <section class="service-strip" aria-label="商城服务保障">
      <div class="shell service-grid">
        <span><el-icon><CircleCheck /></el-icon><b>正品保障</b><small>品牌与品质严格筛选</small></span>
        <span><el-icon><Van /></el-icon><b>极速配送</b><small>订单进度实时可查</small></span>
        <span><el-icon><Service /></el-icon><b>安心售后</b><small>问题处理清晰透明</small></span>
        <span><el-icon><PriceTag /></el-icon><b>价格透明</b><small>实时价格清楚展示</small></span>
      </div>
    </section>

    <section v-if="categories.length" class="shell category-shortcuts">
      <RouterLink v-for="category in categories.slice(0, 8)" :key="category.id" :to="{ path: '/products', query: { categoryId: category.id } }">
        <span>{{ category.name.slice(0, 1) }}</span>{{ category.name }}
      </RouterLink>
    </section>

    <section id="hot" class="shell home-section">
      <div class="section-title modern-section-title">
        <div><h2>本周值得买</h2><p>根据真实销量整理的热门商品。</p></div>
        <RouterLink to="/products">查看全部 <el-icon><ArrowRight /></el-icon></RouterLink>
      </div>
      <el-skeleton :loading="loading" animated :count="1">
        <template #template><div class="featured-product-grid"><el-skeleton-item v-for="item in 4" :key="item" variant="rect" style="height:390px" /></div></template>
        <template #default><div class="featured-product-grid"><ProductCard v-for="product in products.slice(0, 4)" :key="product.id" :product="product" @add="add" /></div></template>
      </el-skeleton>
    </section>

    <section id="recommend" class="shell home-section recommendation-section">
      <div class="section-title modern-section-title"><div><h2>更多日常精选</h2><p>从数码家电到家居个护，找到真正需要的商品。</p></div></div>
      <div class="product-grid"><ProductCard v-for="product in products.slice(4, 12)" :key="product.id" :product="product" @add="add" /></div>
    </section>
  </div>
</template>
