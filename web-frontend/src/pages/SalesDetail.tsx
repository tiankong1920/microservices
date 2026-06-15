import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
  Button, IconButton, Tooltip, CircularProgress, Backdrop, Select, MenuItem, FormControl 
} from '@mui/material';
import { ArrowBack as ArrowBackIcon, Edit as EditIcon, Print as PrintIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { salesApi } from '../services/api';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import type { ApiResponse, SalesOrder, SalesOrderItem } from '../types';

interface SaleItem {
  id: number;
  productName: string;
  productCode: string;
  unitPrice: number;
  quantity: number;
}

interface SaleDetailData extends SalesOrder {
  saleItems?: SaleItem[];
}

interface SaleStatus {
  value: string;
  label: string;
}

const SalesDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [sale, setSale] = useState<SaleDetailData | null>(null);
  const [saleItems, setSaleItems] = useState<SaleItem[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [isEditingStatus, setIsEditingStatus] = useState<boolean>(false);
  const [newStatus, setNewStatus] = useState<string>('');
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [deleteLoading, setDeleteLoading] = useState<boolean>(false);
  const { showSuccess, showError } = useToast();
  
  const saleStatuses: SaleStatus[] = [
    { value: '待付款', label: '待付款' },
    { value: '已付款', label: '已付款' },
    { value: '处理中', label: '处理中' },
    { value: '已发货', label: '已发货' },
    { value: '已完成', label: '已完成' },
    { value: '已取消', label: '已取消' }
  ];

  const loadSaleDetail = async (): Promise<void> => {
    if (!id) {
      setError('销售订单ID不能为空');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const response: ApiResponse<SaleDetailData> = await salesApi.getSaleById(Number(id));
      const data = response.data;
      setSale(data);
      setSaleItems(data.saleItems || []);
      setNewStatus(data.status);
    } catch (err) {
      console.error('Failed to load sale detail:', err);
      setError('加载销售订单详情失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSaleDetail();
  }, [id]);

  const handleBack = (): void => {
    navigate('/sales');
  };

  const handleStartEditStatus = (): void => {
    setIsEditingStatus(true);
  };

  const handleSaveStatus = async (): Promise<void> => {
    if (!sale || !newStatus || newStatus === sale.status) {
      setIsEditingStatus(false);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      await salesApi.updateSaleStatus(sale.id, newStatus);
      showSuccess('销售订单状态更新成功');
      setIsEditingStatus(false);
      loadSaleDetail();
    } catch (err) {
      console.error('Failed to update sale status:', err);
      showError('销售订单状态更新失败');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelEditStatus = (): void => {
    setNewStatus(sale?.status || '');
    setIsEditingStatus(false);
  };

  const handleDelete = (): void => {
    setDeleteDialogOpen(true);
  };

  const confirmDelete = async (): Promise<void> => {
    if (!sale) return;

    setDeleteLoading(true);
    setError(null);
    try {
      await salesApi.deleteSale(sale.id);
      showSuccess('销售订单删除成功');
      setDeleteDialogOpen(false);
      navigate('/sales');
    } catch (err) {
      console.error('Failed to delete sale:', err);
      showError('删除销售订单失败');
    } finally {
      setDeleteLoading(false);
    }
  };

  const handlePrint = (): void => {
    if (!sale) return;
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

  if (loading && !sale) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error && !sale) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h6" color="error" gutterBottom>
          {error}
        </Typography>
      </Box>
    );
  }

  return (
    <Box>
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个销售订单吗？删除后无法恢复。"
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
            销售订单详情
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
          销售订单基本信息
        </Typography>
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' }, gap: 2, mb: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>销售订单号：</Typography>
            <Typography variant="body1">{sale?.saleNumber}</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>客户：</Typography>
            <Typography variant="body1">{sale?.customerName}</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>下单日期：</Typography>
            <Typography variant="body1">{sale?.saleDate}</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>销售金额：</Typography>
            <Typography variant="body1" sx={{ color: '#d32f2f', fontWeight: 'bold' }}>¥{sale?.totalAmount}</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>销售员：</Typography>
            <Typography variant="body1">{sale?.salesperson}</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>订单状态：</Typography>
            {isEditingStatus ? (
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <FormControl size="small" sx={{ minWidth: 120 }}>
                  <Select
                    value={newStatus}
                    onChange={(e) => setNewStatus(e.target.value as string)}
                    displayEmpty
                  >
                    {saleStatuses.map((status) => (
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
                  color: getStatusColor(sale?.status || '')
                }}>
                  {sale?.status}
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
          销售订单商品列表
        </Typography>
        <TableContainer>
          <Table sx={{ minWidth: 650 }} aria-label="sale items table">
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
              {saleItems.map((item) => (
                <TableRow
                  key={item.id}
                  sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                >
                  <TableCell>{item.productName}</TableCell>
                  <TableCell>{item.productCode}</TableCell>
                  <TableCell>¥{item.unitPrice}</TableCell>
                  <TableCell>{item.quantity}</TableCell>
                  <TableCell>¥{item.unitPrice * item.quantity}</TableCell>
                </TableRow>
              ))}
              <TableRow>
                <TableCell colSpan={3}></TableCell>
                <TableCell sx={{ fontWeight: 'bold' }}>总计</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#d32f2f' }}>¥{sale?.totalAmount}</TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-start' }}>
        <Button variant="contained" onClick={handleBack} startIcon={<ArrowBackIcon />}>
          返回销售管理
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

export default SalesDetail;