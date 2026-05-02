import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPass, setShowPass] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    try {
      await login(username, password);
      if (username === 'admin') {
        navigate('/admin');  // admin ise admin paneline git
      } else {
        navigate('/');
      }
    } catch {
      setError('Kullanıcı adı veya şifre hatalı');
    } finally {
      setLoading(false);
    }
  };




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

        <h2 style={s.title}>Giriş Yap</h2>
        <p style={s.sub}>Hesabına giriş yap, alışverişe devam et</p>

        {error && <div style={s.error}>⚠️ {error}</div>}

        <form onSubmit={handleSubmit}>
          <div style={s.field}>
            <label style={s.label}>Kullanıcı Adı</label>
            <input
              style={s.input}
              type="text"
              value={username}
              onChange={e => setUsername(e.target.value)}
              placeholder="Kullanıcı adınız"
              required
            />
          </div>

          <div style={s.field}>
            <label style={s.label}>Şifre</label>
            <div style={s.passWrap}>
              <input
                style={{ ...s.input, paddingRight: '48px' }}
                type={showPass ? 'text' : 'password'}
                value={password}
                onChange={e => setPassword(e.target.value)}
                placeholder="••••••••"
                required
              />
              <button type="button" style={s.eyeBtn} onClick={() => setShowPass(!showPass)}>
                {showPass ? '🙈' : '👁️'}
              </button>
            </div>
          </div>

          <button style={{ ...s.btn, opacity: loading ? 0.7 : 1 }} type="submit" disabled={loading}>
            {loading ? 'Giriş yapılıyor...' : 'Giriş Yap'}
          </button>
        </form>

        <div style={s.divider}>
          <span style={s.dividerLine} />
          <span style={s.dividerText}>veya</span>
          <span style={s.dividerLine} />
        </div>

        <div style={s.registerRow}>
          <span style={s.registerText}>Hesabın yok mu?</span>
          <Link to="/register" style={s.registerLink}>Üye Ol →</Link>
        </div>

        <div style={s.testBox}>
          <div style={s.testTitle}>🧪 Test Hesabı</div>
          <div style={s.testInfo}>Kullanıcı: <strong>test</strong> / Şifre: <strong>test123</strong></div>
          <button style={s.testBtn} onClick={() => { setUsername('test'); setPassword('test123'); }}>
            Otomatik Doldur
          </button>
        </div>
      </div>
    </div>
  );
}

const s = {
  page: { minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#f2f3f5', padding: '24px' },
  card: { background: '#fff', padding: '40px', borderRadius: '16px', boxShadow: '0 4px 24px rgba(0,0,0,0.08)', width: '100%', maxWidth: '420px' },
  logoRow: { textAlign: 'center', marginBottom: '24px' },
  logo: { textDecoration: 'none', display: 'inline-flex', alignItems: 'center', gap: '6px' },
  logoMark: { width: '36px', height: '36px', background: '#c62828', color: '#fff', display: 'flex', alignItems: 'center', justifyContent: 'center', borderRadius: '10px', fontWeight: '800', fontSize: '20px', fontFamily: 'Georgia,serif' },
  logoText: { fontSize: '22px', fontWeight: '800', color: '#111', fontFamily: 'Georgia,serif' },
  logoPro: { fontSize: '9px', fontWeight: '700', color: '#fff', background: '#c62828', padding: '2px 5px', borderRadius: '4px', alignSelf: 'flex-start', marginTop: '2px' },
  title: { fontSize: '24px', fontWeight: '800', color: '#111', textAlign: 'center', margin: '0 0 6px' },
  sub: { fontSize: '14px', color: '#999', textAlign: 'center', margin: '0 0 24px' },
  error: { background: '#ffebee', color: '#c62828', padding: '12px', borderRadius: '10px', marginBottom: '16px', fontSize: '14px', textAlign: 'center' },
  field: { marginBottom: '16px' },
  label: { display: 'block', marginBottom: '6px', color: '#333', fontWeight: '600', fontSize: '14px' },
  input: { width: '100%', padding: '12px 14px', border: '1.5px solid #e0e0e0', borderRadius: '10px', fontSize: '14px', boxSizing: 'border-box', outline: 'none', transition: 'border-color 0.2s' },
  passWrap: { position: 'relative' },
  eyeBtn: { position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', fontSize: '16px' },
  btn: { width: '100%', padding: '14px', background: '#c62828', color: '#fff', border: 'none', borderRadius: '10px', fontSize: '15px', cursor: 'pointer', fontWeight: '700', marginTop: '8px', transition: 'opacity 0.2s' },
  divider: { display: 'flex', alignItems: 'center', gap: '12px', margin: '20px 0' },
  dividerLine: { flex: 1, height: '1px', background: '#f0f0f0' },
  dividerText: { fontSize: '12px', color: '#bbb', fontWeight: '500' },
  registerRow: { display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px' },
  registerText: { fontSize: '14px', color: '#666' },
  registerLink: { fontSize: '14px', color: '#c62828', fontWeight: '700', textDecoration: 'none' },
  testBox: { marginTop: '20px', background: '#f8f8f8', borderRadius: '10px', padding: '14px', textAlign: 'center' },
  testTitle: { fontSize: '12px', fontWeight: '700', color: '#999', marginBottom: '4px' },
  testInfo: { fontSize: '13px', color: '#555', marginBottom: '8px' },
  testBtn: { background: '#fff', border: '1px solid #e0e0e0', borderRadius: '8px', padding: '6px 16px', fontSize: '12px', cursor: 'pointer', color: '#333', fontWeight: '600' },
};