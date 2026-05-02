import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';

/**
 * Sidebar Component - Kategoriler, fiyat filtresi, ayarlar
 */
export default function Sidebar({ categories = [], onFilterChange }) {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [expandedCategories, setExpandedCategories] = useState({});
  const [showSettings, setShowSettings] = useState(false);
  const [gridColumns, setGridColumns] = useState(4);

  const categoryId = searchParams.get('categoryId') || '';

  const toggleCategory = (catId) => {
    setExpandedCategories((prev) => ({
      ...prev,
      [catId]: !prev[catId],
    }));
  };

  const handleGridChange = (cols) => {
    setGridColumns(cols);
    onFilterChange?.({ gridColumns: cols });
  };

  return (
    <>
      {/* Sidebar */}
      <aside className="w-56 flex-shrink-0 space-y-4">
        {/* Kategoriler */}
        <div className="bg-white rounded-lg shadow-sm p-4">
          <h3 className="text-sm font-bold text-red-600 mb-3 pb-2 border-b-2 border-red-100">
            📁 Kategoriler
          </h3>
          <div
            onClick={() => navigate('/')}
            className={`py-2 px-2 rounded cursor-pointer text-sm transition ${
              categoryId === ''
                ? 'bg-red-50 text-red-600 font-semibold'
                : 'text-gray-700 hover:bg-gray-50'
            }`}
          >
            <div className="flex justify-between items-center">
              <span>Tümü</span>
              {categoryId === '' && <span className="text-green-500">✓</span>}
            </div>
          </div>
          {categories.map((cat) => (
            <div
              key={cat.id}
              onClick={() => navigate(`/?categoryId=${cat.id}`)}
              className={`py-2 px-2 rounded cursor-pointer text-sm transition ${
                String(categoryId) === String(cat.id)
                  ? 'bg-red-50 text-red-600 font-semibold'
                  : 'text-gray-700 hover:bg-gray-50'
              }`}
            >
              <div className="flex justify-between items-center">
                <span>{cat.name}</span>
                {String(categoryId) === String(cat.id) && <span className="text-green-500">✓</span>}
              </div>
            </div>
          ))}
        </div>

        {/* Fiyat Aralığı */}
        <div className="bg-white rounded-lg shadow-sm p-4">
          <h3 className="text-sm font-bold text-red-600 mb-3 pb-2 border-b-2 border-red-100">
            💰 Fiyat Aralığı
          </h3>
          <div className="space-y-2">
            {[
              { label: '0 - 1.000 ₺', minPrice: null, maxPrice: 1000 },
              { label: '1.000 - 5.000 ₺', minPrice: 1000, maxPrice: 5000 },
              { label: '5.000 - 20.000 ₺', minPrice: 5000, maxPrice: 20000 },
              { label: '20.000 ₺ ve üzeri', minPrice: 20000, maxPrice: null },
            ].map((range, idx) => (
              <div
                key={idx}
                onClick={() => {
                  const params = new URLSearchParams(searchParams);
                  if (range.minPrice) params.set('minPrice', range.minPrice);
                  else params.delete('minPrice');
                  if (range.maxPrice) params.set('maxPrice', range.maxPrice);
                  else params.delete('maxPrice');
                  navigate(`/?${params.toString()}`);
                }}
                className="py-2 px-2 rounded cursor-pointer text-sm text-gray-700 hover:bg-red-50 hover:text-red-600 transition"
              >
                {range.label}
              </div>
            ))}
          </div>
        </div>

        {/* Görünüm Ayarları */}
        <div className="bg-white rounded-lg shadow-sm p-4">
          <h3 className="text-sm font-bold text-red-600 mb-3 pb-2 border-b-2 border-red-100">
            ⚙️ Görünüm
          </h3>
          <div className="grid grid-cols-3 gap-2">
            {[2, 3, 4].map((cols) => (
              <button
                key={cols}
                onClick={() => handleGridChange(cols)}
                className={`px-3 py-2 rounded font-semibold text-sm transition ${
                  gridColumns === cols
                    ? 'bg-red-600 text-white shadow-sm'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
                title={`${cols} sütunlu görünüm`}
              >
                {cols}✕
              </button>
            ))}
          </div>
        </div>

        {/* Filtreleri Temizle */}
        <button
          onClick={() => navigate('/')}
          className="w-full py-3 px-4 rounded-lg font-semibold text-sm text-red-600 bg-red-50 hover:bg-red-100 transition border border-red-200"
        >
          🗑️ Filtreleri Temizle
        </button>
      </aside>
    </>
  );

}