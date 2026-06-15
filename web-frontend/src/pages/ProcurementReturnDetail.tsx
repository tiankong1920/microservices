import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Button, CircularProgress, Alert, Chip } from '@mui/material';
import { ArrowBack as ArrowBackIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { procurementReturnApi } from '../services/api';
import type { ProcurementReturn, ProcurementReturnItem } from '../types';

const ProcurementReturnDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [returnOrder, setReturnOrder] = useState<ProcurementReturn | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const loadReturnOrder = async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const response = await procurementReturnApi.getProcurementReturnById(Number(id));
      setReturnOrder(response.data);
    } catch (err) {
      console.error('Failed to load procurement return order:', err);
      setError('加载采购退货单详情失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReturnOrder();
  }, [id]);

  const handleBack = (): void => {
    navigate('/procurement-return');
  };

  const handleEdit = (): void => {
    navigate(`/procurement-return/${id}/edit`);
  };

  const handleDelete = async (): Promise<void> => {
    if (window.confirm('确定要删除这个采购退货单吗？')) {
      try {
        await procurementReturnApi.deleteProcurementReturn(Number(id));
        navigate('/procurement-return');
      } catch (err) {
        console.error('Failed to delete procurement return order:', err);
        setError('删除采购退货单失败');
      }
    }
  };

  const getStatusColor = (status: string): 'success' | 'info' | 'warning' | 'error' | 'default' => {
    switch (status) {
      case 'COMPLETED': return 'success';
      case 'APPROVED': return 'info';
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
        <Alert severity="error">{error}</Alert>
      </Box>
    );
  }

  if (!returnOrder) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="warning">未找到采购退货单</Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<ArrowBackIcon />}
            onClick={handleBack}
          >
            返回
          </Button>
          <Typography variant="h5">
            采购退货单详情
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
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
        <Typography variant="h6" gutterBottom>
          基本信息
        </Typography>
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 2 }}>
          <Box sx={{ width: { xs: '100%', sm: '50%' } }}>
            <Typography variant="body1">
              <strong>退货单号:</strong> {returnOrder.returnNumber}
            </Typography>
          </Box>
          <Box sx={{ width: { xs: '100%', sm: '50%' } }}>
            <Typography variant="body1">
              <strong>供应商名称:</strong> {returnOrder.supplierName}
            </Typography>
          </Box>
          <Box sx={{ width: { xs: '100%', sm: '50%' } }}>
            <Typography variant="body1">
              <strong>退货日期:</strong> {returnOrder.returnDate}
            </Typography>
          </Box>
          <Box sx={{ width: { xs: '100%', sm: '50%' } }}>
            <Typography variant="body1">
              <strong>订单状态:</strong>{' '}
              <Chip label={returnOrder.status} color={getStatusColor(returnOrder.status)} size="small" />
            </Typography>
          </Box>
          <Box sx={{ width: { xs: '100%', sm: '50%' } }}>
            <Typography variant="body1">
              <strong>总金额:</strong> ¥{returnOrder.totalAmount}
            </Typography>
          </Box>
        </Box>
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
                  <TableCell>小计</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {returnOrder.items.map((item: ProcurementReturnItem, index: number) => (
                  <TableRow key={index}>
                    <TableCell>{item.productName}</TableCell>
                    <TableCell>{item.productSku}</TableCell>
                    <TableCell>{item.quantity}</TableCell>
                    <TableCell>{item.unit}</TableCell>
                    <TableCell>¥{item.unitPrice}</TableCell>
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

export default ProcurementReturnDetail;
