import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { productService, categoryService } from '../services/productService';

const ADMIN_PASSWORD = 'admin123';

export default function AdminPage() {
  const navigate = useNavigate();
  const [authed, setAuthed] = useState(false);
  const [adminPass, setAdminPass] = useState('');
  const [authError, setAuthError] = useState('');

  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState(null);
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState({ name: '', description: '', price: '', stock: '', categoryId: '', imageUrl: '' });
  const [searchQ, setSearchQ] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const showToast = (msg, type = 'success') => {
    setToast({ msg, type });
    setTimeout(() => setToast(null), 3000);
  };

  const fetchProducts = async () => {
    setLoading(true);
    try {
      const params = { page, size: 12, ...(searchQ && { keyword: searchQ }) };
      const res = await productService.getAll(params);
      const data = res.data.data;
      setProducts(data.content || []);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch { showToast('Ürünler yüklenemedi', 'error'); }
    finally { setLoading(false); }
  };

  useEffect(() => {
    if (authed) {
      categoryService.getAll().then(res => setCategories(res.data.data || [])).catch(() => {});
    }
  }, [authed]);

  useEffect(() => { if (authed) fetchProducts(); }, [page, searchQ, authed]);

  const handleLogin = () => {
    if (adminPass === ADMIN_PASSWORD) {
      setAuthed(true);
      setAuthError('');
    } else {
      setAuthError('Hatalı şifre!');
    }
  };

  const openAdd = () => {
    setForm({ name: '', description: '', price: '', stock: '', categoryId: categories[0]?.id || '', imageUrl: '' });
    setModal({ type: 'add' });
  };

  const openEdit = (p) => {
    setForm({
      name: p.name, description: p.description, price: p.price,
      stock: p.stock, categoryId: categories.find(c => c.name === p.categoryName)?.id || p.categoryId || '',
      imageUrl: p.imageUrl || ''
    });
    setModal({ type: 'edit', product: p });
  };

  const handleSave = async () => {
    try {
      const data = {
        ...form,
        price: parseFloat(form.price),
        stock: parseInt(form.stock),
        categoryId: parseInt(form.categoryId)
      };
      if (modal.type === 'add') {
        await productService.create(data);
        showToast('Ürün eklendi ✓');
      } else {
        await productService.update(modal.product.id, data);
        showToast('Ürün güncellendi ✓');
      }
      setModal(null);
      fetchProducts();
    } catch { showToast('Hata oluştu', 'error'); }
  };

  const handleDelete = async () => {
    try {
      await productService.delete(modal.product.id);
      showToast('Ürün silindi ✓');
      setModal(null);
      fetchProducts();
    } catch { showToast('Silinemedi', 'error'); }
  };

  // Giriş ekranı
  if (!authed) {
    return (
      <div style={s.loginPage}>
        <div style={s.loginCard}>
          <div style={s.loginIcon}>⚙️</div>
          <h2 style={s.loginTitle}>Admin Paneli</h2>
          <p style={s.loginSub}>Devam etmek için admin şifresini girin</p>
          {authError && <div style={s.loginError}>{authError}</div>}
          <input
            style={s.loginInput}
            type="password"
            placeholder="Admin şifresi"
            value={adminPass}
            onChange={e => setAdminPass(e.target.value)}
            onKeyDown={e => e.key === 'Enter' && handleLogin()}
            autoFocus
          />
          <button style={s.loginBtn} onClick={handleLogin}>Giriş Yap</button>
          <button style={s.backLink} onClick={() => navigate('/')}>← Siteye Dön</button>
        </div>
      </div>
    );
  }

  return (
    <div style={s.page}>
      {/* Header */}
      <div style={s.header}>
        <div style={s.headerLeft}>
          <span style={s.logo}>⚙️ Admin Panel</span>
          <span style={s.badge}>{totalElements} ürün</span>
        </div>
        <div style={s.headerRight}>
          <button style={s.siteBtn} onClick={() => navigate('/')}>← Siteye Dön</button>
          <button style={s.logoutBtn} onClick={() => setAuthed(false)}>Çıkış</button>
        </div>
      </div>

      <div style={s.container}>
        {/* Stats */}
        <div style={s.stats}>
          {[
            { label: 'Toplam Ürün', value: totalElements, icon: '📦', color: '#1565c0' },
            { label: 'Kategori', value: categories.length, icon: '🏷️', color: '#2e7d32' },
            { label: 'Sayfa', value: `${page + 1}/${totalPages}`, icon: '📄', color: '#c62828' },
          ].map(st => (
            <div key={st.label} style={s.statCard}>
              <span style={s.statIcon}>{st.icon}</span>
              <div>
                <div style={{ ...s.statValue, color: st.color }}>{st.value}</div>
                <div style={s.statLabel}>{st.label}</div>
              </div>
            </div>
          ))}
        </div>

        {/* Toolbar */}
        <div style={s.toolbar}>
          <input
            style={s.searchInput}
            placeholder="🔍 Ürün ara..."
            value={searchQ}
            onChange={e => { setSearchQ(e.target.value); setPage(0); }}
          />
          <button style={s.addBtn} onClick={openAdd}>+ Yeni Ürün Ekle</button>
        </div>

        {/* Table */}
        <div style={s.tableWrap}>
          <table style={s.table}>
            <thead>
              <tr style={s.theadRow}>
                <th style={s.th}>Görsel</th>
                <th style={s.th}>Ürün Adı</th>
                <th style={s.th}>Kategori</th>
                <th style={s.th}>Fiyat</th>
                <th style={s.th}>Stok</th>
                <th style={s.th}>İşlemler</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={6} style={s.loadingCell}>Yükleniyor...</td></tr>
              ) : products.length === 0 ? (
                <tr><td colSpan={6} style={s.loadingCell}>Ürün bulunamadı</td></tr>
              ) : products.map(p => (
                <tr key={p.id} style={s.tr}>
                  <td style={s.td}>
                    <div style={s.imgCell}>
                      {p.imageUrl
                        ? <img src={p.imageUrl} alt={p.name} style={s.tableImg} />
                        : <span style={{ fontSize: '24px' }}>📦</span>}
                    </div>
                  </td>
                  <td style={s.td}>
                    <div style={s.productName}>{p.name}</div>
                    <div style={s.productDesc}>{p.description?.slice(0, 50)}...</div>
                  </td>
                  <td style={s.td}>
                    <span style={s.catBadge}>{p.categoryName}</span>
                  </td>
                  <td style={s.td}>
                    <span style={s.price}>{p.price?.toLocaleString('tr-TR')} ₺</span>
                  </td>
                  <td style={s.td}>
                    <span style={{ color: p.stock < 10 ? '#c62828' : '#2e7d32', fontWeight: '600' }}>
                      {p.stock < 10 ? '⚠️ ' : ''}{p.stock}
                    </span>
                  </td>
                  <td style={s.td}>
                    <div style={s.actions}>
                      <button style={s.editBtn} onClick={() => openEdit(p)}>✏️</button>
                      <button style={s.deleteBtn} onClick={() => setModal({ type: 'delete', product: p })}>🗑️</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div style={s.pagination}>
            <button style={s.pageBtn} disabled={page === 0} onClick={() => setPage(p => p - 1)}>← Önceki</button>
            {[...Array(Math.min(totalPages, 7))].map((_, i) => (
              <button key={i} style={{ ...s.pageNum, ...(i === page ? s.pageActive : {}) }} onClick={() => setPage(i)}>{i + 1}</button>
            ))}
            <button style={s.pageBtn} disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>Sonraki →</button>
          </div>
        )}
      </div>

      {/* Modal Ekle/Düzenle */}
      {modal && (modal.type === 'add' || modal.type === 'edit') && (
        <div style={s.overlay} onClick={() => setModal(null)}>
          <div style={s.modal} onClick={e => e.stopPropagation()}>
            <h2 style={s.modalTitle}>{modal.type === 'add' ? '+ Yeni Ürün' : '✏️ Ürün Düzenle'}</h2>
            <div style={s.formGrid}>
              <div style={s.formField}>
                <label style={s.label}>Ürün Adı *</label>
                <input style={s.input} value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} />
              </div>
              <div style={s.formField}>
                <label style={s.label}>Kategori *</label>
                <select style={s.input} value={form.categoryId} onChange={e => setForm(f => ({ ...f, categoryId: e.target.value }))}>
                  {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
              <div style={s.formField}>
                <label style={s.label}>Fiyat (₺) *</label>
                <input style={s.input} type="number" value={form.price} onChange={e => setForm(f => ({ ...f, price: e.target.value }))} />
              </div>
              <div style={s.formField}>
                <label style={s.label}>Stok *</label>
                <input style={s.input} type="number" value={form.stock} onChange={e => setForm(f => ({ ...f, stock: e.target.value }))} />
              </div>
              <div style={{ ...s.formField, gridColumn: '1/-1' }}>
                <label style={s.label}>Açıklama</label>
                <textarea style={{ ...s.input, height: '80px', resize: 'vertical' }} value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} />
              </div>
              <div style={{ ...s.formField, gridColumn: '1/-1' }}>
                <label style={s.label}>Görsel URL</label>
                <input style={s.input} value={form.imageUrl} onChange={e => setForm(f => ({ ...f, imageUrl: e.target.value }))} placeholder="https://..." />
                {form.imageUrl && <img src={form.imageUrl} alt="preview" style={s.preview} onError={e => e.target.style.display = 'none'} />}
              </div>
            </div>
            <div style={s.modalActions}>
              <button style={s.cancelBtn} onClick={() => setModal(null)}>İptal</button>
              <button style={s.saveBtn} onClick={handleSave}>{modal.type === 'add' ? '+ Ekle' : '✓ Güncelle'}</button>
            </div>
          </div>
        </div>
      )}

      {/* Modal Sil */}
      {modal?.type === 'delete' && (
        <div style={s.overlay} onClick={() => setModal(null)}>
          <div style={{ ...s.modal, maxWidth: '400px' }} onClick={e => e.stopPropagation()}>
            <h2 style={s.modalTitle}>🗑️ Ürünü Sil</h2>
            <p style={{ fontSize: '15px', color: '#444', margin: '0 0 24px', lineHeight: 1.6 }}>
              <strong>{modal.product.name}</strong> silinecek. Bu işlem geri alınamaz.
            </p>
            <div style={s.modalActions}>
              <button style={s.cancelBtn} onClick={() => setModal(null)}>İptal</button>
              <button style={{ ...s.saveBtn, background: '#c62828' }} onClick={handleDelete}>Evet, Sil</button>
            </div>
          </div>
        </div>
      )}

      {toast && (
        <div style={{ ...s.toast, background: toast.type === 'error' ? '#c62828' : '#2e7d32' }}>
          {toast.msg}
        </div>
      )}
    </div>
  );
}

