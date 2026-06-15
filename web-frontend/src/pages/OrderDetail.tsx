import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
  Button, IconButton, Tooltip, CircularProgress, Backdrop, Select, MenuItem, FormControl 
} from '@mui/material';
import { ArrowBack as ArrowBackIcon, Edit as EditIcon, Print as PrintIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { orderApi } from '../services/api';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import type { Order, OrderItem, ApiResponse } from '../types';

interface OrderDetailData {
  id: number;
  orderNumber: string;
  customerName: string;
  orderDate: string;
  amount: number;
  paymentMethod?: string;
  status: string;
  orderItems?: OrderItem[];
}

const OrderDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [order, setOrder] = useState<OrderDetailData | null>(null);
  const [orderItems, setOrderItems] = useState<OrderItem[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [isEditingStatus, setIsEditingStatus] = useState<boolean>(false);
  const [newStatus, setNewStatus] = useState<string>('');
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [deleteLoading, setDeleteLoading] = useState<boolean>(false);
  const { showSuccess, showError } = useToast();
  
  const orderStatuses = [
    { value: '待付款', label: '待付款' },
    { value: '已付款', label: '已付款' },
    { value: '处理中', label: '处理中' },
    { value: '已发货', label: '已发货' },
    { value: '已完成', label: '已完成' },
    { value: '已取消', label: '已取消' }
  ];

  const loadOrderDetail = async (): Promise<void> => {
    if (!id) {
      setError('订单ID不能为空');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response: ApiResponse<Order> = await orderApi.getOrderById(Number(id));
      const data = response.data;
      setOrder({
        id: data.id,
        orderNumber: data.orderNo,
        customerName: data.customerName,
        orderDate: data.orderDate,
        amount: data.totalAmount,
        paymentMethod: data.paymentMethod,
        status: data.status,
        orderItems: data.items
      });
      setOrderItems(data.items || []);
      setNewStatus(data.status);
    } catch (err) {
      console.error('Failed to load order detail:', err);
      setError('加载订单详情失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOrderDetail();
  }, [id]);

  const handleBack = (): void => {
    navigate('/orders');
  };

  const handleStartEditStatus = (): void => {
    setIsEditingStatus(true);
  };

  const handleSaveStatus = async (): Promise<void> => {
    if (!order || !newStatus || newStatus === order.status) {
      setIsEditingStatus(false);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      await orderApi.updateOrderStatus(order.id, newStatus);
      showSuccess('订单状态更新成功');
      setIsEditingStatus(false);
      loadOrderDetail();
    } catch (err) {
      console.error('Failed to update order status:', err);
      showError('订单状态更新失败');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelEditStatus = (): void => {
    setNewStatus(order?.status || '');
    setIsEditingStatus(false);
  };

  const handleStatusChange = (e: { target: { value: string } }): void => {
    setNewStatus(e.target.value);
  };

  const handleDelete = (): void => {
    setDeleteDialogOpen(true);
  };

  const confirmDelete = async (): Promise<void> => {
    if (!order) return;

    setDeleteLoading(true);
    setError(null);
    try {
      await orderApi.deleteOrder(order.id);
      showSuccess('订单删除成功');
      setDeleteDialogOpen(false);
      navigate('/orders');
    } catch (err) {
      console.error('Failed to delete order:', err);
      showError('删除订单失败');
    } finally {
      setDeleteLoading(false);
    }
  };

  const handlePrint = (): void => {
    if (!order) return;
    window.print();
  };

  const getStatusColor = (status: string): string => {
    switch (status) {
      case '已完成': return '#388e3c';
      case '已取消': return '#9e9e9e';
      case '待付款': return '#f57c00';
      default: return '#1976d2';
    }
  };

  if (loading && !order) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error && !order) {
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
          订单不存在
        </Typography>
      </Box>
    );
  }

  return (
    <Box>
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个订单吗？删除后无法恢复。"
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
            订单详情
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
          订单基本信息
        </Typography>
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' }, gap: 2, mb: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>订单号：</Typography>
            <Typography variant="body1">{order.orderNumber}</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>客户名称：</Typography>
            <Typography variant="body1">{order.customerName}</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>下单日期：</Typography>
            <Typography variant="body1">{order.orderDate}</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>订单金额：</Typography>
            <Typography variant="body1" sx={{ color: '#d32f2f', fontWeight: 'bold' }}>¥{order.amount}</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>支付方式：</Typography>
            <Typography variant="body1">{order.paymentMethod || '未选择'}</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>订单状态：</Typography>
            {isEditingStatus ? (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <FormControl size="small" sx={{ minWidth: 120 }}>
                  <Select
                    value={newStatus}
                    onChange={handleStatusChange}
                    displayEmpty
                  >
                    {orderStatuses.map((status) => (
                      <MenuItem key={status.value} value={status.value}>
                        {status.label}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
                <Button variant="contained" size="small" onClick={handleSaveStatus}>
                  保存
                </Button>
                <Button variant="outlined" size="small" onClick={handleCancelEditStatus}>
                  取消
                </Button>
              </Box>
            ) : (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Typography variant="body1" sx={{ 
                  fontWeight: 'bold',
                  color: getStatusColor(order.status)
                }}>
                  {order.status}
                </Typography>
                <Tooltip title="修改状态">
                  <IconButton size="small" onClick={handleStartEditStatus}>
                    <EditIcon />
                  </IconButton>
                </Tooltip>
              </Box>
            )}
          </Box>
        </Box>
      </Paper>

      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>
          订单商品列表
        </Typography>
        <TableContainer>
          <Table sx={{ minWidth: 650 }} aria-label="order items table">
            <TableHead>
              <TableRow>
                <TableCell>商品名称</TableCell>
                <TableCell>商品编码</TableCell>
                <TableCell>单价</TableCell>
                <TableCell>数量</TableCell>
                <TableCell>小计</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {orderItems.map((item) => (
                <TableRow
                  key={item.id}
                  sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                >
                  <TableCell>{item.productName}</TableCell>
                  <TableCell>{item.productId}</TableCell>
                  <TableCell>¥{item.unitPrice}</TableCell>
                  <TableCell>{item.quantity}</TableCell>
                  <TableCell>¥{item.totalPrice}</TableCell>
                </TableRow>
              ))}
              <TableRow>
                <TableCell colSpan={3}></TableCell>
                <TableCell sx={{ fontWeight: 'bold' }}>总计</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#d32f2f' }}>¥{order.amount}</TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-start' }}>
        <Button variant="contained" onClick={handleBack} startIcon={<ArrowBackIcon />}>
          返回订单管理
        </Button>
      </Box>

      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={loading}
      >
        <CircularProgress color="inherit" />
      </Backdrop>
    </Box>
  );
};

export default OrderDetail;