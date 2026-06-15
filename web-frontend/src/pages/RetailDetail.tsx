import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Button, CircularProgress, Chip, Grid, IconButton, Tooltip } from '@mui/material';
import { ArrowBack as ArrowBackIcon, Edit as EditIcon, Print as PrintIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { retailApi } from '../services/api';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import type { ApiResponse, RetailOrder } from '../types';

interface RetailItem {
  productName: string;
  productSku: string;
  quantity: number;
  unit: string;
  unitPrice: number;
  discount: number;
  subtotal: number;
}

const RetailDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [retailOrder, setRetailOrder] = useState<RetailOrder | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [deleteLoading, setDeleteLoading] = useState<boolean>(false);
  const { showSuccess, showError } = useToast();

  const loadRetailOrder = async (): Promise<void> => {
    if (!id) {
      setError('零售单ID不能为空');
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response: ApiResponse<RetailOrder> = await retailApi.getRetailById(Number(id));
      setRetailOrder(response.data);
    } catch (err) {
      console.error('Failed to load retail order:', err);
      setError('加载零售单详情失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRetailOrder();
  }, [id]);

  const handleBack = (): void => {
    navigate('/retail');
  };

  const handleEdit = (): void => {
    if (id) {
      navigate(`/retail/${id}/edit`);
    }
  };

  const handleDelete = (): void => {
    setDeleteDialogOpen(true);
  };

  const confirmDelete = async (): Promise<void> => {
    if (!retailOrder) return;

    setDeleteLoading(true);
    setError(null);
    try {
      await retailApi.deleteRetail(retailOrder.id);
      showSuccess('零售单删除成功');
      setDeleteDialogOpen(false);
      navigate('/retail');
    } catch (err) {
      console.error('Failed to delete retail order:', err);
      showError('删除零售单失败');
    } finally {
      setDeleteLoading(false);
    }
  };

  const handlePrint = (): void => {
    if (!retailOrder) return;
    window.print();
  };

  const getStatusColor = (status: string): 'success' | 'primary' | 'warning' | 'default' => {
    switch (status) {
      case 'COMPLETED': return 'success';
      case 'APPROVED': return 'primary';
      case 'PENDING': return 'warning';
      case 'REJECTED': return 'default';
      case 'CANCELLED': return 'default';
      default: return 'primary';
    }
  };

  const getPaymentStatusColor = (status: string): 'success' | 'warning' | 'error' | 'primary' => {
    switch (status) {
      case 'PAID': return 'success';
      case 'PENDING': return 'warning';
      case 'OVERDUE': return 'error';
      default: return 'primary';
    }
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
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

  if (!retailOrder) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h6" color="info" gutterBottom>
          未找到零售单
        </Typography>
      </Box>
    );
  }

  return (
    <Box>
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个零售单吗？删除后无法恢复。"
        confirmText="删除"
        cancelText="取消"
        severity="warning"
        onConfirm={confirmDelete}
        onCancel={() => setDeleteDialogOpen(false)}
        loading={deleteLoading}
      />

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <IconButton onClick={handleBack}>
            <ArrowBackIcon />
          </IconButton>
          <Typography variant="h4" gutterBottom>
            零售单详情
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="contained"
            startIcon={<PrintIcon />}
            onClick={handlePrint}
            disabled={loading}
          >
            打印
          </Button>
          <Button
            variant="outlined"
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
        </Box>
      </Box>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>
          基本信息
        </Typography>
        <Grid container spacing={2}>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>零售单号:</strong> {retailOrder.retailNumber}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>客户名称:</strong> {retailOrder.customerName}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>零售日期:</strong> {retailOrder.retailDate}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>订单状态:</strong>{' '}
              <Chip label={retailOrder.status} color={getStatusColor(retailOrder.status)} size="small" />
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>支付状态:</strong>{' '}
              <Chip label={retailOrder.paymentStatus} color={getPaymentStatusColor(retailOrder.paymentStatus)} size="small" />
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>支付方式:</strong> {retailOrder.paymentMethod || '-'}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>小计:</strong> ¥{retailOrder.subtotal}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>税额:</strong> ¥{retailOrder.tax}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>折扣:</strong> ¥{retailOrder.discount || 0}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>总金额:</strong> ¥{retailOrder.totalAmount}
            </Typography>
          </Grid>
          {retailOrder.notes && (
            <Grid size={{ xs: 12 }}>
              <Typography variant="body1">
                <strong>备注:</strong> {retailOrder.notes}
              </Typography>
            </Grid>
          )}
        </Grid>
      </Paper>

      {retailOrder.items && retailOrder.items.length > 0 && (
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            零售商品明细
          </Typography>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>产品名称</TableCell>
                  <TableCell>SKU</TableCell>
                  <TableCell>数量</TableCell>
                  <TableCell>单位</TableCell>
                  <TableCell>单价</TableCell>
                  <TableCell>折扣</TableCell>
                  <TableCell>小计</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {retailOrder.items.map((item, index) => (
                  <TableRow
                    key={index}
                    sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                  >
                    <TableCell>{item.productName}</TableCell>
                    <TableCell>{item.productSku}</TableCell>
                    <TableCell>{item.quantity}</TableCell>
                    <TableCell>{item.unit}</TableCell>
                    <TableCell>¥{item.unitPrice}</TableCell>
                    <TableCell>¥{item.discount || 0}</TableCell>
                    <TableCell>¥{item.subtotal}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Paper>
      )}
    </Box>
  );
};

export default RetailDetail;