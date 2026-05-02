import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { productService } from '../services/productService';
import { useCart } from '../context/CartContext';
import api from '../services/api';

export default function BestSellersPage() {
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [addingId, setAddingId] = useState(null);
  const [toast, setToast] = useState(null);

  useEffect(() => {
    const load = async () => {
      try {
        // Order-service'den en çok satılan ID'leri al
        const bestRes = await api.get('/api/orders/best-sellers?limit=12');
        const bestSellers = bestRes.data.data || [];

        if (bestSellers.length > 0) {
          // Her product ID için detay çek
          const productPromises = bestSellers.map(bs =>
            productService.getById(bs.productId).then(r => ({
              ...r.data.data,
              totalSold: bs.totalSold
            })).catch(() => null)
          );
          const results = (await Promise.all(productPromises)).filter(Boolean);
          setProducts(results);
        } else {
          // Fallback: tüm ürünleri getir
          const res = await productService.getAll({ size: 12, page: 0 });
          setProducts(res.data.data.content || []);
        }
      } catch {
        // Fallback
        const res = await productService.getAll({ size: 12, page: 0 });
        setProducts(res.data.data.content || []);
      } finally {
        setLoading(false);
      }
    };
    load();
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

  return (
    <div style={s.page}>
      {/* Hero */}
      <div style={s.hero}>
        <div style={s.heroBadge}>⭐ ÇOK SATANLAR</div>
        <h1 style={s.heroTitle}>En Çok Tercih Edilenler</h1>
        <p style={s.heroSub}>Müşterilerimizin en çok satın aldığı ürünler</p>
      </div>

      <div style={s.container}>
        <div style={s.sectionHeader}>
          <h2 style={s.sectionTitle}>⭐ Popüler Ürünler</h2>
          <span style={s.sectionSub}>{products.length} ürün</span>
        </div>

        {loading ? (
          <div style={s.grid}>{[...Array(8)].map((_, i) => <div key={i} style={s.skeleton} />)}</div>
        ) : (
          <div style={s.grid}>
            {products.map((p, idx) => (
              <div key={p.id} style={s.card} onClick={() => navigate(`/products/${p.id}`)}>
                <div style={s.rankBadge}>#{idx + 1}</div>
                <div style={s.cardImg}>
                  {p.imageUrl
                    ? <img src={p.imageUrl} alt={p.name} style={s.img} />
                    : <div style={s.imgPh}>📦</div>
                  }
                  {idx < 3 && (
                    <div style={{ ...s.medalBadge, background: ['#FFD700', '#C0C0C0', '#CD7F32'][idx] }}>
                      {['🥇', '🥈', '🥉'][idx]}
                    </div>
                  )}
                  <div style={s.freeShip}>ÜCRETSİZ KARGO</div>
                </div>
                <div style={s.cardBody}>
                  <div style={s.cardName}>{p.name}</div>
                  {p.totalSold && (
                    <div style={s.soldCount}>🛒 {p.totalSold} kez satıldı</div>
                  )}
                  <div style={s.priceRow}>
                    <div>
                      <div style={s.oldPrice}>{(p.price * 1.2).toLocaleString('tr-TR', { maximumFractionDigits: 0 })} TL</div>
                      <div style={s.newPrice}>{p.price?.toLocaleString('tr-TR')} TL</div>
                    </div>
                    <button style={s.addBtn} onClick={e => handleAdd(e, p)} disabled={addingId === p.id}>
                      {addingId === p.id ? '...' : '+'}
                    </button>
                  </div>
                  <div style={s.stars}>★★★★☆ <span style={{ color: '#999', fontSize: '11px' }}>(128)</span></div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {toast && <div style={s.toast}>{toast}</div>}
    </div>
  );
}

const s = {
  page: { background: '#f2f3f5', minHeight: '100vh' },
  hero: { background: 'linear-gradient(135deg, #1a237e 0%, #283593 100%)', padding: '60px 24px', textAlign: 'center' },
  heroBadge: { display: 'inline-block', background: 'rgba(255,255,255,0.2)', color: '#fff', padding: '6px 20px', borderRadius: '20px', fontSize: '13px', fontWeight: '700', letterSpacing: '2px', marginBottom: '16px' },
  heroTitle: { fontSize: '42px', fontWeight: '900', color: '#fff', margin: '0 0 8px', fontFamily: 'Georgia,serif' },
  heroSub: { color: 'rgba(255,255,255,0.75)', fontSize: '16px', margin: 0 },
  container: { maxWidth: '1280px', margin: '0 auto', padding: '32px 24px' },
  sectionHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' },
  sectionTitle: { fontSize: '22px', fontWeight: '800', color: '#111', margin: 0 },
  sectionSub: { color: '#999', fontSize: '14px' },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: '16px' },
  card: { background: '#fff', borderRadius: '12px', overflow: 'hidden', border: '1px solid #e8e8e8', cursor: 'pointer', transition: 'all 0.2s', position: 'relative' },
  rankBadge: { position: 'absolute', top: '8px', left: '8px', background: '#1a237e', color: '#fff', fontSize: '11px', fontWeight: '800', padding: '3px 8px', borderRadius: '6px', zIndex: 3 },
  cardImg: { position: 'relative', paddingTop: '100%', background: '#f8f8f8', overflow: 'hidden' },
  img: { position: 'absolute', inset: 0, width: '100%', height: '100%', objectFit: 'cover' },
  imgPh: { position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '48px' },
  medalBadge: { position: 'absolute', top: '8px', right: '8px', width: '32px', height: '32px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '16px', zIndex: 2 },
  freeShip: { position: 'absolute', bottom: 0, left: 0, right: 0, background: '#1b5e20', color: '#fff', fontSize: '10px', fontWeight: '700', padding: '4px', textAlign: 'center' },
  cardBody: { padding: '12px' },
  cardName: { fontSize: '13px', fontWeight: '500', color: '#222', lineHeight: 1.4, marginBottom: '6px', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' },
  soldCount: { fontSize: '11px', color: '#1565c0', fontWeight: '600', marginBottom: '6px' },
  priceRow: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' },
  oldPrice: { fontSize: '11px', color: '#bbb', textDecoration: 'line-through' },
  newPrice: { fontSize: '16px', fontWeight: '800', color: '#c62828' },
  addBtn: { width: '32px', height: '32px', borderRadius: '50%', border: '2px solid #c62828', background: '#fff', color: '#c62828', fontWeight: '700', fontSize: '18px', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 },
  stars: { fontSize: '13px', color: '#f59e0b' },
  skeleton: { background: 'linear-gradient(90deg,#f0f0f0 25%,#e8e8e8 50%,#f0f0f0 75%)', borderRadius: '12px', height: '280px' },
  toast: { position: 'fixed', bottom: '100px', left: '50%', transform: 'translateX(-50%)', background: '#111', color: '#fff', padding: '12px 24px', borderRadius: '10px', fontSize: '14px', fontWeight: '500', zIndex: 2000 },
};