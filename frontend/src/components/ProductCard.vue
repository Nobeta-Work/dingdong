<script setup lang="ts">
import { ShoppingCart } from '@element-plus/icons-vue'
import type { Product } from '@/mock'
import { formatPrice } from '@/mock'
defineProps<{ product: Product }>()
const emit = defineEmits<{ add: [product: Product] }>()
</script>
<template><article class="product-card"><RouterLink :to="`/product/${product.id}`" class="product-image"><img :src="product.image" :alt="product.name" /><span v-if="product.tag">{{ product.tag }}</span></RouterLink><div class="product-info"><RouterLink :to="`/product/${product.id}`" class="product-name">{{ product.name }}</RouterLink><p>{{ product.subtitle }}</p><div class="product-bottom"><div><b>¥{{ formatPrice(product.price) }}</b><del v-if="product.oldPrice">¥{{ formatPrice(product.oldPrice) }}</del></div><el-button circle :icon="ShoppingCart" @click="emit('add', product)" /></div><small>{{ product.sales.toLocaleString() }} 人已购买</small></div></article></template>
