import request from './request'

export const enrollApi = {
  // ✅ 修正：获取某课程的所有报名名单
  listByCourse: (courseId) => request.get(`/enroll/course/${courseId}`),

  // ✅ 新增：批量报名接口（对应后端的 /api/enroll/batch）
  batch: (data) => request.post('/enroll/batch', data),

  // ✅ 修正：签到接口（对应后端的 /api/enroll/{id}/sign）
  sign: (id) => request.post(`/enroll/${id}/sign`),

  // 如果后续需要支付或取消签到，可以在这里按需扩展
  pay: (enrollId) => request.post(`/enroll/${enrollId}/pay`),
  unsign: (enrollId) => request.post(`/enroll/${enrollId}/unsign`)
}