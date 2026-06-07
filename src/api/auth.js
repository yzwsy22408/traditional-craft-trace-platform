import request from './request'

/** 登录 */
export function loginApi(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

/** 当前登录用户 */
export function meApi() {
  return request({
    url: '/auth/me',
    method: 'get'
  })
}

/** 退出登录 */
export function logoutApi() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

/** 注册 */
export function registerApi(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

/**
 * ✅ 用户分页列表 + 搜索
 * GET /api/users?page=1&size=10&keyword=
 */
export function pageUsers(params) {
  return request({
    url: '/users',
    method: 'get',
    params
  })
}

/** 新增用户（管理员） */
export function createUser(data) {
  return request({
    url: '/users',
    method: 'post',
    data
  })
}

/** 更新用户（管理员） */
export function updateUser(id, data) {
  return request({
    url: `/users/${id}`,
    method: 'put',
    data
  })
}

/** 删除用户 */
export function deleteUser(userId) {
  return request({
    url: `/users/${userId}`,
    method: 'delete'
  })
}

/** 分配角色 */
export function assignRoleToUser(userId, roleName) {
  return request({
    url: `/users/assign-role/${userId}`,
    method: 'post',
    params: { roleName }
  })
}

/** ✅ 学员分页列表 + 搜索 */
export function pageStudents(params) {
  return request({
    url: '/users/students',
    method: 'get',
    params
  })
}

/** ✅ 新增学员（管理员） */
export function createStudent(data) {
  return request({
    url: '/users/students',
    method: 'post',
    data
  })
}
