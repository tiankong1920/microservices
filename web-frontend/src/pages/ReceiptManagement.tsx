import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
  Button, TextField, IconButton, Tooltip, CircularProgress, Checkbox, 
  Toolbar, Menu, MenuItem, Chip 
} from '@mui/material';
import { 
  Search as SearchIcon, Visibility as VisibilityIcon, 
  Download as DownloadIcon, FilterList as FilterIcon, Delete as DeleteIcon
} from '@mui/icons-material';
import { receiptApi } from '../services/api';
import ExportService from '../services/exportService';
import Pagination from '../components/Pagination';
import DateRangePicker from '../components/DateRangePicker';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import EmptyState from '../components/EmptyState';
import Skeleton from '../components/Skeleton';

interface Receipt {
  id: number;
  receiptNumber: string;
  customerName: string;
  receiptAmount: number;
  paymentMethod: string;
  status: string;
  createdAt: string;
}

const ReceiptManagement: React.FC = () => {
  const navigate = useNavigate();
  const [receipts, setReceipts] = useState<Receipt[]>([]);
  const [filteredReceipts, setFilteredReceipts] = useState<Receipt[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [_error, setError] = useState<string | null>(null);
  const [filteredStatus, setFilteredStatus] = useState<string>('all');
  const [page, setPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [selectAll, setSelectAll] = useState<boolean>(false);
  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');
  const [sortField, setSortField] = useState<string>('receiptDate');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [batchDeleteLoading, setBatchDeleteLoading] = useState<boolean>(false);
  const { showSuccess, showError, showWarning } = useToast();

  const loadReceipts = async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      let response;
      if (filteredStatus === 'all') {
        response = await receiptApi.getAllReceipts();
      } else {
        response = await receiptApi.getReceiptsByStatus(filteredStatus);
      }
      setReceipts(response.data);
    } catch (err) {
      console.error('Failed to load receipts:', err);
      setError('加载收款单数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReceipts();
  }, [filteredStatus]);

  useEffect(() => {
    applyFiltersAndSort();
  }, [receipts, searchTerm, startDate, endDate, filteredStatus, sortField, sortOrder, page, rowsPerPage]);

  const applyFiltersAndSort = (): void => {
    let filtered = [...receipts];

    if (searchTerm) {
      filtered = filtered.filter(receipt => 
        receipt.receiptNumber?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        receipt.customerName?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    if (startDate) {
      filtered = filtered.filter(receipt => {
        const createdAt = receipt.createdAt;
        return createdAt >= startDate;
      });
    }

    if (endDate) {
      filtered = filtered.filter(receipt => {
        const createdAt = receipt.createdAt;
        return createdAt <= endDate;
      });
    }

    if (filteredStatus !== 'all') {
      filtered = filtered.filter(receipt => receipt.status === filteredStatus);
    }

    filtered.sort((a, b) => {
      const aValue = (a as any)[sortField];
      const bValue = (b as any)[sortField];
      
      if (aValue === undefined || bValue === undefined) return 0;
      if (aValue < bValue) return sortOrder === 'asc' ? -1 :1;
      if (aValue > bValue) return sortOrder === 'asc' ?1 : -1;
      return 0;
    });

    setFilteredReceipts(filtered);
  };

  const handleViewDetail = (receiptId: number): void => {
    navigate(`/receipts/${receiptId}`);
  };

  const _handleDeleteReceipt = async (_receiptId: number): Promise<void> => {
    setDeleteDialogOpen(true);
  };

  const confirmDeleteReceipt = async (receiptId: number): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      await receiptApi.deleteReceipt(receiptId);
      showSuccess('收款单删除成功');
      loadReceipts();
      setDeleteDialogOpen(false);
    } catch (err) {
      console.error('Failed to delete receipt:', err);
      showError('删除收款单失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>): void => {
    const checked = event.target.checked;
    setSelectAll(checked);
    if (checked) {
      setSelectedIds(filteredReceipts.map(r => r.id));
    } else {
      setSelectedIds([]);
    }
  };

  const handleSelectOne = (receiptId: number): void => {
    const selectedIndex = selectedIds.indexOf(receiptId);
    if (selectedIndex === -1) {
      setSelectedIds([...selectedIds, receiptId]);
    } else {
      setSelectedIds(selectedIds.filter(id => id !== receiptId));
    }
  };

  const handleBatchDelete = async (): Promise<void> => {
    if (selectedIds.length === 0) {
      showWarning('请先选择要删除的收款单');
      return;
    }
    setBatchDeleteLoading(true);
    try {
      await Promise.all(selectedIds.map(id => receiptApi.deleteReceipt(id)));
      showSuccess(`成功删除 ${selectedIds.length} 个收款单`);
      setSelectedIds([]);
      setSelectAll(false);
      loadReceipts();
    } catch (err) {
      console.error('Failed to batch delete receipts:', err);
      showError('批量删除失败');
    } finally {
      setBatchDeleteLoading(false);
    }
  };

  const handleExport = (format: 'csv' | 'excel' | 'json' | 'pdf'): void => {
    if (filteredReceipts.length === 0) {
      showWarning('没有可导出的数据');
      return;
    }
    const fields = [
      { key: 'id', label: 'ID' },
      { key: 'receiptNumber', label: '收款单号' },
      { key: 'customerName', label: '客户名称' },
      { key: 'receiptAmount', label: '收款金额', format: (v) => `¥${v}` },
      { key: 'paymentMethod', label: '收款方式' },
      { key: 'status', label: '收款状态' },
      { key: 'createdAt', label: '创建时间' },
    ];
    
    if (format === 'csv') {
      ExportService.exportToCSV(filteredReceipts, { filename: 'receipts', fields });
    } else if (format === 'excel') {
      ExportService.exportToExcel(filteredReceipts, { filename: 'receipts', fields });
    } else if (format === 'pdf') {
      ExportService.exportToPDF(filteredReceipts, { filename: 'receipts', fields });
    } else {
      ExportService.exportToJSON(filteredReceipts, { filename: 'receipts', fields });
    }
    showSuccess('导出成功');
  };

  const handleSort = (field: string): void => {
    if (sortField === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortOrder('desc');
    }
  };

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>): void => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = (): void => {
    setAnchorEl(null);
  };

  const paginatedReceipts = filteredReceipts.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  const getStatusColor = (status: string): 'success' | 'default' | 'warning' | 'primary' => {
    switch (status) {
      case '已收款':
        return 'success';
      case '已取消':
        return 'default';
      case '待收款':
        return 'warning';
      default:
        return 'primary';
    }
  };

  return (
    <Box>
      
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个收款单吗？删除后无法恢复。"
        confirmText="删除"
        cancelText="取消"
        severity="warning"
        onConfirm={() => selectedIds.length > 0 ? handleBatchDelete() : confirmDeleteReceipt(selectedIds[0])}
        onCancel={() => setDeleteDialogOpen(false)}
        loading={loading || batchDeleteLoading}
      />

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          收款单管理
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <Button
            variant="outlined"
            startIcon={<DownloadIcon />}
            onClick={handleMenuOpen}
          >
            导出
          </Button>
          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleMenuClose}
          >
            <MenuItem onClick={() => { handleExport('csv'); handleMenuClose(); }}>
              导出为 CSV
            </MenuItem>
            <MenuItem onClick={() => { handleExport('excel'); handleMenuClose(); }}>
              导出为 Excel
            </MenuItem>
            <MenuItem onClick={() => { handleExport('json'); handleMenuClose(); }}>
              导出为 JSON
            </MenuItem>
            <MenuItem onClick={() => { handleExport('pdf'); handleMenuClose(); }}>
              导出为 PDF
            </MenuItem>
          </Menu>
        </Box>
      </Box>

      <Paper sx={{ mb: 2, p: 2 }}>
        <Toolbar sx={{ display: 'flex', gap: 2, flexWrap: 'wrap', alignItems: 'center' }}>
          <TextField
            placeholder="搜索收款单号或客户名称..."
            variant="outlined"
            size="small"
            value={searchTerm}
            onChange={(e: React.ChangeEvent<HTMLInputElement>) => setSearchTerm(e.target.value)}
            slotProps={{
              input: {
                startAdornment: <SearchIcon />,
              }
            }}
            sx={{ minWidth: 300, flex: 1 }}
          />
          <DateRangePicker
            startDate={startDate}
            endDate={endDate}
            onStartDateChange={setStartDate}
            onEndDateChange={setEndDate}
            onClear={() => { setStartDate(''); setEndDate(''); }}
            label="创建日期"
          />
          <Button
            variant="outlined"
            startIcon={<FilterIcon />}
            onClick={() => { setFilteredStatus('all'); setStartDate(''); setEndDate(''); setSearchTerm(''); }}
          >
            清除筛选
          </Button>
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', ml: 'auto' }}>
            <Chip 
              label={filteredStatus === 'all' ? '全部状态' : filteredStatus} 
              onClick={() => setFilteredStatus(filteredStatus === 'all' ? '待收款' : 'all')}
              color={filteredStatus === 'all' ? 'default' : filteredStatus === '已收款' ? 'success' : filteredStatus === '已取消' ? 'error' : 'warning'}
              size="small"
            />
          </Box>
        </Toolbar>
      </Paper>

      {loading ? (
        <Skeleton type="table" rows={rowsPerPage} columns={5} />
      ) : filteredReceipts.length === 0 ? (
        <EmptyState type="no-results" />
      ) : (
        <Box>
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 2 }}>
            <Checkbox
              checked={selectAll}
              onChange={handleSelectAll}
              indeterminate={selectedIds.length > 0 && selectedIds.length < paginatedReceipts.length}
            />
            <Typography variant="body2">
              已选择 {selectedIds.length} 项
            </Typography>
            {selectedIds.length > 0 && (
              <Button
                variant="contained"
                color="error"
                startIcon={<DeleteIcon />}
                onClick={() => setDeleteDialogOpen(true)}
                disabled={batchDeleteLoading}
              >
                批量删除
              </Button>
            )}
          </Box>

          <Box sx={{ overflowX: 'auto' }}>
            <TableContainer component={Paper} sx={{ minWidth: '100%' }}>
              <Table sx={{ minWidth: 900 }} aria-label="receipt table">
                <TableHead>
                  <TableRow>
                    <TableCell padding="checkbox">
                      <Checkbox checked={selectAll} onChange={handleSelectAll} />
                    </TableCell>
                    <TableCell>
                      <Button
                        size="small"
                        onClick={() => handleSort('receiptDate')}
                        endIcon={sortField === 'receiptDate' ? (sortOrder === 'asc' ? '↑' : '↓') : null}
                      >
                        创建日期
                      </Button>
                    </TableCell>
                    <TableCell>客户名称</TableCell>
                    <TableCell>收款金额</TableCell>
                    <TableCell>收款方式</TableCell>
                    <TableCell>收款状态</TableCell>
                    <TableCell>操作</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {paginatedReceipts.map((receipt) => (
                    <TableRow
                      key={receipt.id}
                      sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                      hover
                    >
                      <TableCell padding="checkbox">
                        <Checkbox
                          checked={selectedIds.includes(receipt.id)}
                          onChange={() => handleSelectOne(receipt.id)}
                          color="primary"
                        />
                      </TableCell>
                      <TableCell sx={{ cursor: 'pointer', fontWeight: 'medium' }} onClick={() => handleViewDetail(receipt.id)}>
                        {receipt.receiptNumber}
                      </TableCell>
                      <TableCell>{receipt.customerName}</TableCell>
                      <TableCell sx={{ color: '#d32f2f', fontWeight: 'bold' }}>¥{receipt.receiptAmount}</TableCell>
                      <TableCell>{receipt.paymentMethod}</TableCell>
                      <TableCell>
                        <Chip 
                          label={receipt.status} 
                          color={getStatusColor(receipt.status)} 
                          size="small" 
                        />
                      </TableCell>
                      <TableCell>
                        <Tooltip title="查看详情">
                          <IconButton size="small" onClick={() => handleViewDetail(receipt.id)}>
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

          <Pagination
            count={filteredReceipts.length}
            page={page}
            rowsPerPage={rowsPerPage}
            onPageChange={(_, newPage) => setPage(newPage)}
            onRowsPerPageChange={(e) => setRowsPerPage(Number((e.target as HTMLInputElement).value))}
            rowsPerPageOptions={[10, 25, 50, 100]}
          />
        </Box>
      )}
    </Box>
  );
};

export default ReceiptManagement;