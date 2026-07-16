<script setup lang="ts">
import { ShoppingCart } from '@element-plus/icons-vue'
import type { ProductListItem } from '@/api/services'
defineProps<{ product: ProductListItem }>()
const emit = defineEmits<{ add: [product: ProductListItem] }>()
const price = (value?: number) => Number(value || 0).toFixed(2)
</script>
<template><article class="product-card"><RouterLink :to="`/product/${product.id}`" class="product-image"><img :src="product.mainImageUrl || 'https://placehold.co/600x600/eaf3ff/1677ff?text=DingDong'" :alt="product.title" /></RouterLink><div class="product-info"><RouterLink :to="`/product/${product.id}`" class="product-name">{{ product.title }}</RouterLink><p>{{ product.subtitle || '叮咚严选，品质保障' }}</p><div class="product-bottom"><div><b>¥{{ price(product.minPrice ?? product.price) }}</b></div><el-button circle :icon="ShoppingCart" @click="emit('add', product)" /></div><small>{{ Number(product.sales || 0).toLocaleString() }} 人已购买</small></div></article></template>
