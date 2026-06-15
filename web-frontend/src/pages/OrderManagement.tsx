import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
  Button, TextField, IconButton, Tooltip, CircularProgress, Checkbox, 
  Toolbar, Menu, MenuItem, Chip 
} from '@mui/material';
import { 
  Search as SearchIcon, FilterList as FilterIcon, Visibility as VisibilityIcon, 
  Add as AddIcon, Download as DownloadIcon, Delete as DeleteIcon
} from '@mui/icons-material';
import { orderApi } from '../services/api';
import ExportService from '../services/exportService';
import Pagination from '../components/Pagination';
import DateRangePicker from '../components/DateRangePicker';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import EmptyState from '../components/EmptyState';
import Skeleton from '../components/Skeleton';
import type { Order, ApiResponse } from '../types';

const OrderManagement: React.FC = () => {
  const navigate = useNavigate();
  const [orders, setOrders] = useState<Order[]>([]);
  const [filteredOrders, setFilteredOrders] = useState<Order[]>([]);
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
  const [sortField, setSortField] = useState<string>('orderDate');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [batchDeleteLoading, setBatchDeleteLoading] = useState<boolean>(false);
  const { showSuccess, showError, showWarning } = useToast();

  const loadOrders = React.useCallback(async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      let response: ApiResponse<Order[]>;
      if (filteredStatus === 'all') {
        response = await orderApi.getAllOrders();
      } else {
        response = await orderApi.getOrdersByStatus(filteredStatus);
      }
      setOrders(response.data);
    } catch (err) {
      console.error('Failed to load orders:', err);
      setError('加载订单数据失败');
    } finally {
      setLoading(false);
    }
  }, [filteredStatus]);

  const applyFiltersAndSort = React.useCallback((): void => {
    let filtered = [...orders];

    if (searchTerm) {
      filtered = filtered.filter(order => 
        order.orderNo?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        order.customerName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        order.status?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    if (startDate) {
      filtered = filtered.filter(order => {
        const orderDate = order.orderDate;
        return orderDate >= startDate;
      });
    }

    if (endDate) {
      filtered = filtered.filter(order => {
        const orderDate = order.orderDate;
        return orderDate <= endDate;
      });
    }

    if (filteredStatus !== 'all') {
      filtered = filtered.filter(order => order.status === filteredStatus);
    }

    filtered.sort((a, b) => {
      const aValue = (a as any)[sortField];
      const bValue = (b as any)[sortField];
      
      if (aValue === undefined || bValue === undefined) return 0;
      if (aValue < bValue) return sortOrder === 'asc' ? -1 :1;
      if (aValue > bValue) return sortOrder === 'asc' ?1 : -1;
      return 0;
    });

    setFilteredOrders(filtered);
  }, [orders, searchTerm, startDate, endDate, filteredStatus, sortField, sortOrder]);

  useEffect(() => {
    loadOrders();
  }, [loadOrders]);

  useEffect(() => {
    applyFiltersAndSort();
  }, [applyFiltersAndSort]);

  const handleViewDetail = (orderId: number): void => {
    navigate(`/orders/${orderId}`);
  };

  const handleCreateOrder = (): void => {
    navigate('/orders/create');
  };

  const _handleDeleteOrder = async (_orderId: number): Promise<void> => {
    setDeleteDialogOpen(true);
  };

  const confirmDeleteOrder = async (orderId: number): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      await orderApi.deleteOrder(orderId);
      showSuccess('订单删除成功');
      loadOrders();
      setDeleteDialogOpen(false);
    } catch (err) {
      console.error('Failed to delete order:', err);
      showError('删除订单失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>): void => {
    const checked = event.target.checked;
    setSelectAll(checked);
    if (checked) {
      setSelectedIds(filteredOrders.map(o => o.id));
    } else {
      setSelectedIds([]);
    }
  };

  const handleSelectOne = (orderId: number): void => {
    const selectedIndex = selectedIds.indexOf(orderId);
    if (selectedIndex === -1) {
      setSelectedIds([...selectedIds, orderId]);
    } else {
      setSelectedIds(selectedIds.filter(id => id !== orderId));
    }
  };

  const handleBatchDelete = async (): Promise<void> => {
    if (selectedIds.length === 0) {
      showWarning('请先选择要删除的订单');
      return;
    }
    setBatchDeleteLoading(true);
    try {
      await Promise.all(selectedIds.map(id => orderApi.deleteOrder(id)));
      showSuccess(`成功删除 ${selectedIds.length} 个订单`);
      setSelectedIds([]);
      setSelectAll(false);
      loadOrders();
    } catch (err) {
      console.error('Failed to batch delete orders:', err);
      showError('批量删除失败');
    } finally {
      setBatchDeleteLoading(false);
    }
  };

  const handleExport = (format: 'csv' | 'excel' | 'json' | 'pdf'): void => {
    if (filteredOrders.length === 0) {
      showWarning('没有可导出的数据');
      return;
    }
    const fields = [
      { key: 'id', label: 'ID' },
      { key: 'orderNo', label: '订单号' },
      { key: 'customerName', label: '客户名称' },
      { key: 'orderDate', label: '下单日期' },
      { key: 'totalAmount', label: '订单金额', format: (v) => `¥${v}` },
      { key: 'status', label: '订单状态' },
    ];
    
    if (format === 'csv') {
      ExportService.exportToCSV(filteredOrders, { filename: 'orders', fields });
    } else if (format === 'excel') {
      ExportService.exportToExcel(filteredOrders, { filename: 'orders', fields });
    } else if (format === 'pdf') {
      ExportService.exportToPDF(filteredOrders, { filename: 'orders', fields });
    } else {
      ExportService.exportToJSON(filteredOrders, { filename: 'orders', fields });
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

  const paginatedOrders = filteredOrders.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  const getStatusColor = (status: string): 'success' | 'default' | 'warning' | 'primary' => {
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
        return 'primary';
    }
  };

  return (
    <Box>
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个订单吗？删除后无法恢复。"
        confirmText="删除"
        cancelText="取消"
        severity="warning"
        onConfirm={() => selectedIds.length > 0 ? handleBatchDelete() : confirmDeleteOrder(selectedIds[0])}
        onCancel={() => setDeleteDialogOpen(false)}
        loading={loading || batchDeleteLoading}
      />

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          订单管理
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleCreateOrder}
          >
            创建订单
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
            placeholder="搜索订单号或客户名称..."
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
            onClick={() => { setFilteredStatus('all'); setStartDate(''); setEndDate(''); setSearchTerm(''); }}
          >
            清除筛选
          </Button>
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', ml: 'auto' }}>
            <Chip 
              label={filteredStatus === 'all' ? '全部状态' : filteredStatus} 
              onClick={() => setFilteredStatus(filteredStatus === 'all' ? '待付款' : 'all')}
              color={filteredStatus === 'all' ? 'default' : filteredStatus === '已完成' ? 'success' : filteredStatus === '已取消' ? 'error' : 'warning'}
              size="small"
            />
          </Box>
        </Toolbar>
      </Paper>

      {loading ? (
        <Skeleton type="table" rows={rowsPerPage} columns={5} />
      ) : filteredOrders.length === 0 ? (
        <EmptyState type="no-results" />
      ) : (
        <Box>
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 2 }}>
            <Checkbox
              checked={selectAll}
              onChange={handleSelectAll}
              indeterminate={selectedIds.length > 0 && selectedIds.length < paginatedOrders.length}
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
              <Table sx={{ minWidth: 900 }} aria-label="order table">
                <TableHead>
                  <TableRow>
                    <TableCell padding="checkbox">
                      <Checkbox checked={selectAll} onChange={handleSelectAll} />
                    </TableCell>
                    <TableCell>
                      <Button
                        size="small"
                        onClick={() => handleSort('orderDate')}
                        endIcon={sortField === 'orderDate' ? (sortOrder === 'asc' ? '↑' : '↓') : null}
                      >
                        下单日期
                      </Button>
                    </TableCell>
                    <TableCell>客户名称</TableCell>
                    <TableCell>订单金额</TableCell>
                    <TableCell>订单状态</TableCell>
                    <TableCell>操作</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {paginatedOrders.map((order) => (
                    <TableRow
                      key={order.id}
                      sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                      hover
                    >
                      <TableCell padding="checkbox">
                        <Checkbox
                          checked={selectedIds.includes(order.id)}
                          onChange={() => handleSelectOne(order.id)}
                          color="primary"
                        />
                      </TableCell>
                      <TableCell sx={{ cursor: 'pointer', fontWeight: 'medium' }} onClick={() => handleViewDetail(order.id)}>
                        {order.orderNo}
                      </TableCell>
                      <TableCell>{order.customerName}</TableCell>
                      <TableCell sx={{ color: '#d32f2f', fontWeight: 'bold' }}>¥{order.totalAmount}</TableCell>
                      <TableCell>
                        <Chip 
                          label={order.status} 
                          color={getStatusColor(order.status)} 
                          size="small" 
                        />
                      </TableCell>
                      <TableCell>
                        <Tooltip title="查看详情">
                          <IconButton size="small" onClick={() => handleViewDetail(order.id)}>
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
            count={filteredOrders.length}
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

export default OrderManagement;