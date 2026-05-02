import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { productService } from '../services/productService';
import { useCart } from '../context/CartContext';

export default function FlashSalePage() {
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [addingId, setAddingId] = useState(null);
  const [toast, setToast] = useState(null);
  const [timeLeft, setTimeLeft] = useState({ h: 5, m: 59, s: 59 });

  // Geri sayım
  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft(t => {
        if (t.s > 0) return { ...t, s: t.s - 1 };
        if (t.m > 0) return { ...t, m: t.m - 1, s: 59 };
        if (t.h > 0) return { h: t.h - 1, m: 59, s: 59 };
        return t;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    productService.getAll({ sortBy: 'price', size: 16, page: 0 })
      .then(res => setProducts(res.data.data.content || []))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const showToast = (msg) => { setToast(msg); setTimeout(() => setToast(null), 2500); };

  const handleAdd = async (e, p) => {
    e.stopPropagation();
    setAddingId(p.id);
    try {
      await addToCart({ productId: p.id, productName: p.name, price: p.price, quantity: 1, imageUrl: p.imageUrl });
      showToast(`${p.name} sepete eklendi ✓`);
    } catch { showToast('Giriş yapmanız gerekiyor'); }
    finally { setAddingId(null); }
  };

  const pad = n => String(n).padStart(2, '0');

  return (
    <div style={s.page}>
      {/* Hero */}
      <div style={s.hero}>
        <div style={s.heroContent}>
          <div style={s.heroBadge}>⚡ FLASH SALE</div>
          <h1 style={s.heroTitle}>Fırsatları Kaçırma!</h1>
          <p style={s.heroSub}>Sınırlı süre, sınırlı stok kampanyaları</p>
          <div style={s.countdown}>
            <div style={s.countItem}>
              <span style={s.countNum}>{pad(timeLeft.h)}</span>
              <span style={s.countLabel}>Saat</span>
            </div>
            <span style={s.countSep}>:</span>
            <div style={s.countItem}>
              <span style={s.countNum}>{pad(timeLeft.m)}</span>
              <span style={s.countLabel}>Dakika</span>
            </div>
            <span style={s.countSep}>:</span>
            <div style={s.countItem}>
              <span style={s.countNum}>{pad(timeLeft.s)}</span>
              <span style={s.countLabel}>Saniye</span>
            </div>
          </div>
        </div>
      </div>

      {/* Products */}
      <div style={s.container}>
        <div style={s.sectionHeader}>
          <h2 style={s.sectionTitle}>⚡ Flash Sale Ürünleri</h2>
          <span style={s.sectionSub}>{products.length} ürün</span>
        </div>

        {loading ? (
          <div style={s.grid}>{[...Array(8)].map((_, i) => <div key={i} style={s.skeleton} />)}</div>
        ) : (
          <div style={s.grid}>
            {products.map((p, idx) => {
              const discount = [20, 25, 30, 35, 40, 45, 50][idx % 7];
              const originalPrice = (p.price / (1 - discount / 100)).toFixed(0);
              return (
                <div key={p.id} style={s.card} onClick={() => navigate(`/products/${p.id}`)}>
                  <div style={s.cardImg}>
                    {p.imageUrl
                      ? <img src={p.imageUrl} alt={p.name} style={s.img} />
                      : <div style={s.imgPh}>📦</div>
                    }
                    <div style={s.discountBadge}>%{discount}</div>
                    <div style={s.flashBadge}>⚡ FLASH</div>
                    {p.stock < 10 && <div style={s.stockBadge}>Son {p.stock}!</div>}
                  </div>
                  <div style={s.cardBody}>
                    <div style={s.cardName}>{p.name}</div>
                    <div style={s.priceRow}>
                      <div>
                        <div style={s.oldPrice}>{Number(originalPrice).toLocaleString('tr-TR')} TL</div>
                        <div style={s.newPrice}>{p.price?.toLocaleString('tr-TR')} TL</div>
                      </div>
                      <button style={s.addBtn} onClick={e => handleAdd(e, p)} disabled={addingId === p.id}>
                        {addingId === p.id ? '...' : '+'}
                      </button>
                    </div>
                    {/* Stok bar */}
                    <div style={s.stockBar}>
                      <div style={{ ...s.stockFill, width: `${Math.min(100, (p.stock / 100) * 100)}%` }} />
                    </div>
                    <div style={s.stockText}>Stok: {p.stock} adet kaldı</div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {toast && <div style={s.toast}>{toast}</div>}
    </div>
  );
}

const s = {
  page: { background: '#f2f3f5', minHeight: '100vh' },
  hero: {
    background: 'linear-gradient(135deg, #b71c1c 0%, #c62828 40%, #e53935 100%)',
    padding: '60px 24px',
    textAlign: 'center',
  },
  heroContent: { maxWidth: '600px', margin: '0 auto' },
  heroBadge: { display: 'inline-block', background: 'rgba(255,255,255,0.2)', color: '#fff', padding: '6px 20px', borderRadius: '20px', fontSize: '13px', fontWeight: '700', letterSpacing: '2px', marginBottom: '16px' },
  heroTitle: { fontSize: '48px', fontWeight: '900', color: '#fff', margin: '0 0 8px', fontFamily: 'Georgia,serif' },
  heroSub: { color: 'rgba(255,255,255,0.8)', fontSize: '16px', margin: '0 0 32px' },
  countdown: { display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '12px' },
  countItem: { background: '#fff', borderRadius: '12px', padding: '16px 20px', textAlign: 'center', minWidth: '80px' },
  countNum: { display: 'block', fontSize: '36px', fontWeight: '900', color: '#c62828', lineHeight: 1 },
  countLabel: { fontSize: '11px', color: '#999', fontWeight: '600', letterSpacing: '1px', textTransform: 'uppercase' },
  countSep: { fontSize: '32px', fontWeight: '900', color: '#fff', marginBottom: '20px' },
  container: { maxWidth: '1280px', margin: '0 auto', padding: '32px 24px' },
  sectionHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' },
  sectionTitle: { fontSize: '22px', fontWeight: '800', color: '#111', margin: 0 },
  sectionSub: { color: '#999', fontSize: '14px' },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '16px' },
  card: { background: '#fff', borderRadius: '12px', overflow: 'hidden', border: '1px solid #e8e8e8', cursor: 'pointer', transition: 'all 0.2s' },
  cardImg: { position: 'relative', paddingTop: '100%', background: '#f8f8f8', overflow: 'hidden' },
  img: { position: 'absolute', inset: 0, width: '100%', height: '100%', objectFit: 'cover' },
  imgPh: { position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '48px' },
  discountBadge: { position: 'absolute', top: '8px', left: '8px', background: '#c62828', color: '#fff', fontSize: '11px', fontWeight: '800', padding: '4px 8px', borderRadius: '6px', zIndex: 2 },
  flashBadge: { position: 'absolute', top: '8px', right: '8px', background: '#ff6f00', color: '#fff', fontSize: '10px', fontWeight: '700', padding: '3px 8px', borderRadius: '6px', zIndex: 2 },
  stockBadge: { position: 'absolute', bottom: '8px', left: '8px', background: 'rgba(0,0,0,0.7)', color: '#fff', fontSize: '10px', fontWeight: '700', padding: '3px 8px', borderRadius: '6px', zIndex: 2 },
  cardBody: { padding: '12px' },
  cardName: { fontSize: '13px', fontWeight: '500', color: '#222', lineHeight: 1.4, marginBottom: '8px', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' },
  priceRow: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' },
  oldPrice: { fontSize: '11px', color: '#bbb', textDecoration: 'line-through' },
  newPrice: { fontSize: '18px', fontWeight: '800', color: '#c62828' },
  addBtn: { width: '32px', height: '32px', borderRadius: '50%', border: '2px solid #c62828', background: '#fff', color: '#c62828', fontWeight: '700', fontSize: '18px', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 },
  stockBar: { height: '4px', background: '#f0f0f0', borderRadius: '2px', overflow: 'hidden', marginBottom: '4px' },
  stockFill: { height: '100%', background: 'linear-gradient(90deg, #c62828, #ff6f00)', borderRadius: '2px', transition: 'width 0.3s' },
  stockText: { fontSize: '10px', color: '#999' },
  skeleton: { background: 'linear-gradient(90deg,#f0f0f0 25%,#e8e8e8 50%,#f0f0f0 75%)', borderRadius: '12px', height: '280px' },
  toast: { position: 'fixed', bottom: '100px', left: '50%', transform: 'translateX(-50%)', background: '#111', color: '#fff', padding: '12px 24px', borderRadius: '10px', fontSize: '14px', fontWeight: '500', zIndex: 2000 },
};