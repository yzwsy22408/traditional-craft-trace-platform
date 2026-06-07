import request from './request'

export const bookingApi = {
  // 学生：报名
  book: (courseId) => request.post(`/booking/${Number(courseId)}`),

  // 学生：取消报名
  cancel: (courseId) => request.post(`/booking/${Number(courseId)}/cancel`),

  // 学生：我的报名
  my: () => request.get('/booking/my'),

  // ✅ 匠人/管理员：某课程报名名单
  listByCourse: (courseId) => request.get(`/booking/course/${Number(courseId)}`),

  // ✅ 匠人/管理员：把某条报名改为 ATTENDED（签到）
  attend: (bookingId) => request.post(`/booking/${Number(bookingId)}/attend`)
}
