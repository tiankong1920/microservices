import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Button, TextField, IconButton, Tooltip, CircularProgress, Alert, Chip } from '@mui/material';
import { Search as SearchIcon, Visibility as VisibilityIcon, Add as AddIcon } from '@mui/icons-material';
import { procurementReturnApi } from '../services/api';
import type { ProcurementReturn } from '../types';

const ProcurementReturnManagement: React.FC = () => {
  const navigate = useNavigate();
  const [returns, setReturns] = useState<ProcurementReturn[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [success] = useState<string>('');
  const [filteredStatus, setFilteredStatus] = useState<string>('all');

  const loadReturns = async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const response = await procurementReturnApi.getAllProcurementReturns();
      setReturns(response.data);
    } catch (err) {
      console.error('Failed to load procurement returns:', err);
      setError('加载采购退货单数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReturns();
  }, [filteredStatus]);

  const handleViewDetail = (returnId: number): void => {
    navigate(`/procurement-return/${returnId}`);
  };

  const handleCreate = (): void => {
    navigate('/procurement-return/create');
  };

  const getStatusColor = (status: string): 'success' | 'default' | 'warning' | 'primary' | 'error' => {
    switch (status) {
      case 'COMPLETED': return 'success';
      case 'REJECTED':
      case 'CANCELLED': return 'default';
      case 'PENDING': return 'warning';
      case 'APPROVED': return 'primary';
      default: return 'error';
    }
  };

  const filteredReturns = returns.filter((ret: ProcurementReturn) => 
    (ret.returnNumber?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    ret.supplierName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    ret.status?.toLowerCase().includes(searchTerm.toLowerCase())) &&
    (filteredStatus === 'all' || ret.status === filteredStatus)
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
          采购退货管理
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleCreate}
          >
            新增退货单
          </Button>
          <TextField
            placeholder="搜索退货单号或供应商名称..."
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
              待审批
            </Button>
            <Button
              variant={filteredStatus === 'APPROVED' ? 'contained' : 'outlined'}
              onClick={() => setFilteredStatus('APPROVED')}
            >
              已审批
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
            <Table sx={{ minWidth: 800 }} aria-label="procurement return table">
              <TableHead>
                <TableRow>
                  <TableCell>退货单号</TableCell>
                  <TableCell>供应商名称</TableCell>
                  <TableCell>退货日期</TableCell>
                  <TableCell>退货金额</TableCell>
                  <TableCell>订单状态</TableCell>
                  <TableCell>操作</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredReturns.map((ret: ProcurementReturn) => (
                  <TableRow
                    key={ret.id}
                    sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                    hover
                  >
                    <TableCell sx={{ cursor: 'pointer', fontWeight: 'medium' }} onClick={() => handleViewDetail(ret.id)}>
                      {ret.returnNumber}
                    </TableCell>
                    <TableCell>{ret.supplierName}</TableCell>
                    <TableCell>{ret.returnDate}</TableCell>
                    <TableCell>¥{ret.totalAmount}</TableCell>
                    <TableCell>
                      <Chip 
                        label={ret.status} 
                        color={getStatusColor(ret.status)} 
                        size="small" 
                      />
                    </TableCell>
                    <TableCell>
                      <Tooltip title="查看详情">
                        <IconButton size="small" onClick={() => handleViewDetail(ret.id)}>
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

export default ProcurementReturnManagement;
