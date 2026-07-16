import axios from 'axios'

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

export default http
