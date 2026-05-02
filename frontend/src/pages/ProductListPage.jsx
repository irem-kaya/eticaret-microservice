import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { productService, categoryService } from '../services/productService';
import { useCart } from '../context/CartContext';

const SORT_OPTIONS = [
  { value: 'createdAt', label: 'Akıllı Sıralama' },
  { value: 'price', label: 'En Düşük Fiyat' },
  { value: 'name_asc', label: 'A-Z' },
  { value: 'name_desc', label: 'Z-A' },
];

const BANNER_SLIDES = [
  { bg: 'linear-gradient(120deg, #1a237e 0%, #3949ab 100%)', title: 'Elektronik Dünyası', sub: 'En yeni teknoloji ürünleri', badge: '%50 İndirim', badgeColor: '#ff6b6b', img: 'https://images.unsplash.com/photo-1498049794561-7780e7231661?w=700&q=80', cat: 'Elektronik' },
  { bg: 'linear-gradient(120deg, #880e4f 0%, #e91e63 100%)', title: 'Moda & Stil', sub: 'Trendleri yakala', badge: '%40 İndirim', badgeColor: '#fff176', img: 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=700&q=80', cat: 'Giyim' },
  { bg: 'linear-gradient(120deg, #1b5e20 0%, #388e3c 100%)', title: 'Ev & Yaşam', sub: 'Evinizi güzelleştirin', badge: '%35 İndirim', badgeColor: '#80deea', img: 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=700&q=80', cat: 'Ev Yasam' },
];

const KAMPANYA_BLOKLAR = [
  { icon: '🔥', title: 'Flash Sale', sub: 'Sınırlı süre', color: '#fff3e0', border: '#ff9800', text: '#e65100', route: '/flash-sale' },
  { icon: '🚀', title: 'Ücretsiz Kargo', sub: '500₺ üzeri', color: '#e3f2fd', border: '#1565c0', text: '#1565c0', route: null },
  { icon: '💎', title: 'Premium Ürünler', sub: 'Seçkin markalar', color: '#f3e5f5', border: '#7b1fa2', text: '#6a1b9a', cat: 'Giyim' },
  { icon: '⭐', title: 'Çok Satanlar', sub: 'En popüler', color: '#e8f5e9', border: '#2e7d32', text: '#1b5e20', route: '/best-sellers' },
  { icon: '🎁', title: 'Özel Teklifler', sub: 'Sadece bugün', color: '#fce4ec', border: '#c62828', text: '#b71c1c', cat: 'Spor' },
];

const PRICE_RANGES = [
  { label: '0 - 2.000 ₺', min: 0, max: 2000 },
  { label: '2.000 - 5.000 ₺', min: 2000, max: 5000 },
  { label: '5.000 - 20.000 ₺', min: 5000, max: 20000 },
  { label: '20.000 ₺ ve üzeri', min: 20000, max: 999999 },
];

export default function ProductListPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { addToCart } = useCart();

  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [addingId, setAddingId] = useState(null);
  const [toast, setToast] = useState(null);
  const [bannerIdx, setBannerIdx] = useState(0);
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [sortBy, setSortBy] = useState('createdAt');
  const [priceMin, setPriceMin] = useState('');
  const [priceMax, setPriceMax] = useState('');
  const [selectedPriceRange, setSelectedPriceRange] = useState(null);

  const keyword = searchParams.get('keyword') || '';
  const categoryParam = searchParams.get('category') || '';

  useEffect(() => {
    if (!categoryParam && !keyword) setSelectedCategory(null);
  }, [categoryParam, keyword]);

  useEffect(() => {
    if (categoryParam && categories.length) {
      const found = categories.find(c => c.name === categoryParam);
      if (found) setSelectedCategory(found.id);
      else setSelectedCategory(null);
    }
  }, [categoryParam, categories]);

  const isHome = !keyword && !selectedCategory;
  const isSearchOrFilter = keyword || selectedCategory;

  useEffect(() => {
    const timer = setInterval(() => setBannerIdx(i => (i + 1) % BANNER_SLIDES.length), 5000);
    return () => clearInterval(timer);
  }, []);

  useEffect(() => {
    categoryService.getAll().then(res => setCategories(res.data.data || [])).catch(() => {});
  }, []);

  const fetchProducts = useCallback(async () => {
    setLoading(true);
    try {
      const params = {
        page, size: 16, sortBy,
        ...(keyword && { keyword }),
        ...(selectedCategory && { categoryId: selectedCategory }),
        ...(priceMin !== '' && { minPrice: priceMin }),
        ...(priceMax !== '' && { maxPrice: priceMax }),
      };
      const res = await productService.getAll(params);
      const data = res.data.data;
      setProducts(data.content || []);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch { setProducts([]); }
    finally { setLoading(false); }
  }, [page, sortBy, keyword, selectedCategory, priceMin, priceMax]);

  useEffect(() => { fetchProducts(); }, [fetchProducts]);

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

  const handlePriceRange = (range, idx) => {
    if (selectedPriceRange === idx) {
      setSelectedPriceRange(null); setPriceMin(''); setPriceMax('');
    } else {
      setSelectedPriceRange(idx); setPriceMin(range.min); setPriceMax(range.max);
    }
    setPage(0);
  };

  const handleSidebarCat = (cat) => {
    setSelectedCategory(cat.id);
    setPage(0);
    navigate(`/?category=${encodeURIComponent(cat.name)}`);
  };

  const handleSidebarAll = () => {
    setSelectedCategory(null);
    setPriceMin('');
    setPriceMax('');
    setSelectedPriceRange(null);
    setPage(0);
    navigate('/');
  };

  const slide = BANNER_SLIDES[bannerIdx];

  return (
    <div style={s.page}>

      {isHome && (
        <>
          <div style={{ ...s.banner, background: slide.bg }}>
            <div style={s.bannerLeft}>
              <span style={{ ...s.bannerBadge, background: slide.badgeColor, color: '#111' }}>{slide.badge}</span>
              <h1 style={s.bannerTitle}>{slide.title}</h1>
              <p style={s.bannerSub}>{slide.sub}</p>
              <button style={s.bannerBtn} onClick={() => navigate(`/?category=${encodeURIComponent(slide.cat)}`)}>
                Alışverişe Başla →
              </button>
            </div>
            <div style={s.bannerRight}>
              <img src={slide.img} alt={slide.title} style={s.bannerImg} />
            </div>
            <div style={s.bannerDots}>
              {BANNER_SLIDES.map((_, i) => (
                <button key={i} style={{ ...s.dot, ...(i === bannerIdx ? s.dotActive : {}) }} onClick={() => setBannerIdx(i)} />
              ))}
            </div>
          </div>

          <div style={s.kampanyaRow}>
            {KAMPANYA_BLOKLAR.map(k => (
              <div key={k.title}
                style={{ ...s.kampanyaBlok, background: k.color, borderColor: k.border + '40' }}
                onClick={() => { if (k.route) navigate(k.route); else if (k.cat) navigate(`/?category=${encodeURIComponent(k.cat)}`); }}
              >
                <span style={s.kampanyaIcon}>{k.icon}</span>
                <div>
                  <div style={{ ...s.kampanyaTitle, color: k.text }}>{k.title}</div>
                  <div style={s.kampanyaSub}>{k.sub}</div>
                </div>
              </div>
            ))}
          </div>

          <div style={s.promoStrip}>
            {[
              { icon: '🚀', text: 'Ücretsiz Kargo', sub: '500₺ üzeri' },
              { icon: '🔄', text: 'Kolay İade', sub: '30 gün içinde' },
              { icon: '🔒', text: 'Güvenli Ödeme', sub: 'SSL korumalı' },
              { icon: '⚡', text: 'Hızlı Teslimat', sub: '1-3 iş günü' },
            ].map(p => (
              <div key={p.text} style={s.promoItem}>
                <span style={s.promoIcon}>{p.icon}</span>
                <div>
                  <div style={s.promoText}>{p.text}</div>
                  <div style={s.promoSub}>{p.sub}</div>
                </div>
              </div>
            ))}
          </div>
        </>
      )}

      {isSearchOrFilter && (
        <div style={s.breadcrumb}>
          <span style={s.breadcrumbLink} onClick={handleSidebarAll}>Ana Sayfa</span>
          <span style={s.breadcrumbSep}>›</span>
          {keyword && <span style={s.breadcrumbCurrent}>"{keyword}" için {totalElements} sonuç</span>}
          {selectedCategory && !keyword && <span style={s.breadcrumbCurrent}>{categories.find(c => c.id === selectedCategory)?.name}</span>}
        </div>
      )}

      <div style={s.contentArea}>
        {isSearchOrFilter && (
          <aside style={s.sidebar}>
            <div style={s.filterBox}>
              <h3 style={s.filterTitle}>Kategori</h3>
              <button style={{ ...s.filterCatBtn, ...(selectedCategory === null ? s.filterCatActive : {}) }} onClick={handleSidebarAll}>
                Tüm Kategoriler
              </button>
              {categories.map(c => (
                <button key={c.id}
                  style={{ ...s.filterCatBtn, ...(selectedCategory === c.id ? s.filterCatActive : {}) }}
                  onClick={() => handleSidebarCat(c)}>
                  {c.name}
                </button>
              ))}
            </div>

            <div style={s.filterBox}>
              <h3 style={s.filterTitle}>Fiyat Aralığı</h3>
              {PRICE_RANGES.map((r, i) => (
                <label key={i} style={s.checkLabel}>
                  <input type="checkbox" checked={selectedPriceRange === i}
                    onChange={() => handlePriceRange(r, i)} style={s.checkbox} />
                  {r.label}
                </label>
              ))}
              <div style={s.customPrice}>
                <input style={s.priceInput} placeholder="Min ₺" value={priceMin}
                  onChange={e => { setPriceMin(e.target.value); setSelectedPriceRange(null); }} type="number" />
                <span>-</span>
                <input style={s.priceInput} placeholder="Max ₺" value={priceMax}
                  onChange={e => { setPriceMax(e.target.value); setSelectedPriceRange(null); }} type="number" />
              </div>
              <button style={s.applyBtn} onClick={() => { setPage(0); fetchProducts(); }}>Uygula</button>
              {(priceMin !== '' || priceMax !== '' || selectedPriceRange !== null) && (
                <button style={{ ...s.applyBtn, background: '#666', marginTop: '6px' }}
                  onClick={() => { setPriceMin(''); setPriceMax(''); setSelectedPriceRange(null); setPage(0); }}>
                  Temizle
                </button>
              )}
            </div>
          </aside>
        )}

        <main style={s.main}>
          <div style={s.toolbar}>
            <span style={s.resultText}>
              {loading ? 'Yükleniyor...' : keyword
                ? <><strong>"{keyword}"</strong> için {totalElements} sonuç</>
                : `${totalElements} ürün bulundu`}
            </span>
            <select style={s.sortSelect} value={sortBy} onChange={e => { setSortBy(e.target.value); setPage(0); }}>
              {SORT_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
            </select>
          </div>

          {loading ? (
            <div style={s.grid}>{[...Array(8)].map((_, i) => <div key={i} style={s.skeleton} />)}</div>
          ) : products.length === 0 ? (
            <div style={s.empty}>
              <div style={{ fontSize: '64px' }}>🔍</div>
              <h3 style={{ fontSize: '20px', fontWeight: '700', margin: '16px 0 8px' }}>Ürün bulunamadı</h3>
              <p style={{ color: '#999' }}>Farklı bir arama yapın veya filtreleri temizleyin</p>
              <button style={{ marginTop: '16px', background: '#c62828', color: '#fff', border: 'none', padding: '10px 24px', borderRadius: '8px', cursor: 'pointer', fontWeight: '600' }} onClick={handleSidebarAll}>
                Ana Sayfaya Dön
              </button>
            </div>
          ) : (
            <div style={s.grid}>
              {products.map(p => (
                <ProductCard key={p.id} product={p} addingId={addingId} onAdd={handleAdd}
                  onClick={() => navigate(`/products/${p.id}`)} />
              ))}
            </div>
          )}

          {totalPages > 1 && (
            <div style={s.pagination}>
              <button style={s.pageBtn} disabled={page === 0} onClick={() => setPage(p => p - 1)}>← Önceki</button>
              {[...Array(Math.min(totalPages, 7))].map((_, i) => (
                <button key={i} style={{ ...s.pageNum, ...(i === page ? s.pageActive : {}) }} onClick={() => setPage(i)}>{i + 1}</button>
              ))}
              <button style={s.pageBtn} disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>Sonraki →</button>
            </div>
          )}
        </main>
      </div>

      {toast && <div style={s.toast}>{toast}</div>}
    </div>
  );
}

function ProductCard({ product, addingId, onAdd, onClick }) {
  const [hovered, setHovered] = useState(false);
  return (
    <div style={{ ...s.card, ...(hovered ? s.cardHover : {}) }} onClick={onClick}
      onMouseEnter={() => setHovered(true)} onMouseLeave={() => setHovered(false)}>
      <div style={s.cardImg}>
        {product.imageUrl
          ? <img src={product.imageUrl} alt={product.name} style={s.img} />
          : <div style={s.imgPh}>📦</div>}
        {product.stock < 5 && product.stock > 0 && <div style={s.stockBadge}>Son {product.stock}!</div>}
        <div style={s.freeShipBanner}>ÜCRETSİZ KARGO</div>
        {hovered && (
          <button style={s.quickAdd} onClick={e => onAdd(e, product)} disabled={addingId === product.id}>
            {addingId === product.id ? '...' : '+'}
          </button>
        )}
      </div>
      <div style={s.cardBody}>
        <div style={s.cardName}>{product.name}</div>
        <div style={s.cardDesc}>{product.description?.slice(0, 55)}{product.description?.length > 55 ? '...' : ''}</div>
        <div style={s.cardPriceRow}>
          <div>
            <div style={s.oldPrice}>{(product.price * 1.2).toLocaleString('tr-TR', { maximumFractionDigits: 0 })} TL</div>
            <div style={s.cardPrice}>{product.price?.toLocaleString('tr-TR')} TL</div>
          </div>
          <button style={s.addBtn} onClick={e => onAdd(e, product)} disabled={addingId === product.id}>
            {addingId === product.id ? '...' : '+'}
          </button>
        </div>
      </div>
    </div>
  );
}

const s = {
  page: { background: '#f2f3f5', minHeight: '100vh' },
  banner: { display: 'flex', alignItems: 'center', minHeight: '280px', position: 'relative', overflow: 'hidden' },
  bannerLeft: { flex: 1, padding: '40px 60px', color: '#fff', zIndex: 2, position: 'relative' },
  bannerBadge: { display: 'inline-block', padding: '4px 14px', borderRadius: '20px', fontSize: '12px', fontWeight: '700', marginBottom: '12px' },
  bannerTitle: { fontSize: '38px', fontWeight: '800', margin: '0 0 8px', fontFamily: 'Georgia,serif', lineHeight: 1.1 },
  bannerSub: { color: 'rgba(255,255,255,0.75)', fontSize: '15px', margin: '0 0 20px' },
  bannerBtn: { background: '#fff', color: '#111', border: 'none', padding: '12px 24px', borderRadius: '10px', fontWeight: '700', fontSize: '14px', cursor: 'pointer' },
  bannerRight: { flex: '0 0 360px', height: '280px', overflow: 'hidden' },
  bannerImg: { width: '100%', height: '100%', objectFit: 'cover', opacity: 0.6 },
  bannerDots: { position: 'absolute', bottom: '16px', left: '60px', display: 'flex', gap: '8px', zIndex: 3 },
  dot: { width: '8px', height: '8px', borderRadius: '50%', border: 'none', background: 'rgba(255,255,255,0.4)', cursor: 'pointer', padding: 0, transition: 'all 0.3s' },
  dotActive: { background: '#fff', width: '24px', borderRadius: '4px' },
  kampanyaRow: { background: '#fff', padding: '12px 0', borderBottom: '1px solid #eee', display: 'flex', justifyContent: 'center', overflowX: 'auto' },
  kampanyaBlok: { display: 'flex', alignItems: 'center', gap: '10px', padding: '12px 24px', cursor: 'pointer', border: '1px solid transparent', borderRight: '1px solid #f0f0f0', transition: 'all 0.2s', minWidth: '160px' },
  kampanyaIcon: { fontSize: '28px', flexShrink: 0 },
  kampanyaTitle: { fontWeight: '700', fontSize: '13px' },
  kampanyaSub: { fontSize: '11px', color: '#888', marginTop: '2px' },
  promoStrip: { background: '#fff', display: 'flex', justifyContent: 'center', borderBottom: '1px solid #eee', flexWrap: 'wrap' },
  promoItem: { display: 'flex', alignItems: 'center', gap: '10px', padding: '12px 36px', borderRight: '1px solid #eee' },
  promoIcon: { fontSize: '22px' },
  promoText: { fontWeight: '700', fontSize: '13px', color: '#111' },
  promoSub: { fontSize: '11px', color: '#999' },
  breadcrumb: { maxWidth: '1280px', margin: '0 auto', padding: '12px 24px', display: 'flex', gap: '8px', alignItems: 'center', fontSize: '13px' },
  breadcrumbLink: { color: '#1565c0', cursor: 'pointer' },
  breadcrumbSep: { color: '#bbb' },
  breadcrumbCurrent: { color: '#333', fontWeight: '500' },
  contentArea: { maxWidth: '1280px', margin: '0 auto', padding: '16px 24px', display: 'flex', gap: '20px', alignItems: 'flex-start' },
  sidebar: { width: '220px', flexShrink: 0 },
  filterBox: { background: '#fff', borderRadius: '12px', padding: '16px', marginBottom: '12px', border: '1px solid #e8e8e8' },
  filterTitle: { fontSize: '14px', fontWeight: '700', color: '#111', margin: '0 0 12px', paddingBottom: '10px', borderBottom: '1px solid #f0f0f0' },
  filterCatBtn: { display: 'block', width: '100%', padding: '8px 10px', background: 'none', border: 'none', textAlign: 'left', fontSize: '13px', color: '#444', cursor: 'pointer', borderRadius: '6px', marginBottom: '2px' },
  filterCatActive: { background: '#c62828', color: '#fff', fontWeight: '600' },
  checkLabel: { display: 'flex', alignItems: 'center', gap: '8px', fontSize: '13px', color: '#444', padding: '6px 0', cursor: 'pointer' },
  checkbox: { accentColor: '#c62828', width: '15px', height: '15px', cursor: 'pointer' },
  customPrice: { display: 'flex', gap: '6px', alignItems: 'center', marginTop: '10px' },
  priceInput: { flex: 1, border: '1px solid #e0e0e0', borderRadius: '6px', padding: '6px 8px', fontSize: '12px', outline: 'none', width: '60px' },
  applyBtn: { width: '100%', background: '#c62828', color: '#fff', border: 'none', borderRadius: '8px', padding: '9px', cursor: 'pointer', fontWeight: '600', fontSize: '13px', marginTop: '10px' },
  main: { flex: 1, minWidth: 0 },
  toolbar: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: '#fff', padding: '12px 16px', borderRadius: '10px', marginBottom: '12px', border: '1px solid #e8e8e8' },
  resultText: { fontSize: '13px', color: '#555' },
  sortSelect: { border: '1px solid #e0e0e0', borderRadius: '8px', padding: '7px 12px', fontSize: '13px', outline: 'none', cursor: 'pointer', background: '#fff' },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(185px, 1fr))', gap: '12px' },
  card: { background: '#fff', borderRadius: '10px', overflow: 'hidden', border: '1px solid #e8e8e8', cursor: 'pointer', transition: 'all 0.2s' },
  cardHover: { boxShadow: '0 4px 20px rgba(0,0,0,0.1)', transform: 'translateY(-2px)' },
  cardImg: { position: 'relative', paddingTop: '100%', background: '#f8f8f8', overflow: 'hidden' },
  img: { position: 'absolute', inset: 0, width: '100%', height: '100%', objectFit: 'cover' },
  imgPh: { position: 'absolute', inset: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '48px' },
  stockBadge: { position: 'absolute', top: '8px', left: '8px', background: '#c62828', color: '#fff', fontSize: '10px', fontWeight: '700', padding: '3px 8px', borderRadius: '6px', zIndex: 2 },
  freeShipBanner: { position: 'absolute', bottom: 0, left: 0, right: 0, background: '#1b5e20', color: '#fff', fontSize: '10px', fontWeight: '700', padding: '4px', textAlign: 'center' },
  quickAdd: { position: 'absolute', top: '8px', right: '8px', background: '#c62828', color: '#fff', border: 'none', borderRadius: '50%', width: '32px', height: '32px', fontWeight: '700', fontSize: '20px', cursor: 'pointer', zIndex: 3, display: 'flex', alignItems: 'center', justifyContent: 'center' },
  cardBody: { padding: '12px' },
  cardName: { fontSize: '13px', fontWeight: '500', color: '#222', lineHeight: 1.4, marginBottom: '4px', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' },
  cardDesc: { fontSize: '11px', color: '#999', lineHeight: 1.4, marginBottom: '8px' },
  cardPriceRow: { display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  oldPrice: { fontSize: '11px', color: '#bbb', textDecoration: 'line-through' },
  cardPrice: { fontSize: '16px', fontWeight: '800', color: '#c62828' },
  addBtn: { width: '32px', height: '32px', borderRadius: '50%', border: '2px solid #c62828', background: '#fff', color: '#c62828', fontWeight: '700', fontSize: '18px', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 },
  skeleton: { background: 'linear-gradient(90deg,#f0f0f0 25%,#e8e8e8 50%,#f0f0f0 75%)', borderRadius: '10px', height: '260px' },
  empty: { textAlign: 'center', padding: '80px 0' },
  pagination: { display: 'flex', justifyContent: 'center', gap: '6px', marginTop: '24px', flexWrap: 'wrap' },
  pageBtn: { padding: '8px 16px', border: '1px solid #e0e0e0', borderRadius: '6px', background: '#fff', cursor: 'pointer', fontSize: '13px', fontWeight: '600' },
  pageNum: { width: '36px', height: '36px', border: '1px solid #e0e0e0', borderRadius: '6px', background: '#fff', cursor: 'pointer', fontSize: '13px' },
  pageActive: { background: '#c62828', color: '#fff', borderColor: '#c62828' },
  toast: { position: 'fixed', bottom: '100px', left: '50%', transform: 'translateX(-50%)', background: '#111', color: '#fff', padding: '12px 24px', borderRadius: '10px', fontSize: '14px', fontWeight: '500', zIndex: 2000 },
};