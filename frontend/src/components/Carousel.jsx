import { useState } from 'react';

/**
 * Carousel Component - Benzer ürünleri gösteren kaydırılabilir slider
 * İçeriği responsive
 */
export default function Carousel({
  items = [],
  itemsPerView = 4,
  spaceBetween = 16,
  onItemClick,
  renderItem,
  title,
}) {
  const [currentIndex, setCurrentIndex] = useState(0);

  if (!items || items.length === 0) {
    return null;
  }

  const itemWidth = `calc((100% - ${spaceBetween * (itemsPerView - 1)}px) / ${itemsPerView})`;
  const canPrevious = currentIndex > 0;
  const canNext = currentIndex < items.length - itemsPerView;

  const handlePrevious = () => {
    if (canPrevious) {
      setCurrentIndex(currentIndex - 1);
    }
  };

  const handleNext = () => {
    if (canNext) {
      setCurrentIndex(currentIndex + 1);
    }
  };

  const visibleItems = items.slice(currentIndex, currentIndex + itemsPerView);

  return (
    <div className="w-full">
      {title && <h3 className="text-lg font-bold text-gray-800 mb-4 pl-1">{title}</h3>}

      <div className="relative flex items-center gap-4">
        {/* Sol buton */}
        <button
          onClick={handlePrevious}
          disabled={!canPrevious}
          className={`absolute -left-14 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full flex items-center justify-center text-xl font-bold z-10 transition ${
            canPrevious
              ? 'bg-white border border-gray-300 text-red-600 hover:bg-red-50 shadow-md'
              : 'bg-gray-100 text-gray-400 cursor-not-allowed'
          }`}
          title="Öncekiler"
        >
          ‹
        </button>

        {/* Items container */}
        <div className="flex-1 overflow-hidden rounded-lg">
          <div
            className="flex transition-transform duration-300 ease-in-out"
            style={{
              transform: `translateX(-${(currentIndex * 100) / itemsPerView}%)`,
            }}
          >
            {items.map((item, idx) => (
              <div
                key={idx}
                className="flex-shrink-0 cursor-pointer group"
                style={{
                  width: itemWidth,
                  marginRight: idx === items.length - 1 ? 0 : spaceBetween,
                }}
                onClick={() => onItemClick?.(item, idx)}
              >
                {renderItem ? (
                  renderItem(item)
                ) : (
                  <div className="p-3 h-full flex flex-col">
                    <div className="aspect-square bg-gray-200 rounded-lg flex items-center justify-center text-4xl group-hover:bg-gray-300 transition mb-2">
                      {item.image}
                    </div>
                    <div className="text-sm font-semibold text-gray-800 line-clamp-2 group-hover:text-red-600 transition">
                      {item.name}
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Sağ buton */}
        <button
          onClick={handleNext}
          disabled={!canNext}
          className={`absolute -right-14 top-1/2 -translate-y-1/2 w-10 h-10 rounded-full flex items-center justify-center text-xl font-bold z-10 transition ${
            canNext
              ? 'bg-white border border-gray-300 text-red-600 hover:bg-red-50 shadow-md'
              : 'bg-gray-100 text-gray-400 cursor-not-allowed'
          }`}
          title="Sonrakiler"
        >
          ›
        </button>
      </div>

      {/* Sayfa göstergesi */}
      {items.length > itemsPerView && (
        <div className="flex justify-center gap-2 mt-4">
          {Array.from({ length: Math.ceil(items.length / itemsPerView) }).map(
            (_, i) => (
              <button
                key={i}
                onClick={() => setCurrentIndex(i * itemsPerView)}
                className={`w-2.5 h-2.5 rounded-full transition ${
                  i === Math.floor(currentIndex / itemsPerView)
                    ? 'bg-red-600'
                    : 'bg-gray-300 hover:bg-gray-400'
                }`}
              />
            )
          )}
        </div>
      )}
    </div>
  );
}


