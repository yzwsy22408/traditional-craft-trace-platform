import request from './request'

export const orderApi = {
  // 创建订单
  create: (courseId) => request.post(`/order/${Number(courseId)}`),

  // 支付（模拟）
  pay: (orderId) => request.post(`/order/${Number(orderId)}/pay`),

  // 取消订单（UNPAID）
  cancel: (orderId) => request.post(`/order/${Number(orderId)}/cancel`),

  // ✅ 退款（PAID -> REFUNDED）
  refund: (orderId) => request.post(`/order/${Number(orderId)}/refund`),

  // 我的订单
  my: () => request.get('/order/my')
}
