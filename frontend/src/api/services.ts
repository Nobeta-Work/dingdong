import http from './http'

export type ApiPage<T> = { items: T[]; page: number; pageSize: number; total: number; pages: number }
export type Category = { id: number; name: string; parentId?: number; sortOrder?: number; status?: number }
export type Brand = { id: number; name: string; logoUrl?: string; sortOrder?: number; status?: number }
export type ProductListItem = { id: number; title: string; subtitle?: string; mainImageUrl?: string; minPrice?: number; price?: number; sales?: number; categoryId?: number; brandId?: number; status?: number; createdAt?: string }
export type Sku = { id: number; skuCode: string; specJson: string; price: number; availableStock: number; lockedStock?: number; sales?: number; status: number }
export type ProductDetail = ProductListItem & { description?: string; skus: Sku[] }
export type CartItem = { id: number; skuId: number; quantity: number; selected: boolean; productTitle: string; mainImageUrl?: string; specJson?: string; unitPrice: number; availableStock?: number; valid?: boolean }
/** 收货地址数据模型 —— 用于收货地址增删改查的前后端数据契约 */
export type Address = { id: number; receiverName: string; receiverPhone: string; province: string; city: string; district: string; detailAddress: string; defaultAddress: boolean }
export type OrderItem = { skuId: number; productTitle: string; mainImageUrl?: string; specJson: string; unitPrice: number; quantity: number; totalAmount: number }
export type Order = { orderNo: string; status: string; totalAmount: number; createdAt?: string; items: OrderItem[]; receiverName?: string; receiverPhone?: string; receiverAddress?: string; carrier?: string; trackingNo?: string; shippedAt?: string }
export type AdminOrder = Order & { userId: number }
export type TopProduct = { skuId: number; productTitle: string; productImageUrl?: string; quantity: number; salesAmount: number }
export type DashboardOverview = { todayOrderCount: number; todayPaidAmount: number; pendingShipmentCount: number; topProducts: TopProduct[] }
export type User = { id: number; username: string; nickname?: string; phone?: string; email?: string; avatarUrl?: string; role?: 'USER' | 'ADMIN' }
export type AdminUser = User & { status: number; createdAt?: string; updatedAt?: string }
export type SeckillActivity = { id: number; name: string; skuId: number; seckillPrice: number; totalStock: number; availableStock: number; status: 'DRAFT' | 'ACTIVE' | 'ENDED'; startTime: string; endTime: string }
export type SeckillConsistency = { activityId: number; initialStock: number; redisStock: number; databaseStock: number; successfulOrders: number; pendingMessages: number; consistent: boolean }
export type SmsCodeResult = { mock: boolean; debugCode: string; expireSeconds: number; retryAfterSeconds: number }

const data = <T>(promise: Promise<{ data: { data: T } }>) => promise.then((response) => response.data.data)

export const authApi = {
  login: (payload: { username: string; password: string }) => data<{ token: string; expiresIn: number; user: User }>(http.post('/auth/login', payload)),
  register: (payload: { username: string; password: string; nickname?: string; phone?: string; email?: string }) => data<User>(http.post('/auth/register', payload)),
  sendSmsCode: (phone: string, scene: 'login' | 'register' | 'change-phone') => data<SmsCodeResult>(http.post('/auth/sms/code', { phone, scene })),
  smsLogin: (phone: string, code: string) => data<{ token: string; expiresIn: number; user: User }>(http.post('/auth/sms/login', { phone, code })),
  /** 查询当前登录用户的个人资料 → GET /api/users/me */
  me: () => data<User>(http.get('/users/me')),
  /** 修改当前登录用户的个人资料 → PUT /api/users/me，payload 为要修改的字段（昵称必填，其余可选） */
  updateMe: (payload: Partial<User> & { smsCode?: string }) => data<User>(http.put('/users/me', payload)),
  changePassword: (payload: { currentPassword: string; newPassword: string }) => data<void>(http.put('/users/me/password', payload)),
}

export const fileApi = {
  uploadImage: (file: File) => {
    const body = new FormData()
    body.append('file', file)
    return data<{ url: string; path: string }>(http.post('/files/images', body, { timeout: 30_000 }))
  },
}

export const catalogApi = {
  categories: () => data<Category[]>(http.get('/categories')),
  brands: () => data<Brand[]>(http.get('/brands')),
  products: (params: Record<string, unknown>) => data<ApiPage<ProductListItem>>(http.get('/products', { params })),
  product: (id: number) => data<ProductDetail>(http.get(`/products/${id}`)),
}

export const cartApi = {
  list: () => data<CartItem[]>(http.get('/cart/items')),
  add: (skuId: number, quantity: number) => data<CartItem>(http.post('/cart/items', { skuId, quantity })),
  update: (id: number, payload: Pick<CartItem, 'quantity' | 'selected'>) => data<CartItem>(http.put(`/cart/items/${id}`, payload)),
  remove: (id: number) => data<void>(http.delete(`/cart/items/${id}`)),
}

