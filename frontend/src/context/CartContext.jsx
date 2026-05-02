import { createContext, useContext, useState, useEffect } from 'react';
import { cartService } from '../services/cartService';
import { authService } from '../services/authService';

const CartContext = createContext();

export const CartProvider = ({ children }) => {
  const [cart, setCart] = useState({ items: [], totalPrice: 0, totalItems: 0 });
  const [loading, setLoading] = useState(false);

  const fetchCart = async () => {
    if (!authService.isLoggedIn()) return;
    try {
      const res = await cartService.getCart();
      setCart(res.data.data);
    } catch (err) {
      console.error('Sepet yuklenemedi:', err);
    }
  };

  useEffect(() => {
    fetchCart();
  }, []);

  const addToCart = async (item) => {
    setLoading(true);
    try {
      const res = await cartService.addItem(item);
      setCart(res.data.data);
    } finally {
      setLoading(false);
    }
  };

  const removeFromCart = async (productId) => {
    setLoading(true);
    try {
      const res = await cartService.removeItem(productId);
      setCart(res.data.data);
    } finally {
      setLoading(false);
    }
  };

  const updateQuantity = async (productId, quantity) => {
    setLoading(true);
    try {
      const res = await cartService.updateQuantity(productId, quantity);
      setCart(res.data.data);
    } finally {
      setLoading(false);
    }
  };

  const clearCart = async () => {
    await cartService.clearCart();
    setCart({ items: [], totalPrice: 0, totalItems: 0 });
  };

  return (
    <CartContext.Provider value={{
      cart, loading, fetchCart,
      addToCart, removeFromCart, updateQuantity, clearCart
    }}>
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => useContext(CartContext);
