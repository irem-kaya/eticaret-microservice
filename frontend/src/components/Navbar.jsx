import { useState, useRef } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../context/CartContext';
import { useAuth } from '../context/AuthContext';

const MEGA_MENU = {
  'Elektronik': {
    sections: [
      { title: 'Telefon & Tablet', items: ['Akıllı Telefon', 'Tablet', 'Telefon Kılıfı', 'Şarj Aleti', 'Kulaklık'] },
      { title: 'Bilgisayar', items: ['Laptop', 'Masaüstü', 'Monitör', 'Klavye & Mouse', 'SSD & HDD'] },
      { title: 'Ev Elektroniği', items: ['TV', 'Ses Sistemi', 'Projeksiyon', 'Akıllı Saat', 'Drone'] },
    ]
  },
  'Giyim & Moda': {
    sections: [
      { title: 'Kadın', items: ['Elbise', 'Bluz', 'Pantolon', 'Etek', 'Ceket'] },
      { title: 'Erkek', items: ['Gömlek', 'Tişört', 'Jean', 'Takım Elbise', 'Spor Giyim'] },
      { title: 'Aksesuar', items: ['Çanta', 'Kemer', 'Şapka', 'Atkı & Eldiven', 'Gözlük'] },
    ]
  },
  'Ev & Yaşam': {
    sections: [
      { title: 'Mutfak', items: ['Tencere & Tava', 'Kahve Makinesi', 'Blender', 'Fırın', 'Bulaşık Makinesi'] },
      { title: 'Mobilya', items: ['Koltuk', 'Yatak', 'Masa & Sandalye', 'Raf & Dolap', 'Aydınlatma'] },
      { title: 'Dekorasyon', items: ['Tablo', 'Çiçek & Saksı', 'Heykel', 'Yastık & Battaniye', 'Halı'] },
    ]
  },
  'Spor & Outdoor': {
    sections: [
      { title: 'Spor Giyim', items: ['Eşofman', 'Spor Ayakkabı', 'Mayo', 'Tayt', 'Spor Çorap'] },
      { title: 'Ekipman', items: ['Dambıl', 'Yoga Matı', 'Bisiklet', 'Kamp Malzemeleri', 'Çadır'] },
      { title: 'Top Sporları', items: ['Futbol', 'Basketbol', 'Tenis', 'Voleybol', 'Yüzme'] },
    ]
  },
  'Kadın': {
    sections: [
      { title: 'Giyim', items: ['Elbise', 'Bluz', 'Pantolon', 'Etek', 'Ceket'] },
      { title: 'Ayakkabı & Çanta', items: ['Topuklu', 'Sneaker', 'Bot', 'Çanta', 'Cüzdan'] },
      { title: 'Kozmetik', items: ['Parfüm', 'Makyaj', 'Cilt Bakım', 'Saç Bakım', 'El Bakım'] },
    ]
  },
  'Erkek': {
    sections: [
      { title: 'Giyim', items: ['Gömlek', 'Tişört', 'Jean', 'Kazak', 'Ceket'] },
      { title: 'Ayakkabı', items: ['Sneaker', 'Bot', 'Loafer', 'Sandalet', 'Spor Ayakkabı'] },
      { title: 'Aksesuar', items: ['Kravat', 'Kemer', 'Saat', 'Cüzdan', 'Gözlük'] },
    ]
  },
};

const CATEGORIES = [
  'Kadın', 'Erkek', 'Elektronik', 'Ev & Yaşam', 'Spor & Outdoor',
  'Giyim & Moda', 'Kozmetik', 'Ayakkabı', 'Saat & Aksesuar', 'Anne & Bebek'
];

