import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Button, CircularProgress, Chip, Divider, Grid, IconButton, Tooltip } from '@mui/material';
import { ArrowBack as ArrowBackIcon, Edit as EditIcon, Print as PrintIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { salesReturnApi } from '../services/api';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import type { ApiResponse } from '../types';

interface SalesReturnItem {
  productName: string;
  productSku: string;
  quantity: number;
  unit: string;
  unitPrice: number;
  discount: number;
  subtotal: number;
  reason?: string;
}

interface SalesReturnOrder {
  id: number;
  returnNumber: string;
  customerName: string;
  returnDate: string;
  status: string;
  subtotal: number;
  tax: number;
  discount: number;
  totalAmount: number;
  reason?: string;
  notes?: string;
  items?: SalesReturnItem[];
}

const SalesReturnDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [returnOrder, setReturnOrder] = useState<SalesReturnOrder | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [deleteLoading, setDeleteLoading] = useState<boolean>(false);
  const { showSuccess, showError } = useToast();

  const loadReturnOrder = async (): Promise<void> => {
    if (!id) {
      setError('退货单ID不能为空');
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response: ApiResponse<SalesReturnOrder> = await salesReturnApi.getSalesReturnById(Number(id));
      setReturnOrder(response.data);
    } catch (err) {
      console.error('Failed to load sales return order:', err);
      setError('加载销售退货单详情失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReturnOrder();
  }, [id]);

  const handleBack = (): void => {
    navigate('/sales-return');
  };

  const handleEdit = (): void => {
    if (id) {
      navigate(`/sales-return/${id}/edit`);
    }
  };

  const handleDelete = (): void => {
    setDeleteDialogOpen(true);
  };

  const confirmDelete = async (): Promise<void> => {
    if (!returnOrder) return;

    setDeleteLoading(true);
    setError(null);
    try {
      await salesReturnApi.deleteSalesReturn(returnOrder.id);
      showSuccess('销售退货单删除成功');
      setDeleteDialogOpen(false);
      navigate('/sales-return');
    } catch (err) {
      console.error('Failed to delete sales return order:', err);
      showError('删除销售退货单失败');
    } finally {
      setDeleteLoading(false);
    }
  };

  const handlePrint = (): void => {
    if (!returnOrder) return;
    window.print();
  };

  const getStatusColor = (status: string): 'success' | 'primary' | 'warning' | 'error' | 'default' => {
    switch (status) {
      case 'COMPLETED': return 'success';
      case 'APPROVED': return 'primary';
      case 'PENDING': return 'warning';
      case 'REJECTED': return 'error';
      case 'CANCELLED': return 'default';
      default: return 'default';
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

  if (!returnOrder) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h6" color="info" gutterBottom>
          未找到销售退货单
        </Typography>
      </Box>
    );
  }

  return (
    <Box>
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个销售退货单吗？删除后无法恢复。"
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
            销售退货单详情
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
              <strong>退货单号:</strong> {returnOrder.returnNumber}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>客户名称:</strong> {returnOrder.customerName}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>退货日期:</strong> {returnOrder.returnDate}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>订单状态:</strong>{' '}
              <Chip label={returnOrder.status} color={getStatusColor(returnOrder.status)} size="small" />
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>小计:</strong> ¥{returnOrder.subtotal}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>税额:</strong> ¥{returnOrder.tax}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>折扣:</strong> ¥{returnOrder.discount || 0}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>总金额:</strong> ¥{returnOrder.totalAmount}
            </Typography>
          </Grid>
          {returnOrder.reason && (
            <Grid size={{ xs: 12 }}>
              <Typography variant="body1">
                <strong>退货原因:</strong> {returnOrder.reason}
              </Typography>
            </Grid>
          )}
          {returnOrder.notes && (
            <Grid size={{ xs: 12 }}>
              <Typography variant="body1">
                <strong>备注:</strong> {returnOrder.notes}
              </Typography>
            </Grid>
          )}
        </Grid>
      </Paper>

      {returnOrder.items && returnOrder.items.length > 0 && (
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            退货商品明细
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
                  <TableCell>退货原因</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {returnOrder.items.map((item, index) => (
                  <TableRow key={index}>
                    <TableCell>{item.productName}</TableCell>
                    <TableCell>{item.productSku}</TableCell>
                    <TableCell>{item.quantity}</TableCell>
                    <TableCell>{item.unit}</TableCell>
                    <TableCell>¥{item.unitPrice}</TableCell>
                    <TableCell>¥{item.discount || 0}</TableCell>
                    <TableCell>¥{item.subtotal}</TableCell>
                    <TableCell>{item.reason || '-'}</TableCell>
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

export default SalesReturnDetail;