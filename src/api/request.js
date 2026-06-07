import axios from 'axios'
import { ElMessage } from 'element-plus'
import { clearUser, isLogin } from '../utils/auth'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true
})

let authExpiredHandled = false

function redirectToLogin() {
  if (typeof window === 'undefined') return

  const currentPath = `${window.location.pathname}${window.location.search}`
  const isPublicPage =
    currentPath.startsWith('/login') ||
    currentPath.startsWith('/register') ||
    currentPath.startsWith('/trace') ||
    currentPath.startsWith('/image-search') ||
    currentPath.startsWith('/public/trace') ||
    currentPath.startsWith('/public/image-search')

  if (isPublicPage) return

  const target = `/login?redirect=${encodeURIComponent(currentPath)}`
  window.location.replace(target)
}

service.interceptors.request.use(
  (config) => config,
  (error) => {
    ElMessage.error('请求发送失败')
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response) => {
    const res = response.data

    if (Object.prototype.hasOwnProperty.call(res, 'success') && res.success === false) {
      ElMessage.error(res.message || '操作失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }

    if (typeof res.code === 'number' && res.code !== 200) {
      ElMessage.error(res.message || '操作执行失败')
      return Promise.reject(new Error(res.message || 'Error'))
    }

    authExpiredHandled = false
    return res
  },
  (error) => {
    const status = error?.response?.status
    const data = error?.response?.data
    let msg = data?.message || '服务连接异常'

    if (status === 401 && isLogin()) {
      if (!authExpiredHandled) {
        authExpiredHandled = true
        clearUser()
        ElMessage.error('登录状态已失效，请重新登录')
        redirectToLogin()
      }
      return Promise.reject(error)
    }

    if (status === 403) {
      ElMessage.error(data?.message || '没有权限执行当前操作')
      return Promise.reject(error)
    }

    if (status === 400 && typeof msg === 'string' && msg.includes('已存在')) {
      msg = '编号已存在，请更换后重试'
    }

    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default service
