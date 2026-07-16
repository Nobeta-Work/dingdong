import http from './http'

export type ApiPage<T> = { items: T[]; page: number; pageSize: number; total: number; pages: number }
export type Category = { id: number; name: string }
export type Brand = { id: number; name: string; logoUrl?: string }
export type ProductListItem = { id: number; title: string; subtitle?: string; mainImageUrl?: string; minPrice?: number; price?: number; sales?: number; categoryId?: number; brandId?: number }
export type Sku = { id: number; skuCode: string; specJson: string; price: number; availableStock: number; status: number }
export type ProductDetail = ProductListItem & { description?: string; skus: Sku[] }
export type CartItem = { id: number; skuId: number; quantity: number; selected: boolean; productTitle: string; mainImageUrl?: string; specJson?: string; unitPrice: number; availableStock?: number; valid?: boolean }
export type Address = { id: number; receiverName: string; receiverPhone: string; province: string; city: string; district: string; detailAddress: string; defaultAddress: boolean }
export type OrderItem = { skuId: number; productTitle: string; mainImageUrl?: string; specJson: string; unitPrice: number; quantity: number; totalAmount: number }
export type Order = { orderNo: string; status: string; totalAmount: number; createdAt?: string; items: OrderItem[]; receiverName?: string; receiverPhone?: string; receiverAddress?: string }
export type User = { id: number; username: string; nickname?: string; phone?: string; email?: string; avatarUrl?: string; role?: 'USER' | 'ADMIN' }

const data = <T>(promise: Promise<{ data: { data: T } }>) => promise.then((response) => response.data.data)

export const authApi = {
  login: (payload: { username: string; password: string }) => data<{ token: string; expiresIn: number; user: User }>(http.post('/auth/login', payload)),
  register: (payload: { username: string; password: string; nickname?: string; phone?: string; email?: string }) => data<User>(http.post('/auth/register', payload)),
  me: () => data<User>(http.get('/users/me')),
  updateMe: (payload: Partial<User>) => data<User>(http.put('/users/me', payload)),
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

export const addressApi = {
  list: () => data<Address[]>(http.get('/addresses')),
  create: (payload: Omit<Address, 'id'>) => data<Address>(http.post('/addresses', payload)),
  update: (id: number, payload: Omit<Address, 'id'>) => data<Address>(http.put(`/addresses/${id}`, payload)),
  remove: (id: number) => data<void>(http.delete(`/addresses/${id}`)),
}

export const orderApi = {
  create: (payload: { addressId: number; cartItemIds?: number[] }) => data<Order>(http.post('/orders', payload)),
  list: (params: { page: number; size: number }) => data<ApiPage<Order>>(http.get('/orders', { params })),
  detail: (orderNo: string) => data<Order>(http.get(`/orders/${orderNo}`)),
  confirm: (orderNo: string) => data<void>(http.post(`/orders/${orderNo}/confirm-receipt`)),
}

export const paymentApi = {
  create: (orderNo: string) => data<{ paymentNo: string; amount: number; status: string }>(http.post('/payments', { orderNo })),
  simulate: (paymentNo: string, success: boolean) => data<void>(http.post(`/payments/${paymentNo}/simulate`, { success })),
}

/**
 * 独立的文件上传接口：先上传文件并获得 URL，再由商品/用户等业务接口提交该 URL。
 * 默认约定为 POST /api/files，后端实现时可通过 VITE_FILE_UPLOAD_PATH 覆盖。
 */
export const fileApi = {
  upload: async (file: File) => {
    const body = new FormData()
    body.append('file', file)
    const path = import.meta.env.VITE_FILE_UPLOAD_PATH || '/files'
    return data<{ url: string }>(http.post(path, body))
  },
}

export const adminApi = {
  products: (params: Record<string, unknown>) => data<ApiPage<ProductListItem>>(http.get('/admin/products', { params })),
  createProduct: (payload: Record<string, unknown>) => data<ProductDetail>(http.post('/admin/products', payload)),
  updateProduct: (id: number, payload: Record<string, unknown>) => data<ProductDetail>(http.put(`/admin/products/${id}`, payload)),
  createSku: (spuId: number, payload: Record<string, unknown>) => data<Sku>(http.post(`/admin/products/${spuId}/skus`, payload)),
  updateSku: (spuId: number, id: number, payload: Record<string, unknown>) => data<Sku>(http.put(`/admin/products/${spuId}/skus/${id}`, payload)),
  ship: (orderNo: string, payload: { carrier: string; trackingNo: string }) => data<void>(http.post(`/admin/orders/${orderNo}/shipment`, payload)),
}
