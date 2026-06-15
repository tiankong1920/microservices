import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
  Button, TextField, IconButton, Tooltip, CircularProgress, Switch, FormControlLabel, 
  Checkbox, Toolbar, Menu, MenuItem, Chip 
} from '@mui/material';
import { 
  Search as SearchIcon, Visibility as VisibilityIcon, Add as AddIcon, MoreVert as MoreVertIcon, 
  Download as DownloadIcon, FilterList as FilterIcon, Delete as DeleteIcon
} from '@mui/icons-material';
import { procurementApi } from '../services/api';
import ExportService from '../services/exportService';
import Pagination from '../components/Pagination';
import DateRangePicker from '../components/DateRangePicker';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import EmptyState from '../components/EmptyState';
import Skeleton from '../components/Skeleton';
import type { ApiResponse, ProcurementOrder } from '../types';

const ProcurementManagement: React.FC = () => {
  const navigate = useNavigate();
  const [procurements, setProcurements] = useState<ProcurementOrder[]>([]);
  const [filteredProcurements, setFilteredProcurements] = useState<ProcurementOrder[]>([]);
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
  const [sortField, setSortField] = useState<string>('orderDate');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [batchDeleteLoading, setBatchDeleteLoading] = useState<boolean>(false);
  const { showSuccess, showError, showWarning } = useToast();

  const loadProcurements = React.useCallback(async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const response: ApiResponse<ProcurementOrder[]> = await procurementApi.getAllProcurements();
      setProcurements(response.data);
    } catch (err) {
      console.error('Failed to load procurements:', err);
      setError('加载采购订单数据失败');
    } finally {
      setLoading(false);
    }
  }, []);

  const applyFiltersAndSort = React.useCallback((): void => {
    let filtered = [...procurements];

    if (searchTerm) {
      filtered = filtered.filter(procurement => 
        procurement.orderNo?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        procurement.supplierName?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    if (startDate) {
      filtered = filtered.filter(procurement => {
        const orderDate = procurement.orderDate;
        return orderDate >= startDate;
      });
    }

    if (endDate) {
      filtered = filtered.filter(procurement => {
        const orderDate = procurement.orderDate;
        return orderDate <= endDate;
      });
    }

    if (statusFilter !== 'all') {
      filtered = filtered.filter(procurement => procurement.status === statusFilter);
    }

    filtered.sort((a, b) => {
      const aValue = (a as any)[sortField];
      const bValue = (b as any)[sortField];
      
      if (aValue === undefined || bValue === undefined) return 0;
      if (aValue < bValue) return sortOrder === 'asc' ? -1 :1;
      if (aValue > bValue) return sortOrder === 'asc' ?1 : -1;
      return 0;
    });

    setFilteredProcurements(filtered);
  }, [procurements, searchTerm, startDate, endDate, statusFilter, sortField, sortOrder]);

  useEffect(() => {
    loadProcurements();
  }, [loadProcurements]);

  useEffect(() => {
    applyFiltersAndSort();
  }, [applyFiltersAndSort]);

  const handleViewDetail = (procurementId: number): void => {
    navigate(`/procurement/${procurementId}`);
  };

  const handleAddProcurement = (): void => {
    navigate('/procurement/create');
  };

  const _handleDeleteProcurement = async (_procurementId: number): Promise<void> => {
    setDeleteDialogOpen(true);
  };

  const confirmDeleteProcurement = async (procurementId: number): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      await procurementApi.deleteProcurement(procurementId);
      showSuccess('采购订单删除成功');
      loadProcurements();
      setDeleteDialogOpen(false);
    } catch (err) {
      console.error('Failed to delete procurement:', err);
      showError('删除采购订单失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>): void => {
    const checked = event.target.checked;
    setSelectAll(checked);
    if (checked) {
      setSelectedIds(filteredProcurements.map(p => p.id));
    } else {
      setSelectedIds([]);
    }
  };

  const handleSelectOne = (procurementId: number): void => {
    const selectedIndex = selectedIds.indexOf(procurementId);
    if (selectedIndex === -1) {
      setSelectedIds([...selectedIds, procurementId]);
    } else {
      setSelectedIds(selectedIds.filter(id => id !== procurementId));
    }
  };

  const handleBatchDelete = async (): Promise<void> => {
    if (selectedIds.length === 0) {
      showWarning('请先选择要删除的采购订单');
      return;
    }
    setBatchDeleteLoading(true);
    try {
      await Promise.all(selectedIds.map(id => procurementApi.deleteProcurement(id)));
      showSuccess(`成功删除 ${selectedIds.length} 个采购订单`);
      setSelectedIds([]);
      setSelectAll(false);
      loadProcurements();
    } catch (err) {
      console.error('Failed to batch delete procurements:', err);
      showError('批量删除失败');
    } finally {
      setBatchDeleteLoading(false);
    }
  };

  const handleExport = (format: 'csv' | 'excel' | 'json' | 'pdf'): void => {
    if (filteredProcurements.length === 0) {
      showWarning('没有可导出的数据');
      return;
    }
    const fields = [
      { key: 'id', label: 'ID' },
      { key: 'orderNo', label: '采购订单号' },
      { key: 'supplierName', label: '供应商名称' },
      { key: 'orderDate', label: '下单日期' },
      { key: 'totalAmount', label: '采购金额', format: (v) => `¥${v}` },
      { key: 'status', label: '订单状态' },
      { key: 'createdAt', label: '创建时间' },
    ];
    
    if (format === 'csv') {
      ExportService.exportToCSV(filteredProcurements, { filename: 'procurements', fields });
    } else if (format === 'excel') {
      ExportService.exportToExcel(filteredProcurements, { filename: 'procurements', fields });
    } else if (format === 'pdf') {
      ExportService.exportToPDF(filteredProcurements, { filename: 'procurements', fields });
    } else {
      ExportService.exportToJSON(filteredProcurements, { filename: 'procurements', fields });
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

  const paginatedProcurements = filteredProcurements.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  const getStatusColor = (status: string): 'success' | 'default' | 'warning' | 'primary' => {
    switch (status) {
      case '已完成':
        return 'success';
      case '已拒绝':
      case '已取消':
        return 'default';
      case '待审批':
        return 'warning';
      case '已审批':
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
        content="确定要删除这个采购订单吗？删除后无法恢复。"
        confirmText="删除"
        cancelText="取消"
        severity="warning"
        onConfirm={() => selectedIds.length > 0 ? handleBatchDelete() : confirmDeleteProcurement(selectedIds[0])}
        onCancel={() => setDeleteDialogOpen(false)}
        loading={loading || batchDeleteLoading}
      />

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          采购管理
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleAddProcurement}
          >
            创建采购订单
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
            placeholder="搜索采购订单号或供应商名称..."
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
              onClick={() => setStatusFilter(statusFilter === 'all' ? '待审批' : 'all')}
              color={statusFilter === 'all' ? 'default' : statusFilter === '已完成' ? 'success' : statusFilter === '已拒绝' || statusFilter === '已取消' ? 'error' : 'warning'}
              size="small"
            />
          </Box>
        </Toolbar>
      </Paper>

      {loading ? (
        <Skeleton type="table" rows={rowsPerPage} columns={5} />
      ) : filteredProcurements.length === 0 ? (
        <EmptyState type="no-results" />
      ) : (
        <Box>
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 2 }}>
            <Checkbox
              checked={selectAll}
              onChange={handleSelectAll}
              indeterminate={selectedIds.length > 0 && selectedIds.length < paginatedProcurements.length}
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
              <Table sx={{ minWidth: 900 }} aria-label="procurement table">
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
                    <TableCell>供应商名称</TableCell>
                    <TableCell>采购金额</TableCell>
                    <TableCell>订单状态</TableCell>
                    <TableCell>操作</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {paginatedProcurements.map((procurement) => (
                    <TableRow
                      key={procurement.id}
                      sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                      hover
                    >
                      <TableCell padding="checkbox">
                        <Checkbox
                          checked={selectedIds.includes(procurement.id)}
                          onChange={() => handleSelectOne(procurement.id)}
                        />
                      </TableCell>
                      <TableCell sx={{ cursor: 'pointer', fontWeight: 'medium' }} onClick={() => handleViewDetail(procurement.id)}>
                        {procurement.orderNo}
                      </TableCell>
                      <TableCell>{procurement.supplierName}</TableCell>
                      <TableCell sx={{ color: '#d32f2f', fontWeight: 'bold' }}>¥{procurement.totalAmount}</TableCell>
                      <TableCell>
                        <Chip 
                          label={procurement.status} 
                          color={getStatusColor(procurement.status)} 
                          size="small" 
                        />
                      </TableCell>
                      <TableCell>
                        <Tooltip title="查看详情">
                          <IconButton size="small" onClick={() => handleViewDetail(procurement.id)}>
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
            count={filteredProcurements.length}
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

export default ProcurementManagement;