import request from './request'

export const attendanceApi = {
  // 学员签到
  sign(courseId) {
    return request.post(`/attendance/sign/${courseId}`)
  },

  // 我的签到列表
  my() {
    return request.get('/attendance/my')
  },

  // 某课程签到列表（匠人/老师）
  listByCourse(courseId) {
    return request.get(`/attendance/course/${courseId}`)
  },

  // ✅ B3：统计（报名/支付/签到/实到率）
  summaryByCourse(courseId) {
    return request.get(`/attendance/course/${courseId}/summary`)
  }
}
