const USER_STORAGE_KEY = 'user'
const AUTH_CHANGE_EVENT = 'crafttrace-auth-change'

function emitAuthChange() {
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new Event(AUTH_CHANGE_EVENT))
  }
}

function normalizeUser(user) {
  if (!user || typeof user !== 'object') return null

  return {
    id: user.id ?? user.uid ?? '',
    username: user.username ?? '',
    role: user.role ?? '',
    name: user.name ?? '',
    avatar: user.avatar ?? ''
  }
}

export function getUser() {
  const userStr = localStorage.getItem(USER_STORAGE_KEY)
  if (!userStr) return null

  try {
    const parsed = JSON.parse(userStr)
    return normalizeUser(parsed)
  } catch (error) {
    localStorage.removeItem(USER_STORAGE_KEY)
    return null
  }
}

export function setUser(user) {
  const normalizedUser = normalizeUser(user)
  if (!normalizedUser) {
    clearUser()
    return null
  }

  localStorage.setItem(USER_STORAGE_KEY, JSON.stringify(normalizedUser))
  emitAuthChange()
  return normalizedUser
}

export function clearUser() {
  localStorage.removeItem(USER_STORAGE_KEY)
  emitAuthChange()
}

export function isLogin() {
  return !!getUser()
}

export function getUserRole() {
  return String(getUser()?.role || '').toLowerCase()
}

export function hasRole(...roles) {
  const currentRole = getUserRole()
  return roles
    .map((role) => String(role || '').toLowerCase())
    .includes(currentRole)
}

export function onAuthChange(callback) {
  if (typeof window === 'undefined' || typeof callback !== 'function') {
    return () => {}
  }

  window.addEventListener(AUTH_CHANGE_EVENT, callback)
  window.addEventListener('storage', callback)

  return () => {
    window.removeEventListener(AUTH_CHANGE_EVENT, callback)
    window.removeEventListener('storage', callback)
  }
}
