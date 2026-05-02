import { Link } from 'react-router-dom';

export default function Footer() {
  return (
    <footer style={styles.footer}>
      <div style={styles.inner}>
        {/* Top */}
        <div style={styles.top}>
          {/* Logo & Desc */}
          <div style={styles.brand}>
            <div style={styles.logo}>
              <span style={styles.logoMark}>T</span>
              <span style={styles.logoText}>Market</span>
              <span style={styles.logoPro}>PRO</span>
            </div>
            <p style={styles.desc}>
              Türkiye'nin en güvenilir e-ticaret platformu. Binlerce ürün, en iyi fiyatlar.
            </p>
            <div style={styles.socials}>
              {['Instagram', 'Twitter', 'Facebook', 'YouTube'].map(s => (
                <a key={s} href="#" style={styles.socialLink}>{s[0]}</a>
              ))}
            </div>
          </div>

          {/* Links */}
          <div style={styles.links}>
            <div style={styles.linkCol}>
              <h4 style={styles.colTitle}>Alışveriş</h4>
              {['Elektronik', 'Giyim & Moda', 'Ev & Yaşam', 'Spor & Outdoor'].map(l => (
                <Link key={l} to={`/?category=${encodeURIComponent(l)}`} style={styles.link}>{l}</Link>
              ))}
            </div>
            <div style={styles.linkCol}>
              <h4 style={styles.colTitle}>Hesabım</h4>
              {[
                { label: 'Giriş Yap', to: '/login' },
                { label: 'Siparişlerim', to: '/orders' },
                { label: 'Sepetim', to: '/cart' },
              ].map(l => (
                <Link key={l.label} to={l.to} style={styles.link}>{l.label}</Link>
              ))}
            </div>
            <div style={styles.linkCol}>
              <h4 style={styles.colTitle}>Yardım</h4>
              {['SSS', 'İade & Değişim', 'Kargo Takip', 'İletişim'].map(l => (
                <a key={l} href="#" style={styles.link}>{l}</a>
              ))}
            </div>
          </div>
        </div>

        {/* Divider */}
        <div style={styles.divider} />

        {/* Bottom */}
        <div style={styles.bottom}>
          <span style={styles.copy}>© 2026 TMarket Pro. Tüm hakları saklıdır.</span>
          <div style={styles.payments}>
            {['💳 Kredi Kartı', '🏦 Havale', '💰 Kapıda Ödeme'].map(p => (
              <span key={p} style={styles.paymentBadge}>{p}</span>
            ))}
          </div>
        </div>
      </div>
    </footer>
  );
}

const styles = {
  footer: {
    background: '#111',
    color: '#fff',
    marginTop: '80px',
  },
  inner: {
    maxWidth: '1280px',
    margin: '0 auto',
    padding: '60px 24px 32px',
  },
  top: {
    display: 'flex',
    gap: '60px',
    flexWrap: 'wrap',
  },
  brand: { flex: '0 0 280px' },
  logo: { display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '16px' },
  logoMark: {
    width: '36px',
    height: '36px',
    background: '#c62828',
    color: '#fff',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    borderRadius: '10px',
    fontWeight: '800',
    fontSize: '20px',
    fontFamily: 'Georgia, serif',
  },
  logoText: {
    fontSize: '20px',
    fontWeight: '800',
    color: '#fff',
    fontFamily: 'Georgia, serif',
  },
  logoPro: {
    fontSize: '9px',
    fontWeight: '700',
    color: '#fff',
    background: '#c62828',
    padding: '2px 5px',
    borderRadius: '4px',
    alignSelf: 'flex-start',
    marginTop: '2px',
  },
  desc: {
    color: 'rgba(255,255,255,0.5)',
    fontSize: '14px',
    lineHeight: '1.6',
    marginBottom: '20px',
  },
  socials: { display: 'flex', gap: '10px' },
  socialLink: {
    width: '36px',
    height: '36px',
    borderRadius: '50%',
    background: 'rgba(255,255,255,0.1)',
    color: '#fff',
    textDecoration: 'none',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '13px',
    fontWeight: '700',
  },
  links: { flex: 1, display: 'flex', gap: '40px', flexWrap: 'wrap' },
  linkCol: { display: 'flex', flexDirection: 'column', gap: '10px', minWidth: '120px' },
  colTitle: {
    fontSize: '12px',
    fontWeight: '700',
    letterSpacing: '1px',
    textTransform: 'uppercase',
    color: 'rgba(255,255,255,0.4)',
    margin: '0 0 4px',
  },
  link: {
    color: 'rgba(255,255,255,0.7)',
    textDecoration: 'none',
    fontSize: '14px',
    transition: 'color 0.2s',
  },
  divider: {
    height: '1px',
    background: 'rgba(255,255,255,0.08)',
    margin: '40px 0 24px',
  },
  bottom: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    flexWrap: 'wrap',
    gap: '12px',
  },
  copy: { color: 'rgba(255,255,255,0.4)', fontSize: '13px' },
  payments: { display: 'flex', gap: '8px' },
  paymentBadge: {
    background: 'rgba(255,255,255,0.08)',
    padding: '6px 12px',
    borderRadius: '8px',
    fontSize: '12px',
    color: 'rgba(255,255,255,0.6)',
  },
};