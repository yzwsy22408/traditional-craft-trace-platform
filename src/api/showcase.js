import request from './request'

export const showcaseApi = {
  my: () => request.get('/student-work/my'),
  listByCourse: (courseId) => request.get(`/student-work/course/${Number(courseId)}`),
  save: (data) => request.post('/student-work', data),
  update: (id, data) => request.put(`/student-work/${Number(id)}`, data),
  remove: (id) => request.delete(`/student-work/${Number(id)}`)
}
