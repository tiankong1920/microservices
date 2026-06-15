import React, { createContext, useContext, useState, useEffect, useCallback, type ReactNode } from 'react';
import Toast from '../components/Toast';

type ToastSeverity = 'success' | 'error' | 'info' | 'warning';

interface ToastState {
  open: boolean;
  message: string;
  severity: ToastSeverity;
}

interface ToastContextType {
  showToast: (message: string, severity: ToastSeverity) => void;
  showSuccess: (message: string) => void;
  showError: (message: string) => void;
  showWarning: (message: string) => void;
  showInfo: (message: string) => void;
}

/** Global ref bridge for use outside React tree (e.g. Axios interceptor) */
// eslint-disable-next-line react-refresh/only-export-components
export const toastApiRef: { current: ToastContextType | null } = { current: null };

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const ToastProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [toast, setToast] = useState<ToastState>({ open: false, message: '', severity: 'info' });

  const showToast = useCallback((message: string, severity: ToastSeverity) => {
    setToast({ open: true, message, severity });
  }, []);

  const showSuccess = useCallback((message: string) => showToast(message, 'success'), [showToast]);
  const showError = useCallback((message: string) => showToast(message, 'error'), [showToast]);
  const showWarning = useCallback((message: string) => showToast(message, 'warning'), [showToast]);
  const showInfo = useCallback((message: string) => showToast(message, 'info'), [showToast]);

  const handleClose = useCallback(() => {
    setToast(prev => ({ ...prev, open: false }));
  }, []);

  // Expose API via global ref for Axios interceptor and other non-React code
  useEffect(() => {
    const api: ToastContextType = { showToast, showSuccess, showError, showWarning, showInfo };
    toastApiRef.current = api;
    return () => {
      toastApiRef.current = null;
    };
  }, [showToast, showSuccess, showError, showWarning, showInfo]);

  const value: ToastContextType = { showToast, showSuccess, showError, showWarning, showInfo };

  return (
    <ToastContext.Provider value={value}>
      {children}
      <Toast
        open={toast.open}
        message={toast.message}
        severity={toast.severity}
        onClose={handleClose}
      />
    </ToastContext.Provider>
  );
};

// eslint-disable-next-line react-refresh/only-export-components
export const useToast = (): ToastContextType => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
};
