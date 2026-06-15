import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableRow, Button, CircularProgress, Grid, Dialog, DialogTitle, DialogContent, DialogActions, TextField, IconButton, Tooltip } from '@mui/material';
import { ArrowBack as ArrowBackIcon, Edit as EditIcon, Delete as DeleteIcon, Print as PrintIcon } from '@mui/icons-material';
import { paymentApi } from '../services/api';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import type { Payment } from '../types';

const PaymentDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [payment, setPayment] = useState<Payment | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [editDialogOpen, setEditDialogOpen] = useState<boolean>(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [editData, setEditData] = useState<Partial<Payment>>({});
  const { showSuccess, showError } = useToast();

  const loadPaymentDetail = async (): Promise<void> => {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const response = await paymentApi.getPaymentById(Number(id));
      setPayment(response.data);
    } catch (err) {
      console.error('Failed to load payment detail:', err);
      setError('加载付款单详情失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (id) {
      loadPaymentDetail();
    }
  }, [id]);

  const handleBackToList = (): void => {
    navigate('/payments');
  };

  const handleEdit = (): void => {
    if (!payment) return;
    setEditData({
      paymentNumber: payment.paymentNumber,
      supplierName: payment.supplierName,
      relatedOrderNumber: payment.relatedOrderNumber,
      paymentMethod: payment.paymentMethod,
      settlementAccountName: payment.settlementAccountName,
      paymentAmount: payment.paymentAmount,
      actualPaymentAmount: payment.actualPaymentAmount,
      differenceAmount: payment.differenceAmount,
      status: payment.status,
      paymentDate: payment.paymentDate,
      description: payment.description,
    });
    setEditDialogOpen(true);
  };

  const handleSaveEdit = async (): Promise<void> => {
    if (!payment || !editData) return;

    setLoading(true);
    setError(null);
    try {
      await paymentApi.updatePayment(payment.id, editData);
      showSuccess('付款单更新成功');
      setEditDialogOpen(false);
      loadPaymentDetail();
    } catch (err) {
      console.error('Failed to update payment:', err);
      showError('更新付款单失败');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = (): void => {
    setDeleteDialogOpen(true);
  };

  const confirmDelete = async (): Promise<void> => {
    if (!payment) return;

    setLoading(true);
    setError(null);
    try {
      await paymentApi.deletePayment(payment.id);
      showSuccess('付款单删除成功');
      setDeleteDialogOpen(false);
      navigate('/payments');
    } catch (err) {
      console.error('Failed to delete payment:', err);
      showError('删除付款单失败');
    } finally {
      setLoading(false);
    }
  };

  const handlePrint = (): void => {
    if (!payment) return;
    window.print();
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 300 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h6" color="error" gutterBottom>
          {error}
        </Typography>
      </Box>
    );
  }

  if (!payment) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h6" color="info" gutterBottom>
          付款单不存在
        </Typography>
      </Box>
    );
  }

  return (
    <Box>
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个付款单吗？删除后无法恢复。"
        confirmText="删除"
        cancelText="取消"
        severity="warning"
        onConfirm={confirmDelete}
        onCancel={() => setDeleteDialogOpen(false)}
        loading={loading}
      />

      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={handleBackToList}
          sx={{ mr: 2 }}
        >
          返回列表
        </Button>
        <Typography variant="h4" gutterBottom>
          付款单详情
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="contained"
            startIcon={<EditIcon />}
            onClick={handleEdit}
            disabled={loading}
          >
            编辑
          </Button>
          <Button
            variant="outlined"
            startIcon={<DeleteIcon />}
            onClick={handleDelete}
            color="error"
            disabled={loading}
          >
            删除
          </Button>
          <Button
            variant="outlined"
            startIcon={<PrintIcon />}
            onClick={handlePrint}
            disabled={loading}
          >
            打印
          </Button>
        </Box>
      </Box>

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              基本信息
            </Typography>
            <TableContainer>
              <Table size="small" aria-label="payment basic info">
                <TableBody>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>付款单号</TableCell>
                    <TableCell>{payment.paymentNumber}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>供应商名称</TableCell>
                    <TableCell>{payment.supplierName}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>相关订单号</TableCell>
                    <TableCell>{payment.relatedOrderNumber}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>付款方式</TableCell>
                    <TableCell>{payment.paymentMethod}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>结算账户</TableCell>
                    <TableCell>{payment.settlementAccountName}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>付款状态</TableCell>
                    <TableCell>
                      <Box sx={{ 
                        fontWeight: 'bold',
                        color: payment.status === '已付款' ? '#388e3c' : 
                               payment.status === '已取消' ? '#9e9e9e' : '#f57c00'
                      }}>
                        {payment.status}
                      </Box>
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>付款日期</TableCell>
                    <TableCell>{payment.paymentDate}</TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              金额信息
            </Typography>
            <TableContainer>
              <Table size="small" aria-label="payment amount info">
                <TableBody>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>付款金额</TableCell>
                    <TableCell sx={{ fontWeight: 'bold', color: '#d32f2f' }}>
                      ¥{payment.paymentAmount}
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>实际付款</TableCell>
                    <TableCell>¥{payment.actualPaymentAmount}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>差异金额</TableCell>
                    <TableCell>¥{payment.differenceAmount}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>付款状态</TableCell>
                    <TableCell>
                      <Box sx={{ 
                        fontWeight: 'bold',
                        color: payment.status === '已付款' ? '#388e3c' : 
                               payment.status === '已取消' ? '#9e9e9e' : '#f57c00'
                      }}>
                        {payment.status}
                      </Box>
                    </TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              时间信息
            </Typography>
            <TableContainer>
              <Table size="small" aria-label="payment time info">
                <TableBody>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>付款日期</TableCell>
                    <TableCell>{payment.paymentDate}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>创建时间</TableCell>
                    <TableCell>{payment.createdAt}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>更新时间</TableCell>
                    <TableCell>{payment.updatedAt}</TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>
        <Grid size={{ xs: 12 }}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              备注信息
            </Typography>
            <Typography variant="body1" paragraph>
              {payment.description || '无备注'}
            </Typography>
          </Paper>
        </Grid>
      </Grid>

      <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>编辑付款单</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="供应商名称"
            fullWidth
            variant="outlined"
            value={editData.supplierName}
            onChange={(e) => setEditData({ ...editData, supplierName: e.target.value })}
          />
          <TextField
            margin="dense"
            label="相关订单号"
            fullWidth
            variant="outlined"
            value={editData.relatedOrderNumber}
            onChange={(e) => setEditData({ ...editData, relatedOrderNumber: e.target.value })}
          />
          <TextField
            margin="dense"
            label="付款方式"
            fullWidth
            variant="outlined"
            value={editData.paymentMethod}
            onChange={(e) => setEditData({ ...editData, paymentMethod: e.target.value })}
          />
          <TextField
            margin="dense"
            label="结算账户"
            fullWidth
            variant="outlined"
            value={editData.settlementAccountName}
            onChange={(e) => setEditData({ ...editData, settlementAccountName: e.target.value })}
          />
          <TextField
            margin="dense"
            label="付款金额"
            type="number"
            fullWidth
            variant="outlined"
            value={editData.paymentAmount}
            onChange={(e) => setEditData({ ...editData, paymentAmount: Number(e.target.value) })}
          />
          <TextField
            margin="dense"
            label="实际付款"
            type="number"
            fullWidth
            variant="outlined"
            value={editData.actualPaymentAmount}
            onChange={(e) => setEditData({ ...editData, actualPaymentAmount: Number(e.target.value) })}
          />
          <TextField
            margin="dense"
            label="付款状态"
            fullWidth
            variant="outlined"
            value={editData.status}
            onChange={(e) => setEditData({ ...editData, status: e.target.value })}
          />
          <TextField
            margin="dense"
            label="付款日期"
            type="date"
            fullWidth
            variant="outlined"
            value={editData.paymentDate}
            onChange={(e) => setEditData({ ...editData, paymentDate: e.target.value })}
          />
          <TextField
            margin="dense"
            label="备注"
            fullWidth
            variant="outlined"
            multiline
            rows={3}
            value={editData.description}
            onChange={(e) => setEditData({ ...editData, description: e.target.value })}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditDialogOpen(false)}>取消</Button>
          <Button onClick={handleSaveEdit} variant="contained">保存</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default PaymentDetail;
