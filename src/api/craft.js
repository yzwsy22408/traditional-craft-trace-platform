import request from './request'

// ✅ 获取所有工艺品（列表 / 下拉框）
export function listCraftApi() {
  return request({
    url: '/craft',
    method: 'get'
  })
}

// ✅ 新增工艺品
export function createCraftApi(data) {
  return request({
    url: '/craft',
    method: 'post',
    data
  })
}

// ✅ 更新工艺品
export function updateCraftApi(id, data) {
  return request({
    url: `/craft/${id}`,
    method: 'put',
    data
  })
}

// ✅ 删除工艺品
export function deleteCraftApi(id) {
  return request({
    url: `/craft/${id}`,
    method: 'delete'
  })
}
