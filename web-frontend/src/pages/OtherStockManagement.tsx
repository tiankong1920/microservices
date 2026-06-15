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
import { otherStockApi } from '../services/api';
import ExportService from '../services/exportService';
import Pagination from '../components/Pagination';
import DateRangePicker from '../components/DateRangePicker';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import EmptyState from '../components/EmptyState';
import Skeleton from '../components/Skeleton';
import type { ApiResponse, OtherStockOrder } from '../types';

const OtherStockManagement: React.FC = () => {
  const navigate = useNavigate();
  const [orders, setOrders] = useState<OtherStockOrder[]>([]);
  const [filteredOrders, setFilteredOrders] = useState<OtherStockOrder[]>([]);
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
  const [typeFilter, setTypeFilter] = useState<string>('all');
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
      const response: ApiResponse<OtherStockOrder[]> = await otherStockApi.getAllOtherStocks();
      setOrders(response.data);
    } catch (err) {
      console.error('Failed to load other stock orders:', err);
      setError('加载其他出入库单数据失败');
    } finally {
      setLoading(false);
    }
  }, []);

  const applyFiltersAndSort = React.useCallback((): void => {
    let filtered = [...orders];

    if (searchTerm) {
      filtered = filtered.filter(order => 
        order.orderNumber?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        order.warehouseName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        order.operatorName?.toLowerCase().includes(searchTerm.toLowerCase())
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

    if (statusFilter !== 'all') {
      filtered = filtered.filter(order => order.status === statusFilter);
    }

    if (typeFilter !== 'all') {
      filtered = filtered.filter(order => order.orderType === typeFilter);
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
  }, [orders, searchTerm, startDate, endDate, statusFilter, typeFilter, sortField, sortOrder]);

  useEffect(() => {
    loadOrders();
  }, [loadOrders]);

  useEffect(() => {
    applyFiltersAndSort();
  }, [applyFiltersAndSort]);

  const handleViewDetail = (orderId: number): void => {
    navigate(`/other-stock/${orderId}`);
  };

  const handleCreateOrder = (): void => {
    navigate('/other-stock/create');
  };

  const _handleDeleteOrder = async (_orderId: number): Promise<void> => {
    setDeleteDialogOpen(true);
  };

  const confirmDeleteOrder = async (orderId: number): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      await otherStockApi.deleteOtherStock(orderId);
      showSuccess('其他出入库单删除成功');
      loadOrders();
      setDeleteDialogOpen(false);
    } catch (err) {
      console.error('Failed to delete other stock order:', err);
      showError('删除其他出入库单失败');
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
      showWarning('请先选择要删除的其他出入库单');
      return;
    }
    setBatchDeleteLoading(true);
    try {
      await Promise.all(selectedIds.map(id => otherStockApi.deleteOtherStock(id)));
      showSuccess(`成功删除 ${selectedIds.length} 个其他出入库单`);
      setSelectedIds([]);
      setSelectAll(false);
      loadOrders();
    } catch (err) {
      console.error('Failed to batch delete other stock orders:', err);
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
      { key: 'orderNumber', label: '单号' },
      { key: 'orderType', label: '类型' },
      { key: 'orderDate', label: '日期' },
      { key: 'warehouseName', label: '仓库' },
      { key: 'operatorName', label: '操作员' },
      { key: 'totalAmount', label: '总金额', format: (v) => `¥${v}` },
      { key: 'status', label: '状态' },
    ];
    
    if (format === 'csv') {
      ExportService.exportToCSV(filteredOrders, { filename: 'other-stocks', fields });
    } else if (format === 'excel') {
      ExportService.exportToExcel(filteredOrders, { filename: 'other-stocks', fields });
    } else if (format === 'pdf') {
      ExportService.exportToPDF(filteredOrders, { filename: 'other-stocks', fields });
    } else {
      ExportService.exportToJSON(filteredOrders, { filename: 'other-stocks', fields });
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

  const getStatusColor = (status: string): 'success' | 'info' | 'warning' | 'error' | 'default' => {
    switch (status) {
      case 'COMPLETED': return 'success';
      case 'APPROVED': return 'info';
      case 'PENDING': return 'warning';
      case 'REJECTED': return 'error';
      default: return 'default';
    }
  };

  const getTypeLabel = (type: string): string => {
    switch (type) {
      case 'IN': return '入库';
      case 'OUT': return '出库';
      default: return type;
    }
  };

  return (
    <Box>
      
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个其他出入库单吗？删除后无法恢复。"
        confirmText="删除"
        cancelText="取消"
        severity="warning"
        onConfirm={() => selectedIds.length > 0 ? handleBatchDelete() : confirmDeleteOrder(selectedIds[0])}
        onCancel={() => setDeleteDialogOpen(false)}
        loading={loading || batchDeleteLoading}
      />

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          其他出入库管理
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleCreateOrder}
          >
            创建出入库单
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
            placeholder="搜索单号、仓库名称或操作员..."
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
            label="出入库日期"
          />
          <Button
            variant="outlined"
            startIcon={<FilterIcon />}
            onClick={() => { setStatusFilter('all'); setTypeFilter('all'); setStartDate(''); setEndDate(''); setSearchTerm(''); }}
          >
            清除筛选
          </Button>
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', ml: 'auto' }}>
            <Chip 
              label={typeFilter === 'all' ? '全部类型' : getTypeLabel(typeFilter)} 
              onClick={() => setTypeFilter(typeFilter === 'all' ? 'IN' : 'all')}
              color={typeFilter === 'all' ? 'default' : typeFilter === 'IN' ? 'success' : 'error'}
              size="small"
            />
            <Chip 
              label={statusFilter === 'all' ? '全部状态' : statusFilter} 
              onClick={() => setStatusFilter(statusFilter === 'all' ? 'PENDING' : 'all')}
              color={statusFilter === 'all' ? 'default' : statusFilter === 'COMPLETED' ? 'success' : statusFilter === 'REJECTED' ? 'error' : 'warning'}
              size="small"
            />
          </Box>
        </Toolbar>
      </Paper>

      {loading ? (
        <Skeleton type="table" rows={rowsPerPage} columns={7} />
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
              <Table sx={{ minWidth: 1000 }} aria-label="other stock table">
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
                        出入库日期
                      </Button>
                    </TableCell>
                    <TableCell>仓库</TableCell>
                    <TableCell>操作员</TableCell>
                    <TableCell>总金额</TableCell>
                    <TableCell>状态</TableCell>
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
                        />
                      </TableCell>
                      <TableCell sx={{ cursor: 'pointer', fontWeight: 'medium' }} onClick={() => handleViewDetail(order.id)}>
                        {order.orderNumber}
                      </TableCell>
                      <TableCell>
                        <Chip 
                          label={getTypeLabel(order.orderType)} 
                          color={order.orderType === 'IN' ? 'success' : 'error'} 
                          size="small" 
                        />
                      </TableCell>
                      <TableCell>{order.warehouseName}</TableCell>
                      <TableCell>{order.operatorName}</TableCell>
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

export default OtherStockManagement;