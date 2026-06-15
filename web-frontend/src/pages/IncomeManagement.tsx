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
import { incomeApi } from '../services/api';
import ExportService from '../services/exportService';
import Pagination from '../components/Pagination';
import DateRangePicker from '../components/DateRangePicker';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import EmptyState from '../components/EmptyState';
import Skeleton from '../components/Skeleton';
import type { Income } from '../types';

const IncomeManagement: React.FC = () => {
  const navigate = useNavigate();
  const [incomes, setIncomes] = useState<Income[]>([]);
  const [filteredIncomes, setFilteredIncomes] = useState<Income[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [_error, setError] = useState<string | null>(null);
  const [filteredType, setFilteredType] = useState<string>('all');
  const [filteredStatus, setFilteredStatus] = useState<string>('all');
  const [page, setPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [selectAll, setSelectAll] = useState<boolean>(false);
  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');
  const [sortField, setSortField] = useState<string>('incomeDate');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('desc');
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [batchDeleteLoading, setBatchDeleteLoading] = useState<boolean>(false);
  const { showSuccess, showError, showWarning } = useToast();

  const loadIncomes = React.useCallback(async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const response = await incomeApi.getAllIncomes();
      setIncomes(response.data);
    } catch (err) {
      console.error('Failed to load incomes:', err);
      setError('加载收入单数据失败');
    } finally {
      setLoading(false);
    }
  }, []);

  const applyFiltersAndSort = React.useCallback((): void => {
    let filtered = [...incomes];

    if (searchTerm) {
      filtered = filtered.filter(income => 
        income.incomeNumber?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        income.incomeSource?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        income.incomeType?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    if (startDate) {
      filtered = filtered.filter(income => {
        const incomeDate = income.incomeDate;
        return incomeDate >= startDate;
      });
    }

    if (endDate) {
      filtered = filtered.filter(income => {
        const incomeDate = income.incomeDate;
        return incomeDate <= endDate;
      });
    }

    if (filteredType !== 'all') {
      filtered = filtered.filter(income => income.incomeType === filteredType);
    }

    if (filteredStatus !== 'all') {
      filtered = filtered.filter(income => income.incomeStatus === filteredStatus);
    }

    filtered.sort((a, b) => {
      const aValue = (a as any)[sortField];
      const bValue = (b as any)[sortField];
      
      if (aValue === undefined || bValue === undefined) return 0;
      if (aValue < bValue) return sortOrder === 'asc' ? -1 :1;
      if (aValue > bValue) return sortOrder === 'asc' ?1 : -1;
      return 0;
    });

    setFilteredIncomes(filtered);
  }, [incomes, searchTerm, startDate, endDate, filteredType, filteredStatus, sortField, sortOrder]);

  useEffect(() => {
    loadIncomes();
  }, [loadIncomes]);

  useEffect(() => {
    applyFiltersAndSort();
  }, [applyFiltersAndSort]);

  const handleViewDetail = (incomeId: number): void => {
    navigate(`/incomes/${incomeId}`);
  };

  const _handleDeleteIncome = async (_incomeId: number): Promise<void> => {
    setDeleteDialogOpen(true);
  };

  const confirmDeleteIncome = async (incomeId: number): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      await incomeApi.deleteIncome(incomeId);
      showSuccess('收入单删除成功');
      loadIncomes();
      setDeleteDialogOpen(false);
    } catch (err) {
      console.error('Failed to delete income:', err);
      showError('删除收入单失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>): void => {
    const checked = event.target.checked;
    setSelectAll(checked);
    if (checked) {
      setSelectedIds(filteredIncomes.map(i => i.id));
    } else {
      setSelectedIds([]);
    }
  };

  const handleSelectOne = (incomeId: number): void => {
    const selectedIndex = selectedIds.indexOf(incomeId);
    if (selectedIndex === -1) {
      setSelectedIds([...selectedIds, incomeId]);
    } else {
      setSelectedIds(selectedIds.filter(id => id !== incomeId));
    }
  };

  const handleBatchDelete = async (): Promise<void> => {
    if (selectedIds.length === 0) {
      showWarning('请先选择要删除的收入单');
      return;
    }
    setBatchDeleteLoading(true);
    try {
      await Promise.all(selectedIds.map(id => incomeApi.deleteIncome(id)));
      showSuccess(`成功删除 ${selectedIds.length} 个收入单`);
      setSelectedIds([]);
      setSelectAll(false);
      loadIncomes();
    } catch (err) {
      console.error('Failed to batch delete incomes:', err);
      showError('批量删除失败');
    } finally {
      setBatchDeleteLoading(false);
    }
  };

  const handleExport = (format: 'csv' | 'excel' | 'json' | 'pdf'): void => {
    if (filteredIncomes.length === 0) {
      showWarning('没有可导出的数据');
      return;
    }
    const fields = [
      { key: 'id', label: 'ID' },
      { key: 'incomeNumber', label: '收入单号' },
      { key: 'incomeDate', label: '收入日期' },
      { key: 'incomeAmount', label: '收入金额', format: (v) => `¥${v}` },
      { key: 'incomeType', label: '收入类型' },
      { key: 'incomeSource', label: '收入来源' },
      { key: 'incomeStatus', label: '收入状态' },
      { key: 'createdAt', label: '创建时间' },
    ];
    
    if (format === 'csv') {
      ExportService.exportToCSV(filteredIncomes, { filename: 'incomes', fields });
    } else if (format === 'excel') {
      ExportService.exportToExcel(filteredIncomes, { filename: 'incomes', fields });
    } else if (format === 'pdf') {
      ExportService.exportToPDF(filteredIncomes, { filename: 'incomes', fields });
    } else {
      ExportService.exportToJSON(filteredIncomes, { filename: 'incomes', fields });
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

  const paginatedIncomes = filteredIncomes.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  const getStatusColor = (status: string): 'success' | 'warning' | 'primary' => {
    switch (status) {
      case '已确认':
        return 'success';
      case '未确认':
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
        content="确定要删除这个收入单吗？删除后无法恢复。"
        confirmText="删除"
        cancelText="取消"
        severity="warning"
        onConfirm={() => selectedIds.length > 0 ? handleBatchDelete() : confirmDeleteIncome(selectedIds[0])}
        onCancel={() => setDeleteDialogOpen(false)}
        loading={loading || batchDeleteLoading}
      />

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          收入单管理
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
            placeholder="搜索收入单号或来源..."
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
            label="收入日期"
          />
          <Button
            variant="outlined"
            startIcon={<FilterIcon />}
            onClick={() => { setFilteredType('all'); setFilteredStatus('all'); setStartDate(''); setEndDate(''); setSearchTerm(''); }}
          >
            清除筛选
          </Button>
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', ml: 'auto' }}>
            <Chip 
              label={filteredType === 'all' ? '全部类型' : filteredType} 
              onClick={() => setFilteredType(filteredType === 'all' ? '销售' : 'all')}
              color={filteredType === 'all' ? 'default' : 'primary'}
              size="small"
            />
            <Chip 
              label={filteredStatus === 'all' ? '全部状态' : filteredStatus} 
              onClick={() => setFilteredStatus(filteredStatus === 'all' ? '已确认' : 'all')}
              color={filteredStatus === 'all' ? 'default' : filteredStatus === '已确认' ? 'success' : 'warning'}
              size="small"
            />
          </Box>
        </Toolbar>
      </Paper>

      {loading ? (
        <Skeleton type="table" rows={rowsPerPage} columns={6} />
      ) : filteredIncomes.length === 0 ? (
        <EmptyState type="no-results" />
      ) : (
        <Box>
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 2 }}>
            <Checkbox
              checked={selectAll}
              onChange={handleSelectAll}
              indeterminate={selectedIds.length > 0 && selectedIds.length < paginatedIncomes.length}
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
              <Table sx={{ minWidth: 1000 }} aria-label="income table">
                <TableHead>
                  <TableRow>
                    <TableCell padding="checkbox">
                      <Checkbox checked={selectAll} onChange={handleSelectAll} />
                    </TableCell>
                    <TableCell>
                      <Button
                        size="small"
                        onClick={() => handleSort('incomeDate')}
                        endIcon={sortField === 'incomeDate' ? (sortOrder === 'asc' ? '↑' : '↓') : null}
                      >
                        收入日期
                      </Button>
                    </TableCell>
                    <TableCell>收入金额</TableCell>
                    <TableCell>收入类型</TableCell>
                    <TableCell>收入来源</TableCell>
                    <TableCell>收入状态</TableCell>
                    <TableCell>创建时间</TableCell>
                    <TableCell>操作</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {paginatedIncomes.map((income) => (
                    <TableRow
                      key={income.id}
                      sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                      hover
                    >
                      <TableCell padding="checkbox">
                        <Checkbox
                          checked={selectedIds.includes(income.id)}
                          onChange={() => handleSelectOne(income.id)}
                          color="primary"
                        />
                      </TableCell>
                      <TableCell sx={{ cursor: 'pointer', fontWeight: 'medium' }} onClick={() => handleViewDetail(income.id)}>
                        {income.incomeNumber}
                      </TableCell>
                      <TableCell>{income.incomeDate}</TableCell>
                      <TableCell sx={{ color: '#d32f2f', fontWeight: 'bold' }}>¥{income.incomeAmount}</TableCell>
                      <TableCell>{income.incomeType}</TableCell>
                      <TableCell>{income.incomeSource}</TableCell>
                      <TableCell>
                        <Chip 
                          label={income.incomeStatus} 
                          color={getStatusColor(income.incomeStatus)} 
                          size="small" 
                        />
                      </TableCell>
                      <TableCell>{income.createdAt}</TableCell>
                      <TableCell>
                        <Tooltip title="查看详情">
                          <IconButton size="small" onClick={() => handleViewDetail(income.id)}>
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
            count={filteredIncomes.length}
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

export default IncomeManagement;