import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Button, CircularProgress, Alert, Chip } from '@mui/material';
import { ArrowBack as ArrowBackIcon, Edit as EditIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { stockTransferApi } from '../services/api';
import type { StockTransfer, StockTransferItem } from '../types';

const StockTransferDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [transferOrder, setTransferOrder] = useState<StockTransfer | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const loadTransferOrder = async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const response = await stockTransferApi.getStockTransferById(Number(id));
      setTransferOrder(response.data);
    } catch (err) {
      console.error('Failed to load stock transfer order:', err);
      setError('加载调拨单详情失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTransferOrder();
  }, [id]);

  const handleBack = (): void => {
    navigate('/stock-transfer');
  };

  const handleEdit = (): void => {
    navigate(`/stock-transfer/${id}/edit`);
  };

  const handleDelete = async (): Promise<void> => {
    if (window.confirm('确定要删除这个调拨单吗？')) {
      try {
        await stockTransferApi.deleteStockTransfer(Number(id));
        navigate('/stock-transfer');
      } catch (err) {
        console.error('Failed to delete stock transfer order:', err);
        setError('删除调拨单失败');
      }
    }
  };

  const getStatusColor = (status: string): 'success' | 'info' | 'warning' | 'default' => {
    switch (status) {
      case 'COMPLETED': return 'success';
      case 'IN_TRANSIT': return 'info';
      case 'PENDING': return 'warning';
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

  if (!transferOrder) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="warning">未找到调拨单</Alert>
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
            库存调拨单详情
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
              <strong>调拨单号:</strong> {transferOrder.transferNumber}
            </Typography>
          </Box>
          <Box sx={{ width: { xs: '100%', sm: '50%' } }}>
            <Typography variant="body1">
              <strong>源仓库:</strong> {transferOrder.sourceWarehouseName}
            </Typography>
          </Box>
          <Box sx={{ width: { xs: '100%', sm: '50%' } }}>
            <Typography variant="body1">
              <strong>目标仓库:</strong> {transferOrder.targetWarehouseName}
            </Typography>
          </Box>
          <Box sx={{ width: { xs: '100%', sm: '50%' } }}>
            <Typography variant="body1">
              <strong>调拨日期:</strong> {transferOrder.transferDate}
            </Typography>
          </Box>
          <Box sx={{ width: { xs: '100%', sm: '50%' } }}>
            <Typography variant="body1">
              <strong>调拨状态:</strong>{' '}
              <Chip label={transferOrder.status} color={getStatusColor(transferOrder.status)} size="small" />
            </Typography>
          </Box>
          <Box sx={{ width: { xs: '100%', sm: '50%' } }}>
            <Typography variant="body1">
              <strong>调拨数量:</strong> {transferOrder.totalQuantity}
            </Typography>
          </Box>
        </Box>
      </Paper>

      {transferOrder.items && transferOrder.items.length > 0 && (
        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            调拨商品明细
          </Typography>
          <TableContainer>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>产品名称</TableCell>
                  <TableCell>SKU</TableCell>
                  <TableCell>数量</TableCell>
                  <TableCell>单位</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {transferOrder.items.map((item: StockTransferItem, index: number) => (
                  <TableRow key={index}>
                    <TableCell>{item.productName}</TableCell>
                    <TableCell>{item.productSku}</TableCell>
                    <TableCell>{item.quantity}</TableCell>
                    <TableCell>{item.unit}</TableCell>
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

export default StockTransferDetail;
