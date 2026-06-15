import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableRow, Button, CircularProgress, Grid, Dialog, DialogTitle, DialogContent, DialogActions, TextField, IconButton, Tooltip } from '@mui/material';
import { ArrowBack as ArrowBackIcon, Edit as EditIcon, Delete as DeleteIcon, Print as PrintIcon } from '@mui/icons-material';
import { receiptApi } from '../services/api';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import type { Receipt } from '../types';

const ReceiptDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [receipt, setReceipt] = useState<Receipt | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [editDialogOpen, setEditDialogOpen] = useState<boolean>(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [editData, setEditData] = useState<Partial<Receipt>>({});
  const { showSuccess, showError } = useToast();

  const loadReceiptDetail = async (): Promise<void> => {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const response = await receiptApi.getReceiptById(Number(id));
      setReceipt(response.data);
    } catch (err) {
      console.error('Failed to load receipt detail:', err);
      setError('加载收款单详情失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (id) {
      loadReceiptDetail();
    }
  }, [id]);

  const handleBackToList = (): void => {
    navigate('/receipts');
  };

  const handleEdit = (): void => {
    if (!receipt) return;
    setEditData({
      receiptNumber: receipt.receiptNumber,
      customerName: receipt.customerName,
      relatedOrderNumber: receipt.relatedOrderNumber,
      paymentMethod: receipt.paymentMethod,
      settlementAccountName: receipt.settlementAccountName,
      receiptAmount: receipt.receiptAmount,
      actualReceiptAmount: receipt.actualReceiptAmount,
      differenceAmount: receipt.differenceAmount,
      status: receipt.status,
      receiptDate: receipt.receiptDate,
      description: receipt.description,
    });
    setEditDialogOpen(true);
  };

  const handleSaveEdit = async (): Promise<void> => {
    if (!receipt || !editData) return;

    setLoading(true);
    setError(null);
    try {
      await receiptApi.updateReceipt(receipt.id, editData);
      showSuccess('收款单更新成功');
      setEditDialogOpen(false);
      loadReceiptDetail();
    } catch (err) {
      console.error('Failed to update receipt:', err);
      showError('更新收款单失败');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = (): void => {
    setDeleteDialogOpen(true);
  };

  const confirmDelete = async (): Promise<void> => {
    if (!receipt) return;

    setLoading(true);
    setError(null);
    try {
      await receiptApi.deleteReceipt(receipt.id);
      showSuccess('收款单删除成功');
      setDeleteDialogOpen(false);
      navigate('/receipts');
    } catch (err) {
      console.error('Failed to delete receipt:', err);
      showError('删除收款单失败');
    } finally {
      setLoading(false);
    }
  };

  const handlePrint = (): void => {
    if (!receipt) return;
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

  if (!receipt) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h6" color="info" gutterBottom>
          收款单不存在
        </Typography>
      </Box>
    );
  }

  return (
    <Box>
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个收款单吗？删除后无法恢复。"
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
          收款单详情
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
              <Table size="small" aria-label="receipt basic info">
                <TableBody>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>收款单号</TableCell>
                    <TableCell>{receipt.receiptNumber}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>客户名称</TableCell>
                    <TableCell>{receipt.customerName}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>相关订单号</TableCell>
                    <TableCell>{receipt.relatedOrderNumber}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>收款方式</TableCell>
                    <TableCell>{receipt.paymentMethod}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>结算账户</TableCell>
                    <TableCell>{receipt.settlementAccountName}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>收款状态</TableCell>
                    <TableCell>
                      <Box sx={{ 
                        fontWeight: 'bold',
                        color: receipt.status === '已收款' ? '#388e3c' : 
                               receipt.status === '已取消' ? '#9e9e9e' : '#f57c00'
                      }}>
                        {receipt.status}
                      </Box>
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>收款日期</TableCell>
                    <TableCell>{receipt.receiptDate}</TableCell>
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
              <Table size="small" aria-label="receipt amount info">
                <TableBody>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>收款金额</TableCell>
                    <TableCell sx={{ fontWeight: 'bold', color: '#388e3c' }}>
                      ¥{receipt.receiptAmount}
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>实际收款</TableCell>
                    <TableCell>¥{receipt.actualReceiptAmount}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>差异金额</TableCell>
                    <TableCell>¥{receipt.differenceAmount}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>收款状态</TableCell>
                    <TableCell>
                      <Box sx={{ 
                        fontWeight: 'bold',
                        color: receipt.status === '已收款' ? '#388e3c' : 
                               receipt.status === '已取消' ? '#9e9e9e' : '#f57c00'
                      }}>
                        {receipt.status}
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
              备注信息
            </Typography>
            <Typography variant="body1" paragraph>
              {receipt.description || '无备注'}
            </Typography>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 2 }}>
              <Typography variant="body2" color="text.secondary">
                创建时间: {receipt.createdAt}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                更新时间: {receipt.updatedAt}
              </Typography>
            </Box>
          </Paper>
        </Grid>
      </Grid>

      <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>编辑收款单</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="客户名称"
            fullWidth
            variant="outlined"
            value={editData.customerName}
            onChange={(e) => setEditData({ ...editData, customerName: e.target.value })}
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
            label="收款方式"
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
            label="收款金额"
            type="number"
            fullWidth
            variant="outlined"
            value={editData.receiptAmount}
            onChange={(e) => setEditData({ ...editData, receiptAmount: Number(e.target.value) })}
          />
          <TextField
            margin="dense"
            label="实际收款"
            type="number"
            fullWidth
            variant="outlined"
            value={editData.actualReceiptAmount}
            onChange={(e) => setEditData({ ...editData, actualReceiptAmount: Number(e.target.value) })}
          />
          <TextField
            margin="dense"
            label="收款状态"
            fullWidth
            variant="outlined"
            value={editData.status}
            onChange={(e) => setEditData({ ...editData, status: e.target.value })}
          />
          <TextField
            margin="dense"
            label="收款日期"
            type="date"
            fullWidth
            variant="outlined"
            value={editData.receiptDate}
            onChange={(e) => setEditData({ ...editData, receiptDate: e.target.value })}
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

export default ReceiptDetail;
