<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { products, categories, type Product } from '@/mock'
import ProductCard from '@/components/ProductCard.vue'
const route = useRoute(); const activeCategory = ref(String(route.query.category || '全部')); const order = ref('综合'); const message = ref('')
const displayed = computed(() => activeCategory.value === '全部' ? products : products.filter((item) => item.category === activeCategory.value))
const add = (product: Product) => message.value = `已将「${product.name}」加入购物车`
</script>
<template><div class="shell page"><el-breadcrumb separator="/"><el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item><el-breadcrumb-item>全部商品</el-breadcrumb-item></el-breadcrumb><div class="listing-head"><div><span>叮咚精选</span><h1>发现值得买的好物</h1><p>共 {{ displayed.length }} 件商品，品质生活从这里开始。</p></div></div><section class="filter-box"><div><strong>商品分类</strong><el-radio-group v-model="activeCategory"><el-radio-button label="全部" /><el-radio-button v-for="item in categories" :key="item" :label="item" /></el-radio-group></div><div><strong>排序方式</strong><el-radio-group v-model="order"><el-radio-button label="综合" /><el-radio-button label="销量" /><el-radio-button label="价格" /><el-radio-button label="上新" /></el-radio-group></div></section><div class="listing-bar"><span>为你找到 <b>{{ displayed.length }}</b> 件商品</span><el-select v-model="order" style="width: 130px"><el-option v-for="item in ['综合', '销量', '价格', '上新']" :key="item" :label="item" :value="item" /></el-select></div><div class="product-grid"><ProductCard v-for="product in displayed" :key="product.id" :product="product" @add="add" /></div><el-empty v-if="!displayed.length" description="该分类暂无商品" /><div class="pager"><el-pagination background layout="prev, pager, next" :total="60" /></div><el-alert v-if="message" class="floating-notice" type="success" :closable="true" @close="message = ''" show-icon>{{ message }}</el-alert></div></template>
