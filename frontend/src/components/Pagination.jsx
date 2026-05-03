/**
 * Pagination Component - Reusable sayfalama bileşeni
 */
export default function Pagination({
  currentPage = 0,
  totalPages = 1,
  onChange,
  itemsPerPage = 20,
}) {
  if (totalPages <= 1) return null;

  const pages = [];
  const maxPagesToShow = 7;
  const leftSibling = Math.max(currentPage - 2, 0);
  const rightSibling = Math.min(currentPage + 2, totalPages - 1);

  // First page
  if (leftSibling > 0) {
    pages.push(0);
  }

  // Ellipsis ...
  if (leftSibling > 1) {
    pages.push('...');
  }

  // Numbered pages
  for (let i = leftSibling; i <= rightSibling; i++) {
    pages.push(i);
  }

  // Ellipsis ...
  if (rightSibling < totalPages - 2) {
    pages.push('...');
  }

  // Last page
  if (rightSibling < totalPages - 1) {
    pages.push(totalPages - 1);
  }

  return (
    <div className="flex justify-center items-center gap-2 mt-8 mb-8 flex-wrap">
      {/* First page */}
      <button
        onClick={() => onChange?.(0)}
        disabled={currentPage === 0}
        className={`px-4 py-2 rounded-lg text-sm font-semibold transition ${
          currentPage === 0
            ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
            : 'bg-white text-red-600 border border-red-200 hover:bg-red-50'
        }`}
        title="İlk sayfa"
      >
        « Başa
      </button>

      {/* Previous page */}
      <button
        onClick={() => onChange?.(currentPage - 1)}
        disabled={currentPage === 0}
        className={`px-4 py-2 rounded-lg text-sm font-semibold transition ${
          currentPage === 0
            ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
            : 'bg-white text-red-600 border border-red-200 hover:bg-red-50'
        }`}
        title="Önceki sayfa"
      >
        ‹ Önceki
      </button>

      {/* Page numbers */}
      {pages.map((page, idx) => {
        if (page === '...') {
          return (
            <span key={`ellipsis-${idx}`} className="px-2 py-2 text-gray-400 text-sm">
              ...
            </span>
          );
        }

        const isActive = page === currentPage;
        return (
          <button
            key={page}
            onClick={() => onChange?.(page)}
            className={`px-3.5 py-2 rounded-lg text-sm font-semibold transition ${
              isActive
                ? 'bg-red-600 text-white shadow-md'
                : 'bg-white text-gray-700 border border-gray-200 hover:border-red-300 hover:text-red-600'
            }`}
          >
            {page + 1}
          </button>
        );
      })}

      {/* Next page */}
      <button
        onClick={() => onChange?.(currentPage + 1)}
        disabled={currentPage >= totalPages - 1}
        className={`px-4 py-2 rounded-lg text-sm font-semibold transition ${
          currentPage >= totalPages - 1
            ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
            : 'bg-white text-red-600 border border-red-200 hover:bg-red-50'
        }`}
        title="Sonraki sayfa"
      >
        Sonraki ›
      </button>

      {/* Last page */}
      <button
        onClick={() => onChange?.(totalPages - 1)}
        disabled={currentPage >= totalPages - 1}
        className={`px-4 py-2 rounded-lg text-sm font-semibold transition ${
          currentPage >= totalPages - 1
            ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
            : 'bg-white text-red-600 border border-red-200 hover:bg-red-50'
        }`}
        title="Son sayfa"
      >
        Son »
      </button>

      {/* Page info */}
      <div className="ml-4 px-4 py-2 bg-gray-50 rounded-lg text-sm text-gray-600 border border-gray-200">
        Sayfa <span className="font-semibold text-gray-800">{currentPage + 1}</span> / <span className="font-semibold text-gray-800">{totalPages}</span>
      </div>
    </div>
  );
}


