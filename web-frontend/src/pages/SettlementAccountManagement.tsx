import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
  Button, TextField, IconButton, Tooltip, CircularProgress, Switch, FormControlLabel, 
  Checkbox, Toolbar, Menu, MenuItem, Chip, InputAdornment 
} from '@mui/material';
import { 
  Search as SearchIcon, Visibility as VisibilityIcon, Edit as EditIcon, Delete as DeleteIcon, 
  Add as AddIcon, MoreVert as MoreVertIcon, Download as DownloadIcon,
  FilterList as FilterIcon 
} from '@mui/icons-material';
import { settlementAccountApi } from '../services/api';
import ExportService from '../services/exportService';
import Pagination from '../components/Pagination';
import DateRangePicker from '../components/DateRangePicker';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import EmptyState from '../components/EmptyState';
import Skeleton from '../components/Skeleton';
import type { ApiResponse, SettlementAccount } from '../types';

const SettlementAccountManagement: React.FC = () => {
  const navigate = useNavigate();
  const [accounts, setAccounts] = useState<SettlementAccount[]>([]);
  const [filteredAccounts, setFilteredAccounts] = useState<SettlementAccount[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [selectAll, setSelectAll] = useState<boolean>(false);
  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [sortField, setSortField] = useState<string>('accountName');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [_batchDeleteLoading, _setBatchDeleteLoading] = useState<boolean>(false);
  const { showSuccess, showError } = useToast();

  const loadAccounts = async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const response: ApiResponse<SettlementAccount[]> = await settlementAccountApi.getAllSettlementAccounts();
      setAccounts(response.data);
    } catch (err) {
      console.error('Failed to load settlement accounts:', err);
      setError('加载结算账户数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAccounts();
  }, []);

  useEffect(() => {
    applyFiltersAndSort();
  }, [accounts, searchTerm, startDate, endDate, statusFilter, sortField, sortOrder, page, rowsPerPage]);

  const applyFiltersAndSort = (): void => {
    let filtered = [...accounts];

    if (searchTerm) {
      filtered = filtered.filter(account => 
        account.accountName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        account.bankName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        account.accountNumber?.includes(searchTerm)
      );
    }

    if (startDate) {
      filtered = filtered.filter(account => {
        const accountDate = account.createdAt;
        return accountDate >= startDate;
      });
    }

    if (endDate) {
      filtered = filtered.filter(account => {
        const accountDate = account.createdAt;
        return accountDate <= endDate;
      });
    }

    if (statusFilter !== 'all') {
      filtered = filtered.filter(account => account.status === statusFilter);
    }

    filtered.sort((a, b) => {
      const aValue = (a as any)[sortField];
      const bValue = (b as any)[sortField];
      
      if (aValue === undefined || bValue === undefined) return 0;
      if (aValue < bValue) return sortOrder === 'asc' ? -1 : 1;
      if (aValue > bValue) return sortOrder === 'asc' ? 1 : -1;
      return 0;
    });

    setFilteredAccounts(filtered);
  };

  const handleViewDetail = (accountId: number): void => {
    navigate(`/settlement-accounts/${accountId}`);
  };

  const handleEditAccount = (accountId: number): void => {
    navigate(`/settlement-accounts/edit/${accountId}`);
  };

  const handleAddAccount = (): void => {
    navigate('/settlement-accounts/edit/new');
  };

  const handleDeleteAccount = async (): Promise<void> => {
    setDeleteDialogOpen(true);
  };

  const confirmDeleteAccount = async (accountId: number): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      await settlementAccountApi.deleteSettlementAccount(accountId);
      showSuccess('结算账户删除成功');
      loadAccounts();
      setDeleteDialogOpen(false);
    } catch (err) {
      console.error('Failed to delete settlement account:', err);
      showError('删除结算账户失败');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (account: SettlementAccount): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      await settlementAccountApi.updateSettlementAccount(account.id, { status: account.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE' });
      showSuccess('账户状态更新成功');
      loadAccounts();
    } catch (err) {
      console.error('Failed to update account status:', err);
      showError('更新账户状态失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>): void => {
    if (event.target.checked) {
      const newSelectedIds = filteredAccounts.map(account => account.id);
      setSelectedIds(newSelectedIds);
      setSelectAll(true);
    } else {
      setSelectedIds([]);
      setSelectAll(false);
    }
  };

  const handleSelectOne = (id: number): void => {
    const newSelectedIds = selectedIds.includes(id)
      ? selectedIds.filter(selectedId => selectedId !== id)
      : [...selectedIds, id];
    setSelectedIds(newSelectedIds);
    setSelectAll(newSelectedIds.length === filteredAccounts.length);
  };

  const handleSort = (field: string): void => {
    if (sortField === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      setSortField(field);
      setSortOrder('asc');
    }
  };

  const handleExport = async (format: 'csv' | 'excel' | 'json' | 'pdf'): Promise<void> => {
    try {
      const fields = [
        { key: 'accountId', label: '账户ID' },
        { key: 'accountName', label: '账户名称' },
        { key: 'accountType', label: '账户类型' },
        { key: 'bankName', label: '开户银行' },
        { key: 'accountNumber', label: '账号' },
        { key: 'status', label: '状态' },
        { key: 'balance', label: '余额' },
        { key: 'createdAt', label: '创建时间' },
      ];
      await ExportService.exportData(filteredAccounts, fields, `settlement-accounts.${format}`, format);
      showSuccess('导出成功');
    } catch (err) {
      console.error('Export failed:', err);
      showError('导出失败');
    }
  };

  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>): void => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = (): void => {
    setAnchorEl(null);
  };

  const getStatusColor = (status: string): string => {
    switch (status) {
      case 'ACTIVE': return '#388e3c';
      case 'INACTIVE': return '#9e9e9e';
      default: return '#1976d2';
    }
  };

  const paginatedAccounts = filteredAccounts.slice(page * rowsPerPage, (page + 1) * rowsPerPage);

  if (loading && accounts.length === 0) {
    return (
      <Box>
        <Skeleton type="table" rows={5} />
      </Box>
    );
  }

  if (error && accounts.length === 0) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h6" color="error" gutterBottom>
          {error}
        </Typography>
        <Button variant="contained" onClick={loadAccounts}>
          重试
        </Button>
      </Box>
    );
  }

  if (!loading && filteredAccounts.length === 0) {
    return (
      <Box sx={{ p: 3 }}>
        <EmptyState
          type="no-results"
          title="暂无结算账户"
          description="当前没有结算账户数据，您可以添加新的结算账户"
          actionText="添加结算账户"
          onAction={handleAddAccount}
        />
      </Box>
    );
  }

  return (
    <Box>
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content={`确定要删除选中的 ${selectedIds.length} 个结算账户吗？删除后无法恢复。`}
        confirmText="删除"
        cancelText="取消"
        severity="warning"
        onConfirm={() => confirmDeleteAccount(selectedIds[0])}
        onCancel={() => setDeleteDialogOpen(false)}
        loading={_batchDeleteLoading}
      />

      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h5" gutterBottom>
          结算账户管理
        </Typography>

        <Box sx={{ display: 'flex', gap: 2, mb: 3, flexWrap: 'wrap' }}>
          <TextField
            placeholder="搜索账户名称、银行、账号..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon />
                </InputAdornment>
              ),
            }}
            sx={{ flex: 1, minWidth: 250 }}
          />

          <DateRangePicker
            startDate={startDate}
            endDate={endDate}
            onStartDateChange={setStartDate}
            onEndDateChange={setEndDate}
          />

          <FormControlLabel
            control={
              <Switch
                checked={statusFilter !== 'all'}
                onChange={(e) => setStatusFilter(e.target.checked ? 'ACTIVE' : 'all')}
              />
            }
            label="仅显示启用账户"
          />

          <Button
            variant="outlined"
            startIcon={<FilterIcon />}
            onClick={() => {
              setSearchTerm('');
              setStartDate('');
              setEndDate('');
              setStatusFilter('all');
            }}
          >
            重置筛选
          </Button>

          <Button
            variant="outlined"
            startIcon={<DownloadIcon />}
            onClick={(e) => handleMenuOpen(e)}
            disabled={filteredAccounts.length === 0}
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

        {selectedIds.length > 0 && (
          <Button
            variant="contained"
            color="error"
            startIcon={<DeleteIcon />}
            onClick={handleDeleteAccount}
            disabled={loading}
          >
            批量删除 ({selectedIds.length})
          </Button>
        )}

        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleAddAccount}
        >
          添加结算账户
        </Button>
      </Box>

      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell padding="checkbox">
                <Checkbox
                  checked={selectAll}
                  indeterminate={selectedIds.length > 0 && selectedIds.length < filteredAccounts.length}
                  onChange={handleSelectAll}
                />
              </TableCell>
              <TableCell sortDirection={sortField === 'accountName' ? sortOrder : false}>
                <Button onClick={() => handleSort('accountName')}>
                  账户名称
                </Button>
              </TableCell>
              <TableCell sortDirection={sortField === 'accountType' ? sortOrder : false}>
                <Button onClick={() => handleSort('accountType')}>
                  账户类型
                </Button>
              </TableCell>
              <TableCell sortDirection={sortField === 'bankName' ? sortOrder : false}>
                <Button onClick={() => handleSort('bankName')}>
                  开户银行
                </Button>
              </TableCell>
              <TableCell sortDirection={sortField === 'accountNumber' ? sortOrder : false}>
                <Button onClick={() => handleSort('accountNumber')}>
                  账号
                </Button>
              </TableCell>
              <TableCell sortDirection={sortField === 'balance' ? sortOrder : false}>
                <Button onClick={() => handleSort('balance')}>
                  余额
                </Button>
              </TableCell>
              <TableCell sortDirection={sortField === 'status' ? sortOrder : false}>
                <Button onClick={() => handleSort('status')}>
                  状态
                </Button>
              </TableCell>
              <TableCell>创建时间</TableCell>
              <TableCell align="right">操作</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {paginatedAccounts.map((account) => (
              <TableRow
                key={account.id}
                hover
                selected={selectedIds.includes(account.id)}
              >
                <TableCell padding="checkbox">
                  <Checkbox
                    checked={selectedIds.includes(account.id)}
                    onChange={() => handleSelectOne(account.id)}
                  />
                </TableCell>
                <TableCell>{account.accountId}</TableCell>
                <TableCell>{account.accountName}</TableCell>
                <TableCell>{account.accountType}</TableCell>
                <TableCell>{account.bankName}</TableCell>
                <TableCell>{account.accountNumber}</TableCell>
                <TableCell sx={{ fontWeight: 'bold', color: '#d32f2f' }}>
                  ¥{account.balance.toFixed(2)}
                </TableCell>
                <TableCell>
                  <Chip
                    label={account.status === 'ACTIVE' ? '启用' : '禁用'}
                    sx={{ 
                      backgroundColor: getStatusColor(account.status),
                      color: '#fff',
                      fontWeight: 'bold'
                    }}
                    size="small"
                  />
                </TableCell>
                <TableCell>{new Date(account.createdAt).toLocaleDateString()}</TableCell>
                <TableCell align="right">
                  <Tooltip title="查看详情">
                    <IconButton onClick={() => handleViewDetail(account.id)} size="small">
                      <VisibilityIcon />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="编辑">
                    <IconButton onClick={() => handleEditAccount(account.id)} size="small">
                      <EditIcon />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="切换状态">
                    <IconButton onClick={() => handleToggleStatus(account)} size="small">
                      <Switch
                        checked={account.status === 'ACTIVE'}
                        size="small"
                        onChange={() => handleToggleStatus(account)}
                      />
                    </IconButton>
                  </Tooltip>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Box sx={{ mt: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography variant="body2" color="text.secondary">
          显示 {filteredAccounts.length} 条记录，共 {filteredAccounts.length} 条
        </Typography>
        <Pagination
          count={filteredAccounts.length}
          page={page}
          rowsPerPage={rowsPerPage}
          onPageChange={(_, newPage) => setPage(newPage)}
          onRowsPerPageChange={(e) => setRowsPerPage(Number((e.target as HTMLInputElement).value))}
        />
      </Box>
    </Paper>
    </Box>
  );
};

export default SettlementAccountManagement;