const s = {
  // Login
  loginPage: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#111' },
  loginCard: { background: '#fff', borderRadius: '20px', padding: '48px 40px', width: '100%', maxWidth: '380px', textAlign: 'center' },
  loginIcon: { fontSize: '48px', marginBottom: '16px' },
  loginTitle: { fontSize: '24px', fontWeight: '800', color: '#111', margin: '0 0 8px' },
  loginSub: { fontSize: '14px', color: '#999', margin: '0 0 24px' },
  loginError: { background: '#ffebee', color: '#c62828', padding: '10px', borderRadius: '8px', marginBottom: '16px', fontSize: '13px' },
  loginInput: { width: '100%', padding: '12px 16px', border: '1.5px solid #e0e0e0', borderRadius: '10px', fontSize: '15px', outline: 'none', marginBottom: '12px', boxSizing: 'border-box' },
  loginBtn: { width: '100%', padding: '13px', background: '#111', color: '#fff', border: 'none', borderRadius: '10px', fontSize: '15px', fontWeight: '700', cursor: 'pointer', marginBottom: '12px' },
  backLink: { background: 'none', border: 'none', color: '#999', cursor: 'pointer', fontSize: '13px' },

  // Admin
  page: { background: '#f2f3f5', minHeight: '100vh' },
  header: { background: '#111', padding: '16px 32px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' },
  headerLeft: { display: 'flex', alignItems: 'center', gap: '16px' },
  headerRight: { display: 'flex', gap: '10px' },
  logo: { fontSize: '18px', fontWeight: '800', color: '#fff' },
  badge: { background: '#c62828', color: '#fff', padding: '4px 12px', borderRadius: '20px', fontSize: '12px', fontWeight: '700' },
  siteBtn: { background: 'rgba(255,255,255,0.1)', border: '1px solid rgba(255,255,255,0.2)', color: '#fff', padding: '8px 16px', borderRadius: '8px', cursor: 'pointer', fontSize: '13px' },
  logoutBtn: { background: '#c62828', border: 'none', color: '#fff', padding: '8px 16px', borderRadius: '8px', cursor: 'pointer', fontSize: '13px', fontWeight: '600' },
  container: { maxWidth: '1400px', margin: '0 auto', padding: '24px' },
  stats: { display: 'flex', gap: '16px', marginBottom: '20px' },
  statCard: { background: '#fff', borderRadius: '12px', padding: '20px 24px', display: 'flex', alignItems: 'center', gap: '16px', flex: 1, border: '1px solid #e8e8e8' },
  statIcon: { fontSize: '32px' },
  statValue: { fontSize: '28px', fontWeight: '800' },
  statLabel: { fontSize: '12px', color: '#999' },
  toolbar: { display: 'flex', gap: '12px', marginBottom: '16px' },
  searchInput: { flex: 1, padding: '12px 16px', border: '1.5px solid #e0e0e0', borderRadius: '10px', fontSize: '14px', outline: 'none' },
  addBtn: { background: '#c62828', color: '#fff', border: 'none', padding: '12px 24px', borderRadius: '10px', fontWeight: '700', cursor: 'pointer', whiteSpace: 'nowrap' },
  tableWrap: { background: '#fff', borderRadius: '12px', overflow: 'hidden', border: '1px solid #e8e8e8' },
  table: { width: '100%', borderCollapse: 'collapse' },
  theadRow: { background: '#f8f8f8', borderBottom: '2px solid #e8e8e8' },
  th: { padding: '14px 16px', textAlign: 'left', fontSize: '12px', fontWeight: '700', color: '#666', textTransform: 'uppercase', letterSpacing: '0.5px' },
  tr: { borderBottom: '1px solid #f5f5f5' },
  td: { padding: '14px 16px', verticalAlign: 'middle' },
  imgCell: { width: '60px', height: '60px', borderRadius: '8px', overflow: 'hidden', background: '#f8f8f8', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  tableImg: { width: '60px', height: '60px', objectFit: 'cover' },
  productName: { fontSize: '14px', fontWeight: '600', color: '#111', marginBottom: '4px' },
  productDesc: { fontSize: '12px', color: '#999' },
  catBadge: { background: '#e3f2fd', color: '#1565c0', padding: '4px 10px', borderRadius: '20px', fontSize: '11px', fontWeight: '600' },
  price: { fontSize: '15px', fontWeight: '700', color: '#c62828' },
  actions: { display: 'flex', gap: '6px' },
  editBtn: { background: '#e3f2fd', border: 'none', color: '#1565c0', padding: '8px 12px', borderRadius: '8px', cursor: 'pointer', fontSize: '16px' },
  deleteBtn: { background: '#ffebee', border: 'none', color: '#c62828', padding: '8px 12px', borderRadius: '8px', cursor: 'pointer', fontSize: '16px' },
  loadingCell: { textAlign: 'center', padding: '48px', color: '#999' },
  pagination: { display: 'flex', justifyContent: 'center', gap: '8px', marginTop: '20px' },
  pageBtn: { padding: '8px 16px', border: '1px solid #e0e0e0', borderRadius: '8px', background: '#fff', cursor: 'pointer', fontSize: '13px', fontWeight: '600' },
  pageNum: { width: '36px', height: '36px', border: '1px solid #e0e0e0', borderRadius: '8px', background: '#fff', cursor: 'pointer', fontSize: '13px' },
  pageActive: { background: '#c62828', color: '#fff', borderColor: '#c62828' },
  overlay: { position: 'fixed', inset: 0, background: 'rgba(0,0,0,0.5)', zIndex: 2000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '24px' },
  modal: { background: '#fff', borderRadius: '16px', padding: '32px', width: '100%', maxWidth: '600px', maxHeight: '90vh', overflowY: 'auto' },
  modalTitle: { fontSize: '20px', fontWeight: '800', color: '#111', margin: '0 0 24px' },
  formGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '24px' },
  formField: { display: 'flex', flexDirection: 'column', gap: '6px' },
  label: { fontSize: '13px', fontWeight: '600', color: '#333' },
  input: { border: '1.5px solid #e0e0e0', borderRadius: '8px', padding: '10px 12px', fontSize: '14px', outline: 'none', width: '100%', boxSizing: 'border-box' },
  preview: { width: '80px', height: '80px', objectFit: 'cover', borderRadius: '8px', marginTop: '8px' },
  modalActions: { display: 'flex', justifyContent: 'flex-end', gap: '12px' },
  cancelBtn: { background: '#f5f5f5', border: 'none', color: '#333', padding: '12px 24px', borderRadius: '10px', cursor: 'pointer', fontWeight: '600' },
  saveBtn: { background: '#2e7d32', color: '#fff', border: 'none', padding: '12px 28px', borderRadius: '10px', cursor: 'pointer', fontWeight: '700' },
  toast: { position: 'fixed', bottom: '32px', right: '32px', color: '#fff', padding: '14px 24px', borderRadius: '12px', fontSize: '14px', fontWeight: '600', zIndex: 3000 },
};