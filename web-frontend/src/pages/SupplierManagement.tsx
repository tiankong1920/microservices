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
import { supplierApi } from '../services/api';
import ExportService from '../services/exportService';
import Pagination from '../components/Pagination';
import DateRangePicker from '../components/DateRangePicker';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import EmptyState from '../components/EmptyState';
import Skeleton from '../components/Skeleton';
import type { ApiResponse, Supplier } from '../types';

const SupplierManagement: React.FC = () => {
  const navigate = useNavigate();
  const [suppliers, setSuppliers] = useState<Supplier[]>([]);
  const [filteredSuppliers, setFilteredSuppliers] = useState<Supplier[]>([]);
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
  const [sortField, setSortField] = useState<string>('supplierName');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [batchDeleteLoading, setBatchDeleteLoading] = useState<boolean>(false);
  const { showSuccess, showError, showWarning } = useToast();

  const loadSuppliers = async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const response: ApiResponse<Supplier[]> = await supplierApi.getAllSuppliers();
      setSuppliers(response.data);
    } catch (err) {
      console.error('Failed to load suppliers:', err);
      setError('加载供应商数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSuppliers();
  }, []);

  useEffect(() => {
    applyFiltersAndSort();
  }, [suppliers, searchTerm, startDate, endDate, statusFilter, sortField, sortOrder, page, rowsPerPage]);

  const applyFiltersAndSort = (): void => {
    let filtered = [...suppliers];

    if (searchTerm) {
      filtered = filtered.filter(supplier => 
        supplier.supplierName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        supplier.contactPerson?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        supplier.phone?.includes(searchTerm) ||
        supplier.email?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    if (startDate) {
      filtered = filtered.filter(supplier => {
        const supplierDate = supplier.createdAt;
        return supplierDate >= startDate;
      });
    }

    if (endDate) {
      filtered = filtered.filter(supplier => {
        const supplierDate = supplier.createdAt;
        return supplierDate <= endDate;
      });
    }

    if (statusFilter !== 'all') {
      filtered = filtered.filter(supplier => supplier.status === statusFilter);
    }

    filtered.sort((a, b) => {
      const aValue = (a as any)[sortField];
      const bValue = (b as any)[sortField];
      
      if (aValue === undefined || bValue === undefined) return 0;
      if (aValue < bValue) return sortOrder === 'asc' ? -1 :1;
      if (aValue > bValue) return sortOrder === 'asc' ?1 : -1;
      return 0;
    });

    setFilteredSuppliers(filtered);
  };

  const handleViewDetail = (supplierId: number): void => {
    navigate(`/suppliers/${supplierId}`);
  };

  const handleEditSupplier = (supplierId: number): void => {
    navigate(`/suppliers/edit/${supplierId}`);
  };

  const handleAddSupplier = (): void => {
    navigate('/suppliers/edit/new');
  };

  const _handleDeleteSupplier = async (_supplierId: number): Promise<void> => {
    setDeleteDialogOpen(true);
  };

  const confirmDeleteSupplier = async (supplierId: number): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      await supplierApi.deleteSupplier(supplierId);
      showSuccess('供应商删除成功');
      loadSuppliers();
      setDeleteDialogOpen(false);
    } catch (err) {
      console.error('Failed to delete supplier:', err);
      showError('删除供应商失败');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (supplier: Supplier): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      await supplierApi.updateSupplier(supplier.id, { status: supplier.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE' });
      showSuccess('供应商状态更新成功');
      loadSuppliers();
    } catch (err) {
      console.error('Failed to update supplier status:', err);
      showError('更新供应商状态失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>): void => {
    const checked = event.target.checked;
    setSelectAll(checked);
    if (checked) {
      setSelectedIds(filteredSuppliers.map(s => s.id));
    } else {
      setSelectedIds([]);
    }
  };

  const handleSelectOne = (supplierId: number): void => {
    const selectedIndex = selectedIds.indexOf(supplierId);
    if (selectedIndex === -1) {
      setSelectedIds([...selectedIds, supplierId]);
    } else {
      setSelectedIds(selectedIds.filter(id => id !== supplierId));
    }
  };

  const handleBatchDelete = async (): Promise<void> => {
    if (selectedIds.length === 0) {
      showWarning('请先选择要删除的供应商');
      return;
    }
    setBatchDeleteLoading(true);
    try {
      await Promise.all(selectedIds.map(id => supplierApi.deleteSupplier(id)));
      showSuccess(`成功删除 ${selectedIds.length} 个供应商`);
      setSelectedIds([]);
      setSelectAll(false);
      loadSuppliers();
    } catch (err) {
      console.error('Failed to batch delete suppliers:', err);
      showError('批量删除失败');
    } finally {
      setBatchDeleteLoading(false);
    }
  };

  const handleExport = (format: 'csv' | 'excel' | 'json' | 'pdf'): void => {
    if (filteredSuppliers.length === 0) {
      showWarning('没有可导出的数据');
      return;
    }
    const fields = [
      { key: 'supplierId', label: '供应商ID' },
      { key: 'supplierName', label: '供应商名称' },
      { key: 'contactPerson', label: '联系人' },
      { key: 'phone', label: '电话' },
      { key: 'email', label: '邮箱' },
      { key: 'paymentTerms', label: '付款条件' },
      { key: 'status', label: '状态' },
      { key: 'createdAt', label: '创建时间' },
    ];
    
    if (format === 'csv') {
      ExportService.exportToCSV(filteredSuppliers, { filename: 'suppliers', fields });
    } else if (format === 'excel') {
      ExportService.exportToExcel(filteredSuppliers, { filename: 'suppliers', fields });
    } else if (format === 'pdf') {
      ExportService.exportToPDF(filteredSuppliers, { filename: 'suppliers', fields });
    } else {
      ExportService.exportToJSON(filteredSuppliers, { filename: 'suppliers', fields });
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

  const paginatedSuppliers = filteredSuppliers.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  return (
    <Box>
      
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个供应商吗？删除后无法恢复。"
        confirmText="删除"
        cancelText="取消"
        severity="warning"
        onConfirm={() => selectedIds.length > 0 ? handleBatchDelete() : confirmDeleteSupplier(selectedIds[0])}
        onCancel={() => setDeleteDialogOpen(false)}
        loading={loading || batchDeleteLoading}
      />

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          供应商管理
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleAddSupplier}
          >
            新增供应商
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
            placeholder="搜索供应商名称、联系人、电话或邮箱..."
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
        <Skeleton type="table" rows={rowsPerPage} columns={8} />
      ) : filteredSuppliers.length === 0 ? (
        <EmptyState type="no-results" />
      ) : (
        <Box>
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 2 }}>
            <Checkbox
              checked={selectAll}
              onChange={handleSelectAll}
              indeterminate={selectedIds.length > 0 && selectedIds.length < paginatedSuppliers.length}
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
              <Table sx={{ minWidth: 1000 }} aria-label="supplier table">
                <TableHead>
                  <TableRow>
                    <TableCell padding="checkbox">
                      <Checkbox checked={selectAll} onChange={handleSelectAll} />
                    </TableCell>
                    <TableCell>
                      <Button
                        size="small"
                        onClick={() => handleSort('supplierName')}
                        endIcon={sortField === 'supplierName' ? (sortOrder === 'asc' ? '↑' : '↓') : null}
                      >
                        供应商名称
                      </Button>
                    </TableCell>
                    <TableCell>联系人</TableCell>
                    <TableCell>电话</TableCell>
                    <TableCell>邮箱</TableCell>
                    <TableCell>付款条件</TableCell>
                    <TableCell>状态</TableCell>
                    <TableCell>操作</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {paginatedSuppliers.map((supplier) => (
                    <TableRow
                      key={supplier.id}
                      sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                      hover
                    >
                      <TableCell padding="checkbox">
                        <Checkbox
                          checked={selectedIds.includes(supplier.id)}
                          onChange={() => handleSelectOne(supplier.id)}
                        />
                      </TableCell>
                      <TableCell sx={{ cursor: 'pointer', fontWeight: 'medium' }} onClick={() => handleViewDetail(supplier.id)}>
                        {supplier.supplierName}
                      </TableCell>
                      <TableCell>{supplier.contactPerson}</TableCell>
                      <TableCell>{supplier.phone}</TableCell>
                      <TableCell>{supplier.email}</TableCell>
                      <TableCell>{supplier.paymentTerms || '-'}</TableCell>
                      <TableCell>
                        <Switch
                          checked={supplier.status === 'ACTIVE'}
                          onChange={() => handleToggleStatus(supplier)}
                          color="success"
                        />
                      </TableCell>
                      <TableCell>
                        <Tooltip title="查看详情">
                          <IconButton size="small" onClick={() => handleViewDetail(supplier.id)}>
                            <VisibilityIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="编辑">
                          <IconButton size="small" onClick={() => handleEditSupplier(supplier.id)}>
                            <EditIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="删除">
                          <IconButton size="small" onClick={() => _handleDeleteSupplier(supplier.id)} color="error">
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
            count={filteredSuppliers.length}
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

export default SupplierManagement;