/** 收货地址 API —— 地址增删改查的前端调用封装 */
export const addressApi = {
  /** 查询收货地址列表 → GET /api/addresses */
  list: () => data<Address[]>(http.get('/addresses')),
  /** 新增收货地址 → POST /api/addresses */
  create: (payload: Omit<Address, 'id'>) => data<Address>(http.post('/addresses', payload)),
  /** 修改收货地址 → PUT /api/addresses/{id} */
  update: (id: number, payload: Omit<Address, 'id'>) => data<Address>(http.put(`/addresses/${id}`, payload)),
  /** 删除收货地址 → DELETE /api/addresses/{id} */
  remove: (id: number) => data<void>(http.delete(`/addresses/${id}`)),
}

export const orderApi = {
  create: (payload: { addressId: number; cartItemIds?: number[]; requestId?: string }) => data<Order>(http.post('/orders', { requestId: crypto.randomUUID(), ...payload })),
  list: (params: { page: number; size: number }) => data<ApiPage<Order>>(http.get('/orders', { params })),
  detail: (orderNo: string) => data<Order>(http.get(`/orders/${orderNo}`)),
  cancel: (orderNo: string) => data<Order>(http.post(`/orders/${orderNo}/cancel`)),
  confirm: (orderNo: string) => data<void>(http.post(`/orders/${orderNo}/confirm-receipt`)),
}

export const paymentApi = {
  create: (orderNo: string) => data<{ paymentNo: string; amount: number; status: string }>(http.post('/payments', { orderNo })),
  simulate: (paymentNo: string, success: boolean) => data<void>(http.post(`/payments/${paymentNo}/simulate`, { success })),
  detail: (paymentNo: string) => data<{ paymentNo: string; orderNo: string; amount: number; status: string }>(http.get(`/payments/${paymentNo}`)),
}

export const seckillApi = {
  activities: () => data<SeckillActivity[]>(http.get('/seckill/activities')),
  purchase: (activityId: number, requestId = crypto.randomUUID()) => data<{ requestId: string; status: string; remainingStock: number }>(http.post(`/seckill/activities/${activityId}/orders`, { requestId })),
  result: (requestId: string) => data<{ requestId: string; status: string; orderId?: number }>(http.get(`/seckill/orders/${requestId}`)),
}

export const adminApi = {
  dashboard: () => data<DashboardOverview>(http.get('/admin/dashboard/overview')),
  products: (params: Record<string, unknown>) => data<ApiPage<ProductListItem>>(http.get('/admin/products', { params })),
  product: (id: number) => data<ProductDetail>(http.get(`/admin/products/${id}`)),
  createProduct: (payload: Record<string, unknown>) => data<ProductDetail>(http.post('/admin/products', payload)),
  updateProduct: (id: number, payload: Record<string, unknown>) => data<ProductDetail>(http.put(`/admin/products/${id}`, payload)),
  createSku: (spuId: number, payload: Record<string, unknown>) => data<Sku>(http.post(`/admin/products/${spuId}/skus`, payload)),
  updateSku: (spuId: number, id: number, payload: Record<string, unknown>) => data<Sku>(http.put(`/admin/products/${spuId}/skus/${id}`, payload)),
  categories: () => data<Category[]>(http.get('/admin/categories')),
  createCategory: (payload: Omit<Category, 'id'>) => data<Category>(http.post('/admin/categories', payload)),
  updateCategory: (id: number, payload: Omit<Category, 'id'>) => data<Category>(http.put(`/admin/categories/${id}`, payload)),
  brands: () => data<Brand[]>(http.get('/admin/brands')),
  createBrand: (payload: Omit<Brand, 'id'>) => data<Brand>(http.post('/admin/brands', payload)),
  updateBrand: (id: number, payload: Omit<Brand, 'id'>) => data<Brand>(http.put(`/admin/brands/${id}`, payload)),
  orders: (params: Record<string, unknown>) => data<ApiPage<AdminOrder>>(http.get('/admin/orders', { params })),
  order: (orderNo: string) => data<AdminOrder>(http.get(`/admin/orders/${orderNo}`)),
  ship: (orderNo: string, payload: { carrier: string; trackingNo: string }) => data<AdminOrder>(http.post(`/admin/orders/${orderNo}/shipment`, payload)),
  users: (params: Record<string, unknown>) => data<{ items: AdminUser[]; total: number; page: number; size: number }>(http.get('/admin/users', { params })),
  user: (id: number) => data<AdminUser>(http.get(`/admin/users/${id}`)),
  changeUserStatus: (id: number, status: number) => data<AdminUser>(http.put(`/admin/users/${id}/status`, { status })),
  seckillActivities: () => data<SeckillActivity[]>(http.get('/admin/seckill/activities')),
  createSeckill: (payload: Record<string, unknown>) => data<SeckillActivity>(http.post('/admin/seckill/activities', payload)),
  activateSeckill: (id: number) => data<SeckillActivity>(http.post(`/admin/seckill/activities/${id}/activate`)),
  warmupSeckill: (id: number) => data<void>(http.post(`/admin/seckill/activities/${id}/warmup`)),
  endSeckill: (id: number) => data<SeckillActivity>(http.post(`/admin/seckill/activities/${id}/end`)),
  seckillConsistency: (id: number) => data<SeckillConsistency>(http.get(`/admin/seckill/activities/${id}/consistency`)),
}
