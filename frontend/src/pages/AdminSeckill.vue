<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi, type ProductDetail, type SeckillActivity, type SeckillConsistency, type Sku } from '@/api/services'

const rows = ref<SeckillActivity[]>([])
const loading = ref(false)
const dialog = ref(false)
const saving = ref(false)
const consistency = ref<SeckillConsistency>()
const consistencyVisible = ref(false)

const productOptions = ref<ProductDetail[]>([])
const productsLoading = ref(false)

const form = reactive({
  name: '',
  productId: undefined as number | undefined,
  skuId: undefined as number | undefined,
  seckillPrice: 0,
  totalStock: 1,
  time: [] as Date[],
})

const isAvailableSku = (sku: Sku) => sku.status === 1 && Number(sku.availableStock) > 0
const availableSkus = (product?: ProductDetail) => product?.skus.filter(isAvailableSku) ?? []

const selectableProducts = computed(() =>
  productOptions.value.filter((product) => product.status === 1 && availableSkus(product).length > 0),
)
const selectedProduct = computed(() => productOptions.value.find((product) => product.id === form.productId))
const selectedSku = computed(() => availableSkus(selectedProduct.value).find((sku) => sku.id === form.skuId))

const formatSpec = (specJson: string) => {
  try {
    const spec = JSON.parse(specJson || '{}') as Record<string, unknown>
    const text = Object.entries(spec)
      .map(([key, value]) => `${key}：${String(value)}`)
      .join(' / ')
    return text || '默认规格'
  } catch {
    return specJson || '默认规格'
  }
}

const load = async () => {
  loading.value = true
  try {
    rows.value = await adminApi.seckillActivities()
  } finally {
    loading.value = false
  }
}

const loadProducts = async () => {
  if (productOptions.value.length > 0) return
  productsLoading.value = true
  try {
    const result = await adminApi.products({ status: 1, page: 1, size: 100 })
    productOptions.value = await Promise.all(result.items.map((product) => adminApi.product(product.id)))
  } finally {
    productsLoading.value = false
  }
}

