import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
  Box,
  Typography,
} from '@mui/material';
import { Warning as WarningIcon, Error as ErrorIcon, Info as InfoIcon, CheckCircle as SuccessIcon } from '@mui/icons-material';

export interface ConfirmDialogProps {
  open: boolean;
  title: string;
  content: string;
  confirmText?: string;
  cancelText?: string;
  onConfirm: () => void;
  onCancel: () => void;
  severity?: 'info' | 'warning' | 'error' | 'success';
  loading?: boolean;
}

const ConfirmDialog: React.FC<ConfirmDialogProps> = ({
  open,
  title,
  content,
  confirmText = '确认',
  cancelText = '取消',
  onConfirm,
  onCancel,
  severity = 'warning',
  loading = false,
}) => {
  const getIcon = (): React.ReactNode => {
    switch (severity) {
      case 'info':
        return <InfoIcon color="info" sx={{ fontSize: 48, mb: 2 }} />;
      case 'warning':
        return <WarningIcon color="warning" sx={{ fontSize: 48, mb: 2 }} />;
      case 'error':
        return <ErrorIcon color="error" sx={{ fontSize: 48, mb: 2 }} />;
      case 'success':
        return <SuccessIcon color="success" sx={{ fontSize: 48, mb: 2 }} />;
      default:
        return <WarningIcon color="warning" sx={{ fontSize: 48, mb: 2 }} />;
    }
  };

  const getSeverityColor = (): string => {
    switch (severity) {
      case 'info':
        return '#1976d2';
      case 'warning':
        return '#f57c00';
      case 'error':
        return '#d32f2f';
      case 'success':
        return '#388e3c';
      default:
        return '#f57c00';
    }
  };

  return (
    <Dialog
      open={open}
      onClose={onCancel}
      aria-labelledby="confirm-dialog-title"
      aria-describedby="confirm-dialog-description"
      maxWidth="sm"
      fullWidth
    >
      <DialogTitle id="confirm-dialog-title" sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
        {getIcon()}
        <Typography variant="h6" component="span">
          {title}
        </Typography>
      </DialogTitle>
      <DialogContent>
        <DialogContentText id="confirm-dialog-description">
          {content}
        </DialogContentText>
      </DialogContent>
      <DialogActions>
        <Button onClick={onCancel} disabled={loading}>
          {cancelText}
        </Button>
        <Button
          onClick={onConfirm}
          variant="contained"
          disabled={loading}
          autoFocus
          sx={{ backgroundColor: getSeverityColor() }}
        >
          {loading ? '处理中...' : confirmText}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ConfirmDialog;