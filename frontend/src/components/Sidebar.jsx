import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

export default function Sidebar({ categories = [], onFilterChange }) {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [gridColumns, setGridColumns] = useState(4);
  const categoryId = searchParams.get('categoryId') || '';

  const handleGridChange = (cols) => {
    setGridColumns(cols);
    onFilterChange?.({ gridColumns: cols });
  };

  const priceRanges = [
    { label: '0 - 2.000 TL', minPrice: null, maxPrice: 2000 },
    { label: '2.000 - 5.000 TL', minPrice: 2000, maxPrice: 5000 },
    { label: '5.000 - 20.000 TL', minPrice: 5000, maxPrice: 20000 },
    { label: '20.000 TL ve uzeri', minPrice: 20000, maxPrice: null },
  ];

  const handlePriceRange = (range) => {
    const params = new URLSearchParams(searchParams);
    if (range.minPrice) params.set('minPrice', range.minPrice);
    else params.delete('minPrice');
    if (range.maxPrice) params.set('maxPrice', range.maxPrice);
    else params.delete('maxPrice');
    navigate('/?' + params.toString());
  };

  const currentMin = searchParams.get('minPrice');
  const currentMax = searchParams.get('maxPrice');

  return (
    <aside style={{ width: '220px', flexShrink: 0, display: 'flex', flexDirection: 'column', gap: '1rem' }}>

      {/* Kategoriler */}
      <div style={s.card}>
        <h3 style={s.cardTitle}>Kategoriler</h3>
        <div
          onClick={() => navigate('/')}
          style={{ ...s.catItem, ...(categoryId === '' ? s.catActive : {}) }}
        >
          Tum Kategoriler
        </div>
        {categories.map(cat => (
          <div
            key={cat.id}
            onClick={() => navigate('/?categoryId=' + cat.id)}
            style={{ ...s.catItem, ...(String(categoryId) === String(cat.id) ? s.catActive : {}) }}
          >
            <span>{cat.name}</span>
            {String(categoryId) === String(cat.id) && <span style={{ color: '#4caf50' }}>✓</span>}
          </div>
        ))}
      </div>

      {/* Fiyat Araligi */}
      <div style={s.card}>
        <h3 style={s.cardTitle}>Fiyat Araligi</h3>
        {priceRanges.map((range, idx) => {
          const isActive = String(currentMin || '') === String(range.minPrice || '') &&
                           String(currentMax || '') === String(range.maxPrice || '');
          return (
            <div
              key={idx}
              onClick={() => handlePriceRange(range)}
              style={{ ...s.priceItem, ...(isActive ? s.priceActive : {}) }}
            >
              <input type="checkbox" readOnly checked={isActive} style={{ marginRight: '8px' }} />
              {range.label}
            </div>
          );
        })}
      </div>

      {/* Gorunum Ayarlari */}
      <div style={s.card}>
        <h3 style={s.cardTitle}>Gorunum</h3>
        <div style={{ display: 'flex', gap: '0.5rem' }}>
          {[3, 4, 5].map(col => (
            <button
              key={col}
              onClick={() => handleGridChange(col)}
              style={{
                ...s.gridBtn,
                background: gridColumns === col ? '#e53935' : '#f5f5f5',
                color: gridColumns === col ? 'white' : '#333',
              }}
            >
              {col}
            </button>
          ))}
        </div>
      </div>
    </aside>
  );
}

const s = {
  card: { background: 'white', borderRadius: '8px', padding: '1rem', boxShadow: '0 1px 4px rgba(0,0,0,0.06)' },
  cardTitle: { fontSize: '0.85rem', fontWeight: '700', color: '#e53935', marginBottom: '0.75rem', paddingBottom: '0.5rem', borderBottom: '2px solid #ffebee', margin: '0 0 0.75rem 0' },
  catItem: { padding: '0.4rem 0.5rem', borderRadius: '6px', cursor: 'pointer', fontSize: '0.875rem', color: '#555', display: 'flex', justifyContent: 'space-between', transition: 'background 0.15s' },
  catActive: { background: '#ffebee', color: '#e53935', fontWeight: '600' },
  priceItem: { display: 'flex', alignItems: 'center', padding: '0.4rem 0.5rem', borderRadius: '6px', cursor: 'pointer', fontSize: '0.875rem', color: '#555' },
  priceActive: { background: '#ffebee', color: '#e53935', fontWeight: '600' },
  gridBtn: { border: 'none', borderRadius: '6px', padding: '0.3rem 0.7rem', cursor: 'pointer', fontWeight: '600' },
};