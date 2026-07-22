import axios from 'axios'
import { ElMessage } from 'element-plus'

type ApiEnvelope = { code?: string | number; message?: string; data?: unknown; traceId?: string }

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly code: string,
    public readonly status?: number,
    public readonly traceId?: string,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

const statusMessage = (status?: number) => {
  if (status === 400) return '请求内容有误，请检查后重试'
  if (status === 401) return '登录状态已失效，请重新登录'
  if (status === 403) return '当前账号没有执行此操作的权限'
  if (status === 404) return '请求的数据不存在或已被删除'
  if (status === 409) return '当前数据状态不允许执行此操作'
  if (status === 413) return '上传文件过大'
  if (status === 415) return '不支持该文件格式'
  if (status === 502 || status === 503) return '服务暂时不可用，请稍后重试'
  if (status && status >= 500) return '系统繁忙，请稍后重试'
  return '网络连接失败，请稍后重试'
}

const normalizeError = (error: unknown) => {
  if (error instanceof ApiError) return error
  if (axios.isAxiosError(error)) {
    const body = error.response?.data as ApiEnvelope | undefined
    const status = error.response?.status
    const code = String(body?.code || (status ? `HTTP_${status}` : 'NETWORK_ERROR'))
    return new ApiError(body?.message || statusMessage(status), code, status, body?.traceId)
  }
  return new ApiError(error instanceof Error ? error.message : statusMessage(), 'CLIENT_ERROR')
}

const rejectWithNotice = (error: unknown) => {
  const apiError = normalizeError(error)
  ElMessage.error(`${apiError.message}（${apiError.code}）`)
  return Promise.reject(apiError)
}

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
    const body = response.data as ApiEnvelope
    if (body && 'code' in body && body.code !== 'OK' && body.code !== 200) {
      return rejectWithNotice(new ApiError(body.message || '请求未成功', String(body.code), response.status, body.traceId))
    }
    return response
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('dingdong_token')
      localStorage.removeItem('dingdong_user')
    }
    return rejectWithNotice(error)
  },
)

export default http
