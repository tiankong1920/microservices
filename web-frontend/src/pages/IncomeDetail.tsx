import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableRow, Button, CircularProgress, Grid, Dialog, DialogTitle, DialogContent, DialogActions, TextField, IconButton, Tooltip } from '@mui/material';
import { ArrowBack as ArrowBackIcon, Edit as EditIcon, Delete as DeleteIcon, Print as PrintIcon } from '@mui/icons-material';
import { incomeApi } from '../services/api';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import type { Income } from '../types';

const IncomeDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [income, setIncome] = useState<Income | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [editDialogOpen, setEditDialogOpen] = useState<boolean>(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [editData, setEditData] = useState<Partial<Income>>({});
  const { showSuccess, showError } = useToast();

  const loadIncomeDetail = async (): Promise<void> => {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const response = await incomeApi.getIncomeById(Number(id));
      setIncome(response.data);
    } catch (err) {
      console.error('Failed to load income detail:', err);
      setError('加载收入单详情失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (id) {
      loadIncomeDetail();
    }
  }, [id]);

  const handleBackToList = (): void => {
    navigate('/incomes');
  };

  const handleEdit = (): void => {
    if (!income) return;
    setEditData({
      incomeNumber: income.incomeNumber,
      incomeDate: income.incomeDate,
      incomeType: income.incomeType,
      incomeSource: income.incomeSource,
      incomeAmount: income.incomeAmount,
      incomeStatus: income.incomeStatus,
      settlementAccountName: income.settlementAccountName,
      description: income.description,
    });
    setEditDialogOpen(true);
  };

  const handleSaveEdit = async (): Promise<void> => {
    if (!income || !editData) return;

    setLoading(true);
    setError(null);
    try {
      await incomeApi.updateIncome(income.id, editData);
      showSuccess('收入单更新成功');
      setEditDialogOpen(false);
      loadIncomeDetail();
    } catch (err) {
      console.error('Failed to update income:', err);
      showError('更新收入单失败');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = (): void => {
    setDeleteDialogOpen(true);
  };

  const confirmDelete = async (): Promise<void> => {
    if (!income) return;

    setLoading(true);
    setError(null);
    try {
      await incomeApi.deleteIncome(income.id);
      showSuccess('收入单删除成功');
      setDeleteDialogOpen(false);
      navigate('/incomes');
    } catch (err) {
      console.error('Failed to delete income:', err);
      showError('删除收入单失败');
    } finally {
      setLoading(false);
    }
  };

  const handlePrint = (): void => {
    if (!income) return;
    window.print();
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 300 }}>
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

  if (!income) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h6" color="info" gutterBottom>
          收入单不存在
        </Typography>
      </Box>
    );
  }

  return (
    <Box>
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个收入单吗？删除后无法恢复。"
        confirmText="删除"
        cancelText="取消"
        severity="warning"
        onConfirm={confirmDelete}
        onCancel={() => setDeleteDialogOpen(false)}
        loading={loading}
      />

      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <Button
          startIcon={<ArrowBackIcon />}
          onClick={handleBackToList}
          sx={{ mr: 2 }}
        >
          返回列表
        </Button>
        <Typography variant="h4" gutterBottom>
          收入单详情
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="contained"
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
          <Button
            variant="outlined"
            startIcon={<PrintIcon />}
            onClick={handlePrint}
            disabled={loading}
          >
            打印
          </Button>
        </Box>
      </Box>

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              基本信息
            </Typography>
            <TableContainer>
              <Table size="small" aria-label="income basic info">
                <TableBody>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>收入单号</TableCell>
                    <TableCell>{income.incomeNumber}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>收入日期</TableCell>
                    <TableCell>{income.incomeDate}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>收入类型</TableCell>
                    <TableCell>{income.incomeType}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>收入来源</TableCell>
                    <TableCell>{income.incomeSource}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>相关订单号</TableCell>
                    <TableCell>{income.relatedOrderNumber || '无'}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>收入状态</TableCell>
                    <TableCell>
                      <Box sx={{ 
                        fontWeight: 'bold',
                        color: income.incomeStatus === '已确认' ? '#388e3c' : '#f57c00'
                      }}>
                        {income.incomeStatus}
                      </Box>
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>结算账户</TableCell>
                    <TableCell>{income.settlementAccountName}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>创建时间</TableCell>
                    <TableCell>{income.createdAt}</TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>更新时间</TableCell>
                    <TableCell>{income.updatedAt}</TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              金额信息
            </Typography>
            <TableContainer>
              <Table size="small" aria-label="income amount info">
                <TableBody>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>收入金额</TableCell>
                    <TableCell sx={{ fontWeight: 'bold', color: '#388e3c' }}>
                      ¥{income.incomeAmount}
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>收入状态</TableCell>
                    <TableCell>
                      <Box sx={{ 
                        fontWeight: 'bold',
                        color: income.incomeStatus === '已确认' ? '#388e3c' : '#f57c00'
                      }}>
                        {income.incomeStatus}
                      </Box>
                    </TableCell>
                  </TableRow>
                  <TableRow>
                    <TableCell sx={{ fontWeight: 'bold' }}>结算账户</TableCell>
                    <TableCell>{income.settlementAccountName}</TableCell>
                  </TableRow>
                </TableBody>
              </Table>
            </TableContainer>
          </Paper>
        </Grid>
        <Grid size={{ xs: 12 }}>
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              备注信息
            </Typography>
            <Typography variant="body1" paragraph>
              {income.description || '无备注'}
            </Typography>
          </Paper>
        </Grid>
      </Grid>

      <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>编辑收入单</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="收入单号"
            fullWidth
            variant="outlined"
            value={editData.incomeNumber}
            onChange={(e) => setEditData({ ...editData, incomeNumber: e.target.value })}
            disabled
          />
          <TextField
            margin="dense"
            label="收入日期"
            type="date"
            fullWidth
            variant="outlined"
            value={editData.incomeDate}
            onChange={(e) => setEditData({ ...editData, incomeDate: e.target.value })}
          />
          <TextField
            margin="dense"
            label="收入类型"
            fullWidth
            variant="outlined"
            value={editData.incomeType}
            onChange={(e) => setEditData({ ...editData, incomeType: e.target.value })}
          />
          <TextField
            margin="dense"
            label="收入来源"
            fullWidth
            variant="outlined"
            value={editData.incomeSource}
            onChange={(e) => setEditData({ ...editData, incomeSource: e.target.value })}
          />
          <TextField
            margin="dense"
            label="收入金额"
            type="number"
            fullWidth
            variant="outlined"
            value={editData.incomeAmount}
            onChange={(e) => setEditData({ ...editData, incomeAmount: Number(e.target.value) })}
          />
          <TextField
            margin="dense"
            label="收入状态"
            fullWidth
            variant="outlined"
            value={editData.incomeStatus}
            onChange={(e) => setEditData({ ...editData, incomeStatus: e.target.value })}
          />
          <TextField
            margin="dense"
            label="结算账户"
            fullWidth
            variant="outlined"
            value={editData.settlementAccountName}
            onChange={(e) => setEditData({ ...editData, settlementAccountName: e.target.value })}
          />
          <TextField
            margin="dense"
            label="备注"
            fullWidth
            variant="outlined"
            multiline
            rows={3}
            value={editData.description}
            onChange={(e) => setEditData({ ...editData, description: e.target.value })}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditDialogOpen(false)}>取消</Button>
          <Button onClick={handleSaveEdit} variant="contained">保存</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default IncomeDetail;
