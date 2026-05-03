import { useNavigate } from 'react-router-dom';

export default function OrderSuccessPage() {
  const navigate = useNavigate();

  return (
    <div style={styles.container}>
      <div style={styles.card}>
        <div style={styles.icon}>✅</div>
        <h2 style={styles.title}>Siparişiniz Alındı!</h2>
        <p style={styles.desc}>
          Siparişiniz başarıyla oluşturuldu. En kısa sürede hazırlanacak.
        </p>
        <div style={styles.buttons}>
          <button style={styles.ordersBtn} onClick={() => navigate('/orders')}>
            Siparişlerim
          </button>
          <button style={styles.shopBtn} onClick={() => navigate('/')}>
            Alışverişe Devam Et
          </button>
        </div>
      </div>
    </div>
  );
}

const styles = {
  container: {
    minHeight: '80vh', display: 'flex',
    alignItems: 'center', justifyContent: 'center',
  },
  card: {
    background: 'white', borderRadius: '16px',
    padding: '3rem', textAlign: 'center',
    boxShadow: '0 4px 20px rgba(0,0,0,0.1)', maxWidth: '400px',
  },
  icon: { fontSize: '4rem', marginBottom: '1rem' },
  title: { color: '#333', marginBottom: '1rem' },
  desc: { color: '#666', marginBottom: '2rem', lineHeight: 1.6 },
  buttons: { display: 'flex', gap: '1rem', justifyContent: 'center' },
  ordersBtn: {
    padding: '0.7rem 1.5rem', background: 'white',
    color: '#e53935', border: '2px solid #e53935',
    borderRadius: '8px', cursor: 'pointer', fontWeight: '600',
  },
  shopBtn: {
    padding: '0.7rem 1.5rem', background: '#e53935',
    color: 'white', border: 'none',
    borderRadius: '8px', cursor: 'pointer', fontWeight: '600',
  },
};