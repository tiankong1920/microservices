import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Button, CircularProgress, Chip, Grid, IconButton, Tooltip } from '@mui/material';
import { ArrowBack as ArrowBackIcon, Edit as EditIcon, Print as PrintIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { otherStockApi } from '../services/api';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import type { ApiResponse } from '../types';

interface OtherStockItem {
  productName: string;
  productSku: string;
  quantity: number;
  unit: string;
  unitPrice: number;
  totalAmount: number;
}

interface OtherStockOrder {
  id: number;
  orderNumber: string;
  orderType: string;
  orderDate: string;
  warehouseName: string;
  operatorName: string;
  status: string;
  totalAmount: number;
  notes?: string;
  items?: OtherStockItem[];
}

const OtherStockDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [order, setOrder] = useState<OtherStockOrder | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [deleteLoading, setDeleteLoading] = useState<boolean>(false);
  const { showSuccess, showError } = useToast();

  const loadOrder = async (): Promise<void> => {
    if (!id) {
      setError('订单ID不能为空');
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response: ApiResponse<OtherStockOrder> = await otherStockApi.getOtherStockById(Number(id));
      setOrder(response.data);
    } catch (err) {
      console.error('Failed to load other stock order:', err);
      setError('加载其他出入库单详情失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOrder();
  }, [id]);

  const handleBack = (): void => {
    navigate('/other-stock');
  };

  const handleEdit = (): void => {
    if (id) {
      navigate(`/other-stock/${id}/edit`);
    }
  };

  const handleDelete = (): void => {
    setDeleteDialogOpen(true);
  };

  const confirmDelete = async (): Promise<void> => {
    if (!order) return;

    setDeleteLoading(true);
    setError(null);
    try {
      await otherStockApi.deleteOtherStock(order.id);
      showSuccess('其他出入库单删除成功');
      setDeleteDialogOpen(false);
      navigate('/other-stock');
    } catch (err) {
      console.error('Failed to delete other stock order:', err);
      showError('删除其他出入库单失败');
    } finally {
      setDeleteLoading(false);
    }
  };

  const handlePrint = (): void => {
    if (!order) return;
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

  const getOrderTypeLabel = (type: string): string => {
    switch (type) {
      case 'IN': return '入库';
      case 'OUT': return '出库';
      default: return type;
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

  if (!order) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h6" color="info" gutterBottom>
          未找到出入库单
        </Typography>
      </Box>
    );
  }

  return (
    <Box>
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个出入库单吗？删除后无法恢复。"
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
            其他出入库单详情
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
              <strong>单号:</strong> {order.orderNumber}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>类型:</strong>{' '}
              <Chip 
                label={getOrderTypeLabel(order.orderType)} 
                color={order.orderType === 'IN' ? 'success' : 'error'} 
                size="small" 
              />
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>日期:</strong> {order.orderDate}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>仓库:</strong> {order.warehouseName}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>操作员:</strong> {order.operatorName}
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>状态:</strong>{' '}
              <Chip label={order.status} color={getStatusColor(order.status)} size="small" />
            </Typography>
          </Grid>
          <Grid size={{ xs: 12, sm: 6 }}>
            <Typography variant="body1">
              <strong>总金额:</strong> ¥{order.totalAmount}
            </Typography>
          </Grid>
          {order.notes && (
            <Grid size={{ xs: 12 }}>
              <Typography variant="body1">
                <strong>备注:</strong> {order.notes}
              </Typography>
            </Grid>
          )}
        </Grid>
      </Paper>

      {order.items && order.items.length > 0 && (
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            商品明细
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
                  <TableCell>金额</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {order.items.map((item, index) => (
                  <TableRow key={index}>
                    <TableCell>{item.productName}</TableCell>
                    <TableCell>{item.productSku}</TableCell>
                    <TableCell>{item.quantity}</TableCell>
                    <TableCell>{item.unit}</TableCell>
                    <TableCell>¥{item.unitPrice}</TableCell>
                    <TableCell>¥{item.totalAmount}</TableCell>
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

export default OtherStockDetail;