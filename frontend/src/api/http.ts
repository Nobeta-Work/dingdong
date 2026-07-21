import axios from 'axios'
import { ElMessage } from 'element-plus'

/** 后端启动后，买家端与管理端请求统一从此处进入。 */
const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10_000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('dingdong_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data as { code?: string | number; message?: string; data?: unknown }
    if (body && 'code' in body && body.code !== 'OK' && body.code !== 200) {
      return Promise.reject(new Error(body.message || '请求未成功'))
    }
    return response
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络连接失败，请稍后重试'
    if (error.response?.status === 401) {
      localStorage.removeItem('dingdong_token')
      localStorage.removeItem('dingdong_user')
    }
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export default http
