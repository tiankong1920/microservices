import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { 
  Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
  Button, IconButton, Tooltip, CircularProgress, Backdrop, Select, MenuItem, FormControl 
} from '@mui/material';
import { ArrowBack as ArrowBackIcon, Edit as EditIcon, Print as PrintIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { procurementApi } from '../services/api';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import type { ProcurementOrder, ProcurementOrderItem } from '../types';

interface ProcurementItem extends ProcurementOrderItem {
  productCode?: string;
}

interface Procurement extends ProcurementOrder {
  procurementNumber?: string;
  purchaser?: string;
  procurementItems?: ProcurementItem[];
}

interface ProcurementStatus {
  value: string;
  label: string;
}

const ProcurementDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [procurement, setProcurement] = useState<Procurement | null>(null);
  const [procurementItems, setProcurementItems] = useState<ProcurementItem[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [isEditingStatus, setIsEditingStatus] = useState<boolean>(false);
  const [newStatus, setNewStatus] = useState<string>('');
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [deleteLoading, setDeleteLoading] = useState<boolean>(false);
  const { showSuccess, showError } = useToast();
  
  const procurementStatuses: ProcurementStatus[] = [
    { value: '待审批', label: '待审批' },
    { value: '已审批', label: '已审批' },
    { value: '已拒绝', label: '已拒绝' },
    { value: '已下单', label: '已下单' },
    { value: '已完成', label: '已完成' },
    { value: '已取消', label: '已取消' }
  ];

  const loadProcurementDetail = async (): Promise<void> => {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const response = await procurementApi.getProcurementById(Number(id));
      const data = response.data;
      setProcurement({
        ...data,
        procurementNumber: data.orderNo,
        procurementItems: data.items || []
      });
      setProcurementItems(data.items || []);
      setNewStatus(data.status);
    } catch (err) {
      console.error('Failed to load procurement detail:', err);
      setError('加载采购订单详情失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProcurementDetail();
  }, [id]);

  const handleBack = (): void => {
    navigate('/procurement');
  };

  const handleStartEditStatus = (): void => {
    setIsEditingStatus(true);
  };

  const handleSaveStatus = async (): Promise<void> => {
    if (!newStatus || newStatus === procurement?.status) {
      setIsEditingStatus(false);
      return;
    }

    setLoading(true);
    setError(null);
    try {
      await procurementApi.updateProcurementStatus(procurement!.id, newStatus);
      showSuccess('采购订单状态更新成功');
      setIsEditingStatus(false);
      loadProcurementDetail();
    } catch (err) {
      console.error('Failed to update procurement status:', err);
      showError('采购订单状态更新失败');
    } finally {
      setLoading(false);
    }
  };

  const handleCancelEditStatus = (): void => {
    setNewStatus(procurement?.status || '');
    setIsEditingStatus(false);
  };

  const handleDelete = (): void => {
    setDeleteDialogOpen(true);
  };

  const confirmDelete = async (): Promise<void> => {
    if (!procurement) return;

    setDeleteLoading(true);
    setError(null);
    try {
      await procurementApi.deleteProcurement(procurement.id);
      showSuccess('采购订单删除成功');
      setDeleteDialogOpen(false);
      navigate('/procurement');
    } catch (err) {
      console.error('Failed to delete procurement:', err);
      showError('删除采购订单失败');
    } finally {
      setDeleteLoading(false);
    }
  };

  const handlePrint = (): void => {
    if (!procurement) return;
    window.print();
  };

  const getStatusColor = (status: string): string => {
    switch (status) {
      case '已完成': return '#388e3c';
      case '已拒绝': return '#9e9e9e';
      case '已取消': return '#9e9e9e';
      case '待审批': return '#f57c00';
      default: return '#1976d2';
    }
  };

  if (loading && !procurement) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '80vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error && !procurement) {
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
        content="确定要删除这个采购订单吗？删除后无法恢复。"
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
            采购订单详情
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
          采购订单基本信息
        </Typography>
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' }, gap: 2, mb: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>采购订单号：</Typography>
            <Typography variant="body1">{procurement?.procurementNumber}</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>供应商：</Typography>
            <Typography variant="body1">{procurement?.supplierName}</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>下单日期：</Typography>
            <Typography variant="body1">{procurement?.orderDate}</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>采购金额：</Typography>
            <Typography variant="body1" sx={{ color: '#d32f2f', fontWeight: 'bold' }}>¥{procurement?.totalAmount}</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="body1" sx={{ fontWeight: 'bold' }}>采购员：</Typography>
            <Typography variant="body1">{procurement?.purchaser}</Typography>
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
                    {procurementStatuses.map((status) => (
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
                  color: getStatusColor(procurement?.status || '')
                }}>
                  {procurement?.status}
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
          采购订单商品列表
        </Typography>
        <TableContainer>
          <Table sx={{ minWidth: 650 }} aria-label="procurement items table">
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
              {procurementItems.map((item) => (
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
                <TableCell sx={{ fontWeight: 'bold', color: '#d32f2f' }}>¥{procurement?.totalAmount}</TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      <Box sx={{ mt: 3, display: 'flex', justifyContent: 'flex-start' }}>
        <Button variant="contained" onClick={handleBack} startIcon={<ArrowBackIcon />}>
          返回采购管理
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

export default ProcurementDetail;