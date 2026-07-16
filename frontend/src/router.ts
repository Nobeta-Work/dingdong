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

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: StoreLayout, children: [
      { path: '', component: HomePage }, { path: 'products', component: ProductsPage }, { path: 'product/:id', component: ProductPage },
      { path: 'cart', component: CartPage }, { path: 'checkout', component: CheckoutPage }, { path: 'payment', component: PaymentPage },
      { path: 'orders', component: OrdersPage }, { path: 'profile', component: ProfilePage },
    ] },
    { path: '/admin', component: AdminLayout, children: [{ path: '', component: AdminDashboard }, { path: 'products', component: AdminProducts }, { path: 'orders', component: AdminOrders }] },
    { path: '/auth', component: AuthPage },
  ],
  scrollBehavior: () => ({ top: 0 }),
})
