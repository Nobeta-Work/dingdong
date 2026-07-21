import { ref } from 'vue'
import { cartApi } from '@/api/services'

const cartCount = ref(0)
export const useCartSummary = () => {
  const refreshCartCount = async () => {
    if (!localStorage.getItem('dingdong_token')) { cartCount.value = 0; return }
    try { cartCount.value = (await cartApi.list()).reduce((sum, item) => sum + item.quantity, 0) }
    catch { cartCount.value = 0 }
  }
  return { cartCount, refreshCartCount }
}
