import request from './request'

/**
 * 获取用户列表
 * GET /api/users
 */
export function listUsers() {
  return request({
    url: '/users',
    method: 'get'
  })
}

/**
 * 删除用户
 * DELETE /api/users/{userId}
 */
export function deleteUser(userId) {
  return request({
    url: `/users/${userId}`,
    method: 'delete'
  })
}

/**
 * 为用户分配角色
 * POST /api/users/assign-role/{userId}
 */
export function assignRoleToUser(userId, roleName) {
  return request({
    url: `/users/assign-role/${userId}`,
    method: 'post',
    params: { roleName }
  })
}
