import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

const SLIDES = [
  {
    id: 1,
    title: 'Elektronik Dünyası',
    subtitle: 'En yeni teknoloji ürünleri',
    discount: '%50\'ye varan indirim',
    bg: 'linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%)',
    accent: '#e94560',
    img: 'https://images.unsplash.com/photo-1491933382434-500287f9b54b?w=600&q=80',
    cta: 'Elektronik',
  },
  {
    id: 2,
    title: 'Moda & Stil',
    subtitle: 'Trendleri yakala',
    discount: '%40\'a kadar indirim',
    bg: 'linear-gradient(135deg, #2d1b69 0%, #11998e 100%)',
    accent: '#fff',
    img: 'https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=600&q=80',
    cta: 'Giyim & Moda',
  },
  {
    id: 3,
    title: 'Ev & Yaşam',
    subtitle: 'Evinizi güzelleştirin',
    discount: '%35\'e kadar indirim',
    bg: 'linear-gradient(135deg, #134e5e 0%, #71b280 100%)',
    accent: '#ffd700',
    img: 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=600&q=80',
    cta: 'Ev & Yaşam',
  },
];

export default function HeroBanner() {
  const [active, setActive] = useState(0);
  const navigate = useNavigate();

  useEffect(() => {
    const timer = setInterval(() => {
      setActive(a => (a + 1) % SLIDES.length);
    }, 4000);
    return () => clearInterval(timer);
  }, []);

  const slide = SLIDES[active];

  return (
    <div style={styles.wrapper}>
      {/* Main Banner */}
      <div style={{ ...styles.banner, background: slide.bg }}>
        <div style={styles.bannerContent}>
          <div style={{ ...styles.discountBadge, background: slide.accent, color: slide.accent === '#fff' ? '#333' : '#fff' }}>
            {slide.discount}
          </div>
          <h2 style={styles.bannerTitle}>{slide.title}</h2>
          <p style={styles.bannerSub}>{slide.subtitle}</p>
          <button
            style={styles.bannerBtn}
            onClick={() => navigate(`/?category=${encodeURIComponent(slide.cta)}`)}
          >
            Alışverişe Başla →
          </button>
        </div>
        <div style={styles.bannerImgWrap}>
          <img
            src={slide.img}
            alt={slide.title}
            style={styles.bannerImg}
          />
        </div>

        {/* Dots */}
        <div style={styles.dots}>
          {SLIDES.map((_, i) => (
            <button
              key={i}
              style={{ ...styles.dot, ...(i === active ? styles.dotActive : {}) }}
              onClick={() => setActive(i)}
            />
          ))}
        </div>
      </div>

      {/* Category Cards */}
      <div style={styles.catCards}>
        {[
          { name: 'Elektronik', icon: '💻', color: '#e3f2fd', iconBg: '#1565c0', img: 'https://images.unsplash.com/photo-1498049794561-7780e7231661?w=200&q=80' },
          { name: 'Giyim & Moda', icon: '👗', color: '#fce4ec', iconBg: '#c62828', img: 'https://images.unsplash.com/photo-1558769132-cb1aea458c5e?w=200&q=80' },
          { name: 'Ev & Yaşam', icon: '🏠', color: '#e8f5e9', iconBg: '#2e7d32', img: 'https://images.unsplash.com/photo-1556909114-f6e7ad7d3136?w=200&q=80' },
          { name: 'Spor & Outdoor', icon: '⚽', color: '#fff8e1', iconBg: '#f57f17', img: 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=200&q=80' },
        ].map(cat => (
          <div
            key={cat.name}
            style={{ ...styles.catCard, background: cat.color }}
            onClick={() => navigate(`/?category=${encodeURIComponent(cat.name)}`)}
          >
            <img src={cat.img} alt={cat.name} style={styles.catImg} />
            <div style={styles.catInfo}>
              <div style={{ ...styles.catIcon, background: cat.iconBg }}>{cat.icon}</div>
              <span style={styles.catName}>{cat.name}</span>
              <span style={styles.catArrow}>→</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

const styles = {
  wrapper: { marginBottom: '40px' },
  banner: {
    borderRadius: '20px',
    overflow: 'hidden',
    display: 'flex',
    alignItems: 'center',
    minHeight: '320px',
    position: 'relative',
    marginBottom: '16px',
    transition: 'background 0.5s',
  },
  bannerContent: {
    flex: 1,
    padding: '48px',
    color: '#fff',
    position: 'relative',
    zIndex: 2,
  },
  discountBadge: {
    display: 'inline-block',
    padding: '6px 16px',
    borderRadius: '20px',
    fontSize: '13px',
    fontWeight: '700',
    marginBottom: '16px',
    letterSpacing: '0.5px',
  },
  bannerTitle: {
    fontSize: '40px',
    fontWeight: '800',
    margin: '0 0 8px',
    fontFamily: 'Georgia, serif',
    lineHeight: 1.1,
  },
  bannerSub: {
    color: 'rgba(255,255,255,0.7)',
    fontSize: '16px',
    margin: '0 0 24px',
  },
  bannerBtn: {
    background: '#fff',
    color: '#111',
    border: 'none',
    padding: '14px 28px',
    borderRadius: '12px',
    fontWeight: '700',
    fontSize: '15px',
    cursor: 'pointer',
  },
  bannerImgWrap: {
    flex: '0 0 380px',
    height: '320px',
    position: 'relative',
    overflow: 'hidden',
  },
  bannerImg: {
    width: '100%',
    height: '100%',
    objectFit: 'cover',
    opacity: 0.7,
  },
  dots: {
    position: 'absolute',
    bottom: '20px',
    left: '48px',
    display: 'flex',
    gap: '8px',
    zIndex: 3,
  },
  dot: {
    width: '8px',
    height: '8px',
    borderRadius: '50%',
    border: 'none',
    background: 'rgba(255,255,255,0.4)',
    cursor: 'pointer',
    padding: 0,
    transition: 'all 0.3s',
  },
  dotActive: {
    background: '#fff',
    width: '24px',
    borderRadius: '4px',
  },
  catCards: {
    display: 'grid',
    gridTemplateColumns: 'repeat(4, 1fr)',
    gap: '16px',
  },
  catCard: {
    borderRadius: '16px',
    overflow: 'hidden',
    cursor: 'pointer',
    transition: 'transform 0.2s, box-shadow 0.2s',
    position: 'relative',
    height: '140px',
  },
  catImg: {
    position: 'absolute',
    inset: 0,
    width: '100%',
    height: '100%',
    objectFit: 'cover',
    opacity: 0.3,
  },
  catInfo: {
    position: 'relative',
    zIndex: 2,
    padding: '20px',
    display: 'flex',
    flexDirection: 'column',
    height: '100%',
    justifyContent: 'space-between',
    boxSizing: 'border-box',
  },
  catIcon: {
    width: '40px',
    height: '40px',
    borderRadius: '12px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    fontSize: '20px',
  },
  catName: { fontWeight: '700', fontSize: '15px', color: '#111' },
  catArrow: { fontSize: '18px', color: '#666' },
};