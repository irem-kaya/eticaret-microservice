import { useState, useEffect } from 'react';

/**
 * Toast/Notification Component
 * Reusable bildirim bileşeni - sepete ekleme vb. işlemler için
 */
const Toast = ({
  id,
  message,
  type = 'success',
  actionPrimary,
  actionSecondary,
  onActionPrimary,
  onActionSecondary,
  onClose,
  duration = 3000,
}) => {
  const [isVisible, setIsVisible] = useState(true);

  useEffect(() => {
    const timer = setTimeout(() => {
      setIsVisible(false);
      onClose?.();
    }, duration);

    return () => clearTimeout(timer);
  }, [duration, onClose]);

  if (!isVisible) return null;

  const typeConfig = {
    success: {
      bg: 'bg-green-600',
      border: 'border-green-700',
      icon: '✓',
      light: 'bg-green-50',
    },
    error: {
      bg: 'bg-red-600',
      border: 'border-red-700',
      icon: '✕',
      light: 'bg-red-50',
    },
    info: {
      bg: 'bg-blue-600',
      border: 'border-blue-700',
      icon: 'ℹ',
      light: 'bg-blue-50',
    },
    warning: {
      bg: 'bg-orange-600',
      border: 'border-orange-700',
      icon: '⚠',
      light: 'bg-orange-50',
    },
  };

  const config = typeConfig[type] || typeConfig.success;

  return (
    <div
      className={`fixed top-4 right-4 ${config.bg} text-white px-6 py-4 rounded-lg shadow-lg border-l-4 ${config.border} min-w-80 flex items-start gap-4 z-9999 animation-slide-in`}
    >
      <span className="text-2xl font-bold flex-shrink-0 pt-0.5">{config.icon}</span>
      <div className="flex-1">
        <p className="font-semibold text-sm mb-2">
          {message}
        </p>
        {(actionPrimary || actionSecondary) && (
          <div className="flex gap-3">
            {actionPrimary && (
              <button
                onClick={() => {
                  onActionPrimary?.();
                  setIsVisible(false);
                  onClose?.();
                }}
                className="text-sm font-semibold px-4 py-1.5 rounded bg-white/20 hover:bg-white/30 transition"
              >
                {actionPrimary}
              </button>
            )}
            {actionSecondary && (
              <button
                onClick={() => {
                  onActionSecondary?.();
                  setIsVisible(false);
                  onClose?.();
                }}
                className="text-sm font-semibold px-4 py-1.5 rounded bg-white/20 hover:bg-white/30 transition"
              >
                {actionSecondary}
              </button>
            )}
          </div>
        )}
      </div>
      <button
        onClick={() => {
          setIsVisible(false);
          onClose?.();
        }}
        className="text-white opacity-70 hover:opacity-100 transition font-bold text-xl flex-shrink-0"
      >
        ✕
      </button>
    </div>
  );
};

/**
 * Toast Manager - Birden fazla toast'u yönetmek için
 */
export const useToastManager = () => {
  const [toasts, setToasts] = useState([]);

  const addToast = ({
    message,
    type = 'success',
    duration = 3000,
    actionPrimary,
    actionSecondary,
    onActionPrimary,
    onActionSecondary,
  }) => {
    const id = Date.now();
    let newToast = {
      id,
      message,
      type,
      duration,
      actionPrimary,
      actionSecondary,
      onActionPrimary,
      onActionSecondary,
    };
    setToasts((prev) => [...prev, newToast]);
    return id;
  };

  const removeToast = (id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  return { toasts, addToast, removeToast };
};

/**
 * Toast Container - Bütün toast'ları görüntülemek için
 */
export const ToastContainer = ({ toasts, onRemove }) => {
  return (
    <div>
      {toasts.map((toast) => (
        <Toast
          key={toast.id}
          {...toast}
          onClose={() => onRemove(toast.id)}
        />
      ))}
    </div>
  );
};

export default Toast;

