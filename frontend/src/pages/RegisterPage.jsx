import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';

const KEYCLOAK_URL = 'http://localhost:8180';
const REALM = 'eticaret';

export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    firstName: '', lastName: '', username: '', email: '', password: '', confirmPassword: ''
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);
  const [showPass, setShowPass] = useState(false);

  const handleChange = e => setForm(f => ({ ...f, [e.target.name]: e.target.value }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    if (form.password !== form.confirmPassword) {
      setError('Şifreler eşleşmiyor'); return;
    }
    if (form.password.length < 6) {
      setError('Şifre en az 6 karakter olmalı'); return;
    }

    setLoading(true);
    try {
      // Admin token al
      const tokenRes = await fetch(
        `${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token`,
        {
          method: 'POST',
          headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
          body: 'grant_type=password&client_id=admin-cli&username=admin&password=admin123'
        }
      );
      const tokenData = await tokenRes.json();
      const adminToken = tokenData.access_token;

      // Kullanıcı oluştur
      const userRes = await fetch(
        `${KEYCLOAK_URL}/admin/realms/${REALM}/users`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${adminToken}`
          },
          body: JSON.stringify({
            username: form.username,
            email: form.email,
            firstName: form.firstName,
            lastName: form.lastName,
            enabled: true,
            emailVerified: true,
            credentials: [{
              type: 'password',
              value: form.password,
              temporary: false
            }]
          })
        }
      );

      if (userRes.status === 201) {
        setSuccess(true);
        setTimeout(() => navigate('/login'), 2000);
      } else if (userRes.status === 409) {
        setError('Bu kullanıcı adı veya e-posta zaten kullanılıyor');
      } else {
        const errData = await userRes.json();
        setError(errData.errorMessage || 'Kayıt başarısız');
      }
    } catch (err) {
      setError('Bağlantı hatası. Lütfen tekrar deneyin.');
    } finally {
      setLoading(false);
    }
  };

  if (success) {
    return (
      <div style={s.page}>
        <div style={s.card}>
          <div style={s.successIcon}>✅</div>
          <h2 style={s.title}>Kayıt Başarılı!</h2>
          <p style={s.sub}>Hesabın oluşturuldu. Giriş sayfasına yönlendiriliyorsun...</p>
        </div>
      </div>
    );
  }

  return (
    <div style={s.page}>
      <div style={s.card}>
        {/* Logo */}
        <div style={s.logoRow}>
          <Link to="/" style={s.logo}>
            <span style={s.logoMark}>T</span>
            <span style={s.logoText}>Market</span>
            <span style={s.logoPro}>PRO</span>
          </Link>
        </div>

        <h2 style={s.title}>Üye Ol</h2>
        <p style={s.sub}>Hemen üye ol, avantajlardan yararlan</p>

        {error && <div style={s.error}>⚠️ {error}</div>}

        <form onSubmit={handleSubmit}>
          <div style={s.row}>
            <div style={s.field}>
              <label style={s.label}>Ad</label>
              <input style={s.input} name="firstName" value={form.firstName}
                onChange={handleChange} placeholder="Adınız" required />
            </div>
            <div style={s.field}>
              <label style={s.label}>Soyad</label>
              <input style={s.input} name="lastName" value={form.lastName}
                onChange={handleChange} placeholder="Soyadınız" required />
            </div>
          </div>

          <div style={s.field}>
            <label style={s.label}>Kullanıcı Adı</label>
            <input style={s.input} name="username" value={form.username}
              onChange={handleChange} placeholder="kullanici_adi" required />
          </div>

          <div style={s.field}>
            <label style={s.label}>E-posta</label>
            <input style={s.input} type="email" name="email" value={form.email}
              onChange={handleChange} placeholder="ornek@email.com" required />
          </div>

          <div style={s.field}>
            <label style={s.label}>Şifre</label>
            <div style={s.passWrap}>
              <input
                style={{ ...s.input, paddingRight: '48px' }}
                type={showPass ? 'text' : 'password'}
                name="password"
                value={form.password}
                onChange={handleChange}
                placeholder="En az 6 karakter"
                required
              />
              <button type="button" style={s.eyeBtn} onClick={() => setShowPass(!showPass)}>
                {showPass ? '🙈' : '👁️'}
              </button>
            </div>
          </div>

          <div style={s.field}>
            <label style={s.label}>Şifre Tekrar</label>
            <input
              style={{ ...s.input, borderColor: form.confirmPassword && form.password !== form.confirmPassword ? '#c62828' : '#e0e0e0' }}
              type="password"
              name="confirmPassword"
              value={form.confirmPassword}
              onChange={handleChange}
              placeholder="Şifrenizi tekrar girin"
              required
            />
            {form.confirmPassword && form.password !== form.confirmPassword && (
              <span style={s.passErr}>Şifreler eşleşmiyor</span>
            )}
          </div>

          <div style={s.kvkk}>
            <input type="checkbox" required style={{ marginRight: '8px', accentColor: '#c62828' }} />
            <span style={{ fontSize: '12px', color: '#666' }}>
              <strong>Kullanım Koşulları</strong> ve <strong>Gizlilik Politikası</strong>'nı kabul ediyorum
            </span>
          </div>

          <button style={{ ...s.btn, opacity: loading ? 0.7 : 1 }} type="submit" disabled={loading}>
            {loading ? 'Kayıt yapılıyor...' : 'Üye Ol'}
          </button>
        </form>

        <div style={s.divider}>
          <span style={s.dividerLine} />
          <span style={s.dividerText}>veya</span>
          <span style={s.dividerLine} />
        </div>

        <div style={s.loginRow}>
          <span style={s.loginText}>Zaten hesabın var mı?</span>
          <Link to="/login" style={s.loginLink}>Giriş Yap →</Link>
        </div>
      </div>
    </div>
  );
}

const s = {
  page: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f2f3f5', padding: '24px' },
  card: { background: '#fff', padding: '40px', borderRadius: '16px', boxShadow: '0 4px 24px rgba(0,0,0,0.08)', width: '100%', maxWidth: '480px' },
  logoRow: { textAlign: 'center', marginBottom: '24px' },
  logo: { textDecoration: 'none', display: 'inline-flex', alignItems: 'center', gap: '6px' },
  logoMark: { width: '36px', height: '36px', background: '#c62828', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '10px', fontWeight: '800', fontSize: '20px', fontFamily: 'Georgia,serif' },
  logoText: { fontSize: '22px', fontWeight: '800', color: '#111', fontFamily: 'Georgia,serif' },
  logoPro: { fontSize: '9px', fontWeight: '700', color: '#fff', background: '#c62828', padding: '2px 5px', borderRadius: '4px', alignSelf: 'flex-start', marginTop: '2px' },
  title: { fontSize: '24px', fontWeight: '800', color: '#111', textAlign: 'center', margin: '0 0 6px' },
  sub: { fontSize: '14px', color: '#999', textAlign: 'center', margin: '0 0 24px' },
  error: { background: '#ffebee', color: '#c62828', padding: '12px', borderRadius: '10px', marginBottom: '16px', fontSize: '14px', textAlign: 'center' },
  row: { display: 'flex', gap: '12px' },
  field: { flex: 1, marginBottom: '14px' },
  label: { display: 'block', marginBottom: '6px', color: '#333', fontWeight: '600', fontSize: '13px' },
  input: { width: '100%', padding: '11px 14px', border: '1.5px solid #e0e0e0', borderRadius: '10px', fontSize: '14px', boxSizing: 'border-box', outline: 'none' },
  passWrap: { position: 'relative' },
  eyeBtn: { position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', fontSize: '16px' },
  passErr: { fontSize: '11px', color: '#c62828', marginTop: '4px', display: 'block' },
  kvkk: { display: 'flex', alignItems: 'flex-start', marginBottom: '16px', marginTop: '4px' },
  btn: { width: '100%', padding: '14px', background: '#c62828', color: '#fff', border: 'none', borderRadius: '10px', fontSize: '15px', cursor: 'pointer', fontWeight: '700', transition: 'opacity 0.2s' },
  divider: { display: 'flex', alignItems: 'center', gap: '12px', margin: '20px 0' },
  dividerLine: { flex: 1, height: '1px', background: '#f0f0f0' },
  dividerText: { fontSize: '12px', color: '#bbb' },
  loginRow: { display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px' },
  loginText: { fontSize: '14px', color: '#666' },
  loginLink: { fontSize: '14px', color: '#c62828', fontWeight: '700', textDecoration: 'none' },
  successIcon: { fontSize: '64px', textAlign: 'center', marginBottom: '16px' },
};