import { useState, useEffect } from 'react';
import { orderService } from '../services/orderService';

const STATUS_LABELS = {
  PENDING: { label: 'Bekliyor', color: '#ff9800' },
  CONFIRMED: { label: 'Onaylandı', color: '#2196f3' },
  PAYMENT_COMPLETED: { label: 'Ödendi', color: '#4caf50' },
  SHIPPED: { label: 'Kargoda', color: '#9c27b0' },
  DELIVERED: { label: 'Teslim Edildi', color: '#4caf50' },
  CANCELLED: { label: 'İptal', color: '#f44336' },
};

export default function OrdersPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetch = async () => {
      try {
        const res = await orderService.getMyOrders();
        setOrders(res.data.data.content || []);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetch();
  }, []);

  if (loading) return <div style={styles.loading}>Yükleniyor...</div>;

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>Siparişlerim</h2>
      {orders.length === 0 ? (
        <div style={styles.empty}>Henüz sipariş vermediniz</div>
      ) : (
        orders.map(order => (
          <div key={order.id} style={styles.card}>
            <div style={styles.cardHeader}>
              <div>
                <span style={styles.orderId}>Sipariş #{order.id}</span>
                <span style={styles.date}>
                  {new Date(order.createdAt).toLocaleDateString('tr-TR')}
                </span>
              </div>
              <span style={{
                ...styles.status,
                background: STATUS_LABELS[order.status]?.color + '20',
                color: STATUS_LABELS[order.status]?.color,
              }}>
                {STATUS_LABELS[order.status]?.label || order.status}
              </span>
            </div>
            <div style={styles.items}>
              {order.items.map(item => (
                <div key={item.productId} style={styles.item}>
                  <span>{item.productName} x{item.quantity}</span>
                  <span>{item.totalPrice?.toLocaleString('tr-TR')} ₺</span>
                </div>
              ))}
            </div>
            <div style={styles.total}>
              Toplam: <strong>{order.totalAmount?.toLocaleString('tr-TR')} ₺</strong>
            </div>
          </div>
        ))
      )}
    </div>
  );
}

const styles = {
  container: { padding: '1.5rem', maxWidth: '800px', margin: '0 auto' },
  title: { fontSize: '1.5rem', color: '#333', marginBottom: '1.5rem' },
  loading: { textAlign: 'center', padding: '3rem', color: '#999' },
  empty: { textAlign: 'center', padding: '3rem', color: '#999' },
  card: {
    background: 'white', borderRadius: '12px', padding: '1.5rem',
    marginBottom: '1rem', boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
  },
  cardHeader: {
    display: 'flex', justifyContent: 'space-between',
    alignItems: 'center', marginBottom: '1rem',
  },
  orderId: { fontWeight: '700', color: '#333', marginRight: '1rem' },
  date: { color: '#999', fontSize: '0.85rem' },
  status: { padding: '0.3rem 0.8rem', borderRadius: '20px', fontSize: '0.85rem', fontWeight: '600' },
  items: { borderTop: '1px solid #f5f5f5', paddingTop: '1rem', marginBottom: '1rem' },
  item: {
    display: 'flex', justifyContent: 'space-between',
    padding: '0.3rem 0', color: '#555', fontSize: '0.9rem',
  },
  total: { color: '#333', textAlign: 'right', fontSize: '1rem' },
};