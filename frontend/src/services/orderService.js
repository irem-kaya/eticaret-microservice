import api from './api';

export const orderService = {
  create: (data) => api.post('/api/orders', data),
  getMyOrders: () => api.get('/api/orders'),
  getById: (id) => api.get(`/api/orders/${id}`),
};