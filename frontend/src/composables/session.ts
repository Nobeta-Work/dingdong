import { computed, ref } from 'vue'
import type { User } from '@/api/services'

const stored = localStorage.getItem('dingdong_user')
const user = ref<User | null>(stored ? JSON.parse(stored) : null)
export const useSession = () => {
  const setUser = (value: User) => { user.value = value; localStorage.setItem('dingdong_user', JSON.stringify(value)) }
  const clear = () => { user.value = null; localStorage.removeItem('dingdong_user'); localStorage.removeItem('dingdong_token') }
  return { user, loggedIn: computed(() => Boolean(user.value)), setUser, clear }
}
