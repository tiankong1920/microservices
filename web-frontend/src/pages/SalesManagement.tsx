import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
  Button, TextField, IconButton, Tooltip, CircularProgress, Checkbox, Toolbar, Menu, MenuItem, Chip 
} from '@mui/material';
import { 
  Search as SearchIcon, Visibility as VisibilityIcon, Add as AddIcon, 
  Download as DownloadIcon, FilterList as FilterIcon, Delete as DeleteIcon
} from '@mui/icons-material';
import { salesApi } from '../services/api';
import ExportService from '../services/exportService';
import Pagination from '../components/Pagination';
import DateRangePicker from '../components/DateRangePicker';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import EmptyState from '../components/EmptyState';
import Skeleton from '../components/Skeleton';
import type { ApiResponse, SalesOrder } from '../types';

const SalesManagement: React.FC = () => {
  const navigate = useNavigate();
  const [salesOrders, setSalesOrders] = useState<SalesOrder[]>([]);
  const [filteredSalesOrders, setFilteredSalesOrders] = useState<SalesOrder[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [_error, setError] = useState<string | null>(null);
  const [page, setPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [selectAll, setSelectAll] = useState<boolean>(false);
  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [sortField, setSortField] = useState<string>('saleDate');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [batchDeleteLoading, setBatchDeleteLoading] = useState<boolean>(false);
  const { showSuccess, showError, showWarning } = useToast();

  const loadSalesOrders = async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const response: ApiResponse<SalesOrder[]> = await salesApi.getAllSales();
      setSalesOrders(response.data);
    } catch (err) {
      console.error('Failed to load sales orders:', err);
      setError('加载销售订单数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSalesOrders();
  }, []);

  useEffect(() => {
    applyFiltersAndSort();
  }, [salesOrders, searchTerm, startDate, endDate, statusFilter, sortField, sortOrder, page, rowsPerPage]);

  const applyFiltersAndSort = (): void => {
    let filtered = [...salesOrders];

    if (searchTerm) {
      filtered = filtered.filter(salesOrder => 
        salesOrder.saleNumber?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        salesOrder.customerName?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    if (startDate) {
      filtered = filtered.filter(salesOrder => {
        const saleDate = salesOrder.saleDate || salesOrder.orderDate;
        return saleDate >= startDate;
      });
    }

    if (endDate) {
      filtered = filtered.filter(salesOrder => {
        const saleDate = salesOrder.saleDate || salesOrder.orderDate;
        return saleDate <= endDate;
      });
    }

    if (statusFilter !== 'all') {
      filtered = filtered.filter(salesOrder => salesOrder.status === statusFilter);
    }

    filtered.sort((a, b) => {
      const aValue = (a as any)[sortField];
      const bValue = (b as any)[sortField];
      
      if (aValue === undefined || bValue === undefined) return 0;
      if (aValue < bValue) return sortOrder === 'asc' ? -1 :1;
      if (aValue > bValue) return sortOrder === 'asc' ?1 : -1;
      return 0;
    });

    setFilteredSalesOrders(filtered);
  };

  const handleViewDetail = (salesOrderId: number): void => {
    navigate(`/sales/${salesOrderId}`);
  };

  const handleAddSales = (): void => {
    navigate('/sales/create');
  };

  const _handleDeleteSales = async (_salesOrderId: number): Promise<void> => {
    setDeleteDialogOpen(true);
  };

  const confirmDeleteSales = async (salesOrderId: number): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      await salesApi.deleteSale(salesOrderId);
      showSuccess('销售订单删除成功');
      loadSalesOrders();
      setDeleteDialogOpen(false);
    } catch (err) {
      console.error('Failed to delete sales order:', err);
      showError('删除销售订单失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>): void => {
    const checked = event.target.checked;
    setSelectAll(checked);
    if (checked) {
      setSelectedIds(filteredSalesOrders.map(s => s.id));
    } else {
      setSelectedIds([]);
    }
  };

  const handleSelectOne = (salesOrderId: number): void => {
    const selectedIndex = selectedIds.indexOf(salesOrderId);
    if (selectedIndex === -1) {
      setSelectedIds([...selectedIds, salesOrderId]);
    } else {
      setSelectedIds(selectedIds.filter(id => id !== salesOrderId));
    }
  };

  const handleBatchDelete = async (): Promise<void> => {
    if (selectedIds.length === 0) {
      showWarning('请先选择要删除的销售订单');
      return;
    }
    setBatchDeleteLoading(true);
    try {
      await Promise.all(selectedIds.map(id => salesApi.deleteSale(id)));
      showSuccess(`成功删除 ${selectedIds.length} 个销售订单`);
      setSelectedIds([]);
      setSelectAll(false);
      loadSalesOrders();
    } catch (err) {
      console.error('Failed to batch delete sales orders:', err);
      showError('批量删除失败');
    } finally {
      setBatchDeleteLoading(false);
    }
  };

  const handleExport = (format: 'csv' | 'excel' | 'json' | 'pdf'): void => {
    if (filteredSalesOrders.length === 0) {
      showWarning('没有可导出的数据');
      return;
    }
    const fields = [
      { key: 'id', label: 'ID' },
      { key: 'saleNumber', label: '销售订单号' },
      { key: 'customerName', label: '客户名称' },
      { key: 'saleDate', label: '下单日期' },
      { key: 'totalAmount', label: '销售金额', format: (v) => `¥${v}` },
      { key: 'status', label: '订单状态' },
    ];
    
    if (format === 'csv') {
      ExportService.exportToCSV(filteredSalesOrders, { filename: 'sales', fields });
    } else if (format === 'excel') {
      ExportService.exportToExcel(filteredSalesOrders, { filename: 'sales', fields });
    } else if (format === 'pdf') {
      ExportService.exportToPDF(filteredSalesOrders, { filename: 'sales', fields });
    } else {
      ExportService.exportToJSON(filteredSalesOrders, { filename: 'sales', fields });
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

  const paginatedSalesOrders = filteredSalesOrders.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  const getStatusColor = (status: string): 'success' | 'default' | 'warning' | 'primary' | 'info' => {
    switch (status) {
      case '已完成':
        return 'success';
      case '已取消':
        return 'default';
      case '待付款':
        return 'warning';
      case '处理中':
        return 'primary';
      default:
        return 'info';
    }
  };

  return (
    <Box>
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个销售订单吗？删除后无法恢复。"
        confirmText="删除"
        cancelText="取消"
        severity="warning"
        onConfirm={() => selectedIds.length > 0 ? handleBatchDelete() : confirmDeleteSales(selectedIds[0])}
        onCancel={() => setDeleteDialogOpen(false)}
        loading={loading || batchDeleteLoading}
      />

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          销售管理
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleAddSales}
          >
            创建销售订单
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
            placeholder="搜索销售订单号或客户名称..."
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
            label="下单日期"
          />
          <Button
            variant="outlined"
            startIcon={<FilterIcon />}
            onClick={() => { setStatusFilter('all'); setStartDate(''); setEndDate(''); setSearchTerm(''); }}
          >
            清除筛选
          </Button>
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', ml: 'auto' }}>
            <Chip 
              label={statusFilter === 'all' ? '全部状态' : statusFilter} 
              onClick={() => setStatusFilter(statusFilter === 'all' ? '待付款' : 'all')}
              color={statusFilter === 'all' ? 'default' : statusFilter === '已完成' ? 'success' : statusFilter === '已取消' ? 'error' : 'warning'}
              size="small"
            />
          </Box>
        </Toolbar>
      </Paper>

      {loading ? (
        <Skeleton type="table" rows={rowsPerPage} columns={5} />
      ) : filteredSalesOrders.length === 0 ? (
        <EmptyState type="no-results" />
      ) : (
        <Box>
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 2 }}>
            <Checkbox
              checked={selectAll}
              onChange={handleSelectAll}
              indeterminate={selectedIds.length > 0 && selectedIds.length < paginatedSalesOrders.length}
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
              <Table sx={{ minWidth: 900 }} aria-label="sales table">
                <TableHead>
                  <TableRow>
                    <TableCell padding="checkbox">
                      <Checkbox checked={selectAll} onChange={handleSelectAll} />
                    </TableCell>
                    <TableCell>
                      <Button
                        size="small"
                        onClick={() => handleSort('saleDate')}
                        endIcon={sortField === 'saleDate' ? (sortOrder === 'asc' ? '↑' : '↓') : null}
                      >
                        下单日期
                      </Button>
                    </TableCell>
                    <TableCell>客户名称</TableCell>
                    <TableCell>销售金额</TableCell>
                    <TableCell>订单状态</TableCell>
                    <TableCell>操作</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {paginatedSalesOrders.map((salesOrder) => (
                    <TableRow
                      key={salesOrder.id}
                      sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                      hover
                    >
                      <TableCell padding="checkbox">
                        <Checkbox
                          checked={selectedIds.includes(salesOrder.id)}
                          onChange={() => handleSelectOne(salesOrder.id)}
                        />
                      </TableCell>
                      <TableCell sx={{ cursor: 'pointer', fontWeight: 'medium' }} onClick={() => handleViewDetail(salesOrder.id)}>
                        {salesOrder.saleNumber}
                      </TableCell>
                      <TableCell>{salesOrder.customerName}</TableCell>
                      <TableCell sx={{ color: '#d32f2f', fontWeight: 'bold' }}>¥{salesOrder.totalAmount}</TableCell>
                      <TableCell>
                        <Chip 
                          label={salesOrder.status} 
                          color={getStatusColor(salesOrder.status)} 
                          size="small" 
                        />
                      </TableCell>
                      <TableCell>
                        <Tooltip title="查看详情">
                          <IconButton size="small" onClick={() => handleViewDetail(salesOrder.id)}>
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
            count={filteredSalesOrders.length}
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

export default SalesManagement;