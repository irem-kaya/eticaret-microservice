import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { orderService } from '../services/orderService';

export default function CheckoutPage() {
  const { cart, clearCart } = useCart();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    address: '',
    cardHolder: '',
    cardNumber: '',
    expireMonth: '',
    expireYear: '',
    cvc: '',
  });

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await orderService.create({
        items: cart.items.map(item => ({
          productId: item.productId,
          productName: item.productName,
          price: item.price,
          quantity: item.quantity,
        })),
        totalAmount: cart.totalPrice,
        shippingAddress: form.address,
      });
      await clearCart();
      navigate('/order-success');
    } catch (err) {
      alert('Sipariş oluşturulamadı: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={styles.container}>
      <h2 style={styles.title}>Ödeme</h2>
      <div style={styles.layout}>
        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.section}>
            <h3 style={styles.sectionTitle}>Teslimat Adresi</h3>
            <textarea
              style={styles.textarea}
              name="address"
              placeholder="Adres giriniz..."
              value={form.address}
              onChange={handleChange}
              required
              rows={3}
            />
          </div>

          <div style={styles.section}>
            <h3 style={styles.sectionTitle}>Kart Bilgileri</h3>
            <input style={styles.input} name="cardHolder"
              placeholder="Kart Sahibi" value={form.cardHolder}
              onChange={handleChange} required />
            <input style={styles.input} name="cardNumber"
              placeholder="5528 7900 0000 0008" value={form.cardNumber}
              onChange={handleChange} required maxLength={16} />
            <div style={styles.row}>
              <input style={{ ...styles.input, flex: 1 }} name="expireMonth"
                placeholder="Ay (12)" value={form.expireMonth}
                onChange={handleChange} required />
              <input style={{ ...styles.input, flex: 1 }} name="expireYear"
                placeholder="Yıl (2030)" value={form.expireYear}
                onChange={handleChange} required />
              <input style={{ ...styles.input, flex: 1 }} name="cvc"
                placeholder="CVC" value={form.cvc}
                onChange={handleChange} required maxLength={3} />
            </div>
            <div style={styles.testCard}>
              Test kartı: 5528790000000008 / 12 / 2030 / 123
            </div>
          </div>

          <button style={styles.submitBtn} type="submit" disabled={loading}>
            {loading ? 'İşleniyor...' : `${cart.totalPrice?.toLocaleString('tr-TR')} ₺ Öde`}
          </button>
        </form>

        <div style={styles.orderSummary}>
          <h3 style={styles.sectionTitle}>Sipariş Özeti</h3>
          {cart.items.map(item => (
            <div key={item.productId} style={styles.summaryItem}>
              <span>{item.productName} x{item.quantity}</span>
              <span>{item.totalPrice?.toLocaleString('tr-TR')} ₺</span>
            </div>
          ))}
          <div style={styles.summaryTotal}>
            <span>Toplam</span>
            <span style={{ color: '#e53935', fontWeight: '700' }}>
              {cart.totalPrice?.toLocaleString('tr-TR')} ₺
            </span>
          </div>
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: { padding: '1.5rem', maxWidth: '900px', margin: '0 auto' },
  title: { fontSize: '1.5rem', color: '#333', marginBottom: '1.5rem' },
  layout: { display: 'flex', gap: '2rem', flexWrap: 'wrap' },
  form: { flex: 1, minWidth: '300px' },
  section: {
    background: 'white', borderRadius: '12px', padding: '1.5rem',
    marginBottom: '1rem', boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
  },
  sectionTitle: { marginBottom: '1rem', color: '#333', fontSize: '1rem' },
  input: {
    width: '100%', padding: '0.75rem', border: '1px solid #ddd',
    borderRadius: '8px', fontSize: '0.95rem', marginBottom: '0.8rem',
    boxSizing: 'border-box',
  },
  textarea: {
    width: '100%', padding: '0.75rem', border: '1px solid #ddd',
    borderRadius: '8px', fontSize: '0.95rem', boxSizing: 'border-box',
    resize: 'vertical',
  },
  row: { display: 'flex', gap: '0.5rem' },
  testCard: {
    background: '#fff8e1', padding: '0.5rem 0.75rem',
    borderRadius: '6px', fontSize: '0.8rem', color: '#f57f17',
  },
  submitBtn: {
    width: '100%', padding: '1rem', background: '#e53935',
    color: 'white', border: 'none', borderRadius: '8px',
    fontSize: '1.1rem', cursor: 'pointer', fontWeight: '700',
  },
  orderSummary: {
    width: '260px', background: 'white', borderRadius: '12px',
    padding: '1.5rem', boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
    height: 'fit-content',
  },
  summaryItem: {
    display: 'flex', justifyContent: 'space-between',
    padding: '0.5rem 0', borderBottom: '1px solid #f5f5f5',
    fontSize: '0.9rem', color: '#555',
  },
  summaryTotal: {
    display: 'flex', justifyContent: 'space-between',
    padding: '1rem 0 0', fontWeight: '600',
  },
};