import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { productService, categoryService } from '../services/productService';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';

const TABS = ['Ürün Açıklaması', 'Teslimat Bilgileri', 'İptal & İade'];

export default function ProductDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useCart();
  const { isLoggedIn } = useAuth();

  const [product, setProduct] = useState(null);
  const [similar, setSimilar] = useState([]);
  const [loading, setLoading] = useState(true);
  const [quantity, setQuantity] = useState(1);
  const [activeTab, setActiveTab] = useState(0);
  const [adding, setAdding] = useState(false);
  const [toast, setToast] = useState(null);
  const [stickyVisible, setStickyVisible] = useState(false);

  useEffect(() => {
    const handler = () => setStickyVisible(window.scrollY > 400);
    window.addEventListener('scroll', handler);
    return () => window.removeEventListener('scroll', handler);
  }, []);

  useEffect(() => {
    setLoading(true);
    productService.getById(id)
      .then(res => {
        const p = res.data.data;
        setProduct(p);
        fetchSimilar(p.categoryName, p.id);
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  }, [id]);

  const fetchSimilar = async (categoryName, currentId) => {
    if (!categoryName) return;
    try {
      const catRes = await categoryService.getAll();
      const cat = (catRes.data.data || []).find(c => c.name === categoryName);
      if (!cat) return;
      const res = await productService.getAll({ categoryId: cat.id, size: 8, page: 0 });
      setSimilar((res.data.data.content || []).filter(p => p.id !== currentId).slice(0, 6));
    } catch (_) {}
  };

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 3000);
  };

  const handleAdd = async () => {
    if (!isLoggedIn) { navigate('/login'); return; }
    setAdding(true);
    try {
      await addToCart({ productId: product.id, productName: product.name, price: product.price, quantity, imageUrl: product.imageUrl });
      showToast(`"${product.name}" sepete eklendi! 🛒`);
    } catch { showToast('Hata oluştu', 'error'); }
    finally { setAdding(false); }
  };

  if (loading) return <div style={s.loading}>Yükleniyor...</div>;
  if (!product) return <div style={s.loading}>Ürün bulunamadı</div>;

  const discountedPrice = product.price;
  const originalPrice = (product.price * 1.25).toFixed(2);
  const discountPct = Math.round((1 - discountedPrice / originalPrice) * 100);

  return (
    <div style={s.page}>

      {/* Sticky Bar */}
      {stickyVisible && (
        <div style={s.stickyBar}>
          <div style={s.stickyInner}>
            <span style={s.stickyName}>{product.name?.slice(0, 60)}...</span>
            <div style={s.stickyRight}>
              <span style={s.stickyPrice}>{discountedPrice?.toLocaleString('tr-TR')} TL</span>
              <button style={s.stickyBtn} onClick={handleAdd}>+ Sepete Ekle</button>
            </div>
          </div>
        </div>
      )}

      <div style={s.container}>
        {/* Breadcrumb */}
        <div style={s.breadcrumb}>
          <span style={s.breadLink} onClick={() => navigate('/')}>Ana Sayfa</span>
          <span style={s.breadSep}>›</span>
          <span style={s.breadLink} onClick={() => navigate(`/?category=${encodeURIComponent(product.categoryName || '')}`)}>
            {product.categoryName}
          </span>
          <span style={s.breadSep}>›</span>
          <span style={s.breadCurrent}>{product.name}</span>
        </div>

        {/* Main Content */}
        <div style={s.mainRow}>

          {/* Sol: Görsel */}
          <div style={s.imgSection}>
            <div style={s.imgWrap}>
              {product.imageUrl
                ? <img src={product.imageUrl} alt={product.name} style={s.mainImg} />
                : <div style={s.imgPh}>📦</div>
              }
              {discountPct > 0 && (
                <div style={s.discountBadge}>%{discountPct} İndirim</div>
              )}
            </div>
            {/* Küçük görseller (placeholder) */}
            <div style={s.thumbRow}>
              {[...Array(4)].map((_, i) => (
                <div key={i} style={{ ...s.thumb, ...(i === 0 ? s.thumbActive : {}) }}>
                  {product.imageUrl
                    ? <img src={product.imageUrl} alt="" style={s.thumbImg} />
                    : <span>📦</span>
                  }
                </div>
              ))}
            </div>
          </div>

          {/* Sağ: Bilgiler */}
          <div style={s.infoSection}>
            <div style={s.categoryTag}>{product.categoryName}</div>
            <h1 style={s.productName}>{product.name}</h1>

            {/* Yıldız */}
            <div style={s.ratingRow}>
              <span style={s.stars}>★★★★☆</span>
              <span style={s.ratingCount}>4.2</span>
              <span style={s.reviewCount}>(128 değerlendirme)</span>
            </div>

            {/* En düşük fiyat badge */}
            <div style={s.lowestBadge}>🏷️ 10 günün en düşük fiyatı!</div>

            {/* Fiyat */}
            <div style={s.priceBox}>
              <div style={s.oldPrice}>{Number(originalPrice).toLocaleString('tr-TR')} TL</div>
              <div style={s.currentPrice}>{discountedPrice?.toLocaleString('tr-TR')} TL</div>
              <div style={s.sepette}>SEPETTE</div>
            </div>

            {/* Kargo */}
            <div style={s.cargoBox}>
              <span style={s.cargoIcon}>🚀</span>
              <div>
                <div style={s.cargoTitle}>Ücretsiz Kargo</div>
                <div style={s.cargoSub}>Tahmini teslimat: 1-3 iş günü</div>
              </div>
            </div>

            {/* Stok */}
            {product.stock > 0 ? (
              <div style={s.stockOk}>✅ Stokta var ({product.stock} adet)</div>
            ) : (
              <div style={s.stockNo}>❌ Stokta yok</div>
            )}

            {/* Miktar */}
            <div style={s.qtyRow}>
              <span style={s.qtyLabel}>Adet:</span>
              <div style={s.qtyBox}>
                <button style={s.qtyBtn} onClick={() => setQuantity(q => Math.max(1, q - 1))}>−</button>
                <span style={s.qtyNum}>{quantity}</span>
                <button style={s.qtyBtn} onClick={() => setQuantity(q => Math.min(product.stock, q + 1))}>+</button>
              </div>
            </div>

            {/* Sepete Ekle */}
            <button style={s.addBtn} onClick={handleAdd} disabled={adding || product.stock === 0}>
              {adding ? 'Ekleniyor...' : '+ Sepete Ekle'}
            </button>

            {/* Güvenceler */}
            <div style={s.guarantees}>
              {[
                { icon: '🔒', text: 'Güvenli ödeme' },
                { icon: '🔄', text: '30 gün iade' },
                { icon: '✅', text: 'Orijinal ürün' },
              ].map(g => (
                <div key={g.text} style={s.guarantee}>
                  <span>{g.icon}</span>
                  <span style={s.guaranteeText}>{g.text}</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div style={s.tabsBar}>
          {TABS.map((tab, i) => (
            <button
              key={tab}
              style={{ ...s.tabBtn, ...(activeTab === i ? s.tabActive : {}) }}
              onClick={() => setActiveTab(i)}
            >
              {tab}
            </button>
          ))}
        </div>

        <div style={s.tabContent}>
          {activeTab === 0 && (
            <div>
              <h3 style={s.tabTitle}>Ürün Açıklaması</h3>
              <p style={s.desc}>{product.description || 'Bu ürün için açıklama bulunmamaktadır.'}</p>
              <div style={s.featureGrid}>
                {[
                  { label: 'Kategori', value: product.categoryName },
                  { label: 'Stok', value: `${product.stock} adet` },
                  { label: 'Ürün ID', value: `#${product.id}` },
                  { label: 'Durum', value: product.active ? 'Aktif' : 'Pasif' },
                ].map(f => (
                  <div key={f.label} style={s.featureItem}>
                    <span style={s.featureLabel}>{f.label}</span>
                    <span style={s.featureValue}>{f.value}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
          {activeTab === 1 && (
            <div>
              <h3 style={s.tabTitle}>Teslimat Bilgileri</h3>
              <div style={s.infoCard}>
                <div style={s.infoRow}><span>🚀</span><div><strong>Ücretsiz Kargo</strong><br/><span style={{ color: '#666', fontSize: '13px' }}>500₺ ve üzeri alışverişlerde geçerlidir</span></div></div>
                <div style={s.infoRow}><span>📦</span><div><strong>Tahmini Teslimat</strong><br/><span style={{ color: '#666', fontSize: '13px' }}>1-3 iş günü içinde</span></div></div>
                <div style={s.infoRow}><span>🏪</span><div><strong>Mağazadan Teslim</strong><br/><span style={{ color: '#666', fontSize: '13px' }}>Yakın noktalardan teslim alabilirsiniz</span></div></div>
              </div>
            </div>
          )}
          {activeTab === 2 && (
            <div>
              <h3 style={s.tabTitle}>İptal & İade Bilgileri</h3>
              <div style={s.infoCard}>
                <div style={s.infoRow}><span>🔄</span><div><strong>30 Gün İade Garantisi</strong><br/><span style={{ color: '#666', fontSize: '13px' }}>Ürünü 30 gün içinde iade edebilirsiniz</span></div></div>
                <div style={s.infoRow}><span>✅</span><div><strong>Kolay İade</strong><br/><span style={{ color: '#666', fontSize: '13px' }}>Ücretsiz iade kargo hizmeti</span></div></div>
              </div>
            </div>
          )}
        </div>

        {/* Benzer Ürünler */}
        {similar.length > 0 && (
          <div style={s.similarSection}>
            <div style={s.similarHeader}>
              <h2 style={s.similarTitle}>Bunlar İlgini Çekebilir 🎯</h2>
              <button style={s.seeAll} onClick={() => navigate(`/?category=${encodeURIComponent(product.categoryName || '')}`)}>
                Tümünü Gör →
              </button>
            </div>
            <div style={s.similarGrid}>
              {similar.map(p => (
                <div key={p.id} style={s.simCard} onClick={() => navigate(`/products/${p.id}`)}>
                  <div style={s.simImg}>
                    {p.imageUrl
                      ? <img src={p.imageUrl} alt={p.name} style={s.simImgEl} />
                      : <span style={{ fontSize: '40px' }}>📦</span>
                    }
                    <div style={s.simFreeShip}>ÜCRETSİZ KARGO</div>
                  </div>
                  <div style={s.simBody}>
                    <div style={s.simName}>{p.name}</div>
                    <div style={s.simOldPrice}>{(p.price * 1.2).toLocaleString('tr-TR', { maximumFractionDigits: 0 })} TL</div>
                    <div style={s.simPrice}>{p.price?.toLocaleString('tr-TR')} TL</div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Toast */}
      {toast && (
        <div style={{ ...s.toast, background: toast.type === 'error' ? '#c62828' : '#1b5e20' }}>
          {toast.msg}
          {toast.type === 'success' && (
            <button style={s.toastBtn} onClick={() => navigate('/cart')}>Sepete Git →</button>
          )}
        </div>
      )}
    </div>
  );
}

const s = {
  page: { background: '#f2f3f5', minHeight: '100vh' },
  loading: { textAlign: 'center', padding: '100px', fontSize: '18px', color: '#999' },

  // Sticky
  stickyBar: { position: 'fixed', top: '96px', left: 0, right: 0, background: '#fff', borderBottom: '2px solid #c62828', zIndex: 99, boxShadow: '0 2px 8px rgba(0,0,0,0.1)' },
  stickyInner: { maxWidth: '1280px', margin: '0 auto', padding: '12px 24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  stickyName: { fontSize: '14px', fontWeight: '500', color: '#333' },
  stickyRight: { display: 'flex', alignItems: 'center', gap: '16px' },
  stickyPrice: { fontSize: '18px', fontWeight: '800', color: '#c62828' },
  stickyBtn: { background: '#c62828', color: '#fff', border: 'none', padding: '10px 24px', borderRadius: '8px', fontWeight: '700', cursor: 'pointer', fontSize: '14px' },

  container: { maxWidth: '1280px', margin: '0 auto', padding: '16px 24px' },

  // Breadcrumb
  breadcrumb: { display: 'flex', gap: '6px', alignItems: 'center', fontSize: '13px', marginBottom: '16px' },
  breadLink: { color: '#1565c0', cursor: 'pointer' },
  breadSep: { color: '#bbb' },
  breadCurrent: { color: '#333' },

  // Main row
  mainRow: { display: 'flex', gap: '32px', background: '#fff', borderRadius: '16px', padding: '32px', marginBottom: '20px', border: '1px solid #e8e8e8' },

  // Image
  imgSection: { flex: '0 0 420px' },
  imgWrap: { position: 'relative', borderRadius: '12px', overflow: 'hidden', background: '#f8f8f8', height: '400px', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  mainImg: { width: '100%', height: '100%', objectFit: 'contain' },
  imgPh: { fontSize: '80px' },
  discountBadge: { position: 'absolute', top: '12px', left: '12px', background: '#c62828', color: '#fff', padding: '4px 12px', borderRadius: '6px', fontSize: '12px', fontWeight: '700' },
  thumbRow: { display: 'flex', gap: '8px', marginTop: '12px' },
  thumb: { width: '70px', height: '70px', borderRadius: '8px', border: '2px solid #e8e8e8', overflow: 'hidden', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f8f8f8' },
  thumbActive: { borderColor: '#c62828' },
  thumbImg: { width: '100%', height: '100%', objectFit: 'cover' },

  // Info
  infoSection: { flex: 1 },
  categoryTag: { fontSize: '12px', color: '#c62828', fontWeight: '600', letterSpacing: '0.5px', textTransform: 'uppercase', marginBottom: '8px' },
  productName: { fontSize: '22px', fontWeight: '700', color: '#111', lineHeight: 1.4, margin: '0 0 12px' },
  ratingRow: { display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px' },
  stars: { color: '#f59e0b', fontSize: '18px' },
  ratingCount: { fontWeight: '700', color: '#333' },
  reviewCount: { color: '#999', fontSize: '13px' },
  lowestBadge: { background: '#fffde7', border: '1px solid #f9a825', color: '#f57f17', padding: '6px 12px', borderRadius: '8px', fontSize: '13px', fontWeight: '600', display: 'inline-block', marginBottom: '16px' },
  priceBox: { marginBottom: '16px' },
  oldPrice: { fontSize: '14px', color: '#bbb', textDecoration: 'line-through', marginBottom: '2px' },
  currentPrice: { fontSize: '32px', fontWeight: '800', color: '#c62828', lineHeight: 1 },
  sepette: { fontSize: '11px', color: '#c62828', fontWeight: '700', letterSpacing: '1px', marginTop: '4px' },
  cargoBox: { display: 'flex', alignItems: 'center', gap: '12px', background: '#e8f5e9', borderRadius: '10px', padding: '12px 16px', marginBottom: '16px' },
  cargoIcon: { fontSize: '24px' },
  cargoTitle: { fontWeight: '700', fontSize: '14px', color: '#1b5e20' },
  cargoSub: { fontSize: '12px', color: '#2e7d32', marginTop: '2px' },
  stockOk: { fontSize: '14px', color: '#2e7d32', fontWeight: '600', marginBottom: '16px' },
  stockNo: { fontSize: '14px', color: '#c62828', fontWeight: '600', marginBottom: '16px' },
  qtyRow: { display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '16px' },
  qtyLabel: { fontSize: '14px', fontWeight: '600', color: '#333' },
  qtyBox: { display: 'flex', alignItems: 'center', border: '1.5px solid #e0e0e0', borderRadius: '8px', overflow: 'hidden' },
  qtyBtn: { width: '36px', height: '36px', background: '#f5f5f5', border: 'none', fontSize: '18px', cursor: 'pointer', fontWeight: '700', color: '#333' },
  qtyNum: { width: '48px', textAlign: 'center', fontWeight: '700', fontSize: '16px' },
  addBtn: { width: '100%', padding: '16px', background: '#c62828', color: '#fff', border: 'none', borderRadius: '12px', fontSize: '16px', fontWeight: '800', cursor: 'pointer', marginBottom: '16px', transition: 'opacity 0.2s' },
  guarantees: { display: 'flex', gap: '16px' },
  guarantee: { display: 'flex', alignItems: 'center', gap: '6px', fontSize: '12px', color: '#555' },
  guaranteeText: { fontWeight: '500' },

  // Tabs
  tabsBar: { background: '#fff', borderRadius: '12px 12px 0 0', display: 'flex', borderBottom: '2px solid #f0f0f0', overflow: 'hidden', border: '1px solid #e8e8e8' },
  tabBtn: { padding: '16px 28px', background: 'none', border: 'none', fontSize: '14px', fontWeight: '600', cursor: 'pointer', color: '#666', borderBottom: '3px solid transparent', transition: 'all 0.2s' },
  tabActive: { color: '#c62828', borderBottom: '3px solid #c62828', background: '#fff' },
  tabContent: { background: '#fff', padding: '28px', borderRadius: '0 0 12px 12px', marginBottom: '20px', border: '1px solid #e8e8e8', borderTop: 'none' },
  tabTitle: { fontSize: '18px', fontWeight: '700', color: '#111', margin: '0 0 16px' },
  desc: { fontSize: '15px', color: '#444', lineHeight: 1.8, marginBottom: '20px' },
  featureGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' },
  featureItem: { display: 'flex', justifyContent: 'space-between', padding: '10px 16px', background: '#f8f8f8', borderRadius: '8px' },
  featureLabel: { color: '#666', fontSize: '13px' },
  featureValue: { fontWeight: '600', color: '#111', fontSize: '13px' },
  infoCard: { display: 'flex', flexDirection: 'column', gap: '16px' },
  infoRow: { display: 'flex', gap: '16px', alignItems: 'flex-start', padding: '16px', background: '#f8f8f8', borderRadius: '10px', fontSize: '14px' },

  // Similar
  similarSection: { marginBottom: '40px' },
  similarHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' },
  similarTitle: { fontSize: '20px', fontWeight: '700', color: '#111', margin: 0 },
  seeAll: { background: 'none', border: 'none', color: '#c62828', fontSize: '14px', fontWeight: '700', cursor: 'pointer' },
  similarGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))', gap: '12px' },
  simCard: { background: '#fff', borderRadius: '10px', overflow: 'hidden', border: '1px solid #e8e8e8', cursor: 'pointer', transition: 'all 0.2s' },
  simImg: { position: 'relative', paddingTop: '100%', background: '#f8f8f8', overflow: 'hidden' },
  simImgEl: { position: 'absolute', inset: 0, width: '100%', height: '100%', objectFit: 'cover' },
  simFreeShip: { position: 'absolute', bottom: 0, left: 0, right: 0, background: '#1b5e20', color: '#fff', fontSize: '10px', fontWeight: '700', padding: '4px', textAlign: 'center' },
  simBody: { padding: '12px' },
  simName: { fontSize: '12px', color: '#333', lineHeight: 1.4, marginBottom: '6px', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' },
  simOldPrice: { fontSize: '11px', color: '#bbb', textDecoration: 'line-through' },
  simPrice: { fontSize: '15px', fontWeight: '800', color: '#c62828' },

  // Toast
  toast: { position: 'fixed', bottom: '100px', left: '50%', transform: 'translateX(-50%)', color: '#fff', padding: '14px 24px', borderRadius: '12px', fontSize: '14px', fontWeight: '500', zIndex: 2000, boxShadow: '0 4px 20px rgba(0,0,0,0.2)', display: 'flex', alignItems: 'center', gap: '16px', whiteSpace: 'nowrap' },
  toastBtn: { background: 'rgba(255,255,255,0.2)', border: '1px solid rgba(255,255,255,0.4)', color: '#fff', padding: '6px 14px', borderRadius: '8px', cursor: 'pointer', fontSize: '13px', fontWeight: '700' },
};