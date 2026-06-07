import request from './request'

export const reviewApi = {
  // 管理员/匠人：获取全量列表
  adminList() {
    return request.get('/review/admin/list')
  },

  // 匠人：回复评价
  reply(id, replyText) {
    return request.post(`/review/reply/${id}`, { reply: replyText })
  },

  // 学员：提交/更新评价
  upsert(courseId, data) {
    return request.post(`/review/course/${Number(courseId)}`, data)
  },

  // 匠人/管理员：该课程全部评价
  listByCourse(courseId) {
    return request.get(`/review/course/${Number(courseId)}`)
  }
}