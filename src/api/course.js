import request from './request'

export const courseApi = {
  listAll: () => request.get('/course'),
  create: (data) => request.post('/course', data),
  update: (id, data) => request.put(`/course/${id}`, data),
  publish: (id) => request.post(`/course/${id}/publish`),
  close: (id) => request.post(`/course/${id}/close`)
}

export const publicCourseApi = {
  list: () => request.get('/public/courses'),
  detail: (id) => request.get(`/public/courses/${id}`),
  heatList: () => request.get('/public/course-heat'),
  publishedList: () => request.get('/public/courses')
}
