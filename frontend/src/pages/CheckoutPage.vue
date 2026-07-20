<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { addressApi, cartApi, orderApi, type Address, type CartItem } from '@/api/services'
const router = useRouter(); const addresses = ref<Address[]>([]); const items = ref<CartItem[]>([]); const addressId = ref<number>(); const loading = ref(true); const total = computed(() => items.value.filter((item) => item.selected).reduce((sum, item) => sum + item.unitPrice * item.quantity, 0)); const price = (value: number) => value.toFixed(2)
// 提交订单：将选中的地址 ID 和购物车选中商品提交到后端创建订单
const submit = async () => { if (!addressId.value) return; const order = await orderApi.create({ addressId: addressId.value, cartItemIds: items.value.filter((item) => item.selected).map((item) => item.id) }); router.push({ path: '/payment', query: { orderNo: order.orderNo, amount: order.totalAmount } }) }
// 页面加载：并行请求地址列表和购物车数据 → 默认选中默认地址或第一个地址
onMounted(async () => { try { const [addressData, cartData] = await Promise.all([addressApi.list(), cartApi.list()]); addresses.value = addressData; items.value = cartData; addressId.value = addressData.find((item) => item.defaultAddress)?.id || addressData[0]?.id } finally { loading.value = false } })
</script>
<template>
  <div class="shell page">
    <div class="checkout-steps">
      <span class="done">1. 确认订单</span><i></i><span>2. 模拟支付</span><i></i><span>3. 完成</span>
    </div>
    <h1 class="checkout-title">确认订单信息</h1>
    <el-skeleton :loading="loading" animated :rows="10">
      <template #default>
        <!-- 收货地址选择区域：展示用户所有地址，点击选中用于本次下单 -->
        <section class="checkout-box">
          <h2>收货地址</h2>
          <div class="address-list">
            <!-- 遍历地址列表，高亮当前选中项 -->
            <article v-for="address in addresses" :key="address.id" :class="{ selected: addressId === address.id }" @click="addressId = address.id">
              <b>{{ address.receiverName }}　{{ address.receiverPhone }}</b>
              <p>{{ address.province }} {{ address.city }} {{ address.district }} {{ address.detailAddress }}</p>
              <!-- 默认地址标记 -->
              <span v-if="address.defaultAddress">默认地址</span>
            </article>
            <!-- 无地址时的空状态提示 -->
            <el-empty v-if="!addresses.length" description="暂无收货地址，请先在个人中心添加" />
          </div>
        </section>
        <section class="checkout-box">
          <h2>商品清单</h2>
          <article v-for="item in items.filter((entry) => entry.selected)" :key="item.id" class="checkout-product">
            <img :src="item.mainImageUrl || 'https://placehold.co/200x200/eaf3ff/1677ff?text=DingDong'" :alt="item.productTitle" />
            <div><b>{{ item.productTitle }}</b><p>{{ item.specJson }}</p></div>
            <span>¥{{ price(item.unitPrice) }} × {{ item.quantity }}</span>
            <strong>¥{{ price(item.unitPrice * item.quantity) }}</strong>
          </article>
        </section>
        <section class="checkout-total">
          <h2>应付金额：<strong>¥{{ price(total) }}</strong></h2>
          <!-- 提交订单按钮：地址未选或无可选商品时禁用 -->
          <el-button type="primary" size="large" :disabled="!addressId || !items.some((item) => item.selected)" @click="submit">提交订单</el-button>
        </section>
      </template>
    </el-skeleton>
  </div>
</template>
