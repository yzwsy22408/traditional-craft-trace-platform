import request from './request'

export const teacherApi = {
  list: () => request.get('/teachers'),
  create: (data) => request.post('/teachers', data),
  update: (id, data) => request.put(`/teachers/${id}`, data),
  remove: (id) => request.delete(`/teachers/${id}`)
}
