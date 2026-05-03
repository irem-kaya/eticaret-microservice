import api from './api';

export const productService = {
  getAll: (params) => api.get('/api/products', { params }),
  getById: (id) => api.get(`/api/products/${id}`),
  create: (data) => api.post('/api/products', data),
  update: (id, data) => api.put(`/api/products/${id}`, data),
  delete: (id) => api.delete(`/api/products/${id}`),
};

export const categoryService = {
  getAll: () => api.get('/api/categories'),
  create: (data) => api.post('/api/categories', data),
};