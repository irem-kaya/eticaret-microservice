import api from './api';

export const cartService = {
  getCart: () => api.get('/api/cart'),
  addItem: (item) => api.post('/api/cart/items', item),
  removeItem: (productId) => api.delete(`/api/cart/items/${productId}`),
  updateQuantity: (productId, quantity) =>
    api.patch(`/api/cart/items/${productId}`, { quantity }),
  checkout: () => api.post('/api/cart/checkout'),
  clearCart: () => api.delete('/api/cart'),
};