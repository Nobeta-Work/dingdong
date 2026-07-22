import { createRouter, createWebHistory } from 'vue-router'
import StoreLayout from './layouts/StoreLayout.vue'
import AdminLayout from './layouts/AdminLayout.vue'
import HomePage from './pages/HomePage.vue'
import ProductsPage from './pages/ProductsPage.vue'
import ProductPage from './pages/ProductPage.vue'
import CartPage from './pages/CartPage.vue'
import CheckoutPage from './pages/CheckoutPage.vue'
import PaymentPage from './pages/PaymentPage.vue'
import OrdersPage from './pages/OrdersPage.vue'
import ProfilePage from './pages/ProfilePage.vue'
import AdminDashboard from './pages/AdminDashboard.vue'
import AdminProducts from './pages/AdminProducts.vue'
import AdminOrders from './pages/AdminOrders.vue'
import AuthPage from './pages/AuthPage.vue'
import AddressPage from './pages/AddressPage.vue'
import AccountSecurity from './pages/AccountSecurity.vue'
import AdminCatalog from './pages/AdminCatalog.vue'
import AdminUsers from './pages/AdminUsers.vue'
import AdminSeckill from './pages/AdminSeckill.vue'
import SeckillPage from './pages/SeckillPage.vue'

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
