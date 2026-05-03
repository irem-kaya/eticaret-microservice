import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { productService } from '../services/productService';

const VALID_COUPONS = {
  'HOSGELDIN10': { discount: 10, type: 'percent', desc: '%10 indirim' },
  'KARGO0': { discount: 0, type: 'shipping', desc: 'Ücretsiz kargo' },
  'TMARKET50': { discount: 50, type: 'fixed', desc: '50₺ indirim' },
  'YENI100': { discount: 100, type: 'fixed', desc: '100₺ indirim' },
};

export default function CartPage() {
  const { cart, loading, removeFromCart, updateQuantity, clearCart } = useCart();
  const navigate = useNavigate();
  const [couponCode, setCouponCode] = useState('');
  const [appliedCoupon, setAppliedCoupon] = useState(null);
  const [couponError, setCouponError] = useState('');
  const [couponSuccess, setCouponSuccess] = useState('');
  const [recommendations, setRecommendations] = useState([]);

  // Sepet ürünleriyle aynı kategoriden öneriler çek
  useEffect(() => {
    if (!cart.items?.length) return;
    const productIds = cart.items.map(i => i.productId);
    // Sepetteki ilk ürünün kategorisinden öneri çek
    // categoryId cart item'da olmayabilir, keyword ile çek
    productService.getAll({ size: 8, page: 0 })
      .then(res => {
        const all = (res.data.data.content || []).filter(p => !productIds.includes(p.id));
        setRecommendations(all.slice(0, 6));
      })
      .catch(() => {});
  }, [cart.items?.length]);

  const SHIPPING_FEE = cart.totalPrice >= 500 ? 0 : 49.90;

  const applyCoupon = () => {
    const code = couponCode.trim().toUpperCase();
    if (!code) return;
    const coupon = VALID_COUPONS[code];
    if (coupon) {
      setAppliedCoupon({ code, ...coupon });
      setCouponSuccess(`🎉 "${code}" kuponu uygulandı — ${coupon.desc}`);
      setCouponError('');
    } else {
      setCouponError('Geçersiz kupon kodu');
      setCouponSuccess('');
    }
  };

  const removeCoupon = () => {
    setAppliedCoupon(null); setCouponCode('');
    setCouponSuccess(''); setCouponError('');
  };

  const calcDiscount = () => {
    if (!appliedCoupon) return 0;
    if (appliedCoupon.type === 'percent') return (cart.totalPrice * appliedCoupon.discount) / 100;
    if (appliedCoupon.type === 'fixed') return Math.min(appliedCoupon.discount, cart.totalPrice);
    return 0;
  };
  const calcShipping = () => appliedCoupon?.type === 'shipping' ? 0 : SHIPPING_FEE;
  const grandTotal = Math.max(0, cart.totalPrice - calcDiscount() + calcShipping());

  if (loading) return <div style={s.loading}>Yükleniyor...</div>;

  if (cart.items.length === 0) {
    return (
      <div style={s.page}>
        <div style={s.emptyWrap}>
          <div style={s.emptyIcon}>🛒</div>
          <h2 style={s.emptyTitle}>Sepetiniz boş</h2>
          <p style={s.emptySub}>Ürün eklemek için alışverişe başlayın</p>
          <button style={s.shopBtn} onClick={() => navigate('/')}>Alışverişe Başla</button>
          <div style={s.emptyHints}>
            <div style={s.hintItem} onClick={() => navigate('/flash-sale')}>⚡ Flash Sale</div>
            <div style={s.hintItem} onClick={() => navigate('/best-sellers')}>⭐ Çok Satanlar</div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div style={s.page}>
      <div style={s.container}>
        <div style={s.header}>
          <h1 style={s.title}>Sepetim <span style={s.itemCount}>({cart.totalItems} ürün)</span></h1>
          <button style={s.clearBtn} onClick={clearCart}>Sepeti Temizle</button>
        </div>

        <div style={s.layout}>
          {/* Sol */}
          <div style={s.left}>
            {/* Kargo bar */}
            {calcShipping() > 0 ? (
              <div style={s.cargoInfo}>
                🚚 <strong>{(500 - cart.totalPrice).toLocaleString('tr-TR')} ₺</strong> daha ekleyin, kargo bedava!
                <div style={s.cargoBar}>
                  <div style={{ ...s.cargoFill, width: `${Math.min(100, (cart.totalPrice / 500) * 100)}%` }} />
                </div>
              </div>
            ) : (
              <div style={{ ...s.cargoInfo, background: '#e8f5e9', borderColor: '#2e7d32', color: '#1b5e20' }}>
                ✅ Tebrikler! Ücretsiz kargo hakkı kazandınız.
              </div>
            )}

            {/* Öneriler */}
            {recommendations.length > 0 && (
              <div style={s.recBox}>
                <h3 style={s.recTitle}>😍 Sepetinize Özel Öneriler</h3>
                <div style={s.recGrid}>
                  {recommendations.map(p => (
                    <div key={p.id} style={s.recCard} onClick={() => navigate(`/products/${p.id}`)}>
                      <div style={s.recImg}>
                        {p.imageUrl
                          ? <img src={p.imageUrl} alt={p.name} style={s.recImgEl} />
                          : <span style={{ fontSize: '24px' }}>📦</span>
                        }
                      </div>
                      <div style={s.recInfo}>
                        <div style={s.recName}>{p.name?.slice(0, 35)}{p.name?.length > 35 ? '...' : ''}</div>
                        <div style={s.recPrice}>{p.price?.toLocaleString('tr-TR')} TL</div>
                      </div>
                      <button style={s.recAddBtn} onClick={e => e.stopPropagation()}>+</button>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* Ürün listesi */}
            <div style={s.itemsBox}>
              <div style={s.selectAll}>
                <label style={s.checkLabel}>
                  <input type="checkbox" defaultChecked style={{ accentColor: '#c62828' }} />
                  <span>Tümünü Seç</span>
                </label>
                <span style={s.selectionCount}>{cart.totalItems} ürün seçili</span>
              </div>

              {cart.items.map(item => (
                <div key={item.productId} style={s.item}>
                  <input type="checkbox" defaultChecked style={{ accentColor: '#c62828', flexShrink: 0 }} />
                  <div style={s.itemImg} onClick={() => navigate(`/products/${item.productId}`)}>
                    {item.imageUrl
                      ? <img src={item.imageUrl} alt={item.productName} style={s.img} />
                      : <span style={{ fontSize: '32px' }}>📦</span>
                    }
                  </div>
                  <div style={s.itemInfo}>
                    <div style={s.itemName} onClick={() => navigate(`/products/${item.productId}`)}>
                      {item.productName}
                    </div>
                    <div style={s.itemMeta}>
                      <span style={s.freeShip}>✅ Ücretsiz Kargo</span>
                      <span style={s.stockOk}>Stokta Var</span>
                    </div>
                    <button style={s.delBtn} onClick={() => removeFromCart(item.productId)}>🗑️ Sil</button>
                  </div>
                  <div style={s.itemRight}>
                    <div style={s.qtyBox}>
                      <button style={s.qBtn} onClick={() => updateQuantity(item.productId, item.quantity - 1)} disabled={item.quantity <= 1}>−</button>
                      <span style={s.qty}>{item.quantity}</span>
                      <button style={s.qBtn} onClick={() => updateQuantity(item.productId, item.quantity + 1)}>+</button>
                    </div>
                    <div style={s.itemTotal}>{item.totalPrice?.toLocaleString('tr-TR')} TL</div>
                    <div style={s.itemUnitPrice}>{item.price?.toLocaleString('tr-TR')} TL / adet</div>
                  </div>
                </div>
              ))}
            </div>

            {/* Kupon */}
            <div style={s.couponBox}>
              <h3 style={s.couponTitle}>🎟️ Kupon & İndirim Kodu</h3>
              {appliedCoupon ? (
                <div style={s.appliedCoupon}>
                  <span>✅ <strong>{appliedCoupon.code}</strong> — {appliedCoupon.desc}</span>
                  <button style={s.removeCouponBtn} onClick={removeCoupon}>Kaldır ✕</button>
                </div>
              ) : (
                <div style={s.couponRow}>
                  <input style={s.couponInput} placeholder="Kupon kodunu girin..."
                    value={couponCode} onChange={e => setCouponCode(e.target.value)}
                    onKeyDown={e => e.key === 'Enter' && applyCoupon()} />
                  <button style={s.couponBtn} onClick={applyCoupon}>Uygula</button>
                </div>
              )}
              {couponError && <div style={s.couponError}>❌ {couponError}</div>}
              {couponSuccess && <div style={s.couponSuccess}>{couponSuccess}</div>}
              <div style={s.couponHints}>
                {Object.keys(VALID_COUPONS).map(code => (
                  <span key={code} style={s.hintChip} onClick={() => setCouponCode(code)}>{code}</span>
                ))}
              </div>
            </div>
          </div>

          {/* Sağ — Özet */}
          <div style={s.right}>
            <div style={s.summary}>
              <h3 style={s.summaryTitle}>Sipariş Özeti</h3>
              <div style={s.summaryRow}>
                <span>Ürün Tutarı ({cart.totalItems} adet)</span>
                <span>{cart.totalPrice?.toLocaleString('tr-TR')} TL</span>
              </div>
              <div style={s.summaryRow}>
                <span>Kargo</span>
                <span style={calcShipping() === 0 ? { color: '#2e7d32', fontWeight: '600' } : {}}>
                  {calcShipping() === 0 ? 'Ücretsiz' : `${calcShipping().toFixed(2)} TL`}
                </span>
              </div>
              {calcDiscount() > 0 && (
                <div style={{ ...s.summaryRow, color: '#c62828' }}>
                  <span>Kupon İndirimi</span>
                  <span>-{calcDiscount().toLocaleString('tr-TR')} TL</span>
                </div>
              )}
              <div style={s.totalRow}>
                <span style={s.totalLabel}>Toplam Tutar</span>
                <span style={s.totalAmount}>{grandTotal.toLocaleString('tr-TR')} TL</span>
              </div>
              <button style={s.checkoutBtn} onClick={() => navigate('/checkout')}>Ödemeye Geç →</button>
              <div style={s.secureRow}>
                <span>🔒 Güvenli ödeme</span>
                <span>✅ SSL korumalı</span>
              </div>
              <div style={s.paymentMethods}>
                {['💳 Kredi Kartı', '🏦 Havale', '💰 Kapıda Ödeme'].map(p => (
                  <div key={p} style={s.payMethod}>{p}</div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

const s = {
  page: { background: '#f2f3f5', minHeight: '100vh', padding: '24px 0' },
  container: { maxWidth: '1280px', margin: '0 auto', padding: '0 24px' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' },
  title: { fontSize: '24px', fontWeight: '800', color: '#111', margin: 0 },
  itemCount: { fontSize: '16px', color: '#999', fontWeight: '400' },
  clearBtn: { background: 'none', border: '1px solid #e0e0e0', color: '#999', padding: '8px 16px', borderRadius: '8px', cursor: 'pointer', fontSize: '13px' },
  layout: { display: 'flex', gap: '20px', alignItems: 'flex-start' },
  left: { flex: 1, minWidth: 0 },
  right: { width: '320px', flexShrink: 0 },
  cargoInfo: { background: '#fff3e0', border: '1px solid #ff9800', borderRadius: '10px', padding: '12px 16px', marginBottom: '12px', fontSize: '14px', color: '#e65100' },
  cargoBar: { height: '4px', background: '#f0f0f0', borderRadius: '2px', marginTop: '8px', overflow: 'hidden' },
  cargoFill: { height: '100%', background: '#ff9800', borderRadius: '2px' },

  // Öneriler
  recBox: { background: '#fff', borderRadius: '12px', padding: '16px', border: '1px solid #e8e8e8', marginBottom: '12px' },
  recTitle: { fontSize: '14px', fontWeight: '700', color: '#111', margin: '0 0 12px' },
  recGrid: { display: 'flex', gap: '8px', overflowX: 'auto', paddingBottom: '4px' },
  recCard: { display: 'flex', alignItems: 'center', gap: '8px', minWidth: '240px', background: '#f9f9f9', borderRadius: '10px', padding: '8px', cursor: 'pointer', border: '1px solid #f0f0f0', flexShrink: 0, transition: 'box-shadow 0.2s' },
  recImg: { width: '50px', height: '50px', borderRadius: '8px', overflow: 'hidden', background: '#f0f0f0', flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' },
  recImgEl: { width: '100%', height: '100%', objectFit: 'cover' },
  recInfo: { flex: 1, minWidth: 0 },
  recName: { fontSize: '11px', color: '#333', lineHeight: 1.4, marginBottom: '3px' },
  recPrice: { fontSize: '13px', fontWeight: '700', color: '#c62828' },
  recAddBtn: { width: '26px', height: '26px', borderRadius: '50%', border: '1.5px solid #c62828', background: '#fff', color: '#c62828', fontWeight: '700', fontSize: '16px', cursor: 'pointer', flexShrink: 0, display: 'flex', alignItems: 'center', justifyContent: 'center' },

  itemsBox: { background: '#fff', borderRadius: '12px', border: '1px solid #e8e8e8', overflow: 'hidden', marginBottom: '12px' },
  selectAll: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px 16px', borderBottom: '1px solid #f0f0f0' },
  checkLabel: { display: 'flex', alignItems: 'center', gap: '8px', fontSize: '14px', fontWeight: '600', cursor: 'pointer' },
  selectionCount: { fontSize: '13px', color: '#999' },
  item: { display: 'flex', alignItems: 'center', gap: '12px', padding: '16px', borderBottom: '1px solid #f5f5f5' },
  itemImg: { width: '80px', height: '80px', borderRadius: '10px', overflow: 'hidden', background: '#f8f8f8', flexShrink: 0, cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  img: { width: '100%', height: '100%', objectFit: 'cover' },
  itemInfo: { flex: 1, minWidth: 0 },
  itemName: { fontSize: '14px', fontWeight: '500', color: '#222', marginBottom: '6px', cursor: 'pointer', lineHeight: 1.4 },
  itemMeta: { display: 'flex', gap: '8px', marginBottom: '6px' },
  freeShip: { fontSize: '11px', color: '#2e7d32', fontWeight: '600', background: '#e8f5e9', padding: '2px 8px', borderRadius: '4px' },
  stockOk: { fontSize: '11px', color: '#1565c0', fontWeight: '600', background: '#e3f2fd', padding: '2px 8px', borderRadius: '4px' },
  delBtn: { background: 'none', border: 'none', color: '#999', cursor: 'pointer', fontSize: '12px', padding: '0' },
  itemRight: { textAlign: 'right', flexShrink: 0 },
  qtyBox: { display: 'flex', alignItems: 'center', border: '1px solid #e0e0e0', borderRadius: '8px', overflow: 'hidden', marginBottom: '8px', justifyContent: 'flex-end' },
  qBtn: { width: '32px', height: '32px', background: '#f5f5f5', border: 'none', cursor: 'pointer', fontSize: '16px', fontWeight: '700' },
  qty: { width: '36px', textAlign: 'center', fontWeight: '700', fontSize: '15px' },
  itemTotal: { fontSize: '16px', fontWeight: '800', color: '#c62828' },
  itemUnitPrice: { fontSize: '11px', color: '#bbb', marginTop: '2px' },

  couponBox: { background: '#fff', borderRadius: '12px', padding: '20px', border: '1px solid #e8e8e8' },
  couponTitle: { fontSize: '15px', fontWeight: '700', color: '#111', margin: '0 0 14px' },
  couponRow: { display: 'flex', gap: '8px' },
  couponInput: { flex: 1, border: '1.5px solid #e0e0e0', borderRadius: '8px', padding: '10px 14px', fontSize: '13px', outline: 'none' },
  couponBtn: { background: '#c62828', color: '#fff', border: 'none', borderRadius: '8px', padding: '10px 20px', fontWeight: '700', cursor: 'pointer', fontSize: '13px' },
  couponError: { marginTop: '8px', fontSize: '13px', color: '#c62828' },
  couponSuccess: { marginTop: '8px', fontSize: '13px', color: '#2e7d32', fontWeight: '500' },
  appliedCoupon: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: '#e8f5e9', border: '1px solid #2e7d32', borderRadius: '8px', padding: '10px 14px', fontSize: '13px', color: '#1b5e20' },
  removeCouponBtn: { background: 'none', border: 'none', color: '#c62828', cursor: 'pointer', fontWeight: '700', fontSize: '12px' },
  couponHints: { display: 'flex', gap: '6px', marginTop: '10px', flexWrap: 'wrap' },
  hintChip: { background: '#f5f5f5', border: '1px dashed #ccc', borderRadius: '20px', padding: '4px 12px', fontSize: '11px', cursor: 'pointer', color: '#555', fontWeight: '600' },

  summary: { background: '#fff', borderRadius: '12px', padding: '20px', border: '1px solid #e8e8e8', position: 'sticky', top: '110px' },
  summaryTitle: { fontSize: '16px', fontWeight: '700', color: '#111', margin: '0 0 16px', paddingBottom: '12px', borderBottom: '1px solid #f0f0f0' },
  summaryRow: { display: 'flex', justifyContent: 'space-between', padding: '8px 0', fontSize: '14px', color: '#555', borderBottom: '1px solid #f9f9f9' },
  totalRow: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '14px 0' },
  totalLabel: { fontSize: '15px', fontWeight: '700', color: '#111' },
  totalAmount: { fontSize: '22px', fontWeight: '900', color: '#c62828' },
  checkoutBtn: { width: '100%', padding: '14px', background: '#c62828', color: '#fff', border: 'none', borderRadius: '10px', fontSize: '15px', fontWeight: '800', cursor: 'pointer', marginBottom: '12px' },
  secureRow: { display: 'flex', justifyContent: 'space-around', fontSize: '12px', color: '#999', marginBottom: '12px' },
  paymentMethods: { display: 'flex', gap: '6px', flexWrap: 'wrap' },
  payMethod: { background: '#f5f5f5', borderRadius: '6px', padding: '6px 10px', fontSize: '11px', color: '#555' },

  emptyWrap: { maxWidth: '400px', margin: '80px auto', textAlign: 'center', background: '#fff', borderRadius: '16px', padding: '48px 32px', boxShadow: '0 4px 20px rgba(0,0,0,0.06)' },
  emptyIcon: { fontSize: '80px', marginBottom: '16px' },
  emptyTitle: { fontSize: '22px', fontWeight: '700', color: '#111', margin: '0 0 8px' },
  emptySub: { color: '#999', fontSize: '15px', margin: '0 0 24px' },
  shopBtn: { background: '#c62828', color: '#fff', border: 'none', padding: '12px 32px', borderRadius: '10px', fontSize: '15px', fontWeight: '700', cursor: 'pointer', marginBottom: '16px', display: 'block', width: '100%' },
  emptyHints: { display: 'flex', gap: '10px', justifyContent: 'center' },
  hintItem: { background: '#f5f5f5', padding: '8px 16px', borderRadius: '20px', fontSize: '13px', cursor: 'pointer', color: '#555', fontWeight: '500' },
  loading: { textAlign: 'center', padding: '100px', fontSize: '18px', color: '#999' },
};