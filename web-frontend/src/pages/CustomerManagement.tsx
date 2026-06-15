import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
  Button, TextField, IconButton, Tooltip, CircularProgress, Switch, FormControlLabel, 
  Checkbox, Toolbar, Menu, MenuItem, Chip 
} from '@mui/material';
import { 
  Search as SearchIcon, Visibility as VisibilityIcon, Edit as EditIcon, Delete as DeleteIcon, 
  Add as AddIcon, MoreVert as MoreVertIcon, Download as DownloadIcon,
  FilterList as FilterIcon 
} from '@mui/icons-material';
import { customerApi } from '../services/api';
import ExportService from '../services/exportService';
import Pagination from '../components/Pagination';
import DateRangePicker from '../components/DateRangePicker';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import EmptyState from '../components/EmptyState';
import Skeleton from '../components/Skeleton';
import type { ApiResponse, Customer } from '../types';

const CustomerManagement: React.FC = () => {
  const navigate = useNavigate();
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [filteredCustomers, setFilteredCustomers] = useState<Customer[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [page, setPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [selectAll, setSelectAll] = useState<boolean>(false);
  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [sortField, setSortField] = useState<string>('customerName');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [batchDeleteLoading, setBatchDeleteLoading] = useState<boolean>(false);
  const { showSuccess, showError, showWarning } = useToast();

  const loadCustomers = async (): Promise<void> => {
    setLoading(true);
    try {
      const response: ApiResponse<Customer[]> = await customerApi.getAllCustomers();
      setCustomers(response.data);
      setFilteredCustomers(response.data);
    } catch (err) {
      console.error('Failed to load customers:', err);
      showError('加载客户数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCustomers();
  }, []);

  const applyFiltersAndSort = React.useCallback((): void => {
    let filtered = [...customers];

    if (searchTerm) {
      filtered = filtered.filter(customer => 
        customer.customerName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        customer.contactPerson?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        customer.phone?.includes(searchTerm) ||
        customer.email?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    if (startDate) {
      filtered = filtered.filter(customer => {
        const customerDate = customer.createdAt;
        return customerDate >= startDate;
      });
    }

    if (endDate) {
      filtered = filtered.filter(customer => {
        const customerDate = customer.createdAt;
        return customerDate <= endDate;
      });
    }

    if (statusFilter !== 'all') {
      filtered = filtered.filter(customer => customer.status === statusFilter);
    }

    filtered.sort((a, b) => {
      const aValue = a[sortField as keyof Customer];
      const bValue = b[sortField as keyof Customer];
      
      if (aValue === undefined || bValue === undefined) return 0;
      if (aValue < bValue) return sortOrder === 'asc' ? -1 : 1;
      if (aValue > bValue) return sortOrder === 'asc' ? 1 : -1;
      return 0;
    });

    setFilteredCustomers(filtered);
  }, [customers, searchTerm, startDate, endDate, statusFilter, sortField, sortOrder]);

  useEffect(() => {
    applyFiltersAndSort();
  }, [applyFiltersAndSort]);

  const handleViewDetail = (customerId: number): void => {
    navigate(`/customers/${customerId}`);
  };

  const handleEditCustomer = (customerId: number): void => {
    navigate(`/customers/edit/${customerId}`);
  };

  const handleAddCustomer = (): void => {
    navigate('/customers/edit/new');
  };

  const handleDeleteCustomer = async (_customerId: number): Promise<void> => {
    setDeleteDialogOpen(true);
  };

  const confirmDeleteCustomer = async (customerId: number): Promise<void> => {
    setLoading(true);
    try {
      await customerApi.deleteCustomer(customerId);
      showSuccess('客户删除成功');
      loadCustomers();
      setDeleteDialogOpen(false);
    } catch (err) {
      console.error('Failed to delete customer:', err);
      showError('删除客户失败');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (customer: Customer): Promise<void> => {
    setLoading(true);
    try {
      await customerApi.updateCustomer(customer.id, { status: customer.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE' });
      showSuccess('客户状态更新成功');
      loadCustomers();
    } catch (err) {
      console.error('Failed to update customer status:', err);
      showError('更新客户状态失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>): void => {
    const checked = event.target.checked;
    setSelectAll(checked);
    if (checked) {
      setSelectedIds(filteredCustomers.map(c => c.id));
    } else {
      setSelectedIds([]);
    }
  };

  const handleSelectOne = (customerId: number): void => {
    const selectedIndex = selectedIds.indexOf(customerId);
    if (selectedIndex === -1) {
      setSelectedIds([...selectedIds, customerId]);
    } else {
      setSelectedIds(selectedIds.filter(id => id !== customerId));
    }
  };

  const handleBatchDelete = async (): Promise<void> => {
    if (selectedIds.length === 0) {
      showWarning('请先选择要删除的客户');
      return;
    }
    setBatchDeleteLoading(true);
    try {
      await Promise.all(selectedIds.map(id => customerApi.deleteCustomer(id)));
      showSuccess(`成功删除 ${selectedIds.length} 个客户`);
      setSelectedIds([]);
      setSelectAll(false);
      loadCustomers();
    } catch (err) {
      console.error('Failed to batch delete customers:', err);
      showError('批量删除失败');
    } finally {
      setBatchDeleteLoading(false);
    }
  };

  const handleExport = (format: 'csv' | 'excel' | 'json' | 'pdf'): void => {
    if (filteredCustomers.length === 0) {
      showWarning('没有可导出的数据');
      return;
    }
    const fields = [
      { key: 'customerId', label: '客户ID' },
      { key: 'customerName', label: '客户名称' },
      { key: 'contactPerson', label: '联系人' },
      { key: 'phone', label: '电话' },
      { key: 'email', label: '邮箱' },
      { key: 'creditLimit', label: '信用额度', format: (v) => `¥${v}` },
      { key: 'currentCredit', label: '当前信用', format: (v) => `¥${v}` },
      { key: 'totalOrders', label: '总订单数' },
      { key: 'totalSalesAmount', label: '总销售额', format: (v) => `¥${v}` },
      { key: 'status', label: '状态' },
      { key: 'createdAt', label: '创建时间' },
    ];
    
    if (format === 'csv') {
      ExportService.exportToCSV(filteredCustomers, { filename: 'customers', fields });
    } else if (format === 'excel') {
      ExportService.exportToExcel(filteredCustomers, { filename: 'customers', fields });
    } else if (format === 'pdf') {
      ExportService.exportToPDF(filteredCustomers, { filename: 'customers', fields });
    } else {
      ExportService.exportToJSON(filteredCustomers, { filename: 'customers', fields });
    }
    showSuccess('导出成功');
  };

  const handleSort = (field: string): void => {
    if (sortField === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortOrder('asc');
    }
  };

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>): void => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = (): void => {
    setAnchorEl(null);
  };

  const paginatedCustomers = filteredCustomers.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  return (
    <Box>
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个客户吗？删除后无法恢复。"
        confirmText="删除"
        cancelText="取消"
        severity="warning"
        onConfirm={() => selectedIds.length > 0 ? handleBatchDelete() : confirmDeleteCustomer(selectedIds[0])}
        onCancel={() => setDeleteDialogOpen(false)}
        loading={loading || batchDeleteLoading}
      />

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          客户管理
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleAddCustomer}
          >
            新增客户
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
            placeholder="搜索客户名称、联系人、电话或邮箱..."
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
            onClick={() => { setStatusFilter('all'); setStartDate(''); setEndDate(''); setSearchTerm(''); }}
          >
            清除筛选
          </Button>
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', ml: 'auto' }}>
            <Chip 
              label={statusFilter === 'all' ? '全部状态' : statusFilter === 'ACTIVE' ? '启用' : '禁用'} 
              onClick={() => setStatusFilter(statusFilter === 'all' ? 'ACTIVE' : 'all')}
              color={statusFilter === 'all' ? 'default' : statusFilter === 'ACTIVE' ? 'success' : 'error'}
              size="small"
            />
          </Box>
        </Toolbar>
      </Paper>

      {loading ? (
        <Skeleton type="table" rows={rowsPerPage} columns={10} />
      ) : filteredCustomers.length === 0 ? (
        <EmptyState type="no-results" />
      ) : (
        <Box>
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 2 }}>
            <Checkbox
              checked={selectAll}
              onChange={handleSelectAll}
              indeterminate={selectedIds.length > 0 && selectedIds.length < paginatedCustomers.length}
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
              <Table sx={{ minWidth: 1200 }} aria-label="customer table">
                <TableHead>
                  <TableRow>
                    <TableCell padding="checkbox">
                      <Checkbox checked={selectAll} onChange={handleSelectAll} />
                    </TableCell>
                    <TableCell>
                      <Button
                        size="small"
                        onClick={() => handleSort('customerName')}
                        endIcon={sortField === 'customerName' ? (sortOrder === 'asc' ? '↑' : '↓') : null}
                      >
                        客户名称
                      </Button>
                    </TableCell>
                    <TableCell>联系人</TableCell>
                    <TableCell>电话</TableCell>
                    <TableCell>邮箱</TableCell>
                    <TableCell>信用额度</TableCell>
                    <TableCell>当前信用</TableCell>
                    <TableCell>总订单数</TableCell>
                    <TableCell>总销售额</TableCell>
                    <TableCell>状态</TableCell>
                    <TableCell>操作</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {paginatedCustomers.map((customer) => (
                    <TableRow
                      key={customer.id}
                      sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                      hover
                    >
                      <TableCell padding="checkbox">
                        <Checkbox
                          checked={selectedIds.includes(customer.id)}
                          onChange={() => handleSelectOne(customer.id)}
                        />
                      </TableCell>
                      <TableCell sx={{ cursor: 'pointer', fontWeight: 'medium' }} onClick={() => handleViewDetail(customer.id)}>
                        {customer.customerName}
                      </TableCell>
                      <TableCell>{customer.contactPerson}</TableCell>
                      <TableCell>{customer.phone}</TableCell>
                      <TableCell>{customer.email}</TableCell>
                      <TableCell>¥{customer.creditLimit || 0}</TableCell>
                      <TableCell>¥{customer.currentCredit || 0}</TableCell>
                      <TableCell>{customer.totalOrders || 0}</TableCell>
                      <TableCell>¥{customer.totalSalesAmount || 0}</TableCell>
                      <TableCell>
                        <Switch
                          checked={customer.status === 'ACTIVE'}
                          onChange={() => handleToggleStatus(customer)}
                          color="success"
                        />
                      </TableCell>
                      <TableCell>
                        <Tooltip title="查看详情">
                          <IconButton size="small" onClick={() => handleViewDetail(customer.id)}>
                            <VisibilityIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="编辑">
                          <IconButton size="small" onClick={() => handleEditCustomer(customer.id)}>
                            <EditIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="删除">
                          <IconButton size="small" onClick={() => handleDeleteCustomer(customer.id)} color="error">
                            <DeleteIcon />
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
            count={filteredCustomers.length}
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

export default CustomerManagement;