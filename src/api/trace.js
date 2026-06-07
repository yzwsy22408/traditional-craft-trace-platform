import request from './request'

// ✅ 添加溯源步骤
export function addTraceStepApi(data) {
  return request({
    url: '/trace',
    method: 'post',
    data
  })
}

// ✅ 查询某个工艺品的溯源步骤
export function listTraceStepApi(craftItemId) {
  return request({
    url: `/trace/${craftItemId}`,
    method: 'get'
  })
}

// ✅ 更新某一步的状态
export function updateTraceStepStatusApi(id, status) {
  return request({
    url: `/trace/step/${id}/status`,
    method: 'put',
    params: { status }
  })
}

// ✅ 编辑某一步内容
export function updateTraceStepContentApi(id, data) {
  return request({
    url: `/trace/step/${id}`,
    method: 'put',
    data
  })
}

// ✅ 重建 hash 链
export function rebuildTraceHashApi(craftItemId) {
  return request({
    url: `/trace/${craftItemId}/rebuild-hash`,
    method: 'post'
  })
}

// ✅ 公开溯源查询
export function publicTraceApi(code) {
  return request({
    url: '/public/trace',
    method: 'get',
    params: { code }
  })
}
