import React, { useEffect } from 'react';
import { Snackbar, Alert, AlertColor, Slide, SlideProps } from '@mui/material';

export interface ToastProps {
  open: boolean;
  message: string;
  severity?: AlertColor;
  duration?: number;
  onClose?: () => void;
  position?: 'top' | 'bottom';
}

function SlideTransition(props: SlideProps) {
  return <Slide {...props} direction="up" />;
}

const Toast: React.FC<ToastProps> = ({
  open,
  message,
  severity = 'info',
  duration = 3000,
  onClose,
  position = 'top',
}) => {
  const [isOpen, setIsOpen] = React.useState(open);

  useEffect(() => {
    setIsOpen(open);
  }, [open]);

  const handleClose = (): void => {
    setIsOpen(false);
    onClose?.();
  };

  return (
    <Snackbar
      open={isOpen}
      autoHideDuration={duration}
      onClose={handleClose}
      anchorOrigin={{
        vertical: position === 'top' ? 'top' : 'bottom',
        horizontal: 'center',
      }}
      TransitionComponent={SlideTransition}
    >
      <Alert onClose={handleClose} severity={severity} sx={{ width: '100%' }}>
        {message}
      </Alert>
    </Snackbar>
  );
};

export default Toast;