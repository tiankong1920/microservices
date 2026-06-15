import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Button, TextField, IconButton, Tooltip, CircularProgress, Alert, Chip } from '@mui/material';
import { Search as SearchIcon, Visibility as VisibilityIcon, Add as AddIcon } from '@mui/icons-material';
import { stockTransferApi } from '../services/api';
import type { StockTransfer } from '../types';

const StockTransferManagement: React.FC = () => {
  const navigate = useNavigate();
  const [transfers, setTransfers] = useState<StockTransfer[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [success] = useState<string>('');
  const [filteredStatus, setFilteredStatus] = useState<string>('all');

  const loadTransfers = async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const response = await stockTransferApi.getAllStockTransfers();
      setTransfers(response.data);
    } catch (err) {
      console.error('Failed to load stock transfers:', err);
      setError('加载调拨单数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadTransfers();
  }, [filteredStatus]);

  const handleViewDetail = (transferId: number): void => {
    navigate(`/stock-transfer/${transferId}`);
  };

  const handleCreate = (): void => {
    navigate('/stock-transfer/create');
  };

  const getStatusColor = (status: string): 'success' | 'default' | 'warning' | 'primary' => {
    switch (status) {
      case 'COMPLETED': return 'success';
      case 'CANCELLED': return 'default';
      case 'PENDING': return 'warning';
      default: return 'primary';
    }
  };

  const filteredTransfers = transfers.filter((trans: StockTransfer) => 
    (trans.transferNumber?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    trans.sourceWarehouseName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    trans.targetWarehouseName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    trans.status?.toLowerCase().includes(searchTerm.toLowerCase())) &&
    (filteredStatus === 'all' || trans.status === filteredStatus)
  );

  return (
    <Box>
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}
      
      {success && (
        <Alert severity="success" sx={{ mb: 3 }}>
          {success}
        </Alert>
      )}
      
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          库存调拨管理
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleCreate}
          >
            新增调拨单
          </Button>
          <TextField
            placeholder="搜索调拨单号或仓库名称..."
            variant="outlined"
            size="small"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              endAdornment: <SearchIcon />,
            }}
            sx={{ minWidth: 300 }}
          />
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Button
              variant={filteredStatus === 'all' ? 'contained' : 'outlined'}
              onClick={() => setFilteredStatus('all')}
            >
              全部
            </Button>
            <Button
              variant={filteredStatus === 'PENDING' ? 'contained' : 'outlined'}
              onClick={() => setFilteredStatus('PENDING')}
            >
              待处理
            </Button>
            <Button
              variant={filteredStatus === 'IN_TRANSIT' ? 'contained' : 'outlined'}
              onClick={() => setFilteredStatus('IN_TRANSIT')}
            >
              调拨中
            </Button>
            <Button
              variant={filteredStatus === 'COMPLETED' ? 'contained' : 'outlined'}
              onClick={() => setFilteredStatus('COMPLETED')}
            >
              已完成
            </Button>
          </Box>
        </Box>
      </Box>
      
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 200 }}>
          <CircularProgress />
        </Box>
      ) : (
        <Box sx={{ overflowX: 'auto' }}>
          <TableContainer component={Paper} sx={{ minWidth: '100%' }}>
            <Table sx={{ minWidth: 800 }} aria-label="stock transfer table">
              <TableHead>
                <TableRow>
                  <TableCell>调拨单号</TableCell>
                  <TableCell>源仓库</TableCell>
                  <TableCell>目标仓库</TableCell>
                  <TableCell>调拨日期</TableCell>
                  <TableCell>调拨数量</TableCell>
                  <TableCell>状态</TableCell>
                  <TableCell>操作</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredTransfers.map((trans: StockTransfer) => (
                  <TableRow
                    key={trans.id}
                    sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                    hover
                  >
                    <TableCell sx={{ cursor: 'pointer', fontWeight: 'medium' }} onClick={() => handleViewDetail(trans.id)}>
                      {trans.transferNumber}
                    </TableCell>
                    <TableCell>{trans.sourceWarehouseName}</TableCell>
                    <TableCell>{trans.targetWarehouseName}</TableCell>
                    <TableCell>{trans.transferDate}</TableCell>
                    <TableCell>{trans.totalQuantity}</TableCell>
                    <TableCell>
                      <Chip 
                        label={trans.status} 
                        color={getStatusColor(trans.status)} 
                        size="small" 
                      />
                    </TableCell>
                    <TableCell>
                      <Tooltip title="查看详情">
                        <IconButton size="small" onClick={() => handleViewDetail(trans.id)}>
                          <VisibilityIcon />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Box>
      )}
    </Box>
  );
};

export default StockTransferManagement;
