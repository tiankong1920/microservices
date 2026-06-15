import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
  Button, TextField, IconButton, Tooltip, CircularProgress, Checkbox, Toolbar, Menu, MenuItem, Chip 
} from '@mui/material';
import { 
  Search as SearchIcon, Visibility as VisibilityIcon, Add as AddIcon, PointOfSale as PointOfSaleIcon, 
  Download as DownloadIcon, FilterList as FilterIcon, Delete as DeleteIcon
} from '@mui/icons-material';
import { retailApi } from '../services/api';
import ExportService from '../services/exportService';
import Pagination from '../components/Pagination';
import DateRangePicker from '../components/DateRangePicker';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import EmptyState from '../components/EmptyState';
import Skeleton from '../components/Skeleton';
import type { ApiResponse } from '../types';

interface RetailOrder {
  id: number;
  retailNumber: string;
  customerName: string;
  retailDate: string;
  totalAmount: number;
  paymentStatus: string;
}

const RetailManagement: React.FC = () => {
  const navigate = useNavigate();
  const [retails, setRetails] = useState<RetailOrder[]>([]);
  const [filteredRetails, setFilteredRetails] = useState<RetailOrder[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [_error, setError] = useState<string | null>(null);
  const [page, setPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [selectAll, setSelectAll] = useState<boolean>(false);
  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');
  const [paymentStatusFilter, setPaymentStatusFilter] = useState<string>('all');
  const [sortField, setSortField] = useState<string>('retailDate');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [batchDeleteLoading, setBatchDeleteLoading] = useState<boolean>(false);
  const { showSuccess, showError, showWarning } = useToast();

  const loadRetails = async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const response: ApiResponse<RetailOrder[]> = await retailApi.getAllRetails();
      setRetails(response.data);
    } catch (err) {
      console.error('Failed to load retail orders:', err);
      setError('加载零售订单数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRetails();
  }, []);

  useEffect(() => {
    applyFiltersAndSort();
  }, [retails, searchTerm, startDate, endDate, paymentStatusFilter, sortField, sortOrder, page, rowsPerPage]);

  const applyFiltersAndSort = (): void => {
    let filtered = [...retails];

    if (searchTerm) {
      filtered = filtered.filter(retail => 
        retail.retailNumber?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        retail.customerName?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    if (startDate) {
      filtered = filtered.filter(retail => {
        const retailDate = retail.retailDate;
        return retailDate >= startDate;
      });
    }

    if (endDate) {
      filtered = filtered.filter(retail => {
        const retailDate = retail.retailDate;
        return retailDate <= endDate;
      });
    }

    if (paymentStatusFilter !== 'all') {
      filtered = filtered.filter(retail => retail.paymentStatus === paymentStatusFilter);
    }

    filtered.sort((a, b) => {
      const aValue = (a as any)[sortField];
      const bValue = (b as any)[sortField];
      
      if (aValue === undefined || bValue === undefined) return 0;
      if (aValue < bValue) return sortOrder === 'asc' ? -1 :1;
      if (aValue > bValue) return sortOrder === 'asc' ?1 : -1;
      return 0;
    });

    setFilteredRetails(filtered);
  };

  const handleViewDetail = (retailId: number): void => {
    navigate(`/retail/${retailId}`);
  };

  const handleOpenPOS = (): void => {
    navigate('/retail/pos');
  };

  const handleCreateRetail = (): void => {
    navigate('/retail/create');
  };

  const _handleDeleteRetail = async (_retailId: number): Promise<void> => {
    setDeleteDialogOpen(true);
  };

  const confirmDeleteRetail = async (retailId: number): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      await retailApi.deleteRetail(retailId);
      showSuccess('零售订单删除成功');
      loadRetails();
      setDeleteDialogOpen(false);
    } catch (err) {
      console.error('Failed to delete retail order:', err);
      showError('删除零售订单失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>): void => {
    const checked = event.target.checked;
    setSelectAll(checked);
    if (checked) {
      setSelectedIds(filteredRetails.map(r => r.id));
    } else {
      setSelectedIds([]);
    }
  };

  const handleSelectOne = (retailId: number): void => {
    const selectedIndex = selectedIds.indexOf(retailId);
    if (selectedIndex === -1) {
      setSelectedIds([...selectedIds, retailId]);
    } else {
      setSelectedIds(selectedIds.filter(id => id !== retailId));
    }
  };

  const handleBatchDelete = async (): Promise<void> => {
    if (selectedIds.length === 0) {
      showWarning('请先选择要删除的零售订单');
      return;
    }
    setBatchDeleteLoading(true);
    try {
      await Promise.all(selectedIds.map(id => retailApi.deleteRetail(id)));
      showSuccess(`成功删除 ${selectedIds.length} 个零售订单`);
      setSelectedIds([]);
      setSelectAll(false);
      loadRetails();
    } catch (err) {
      console.error('Failed to batch delete retail orders:', err);
      showError('批量删除失败');
    } finally {
      setBatchDeleteLoading(false);
    }
  };

  const handleExport = (format: 'csv' | 'excel' | 'json' | 'pdf'): void => {
    if (filteredRetails.length === 0) {
      showWarning('没有可导出的数据');
      return;
    }
    const fields = [
      { key: 'id', label: 'ID' },
      { key: 'retailNumber', label: '零售单号' },
      { key: 'customerName', label: '客户名称' },
      { key: 'retailDate', label: '零售日期' },
      { key: 'totalAmount', label: '金额', format: (v) => `¥${v}` },
      { key: 'paymentStatus', label: '支付状态' },
    ];
    
    if (format === 'csv') {
      ExportService.exportToCSV(filteredRetails, { filename: 'retails', fields });
    } else if (format === 'excel') {
      ExportService.exportToExcel(filteredRetails, { filename: 'retails', fields });
    } else if (format === 'pdf') {
      ExportService.exportToPDF(filteredRetails, { filename: 'retails', fields });
    } else {
      ExportService.exportToJSON(filteredRetails, { filename: 'retails', fields });
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

  const paginatedRetails = filteredRetails.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  const getPaymentStatusColor = (status: string): 'success' | 'warning' | 'default' => {
    switch (status) {
      case 'PAID':
        return 'success';
      case 'PENDING':
        return 'warning';
      default:
        return 'default';
    }
  };

  const getPaymentStatusLabel = (status: string): string => {
    switch (status) {
      case 'PAID':
        return '已付款';
      case 'PENDING':
        return '待付款';
      default:
        return status;
    }
  };

  return (
    <Box>
      
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个零售订单吗？删除后无法恢复。"
        confirmText="删除"
        cancelText="取消"
        severity="warning"
        onConfirm={() => selectedIds.length > 0 ? handleBatchDelete() : confirmDeleteRetail(selectedIds[0])}
        onCancel={() => setDeleteDialogOpen(false)}
        loading={loading || batchDeleteLoading}
      />

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          零售管理
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <Button
            variant="contained"
            color="secondary"
            startIcon={<PointOfSaleIcon />}
            onClick={handleOpenPOS}
          >
            POS收银台
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleCreateRetail}
          >
            创建零售单
          </Button>
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
            placeholder="搜索零售单号或客户名称..."
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
            label="零售日期"
          />
          <Button
            variant="outlined"
            startIcon={<FilterIcon />}
            onClick={() => { setPaymentStatusFilter('all'); setStartDate(''); setEndDate(''); setSearchTerm(''); }}
          >
            清除筛选
          </Button>
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', ml: 'auto' }}>
            <Chip 
              label={paymentStatusFilter === 'all' ? '全部状态' : getPaymentStatusLabel(paymentStatusFilter)} 
              onClick={() => setPaymentStatusFilter(paymentStatusFilter === 'all' ? 'PAID' : 'all')}
              color={paymentStatusFilter === 'all' ? 'default' : paymentStatusFilter === 'PAID' ? 'success' : 'warning'}
              size="small"
            />
          </Box>
        </Toolbar>
      </Paper>

      {loading ? (
        <Skeleton type="table" rows={rowsPerPage} columns={5} />
      ) : filteredRetails.length === 0 ? (
        <EmptyState type="no-results" />
      ) : (
        <Box>
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 2 }}>
            <Checkbox
              checked={selectAll}
              onChange={handleSelectAll}
              indeterminate={selectedIds.length > 0 && selectedIds.length < paginatedRetails.length}
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
              <Table sx={{ minWidth: 900 }} aria-label="retail table">
                <TableHead>
                  <TableRow>
                    <TableCell padding="checkbox">
                      <Checkbox checked={selectAll} onChange={handleSelectAll} />
                    </TableCell>
                    <TableCell>
                      <Button
                        size="small"
                        onClick={() => handleSort('retailDate')}
                        endIcon={sortField === 'retailDate' ? (sortOrder === 'asc' ? '↑' : '↓') : null}
                      >
                        零售日期
                      </Button>
                    </TableCell>
                    <TableCell>客户名称</TableCell>
                    <TableCell>金额</TableCell>
                    <TableCell>支付状态</TableCell>
                    <TableCell>操作</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {paginatedRetails.map((retail) => (
                    <TableRow
                      key={retail.id}
                      sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                      hover
                    >
                      <TableCell padding="checkbox">
                        <Checkbox
                          checked={selectedIds.includes(retail.id)}
                          onChange={() => handleSelectOne(retail.id)}
                        />
                      </TableCell>
                      <TableCell sx={{ cursor: 'pointer', fontWeight: 'medium' }} onClick={() => handleViewDetail(retail.id)}>
                        {retail.retailNumber}
                      </TableCell>
                      <TableCell>{retail.customerName}</TableCell>
                      <TableCell sx={{ color: '#d32f2f', fontWeight: 'bold' }}>¥{retail.totalAmount}</TableCell>
                      <TableCell>
                        <Chip 
                          label={getPaymentStatusLabel(retail.paymentStatus)} 
                          color={getPaymentStatusColor(retail.paymentStatus)} 
                          size="small" 
                        />
                      </TableCell>
                      <TableCell>
                        <Tooltip title="查看详情">
                          <IconButton size="small" onClick={() => handleViewDetail(retail.id)}>
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
            count={filteredRetails.length}
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

export default RetailManagement;