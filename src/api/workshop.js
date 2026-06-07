import request from './request'

export const workshopApi = {
  // 获取工坊列表
  list: () => request.get('/workshop'),

  // ✅ 获取未绑定工坊（管理员）
  unbound: () => request.get('/workshop/unbound'),

  // 新增工坊
  create: (data) => request.post('/workshop', data),

  // 编辑工坊
  update: (id, data) => request.put(`/workshop/${id}`, data),

  // 删除工坊
  remove: (id) => request.delete(`/workshop/${id}`),

  // ✅ 管理员：更换/绑定匠人
  assignOwner: (id, ownerUserId) => {
    return request.post(`/workshop/${id}/assign-owner`, null, {
      params: { ownerUserId }
    })
  }
}
