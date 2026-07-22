import { createRouter, createWebHistory } from 'vue-router'
import StoreLayout from './layouts/StoreLayout.vue'
import AdminLayout from './layouts/AdminLayout.vue'

const HomePage = () => import('./pages/HomePage.vue')
const ProductsPage = () => import('./pages/ProductsPage.vue')
const ProductPage = () => import('./pages/ProductPage.vue')
const CartPage = () => import('./pages/CartPage.vue')
const CheckoutPage = () => import('./pages/CheckoutPage.vue')
const PaymentPage = () => import('./pages/PaymentPage.vue')
const OrdersPage = () => import('./pages/OrdersPage.vue')
const ProfilePage = () => import('./pages/ProfilePage.vue')
const AdminDashboard = () => import('./pages/AdminDashboard.vue')
const AdminProducts = () => import('./pages/AdminProducts.vue')
const AdminOrders = () => import('./pages/AdminOrders.vue')
const AuthPage = () => import('./pages/AuthPage.vue')
const AddressPage = () => import('./pages/AddressPage.vue')
const AccountSecurity = () => import('./pages/AccountSecurity.vue')
const AdminCatalog = () => import('./pages/AdminCatalog.vue')
const AdminUsers = () => import('./pages/AdminUsers.vue')
const AdminSeckill = () => import('./pages/AdminSeckill.vue')
const SeckillPage = () => import('./pages/SeckillPage.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: StoreLayout, children: [
      { path: '', component: HomePage }, { path: 'products', component: ProductsPage }, { path: 'product/:id', component: ProductPage },
      { path: 'cart', component: CartPage, meta: { requiresAuth: true } }, { path: 'checkout', component: CheckoutPage, meta: { requiresAuth: true } }, { path: 'payment', component: PaymentPage, meta: { requiresAuth: true } },
      { path: 'orders', component: OrdersPage, meta: { requiresAuth: true } }, { path: 'profile', component: ProfilePage, meta: { requiresAuth: true } },
      { path: 'addresses', component: AddressPage, meta: { requiresAuth: true } }, { path: 'account', component: AccountSecurity, meta: { requiresAuth: true } },
      { path: 'seckill', component: SeckillPage },
    ] },
    { path: '/admin', component: AdminLayout, meta: { requiresAdmin: true }, children: [{ path: '', component: AdminDashboard }, { path: 'products', component: AdminProducts }, { path: 'catalog', component: AdminCatalog }, { path: 'orders', component: AdminOrders }, { path: 'users', component: AdminUsers }, { path: 'seckill', component: AdminSeckill }] },
    { path: '/auth', component: AuthPage },
  ],
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to) => {
  const token = localStorage.getItem('dingdong_token')
  const raw = localStorage.getItem('dingdong_user')
  let user = null
  try { user = raw ? JSON.parse(raw) : null } catch { localStorage.removeItem('dingdong_user') }
  if ((to.meta.requiresAuth || to.matched.some((item) => item.meta.requiresAdmin)) && (!token || !user)) return { path: '/auth', query: { redirect: to.fullPath } }
  if (to.matched.some((item) => item.meta.requiresAdmin) && user?.role !== 'ADMIN') return '/'
  return true
})

export default router
