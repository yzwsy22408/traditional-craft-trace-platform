import request from './request'

export const materialApi = {
  list: () => request.get('/material'),
  create: (data) => request.post('/material', data),
  update: (id, data) => request.put(`/material/${Number(id)}`, data),
  remove: (id) => request.delete(`/material/${Number(id)}`)
}