export default function Navbar() {
  const { cart } = useCart();
  const { isLoggedIn, logout } = useAuth();
  const navigate = useNavigate();
  const [searchQuery, setSearchQuery] = useState('');
  const [menuOpen, setMenuOpen] = useState(false);
  const [activeMega, setActiveMega] = useState(null);
  const megaTimeout = useRef(null);

  const handleSearch = (e) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/?keyword=${encodeURIComponent(searchQuery)}`);
      setSearchQuery('');
    }
  };

  const handleCatEnter = (cat) => {
    clearTimeout(megaTimeout.current);
    if (MEGA_MENU[cat]) setActiveMega(cat);
    else setActiveMega(null);
  };

  const handleCatLeave = () => {
    megaTimeout.current = setTimeout(() => setActiveMega(null), 200);
  };

  const handleMegaEnter = () => {
    clearTimeout(megaTimeout.current);
  };

  return (
    <>
      <nav style={s.nav}>
        {/* Top Row */}
        <div style={s.topRow}>
          <div style={s.topInner}>
            <Link to="/" style={s.logo}>
              <span style={s.logoMark}>T</span>
              <span style={s.logoText}>Market</span>
              <span style={s.logoPro}>PRO</span>
            </Link>

            <form onSubmit={handleSearch} style={s.searchBar}>
              <svg width="18" height="18" fill="none" stroke="#999" strokeWidth="2" viewBox="0 0 24 24" style={{ flexShrink: 0 }}>
                <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
              </svg>
              <input
                style={s.searchInput}
                placeholder="Ürün, marka veya kategori ara..."
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
              />
              <button type="submit" style={s.searchBtn}>Ara</button>
            </form>

            <div style={s.actions}>
              {isLoggedIn ? (
                <div style={{ position: 'relative' }}>
                  <button style={s.actionBtn} onClick={() => setMenuOpen(!menuOpen)}>
                    <span style={s.actionIcon}>👤</span>
                    <span style={s.actionLabel}>Hesabım</span>
                  </button>
                  {menuOpen && (
                    <div style={s.dropdown}>
                      <Link to="/orders" style={s.dropItem} onClick={() => setMenuOpen(false)}>📦 Siparişlerim</Link>
                      <button style={s.dropItemBtn} onClick={() => { logout(); setMenuOpen(false); }}>🚪 Çıkış Yap</button>
                    </div>
                  )}
                </div>
              ) : (
                <Link to="/login" style={{ ...s.actionBtn, textDecoration: 'none' }}>
                  <span style={s.actionIcon}>👤</span>
                  <span style={s.actionLabel}>Giriş Yap</span>
                </Link>
              )}

              <Link to="/cart" style={{ ...s.actionBtn, position: 'relative', textDecoration: 'none', color: '#333' }}>
                <span style={s.actionIcon}>🛒</span>
                <span style={s.actionLabel}>Sepetim</span>
                {cart.totalItems > 0 && <span style={s.badge}>{cart.totalItems}</span>}
              </Link>

            </div>
          </div>
        </div>

        {/* Categories Row */}
        <div style={s.catRow}>
          <div style={s.catInner}>
            {CATEGORIES.map(cat => (
              <div
                key={cat}
                style={s.catItem}
                onMouseEnter={() => handleCatEnter(cat)}
                onMouseLeave={handleCatLeave}
              >
                <Link
                  to={`/?category=${encodeURIComponent(cat)}`}
                  style={{
                    ...s.catLink,
                    ...(activeMega === cat ? s.catLinkActive : {})
                  }}
                >
                  {cat}
                  {MEGA_MENU[cat] && <span style={s.catArrow}>▾</span>}
                </Link>
              </div>
            ))}
          </div>
        </div>

        {/* Mega Menu */}
        {activeMega && MEGA_MENU[activeMega] && (
          <div
            style={s.megaMenu}
            onMouseEnter={handleMegaEnter}
            onMouseLeave={handleCatLeave}
          >
            <div style={s.megaInner}>
              {MEGA_MENU[activeMega].sections.map(section => (
                <div key={section.title} style={s.megaSection}>
                  <div style={s.megaSectionTitle}
                    onClick={() => { navigate(`/?category=${encodeURIComponent(activeMega)}`); setActiveMega(null); }}
                  >
                    {section.title} ›
                  </div>
                  {section.items.map(item => (
                    <div
                      key={item}
                      style={s.megaItem}
                      onClick={() => { navigate(`/?keyword=${encodeURIComponent(item)}`); setActiveMega(null); }}
                      onMouseEnter={e => e.currentTarget.style.color = '#c62828'}
                      onMouseLeave={e => e.currentTarget.style.color = '#444'}
                    >
                      {item}
                    </div>
                  ))}
                </div>
              ))}
            </div>
          </div>
        )}
      </nav>
      <div style={s.spacer} />
    </>
  );
}

const s = {
  nav: { position: 'fixed', top: 0, left: 0, right: 0, zIndex: 100, background: '#fff', boxShadow: '0 2px 8px rgba(0,0,0,0.08)' },
  topRow: { borderBottom: '1px solid #f0f0f0' },
  topInner: { maxWidth: '1280px', margin: '0 auto', padding: '12px 24px', display: 'flex', alignItems: 'center', gap: '16px' },
  logo: { textDecoration: 'none', display: 'flex', alignItems: 'center', gap: '4px', flexShrink: 0 },
  logoMark: { width: '36px', height: '36px', background: '#c62828', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '10px', fontWeight: '800', fontSize: '20px', fontFamily: 'Georgia,serif' },
  logoText: { fontSize: '20px', fontWeight: '800', color: '#111', fontFamily: 'Georgia,serif' },
  logoPro: { fontSize: '9px', fontWeight: '700', color: '#fff', background: '#c62828', padding: '2px 5px', borderRadius: '4px', alignSelf: 'flex-start', marginTop: '2px' },
  searchBar: { flex: 1, display: 'flex', alignItems: 'center', gap: '10px', border: '2px solid #e0e0e0', borderRadius: '8px', padding: '9px 14px', background: '#fafafa' },
  searchInput: { flex: 1, border: 'none', outline: 'none', fontSize: '14px', color: '#111', background: 'none' },
  searchBtn: { background: '#c62828', color: '#fff', border: 'none', padding: '6px 20px', borderRadius: '6px', fontWeight: '700', cursor: 'pointer', fontSize: '13px', flexShrink: 0 },
  actions: { display: 'flex', gap: '4px', alignItems: 'center', flexShrink: 0 },
  actionBtn: { display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '2px', padding: '6px 12px', background: 'none', border: 'none', cursor: 'pointer', color: '#333', borderRadius: '8px' },
  actionIcon: { fontSize: '20px' },
  actionLabel: { fontSize: '11px', color: '#555', fontWeight: '500', whiteSpace: 'nowrap' },
  badge: { position: 'absolute', top: '2px', right: '2px', background: '#c62828', color: '#fff', fontSize: '10px', fontWeight: '700', width: '18px', height: '18px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  dropdown: { position: 'absolute', top: '52px', right: 0, background: '#fff', border: '1px solid #f0f0f0', borderRadius: '12px', boxShadow: '0 8px 30px rgba(0,0,0,0.12)', minWidth: '160px', overflow: 'hidden', zIndex: 200 },
  dropItem: { display: 'block', padding: '12px 16px', textDecoration: 'none', color: '#333', fontSize: '14px' },
  dropItemBtn: { display: 'block', width: '100%', padding: '12px 16px', background: 'none', border: 'none', textAlign: 'left', color: '#c62828', fontSize: '14px', cursor: 'pointer' },

  catRow: { background: '#fff' },
  catInner: { maxWidth: '1280px', margin: '0 auto', padding: '0 24px', display: 'flex', overflowX: 'auto' },
  catItem: { position: 'relative' },
  catLink: { textDecoration: 'none', color: '#333', fontSize: '13px', fontWeight: '500', padding: '10px 16px', whiteSpace: 'nowrap', borderBottom: '2px solid transparent', display: 'flex', alignItems: 'center', gap: '4px', transition: 'all 0.15s' },
  catLinkActive: { color: '#c62828', borderBottom: '2px solid #c62828' },
  catArrow: { fontSize: '10px', opacity: 0.6 },

  // Mega Menu
  megaMenu: {
    position: 'absolute', top: '100%', left: 0, right: 0,
    background: '#fff', boxShadow: '0 8px 32px rgba(0,0,0,0.12)',
    borderTop: '2px solid #c62828', zIndex: 99,
  },
  megaInner: {
    maxWidth: '1280px', margin: '0 auto', padding: '28px 24px',
    display: 'flex', gap: '48px',
  },
  megaSection: { flex: 1, minWidth: '160px' },
  megaSectionTitle: {
    fontSize: '14px', fontWeight: '700', color: '#c62828',
    marginBottom: '12px', cursor: 'pointer', paddingBottom: '8px',
    borderBottom: '1px solid #f0f0f0',
  },
  megaItem: {
    fontSize: '13px', color: '#444', padding: '6px 0',
    cursor: 'pointer', transition: 'color 0.15s',
  },

  spacer: { height: '96px' },
};