const iso = (date: Date) => {
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const openCreate = async () => {
  Object.assign(form, {
    name: '',
    productId: undefined,
    skuId: undefined,
    seckillPrice: 0,
    totalStock: 1,
    time: [],
  })
  dialog.value = true
  await loadProducts()
}

const create = async () => {
  if (
    !form.name.trim()
    || !form.productId
    || !form.skuId
    || form.time.length !== 2
    || form.seckillPrice <= 0
    || form.totalStock <= 0
  ) {
    return ElMessage.warning('请完整填写活动名称、商品 SKU、价格、库存和活动时间')
  }

  saving.value = true
  try {
    await adminApi.createSeckill({
      name: form.name.trim(),
      skuId: form.skuId,
      seckillPrice: form.seckillPrice,
      totalStock: form.totalStock,
      startTime: iso(form.time[0]),
      endTime: iso(form.time[1]),
    })
    ElMessage.success('活动已创建')
    dialog.value = false
    await load()
  } finally {
    saving.value = false
  }
}

const action = async (kind: 'activate' | 'warmup' | 'end', id: number) => {
  if (kind === 'activate') await adminApi.activateSeckill(id)
  else if (kind === 'warmup') await adminApi.warmupSeckill(id)
  else await adminApi.endSeckill(id)
  ElMessage.success(kind === 'activate' ? '已激活并预热' : kind === 'warmup' ? 'Redis 库存已重建' : '活动已结束')
  await load()
}

const inspect = async (id: number) => {
  consistency.value = await adminApi.seckillConsistency(id)
  consistencyVisible.value = true
}

const time = (value: string) => value?.replace('T', ' ').slice(0, 19)

onMounted(load)
</script>

<template>
  <div>
    <div class="admin-page-title">
      <div>
        <span>并发活动</span>
        <h1>秒杀控制台</h1>
        <p>Redis 原子预扣，RocketMQ 排队，MySQL 异步落库，并实时核对三方库存。</p>
      </div>
      <el-button type="primary" @click="openCreate">创建活动</el-button>
    </div>

    <section class="admin-card">
      <el-alert
        title="一致性口径：Redis 剩余库存 = 数据库活动库存 = 初始库存 - 成功落库单数；消息积压期间允许短暂不一致。"
        type="info"
        show-icon
        :closable="false"
      />
      <el-table v-loading="loading" :data="rows" style="margin-top: 18px">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="活动" min-width="160" />
        <el-table-column prop="skuId" label="SKU" width="90" />
        <el-table-column label="价格" width="100">
          <template #default="scope">¥{{ Number(scope.row.seckillPrice).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="DB 库存" width="110">
          <template #default="scope">{{ scope.row.availableStock }} / {{ scope.row.totalStock }}</template>
        </el-table-column>
        <el-table-column label="活动时间" min-width="300">
          <template #default="scope">{{ time(scope.row.startTime) }} - {{ time(scope.row.endTime) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'DRAFT'" link type="success" @click="action('activate', scope.row.id)">
              激活+预热
            </el-button>
            <el-button v-if="scope.row.status === 'ACTIVE'" link @click="action('warmup', scope.row.id)">
              重建缓存
            </el-button>
            <el-button v-if="scope.row.status === 'ACTIVE'" link type="danger" @click="action('end', scope.row.id)">
              结束
            </el-button>
            <el-button link type="primary" @click="inspect(scope.row.id)">一致性检查</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialog" title="创建秒杀活动" width="600px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="活动名称" required>
          <el-input v-model="form.name" placeholder="例如：夏日爆款限时秒杀" />
        </el-form-item>

        <el-form-item label="商品" required>
          <el-select
            v-model="form.productId"
            style="width: 100%"
            filterable
            clearable
            :loading="productsLoading"
            no-data-text="暂无可用于秒杀的上架商品"
            placeholder="请选择商品"
            @change="form.skuId = undefined"
          >
            <el-option
              v-for="product in selectableProducts"
              :key="product.id"
              :label="`${product.title}（商品 #${product.id}）`"
              :value="product.id"
            >
              <div class="seckill-product-option">
                <span>{{ product.title }}</span>
                <small>商品 #{{ product.id }} · {{ availableSkus(product).length }} 个可用 SKU</small>
              </div>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="规格 SKU" required>
          <el-select
            v-model="form.skuId"
            style="width: 100%"
            filterable
            clearable
            :disabled="!form.productId"
            :loading="productsLoading"
            no-data-text="请先选择有库存的商品"
            placeholder="请选择商品规格"
          >
            <el-option
              v-for="sku in availableSkus(selectedProduct)"
              :key="sku.id"
              :label="`${sku.skuCode} · ${formatSpec(sku.specJson)} · 库存 ${sku.availableStock}`"
              :value="sku.id"
            >
              <div class="seckill-product-option">
                <span>{{ sku.skuCode }} · {{ formatSpec(sku.specJson) }}</span>
                <small>SKU #{{ sku.id }} · ¥{{ Number(sku.price).toFixed(2) }} · 可用库存 {{ sku.availableStock }}</small>
              </div>
            </el-option>
          </el-select>
          <p v-if="selectedSku" class="seckill-selection-hint">
            已选择：{{ selectedProduct?.title }} / {{ selectedSku.skuCode }}，可用库存 {{ selectedSku.availableStock }}
          </p>
        </el-form-item>

        <el-form-item label="秒杀价格" required>
          <el-input-number v-model="form.seckillPrice" :min="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="活动库存" required>
          <el-input-number v-model="form.totalStock" :min="1" :max="selectedSku?.availableStock || undefined" />
          <span v-if="selectedSku" class="seckill-stock-hint">最多 {{ selectedSku.availableStock }} 件</span>
        </el-form-item>
        <el-form-item label="活动时间" required>
          <el-date-picker
            v-model="form.time"
            type="datetimerange"
            start-placeholder="开始"
            end-placeholder="结束"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="create">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="consistencyVisible" title="库存一致性检查" width="620px">
      <el-result
        v-if="consistency"
        :icon="consistency.consistent ? 'success' : 'warning'"
        :title="consistency.consistent ? '库存已收敛' : '消息处理中或存在差异'"
        :sub-title="`活动 #${consistency.activityId}`"
      >
        <template #extra>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="初始库存">{{ consistency.initialStock }}</el-descriptions-item>
            <el-descriptions-item label="Redis 库存">{{ consistency.redisStock }}</el-descriptions-item>
            <el-descriptions-item label="数据库库存">{{ consistency.databaseStock }}</el-descriptions-item>
            <el-descriptions-item label="成功订单">{{ consistency.successfulOrders }}</el-descriptions-item>
            <el-descriptions-item label="估算积压" :span="2">{{ consistency.pendingMessages }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </el-result>
    </el-dialog>
  </div>
</template>

