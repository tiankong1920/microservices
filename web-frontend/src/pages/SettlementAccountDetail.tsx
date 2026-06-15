import React, { useState, useEffect } from 'react';
import {
  Typography, Box, Paper, Divider, Button, IconButton, Tooltip, CircularProgress,
  Grid, List, ListItem, ListItemText, Chip, TextField, Dialog, DialogTitle, DialogContent, DialogActions, MenuItem
} from '@mui/material';
import {
  Edit as EditIcon, ArrowBack as ArrowBackIcon,
  Phone as PhoneIcon, Mail as MailIcon, Map as MapIcon,
  CalendarToday as CalendarIcon, Print as PrintIcon, Save as SaveIcon, Cancel as CancelIcon,
  Delete as DeleteIcon
} from '@mui/icons-material';
import { settlementAccountApi } from '../services/api';
import { useNavigate, useParams } from 'react-router-dom';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import type { ApiResponse, SettlementAccount } from '../types';

const SettlementAccountDetail: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [account, setAccount] = useState<SettlementAccount | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [_isEditing, setIsEditing] = useState<boolean>(false);
  const [editData, setEditData] = useState<Partial<SettlementAccount>>({});
  const [editDialogOpen, setEditDialogOpen] = useState<boolean>(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [deleteLoading, setDeleteLoading] = useState<boolean>(false);
  const [saveLoading, setSaveLoading] = useState<boolean>(false);
  const { showSuccess, showError } = useToast();

  const loadAccountDetail = async (): Promise<void> => {
    if (!id) {
      setError('账户ID不能为空');
      setLoading(false);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const response: ApiResponse<SettlementAccount> = await settlementAccountApi.getSettlementAccountById(Number(id));
      setAccount(response.data);
      setEditData(response.data);
    } catch (err) {
      console.error('Failed to load settlement account detail:', err);
      setError('加载结算账户详情失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAccountDetail();
  }, [id]);

  const handleBack = (): void => {
    navigate('/settlement-accounts');
  };

  const handleEdit = (): void => {
    setIsEditing(true);
    setEditDialogOpen(true);
  };

  const handleCancelEdit = (): void => {
    setIsEditing(false);
    setEditDialogOpen(false);
    setEditData(account || {});
  };

  const handleSaveEdit = async (): Promise<void> => {
    if (!account || !editData) return;

    setSaveLoading(true);
    setError(null);
    try {
      await settlementAccountApi.updateSettlementAccount(account.id, editData);
      showSuccess('结算账户更新成功');
      setIsEditing(false);
      setEditDialogOpen(false);
      loadAccountDetail();
    } catch (err) {
      console.error('Failed to update settlement account:', err);
      showError('更新结算账户失败');
    } finally {
      setSaveLoading(false);
    }
  };

  const handleDelete = (): void => {
    setDeleteDialogOpen(true);
  };

  const confirmDelete = async (): Promise<void> => {
    if (!account) return;

    setDeleteLoading(true);
    setError(null);
    try {
      await settlementAccountApi.deleteSettlementAccount(account.id);
      showSuccess('结算账户删除成功');
      setDeleteDialogOpen(false);
      navigate('/settlement-accounts');
    } catch (err) {
      console.error('Failed to delete settlement account:', err);
      showError('删除结算账户失败');
    } finally {
      setDeleteLoading(false);
    }
  };

  const handlePrint = (): void => {
    if (!account) return;
    window.print();
  };

  const getStatusColor = (status: string): 'success' | 'default' | 'primary' => {
    switch (status) {
      case 'ACTIVE': return 'success';
      case 'INACTIVE': return 'default';
      default: return 'primary';
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 200 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error && !account) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h6" color="error" gutterBottom>
          {error}
        </Typography>
      </Box>
    );
  }

  if (!account) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h6" color="info" gutterBottom>
          结算账户不存在
        </Typography>
      </Box>
    );
  }

  return (
    <Box>
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个结算账户吗？删除后无法恢复。"
        confirmText="删除"
        cancelText="取消"
        severity="warning"
        onConfirm={confirmDelete}
        onCancel={() => setDeleteDialogOpen(false)}
        loading={deleteLoading}
      />

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <IconButton onClick={handleBack} color="primary">
            <ArrowBackIcon />
          </IconButton>
          <Typography variant="h4" gutterBottom>
            结算账户详情
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="contained"
            startIcon={<PrintIcon />}
            onClick={handlePrint}
          >
            打印
          </Button>
          <Button
            variant="contained"
            startIcon={<EditIcon />}
            onClick={handleEdit}
          >
            编辑
          </Button>
          <Button
            variant="outlined"
            color="error"
            startIcon={<DeleteIcon />}
            onClick={handleDelete}
          >
            删除
          </Button>
        </Box>
      </Box>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h5" gutterBottom>
          基本信息
        </Typography>
        <Divider sx={{ mb: 3 }} />

        <Grid container spacing={3}>
          <Grid size={{ xs: 12, md: 6 }}>
            <List>
              <ListItem>
                <ListItemText primary="账户ID" secondary={account.accountId} />
              </ListItem>
              <ListItem>
                <ListItemText primary="账户名称" secondary={account.accountName} />
              </ListItem>
              <ListItem>
                <ListItemText primary="账户类型" secondary={account.accountType} />
              </ListItem>
              <ListItem>
                <ListItemText primary="开户银行" secondary={account.bankName} />
              </ListItem>
              <ListItem>
                <ListItemText primary="账号" secondary={account.accountNumber} />
              </ListItem>
              <ListItem>
                <ListItemText primary="状态" secondary={
                  <Chip 
                    label={account.status === 'ACTIVE' ? '启用' : '禁用'} 
                    color={getStatusColor(account.status)} 
                    size="small" 
                  />
                } />
              </ListItem>
              <ListItem>
                <ListItemText 
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <CalendarIcon fontSize="small" />
                      <span>创建时间</span>
                    </Box>
                  } 
                  secondary={new Date(account.createdAt).toLocaleString()} 
                />
              </ListItem>
              <ListItem>
                <ListItemText 
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <CalendarIcon fontSize="small" />
                      <span>更新时间</span>
                    </Box>
                  } 
                  secondary={new Date(account.updatedAt).toLocaleString()} 
                />
              </ListItem>
            </List>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <Box sx={{ textAlign: 'center', p: 2 }}>
              <Typography variant="h6" color="textSecondary">
                账户余额
              </Typography>
              <Typography variant="h4" color="primary" sx={{ mt: 1 }}>
                ¥{account.balance.toFixed(2)}
              </Typography>
            </Box>
          </Grid>
        </Grid>
      </Paper>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h5" gutterBottom>
          备注信息
        </Typography>
        <Divider sx={{ mb: 3 }} />
        <Typography variant="body1">
          {account.remark || '无备注'}
        </Typography>
      </Paper>

      <Dialog open={editDialogOpen} onClose={handleCancelEdit} maxWidth="sm" fullWidth>
        <DialogTitle>编辑结算账户</DialogTitle>
        <DialogContent>
          <Grid container spacing={2}>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="账户名称"
                fullWidth
                value={editData.accountName || ''}
                onChange={(e) => setEditData({ ...editData, accountName: e.target.value })}
                disabled={saveLoading}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="账户类型"
                fullWidth
                value={editData.accountType || ''}
                onChange={(e) => setEditData({ ...editData, accountType: e.target.value })}
                disabled={saveLoading}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="开户银行"
                fullWidth
                value={editData.bankName || ''}
                onChange={(e) => setEditData({ ...editData, bankName: e.target.value })}
                disabled={saveLoading}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="账号"
                fullWidth
                value={editData.accountNumber || ''}
                onChange={(e) => setEditData({ ...editData, accountNumber: e.target.value })}
                disabled={saveLoading}
              />
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="状态"
                fullWidth
                select
                value={editData.status || 'ACTIVE'}
                onChange={(e) => setEditData({ ...editData, status: e.target.value as 'ACTIVE' | 'INACTIVE' })}
                disabled={saveLoading}
              >
                <MenuItem value="ACTIVE">启用</MenuItem>
                <MenuItem value="INACTIVE">禁用</MenuItem>
              </TextField>
            </Grid>
            <Grid size={{ xs: 12 }}>
              <TextField
                label="备注"
                fullWidth
                multiline
                rows={3}
                value={editData.remark || ''}
                onChange={(e) => setEditData({ ...editData, remark: e.target.value })}
                disabled={saveLoading}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCancelEdit} startIcon={<CancelIcon />} disabled={saveLoading}>
            取消
          </Button>
          <Button onClick={handleSaveEdit} variant="contained" startIcon={<SaveIcon />} disabled={saveLoading}>
            保存
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default SettlementAccountDetail;