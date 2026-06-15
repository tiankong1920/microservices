import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
  Button, TextField, IconButton, Tooltip, CircularProgress, Switch, FormControlLabel, 
  Checkbox, Toolbar, Menu, MenuItem, Chip, Dialog, DialogTitle, DialogContent, DialogActions, DialogContentText 
} from '@mui/material';
import { 
  Search as SearchIcon, Visibility as VisibilityIcon, Edit as EditIcon, Delete as DeleteIcon, 
  Add as AddIcon, Lock as LockIcon, Download as DownloadIcon, FilterList as FilterIcon 
} from '@mui/icons-material';
import { userApi, roleApi } from '../services/api';
import ExportService from '../services/exportService';
import Pagination from '../components/Pagination';
import DateRangePicker from '../components/DateRangePicker';
import Toast from '../components/Toast';
import ConfirmDialog from '../components/ConfirmDialog';
import EmptyState from '../components/EmptyState';
import Skeleton from '../components/Skeleton';
import type { ApiResponse, User } from '../types';

interface Role {
  id: number;
  name: string;
  description?: string;
}

const UserManagement: React.FC = () => {
  const navigate = useNavigate();
  const [users, setUsers] = useState<User[]>([]);
  const [filteredUsers, setFilteredUsers] = useState<User[]>([]);
  const [_roles, setRoles] = useState<Role[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [_error, setError] = useState<string | null>(null);
  const [_success, _setSuccess] = useState<string>('');
  const [editDialogOpen, setEditDialogOpen] = useState<boolean>(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [newPassword, setNewPassword] = useState<string>('');
  const [page, setPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);
  const [selectAll, setSelectAll] = useState<boolean>(false);
  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [sortField, setSortField] = useState<string>('username');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [batchDeleteLoading, setBatchDeleteLoading] = useState<boolean>(false);
  const [toast, setToast] = useState<{ open: boolean; message: string; severity: 'success' | 'error' | 'info' | 'warning' }>({ open: false, message: '', severity: 'info' });

  const loadUsers = async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const response: ApiResponse<User[]> = await userApi.getAllUsers();
      setUsers(response.data);
    } catch (err) {
      console.error('Failed to load users:', err);
      setError('加载用户数据失败');
    } finally {
      setLoading(false);
    }
  };

  const loadRoles = async (): Promise<void> => {
    try {
      const response = await roleApi.getAllRoles();
      setRoles(response.data);
    } catch (err) {
      console.error('Failed to load roles:', err);
    }
  };

  useEffect(() => {
    loadUsers();
    loadRoles();
  }, []);

  useEffect(() => {
    applyFiltersAndSort();
  }, [users, searchTerm, startDate, endDate, statusFilter, sortField, sortOrder, page, rowsPerPage]);

  const applyFiltersAndSort = (): void => {
    let filtered = [...users];

    if (searchTerm) {
      filtered = filtered.filter(user => 
        user.username?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.fullName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.role?.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    if (startDate) {
      filtered = filtered.filter(user => {
        const createdAt = user.createdAt;
        return createdAt >= startDate;
      });
    }

    if (endDate) {
      filtered = filtered.filter(user => {
        const createdAt = user.createdAt;
        return createdAt <= endDate;
      });
    }

    if (statusFilter !== 'all') {
      filtered = filtered.filter(user => user.status === statusFilter);
    }

    filtered.sort((a, b) => {
      const aValue = (a as any)[sortField];
      const bValue = (b as any)[sortField];
      
      if (aValue === undefined || bValue === undefined) return 0;
      if (aValue < bValue) return sortOrder === 'asc' ? -1 :1;
      if (aValue > bValue) return sortOrder === 'asc' ?1 : -1;
      return 0;
    });

    setFilteredUsers(filtered);
  };

  const handleViewDetail = (userId: number): void => {
    navigate(`/system/users/${userId}`);
  };

  const handleEditUser = (user: User): void => {
    setSelectedUser(user);
    setEditDialogOpen(true);
  };

  const handleAddUser = (): void => {
    navigate('/system/users/edit/new');
  };

  const _handleDeleteUser = async (_userId: number): Promise<void> => {
    setDeleteDialogOpen(true);
  };

  const confirmDeleteUser = async (userId: number): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      await userApi.deleteUser(userId);
      setToast({ open: true, message: '用户删除成功', severity: 'success' });
      loadUsers();
      setDeleteDialogOpen(false);
    } catch (err) {
      console.error('Failed to delete user:', err);
      setToast({ open: true, message: '删除用户失败', severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleToggleStatus = async (user: User): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      await userApi.updateUser(user.id, { status: user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE' });
      setToast({ open: true, message: '用户状态更新成功', severity: 'success' });
      loadUsers();
    } catch (err) {
      console.error('Failed to update user status:', err);
      setToast({ open: true, message: '更新用户状态失败', severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (): Promise<void> => {
    if (!selectedUser || !newPassword) return;

    setLoading(true);
    setError(null);
    try {
      await userApi.updateUser(selectedUser.id, { password: newPassword });
      setToast({ open: true, message: '密码重置成功', severity: 'success' });
      setEditDialogOpen(false);
      setNewPassword('');
      setSelectedUser(null);
    } catch (err) {
      console.error('Failed to reset password:', err);
      setToast({ open: true, message: '密码重置失败', severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>): void => {
    const checked = event.target.checked;
    setSelectAll(checked);
    if (checked) {
      setSelectedIds(filteredUsers.map(u => u.id));
    } else {
      setSelectedIds([]);
    }
  };

  const handleSelectOne = (userId: number): void => {
    const selectedIndex = selectedIds.indexOf(userId);
    if (selectedIndex === -1) {
      setSelectedIds([...selectedIds, userId]);
    } else {
      setSelectedIds(selectedIds.filter(id => id !== userId));
    }
  };

  const handleBatchDelete = async (): Promise<void> => {
    if (selectedIds.length === 0) {
      setToast({ open: true, message: '请先选择要删除的用户', severity: 'warning' });
      return;
    }
    setBatchDeleteLoading(true);
    try {
      await Promise.all(selectedIds.map(id => userApi.deleteUser(id)));
      setToast({ open: true, message: `成功删除 ${selectedIds.length} 个用户`, severity: 'success' });
      setSelectedIds([]);
      setSelectAll(false);
      loadUsers();
    } catch (err) {
      console.error('Failed to batch delete users:', err);
      setToast({ open: true, message: '批量删除失败', severity: 'error' });
    } finally {
      setBatchDeleteLoading(false);
    }
  };

  const handleExport = (format: 'csv' | 'excel' | 'json' | 'pdf'): void => {
    if (filteredUsers.length === 0) {
      setToast({ open: true, message: '没有可导出的数据', severity: 'warning' });
      return;
    }
    const fields = [
      { key: 'id', label: '用户ID' },
      { key: 'username', label: '用户名' },
      { key: 'fullName', label: '姓名' },
      { key: 'email', label: '邮箱' },
      { key: 'role', label: '角色' },
      { key: 'status', label: '状态' },
      { key: 'lastLogin', label: '最后登录' },
      { key: 'createdAt', label: '创建时间' },
    ];
    
    if (format === 'csv') {
      ExportService.exportToCSV(filteredUsers, { filename: 'users', fields });
    } else if (format === 'excel') {
      ExportService.exportToExcel(filteredUsers, { filename: 'users', fields });
    } else if (format === 'pdf') {
      ExportService.exportToPDF(filteredUsers, { filename: 'users', fields });
    } else {
      ExportService.exportToJSON(filteredUsers, { filename: 'users', fields });
    }
    setToast({ open: true, message: '导出成功', severity: 'success' });
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

  const paginatedUsers = filteredUsers.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  const getStatusLabel = (status: string): string => {
    return status === 'ACTIVE' ? '启用' : '禁用';
  };

  return (
    <Box>
      <Toast
        open={toast.open}
        message={toast.message}
        severity={toast.severity}
        onClose={() => setToast({ ...toast, open: false })}
      />
      
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个用户吗？删除后无法恢复。"
        confirmText="删除"
        cancelText="取消"
        severity="warning"
        onConfirm={() => selectedIds.length > 0 ? handleBatchDelete() : confirmDeleteUser(selectedIds[0])}
        onCancel={() => setDeleteDialogOpen(false)}
        loading={loading || batchDeleteLoading}
      />

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          用户管理
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleAddUser}
          >
            新增用户
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
            placeholder="搜索用户名、姓名、邮箱或角色..."
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
              label={statusFilter === 'all' ? '全部状态' : getStatusLabel(statusFilter)} 
              onClick={() => setStatusFilter(statusFilter === 'all' ? 'ACTIVE' : 'all')}
              color={statusFilter === 'all' ? 'default' : statusFilter === 'ACTIVE' ? 'success' : 'error'}
              size="small"
            />
          </Box>
        </Toolbar>
      </Paper>

      {loading ? (
        <Skeleton type="table" rows={rowsPerPage} columns={8} />
      ) : filteredUsers.length === 0 ? (
        <EmptyState type="no-results" />
      ) : (
        <Box>
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 2 }}>
            <Checkbox
              checked={selectAll}
              onChange={handleSelectAll}
              indeterminate={selectedIds.length > 0 && selectedIds.length < paginatedUsers.length}
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
              <Table sx={{ minWidth: 1200 }} aria-label="user table">
                <TableHead>
                  <TableRow>
                    <TableCell padding="checkbox">
                      <Checkbox checked={selectAll} onChange={handleSelectAll} />
                    </TableCell>
                    <TableCell>
                      <Button
                        size="small"
                        onClick={() => handleSort('username')}
                        endIcon={sortField === 'username' ? (sortOrder === 'asc' ? '↑' : '↓') : null}
                      >
                        用户名
                      </Button>
                    </TableCell>
                    <TableCell>姓名</TableCell>
                    <TableCell>邮箱</TableCell>
                    <TableCell>角色</TableCell>
                    <TableCell>状态</TableCell>
                    <TableCell>最后登录</TableCell>
                    <TableCell>创建时间</TableCell>
                    <TableCell>操作</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {paginatedUsers.map((user) => (
                    <TableRow
                      key={user.id}
                      sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                      hover
                    >
                      <TableCell padding="checkbox">
                        <Checkbox
                          checked={selectedIds.includes(user.id)}
                          onChange={() => handleSelectOne(user.id)}
                          color="primary"
                        />
                      </TableCell>
                      <TableCell>{user.username}</TableCell>
                      <TableCell>{user.fullName}</TableCell>
                      <TableCell>{user.email}</TableCell>
                      <TableCell>{user.role}</TableCell>
                      <TableCell>
                        <FormControlLabel
                          control={
                            <Switch
                              checked={user.status === 'ACTIVE'}
                              onChange={() => handleToggleStatus(user)}
                              color="success"
                            />
                          }
                          label={getStatusLabel(user.status)}
                          labelPlacement="start"
                        />
                      </TableCell>
                      <TableCell>{user.lastLogin || '-'}</TableCell>
                      <TableCell>{user.createdAt}</TableCell>
                      <TableCell>
                        <Tooltip title="查看详情">
                          <IconButton size="small" onClick={() => handleViewDetail(user.id)}>
                            <VisibilityIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="编辑">
                          <IconButton size="small" onClick={() => handleEditUser(user)}>
                            <EditIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="重置密码">
                          <IconButton 
                            size="small" 
                            onClick={() => {
                              setSelectedUser(user);
                              setEditDialogOpen(true);
                            }}
                          >
                            <LockIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="删除">
                          <IconButton size="small" onClick={() => _handleDeleteUser(user.id)} color="error">
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
            count={filteredUsers.length}
            page={page}
            rowsPerPage={rowsPerPage}
            onPageChange={(_, newPage) => setPage(newPage)}
            onRowsPerPageChange={(e) => setRowsPerPage(Number((e.target as HTMLInputElement).value))}
            rowsPerPageOptions={[10, 25, 50, 100]}
          />
        </Box>
      )}

      <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>重置密码</DialogTitle>
        <DialogContent>
          <DialogContentText>
            用户: {selectedUser?.username}
          </DialogContentText>
          <DialogContentText>
            姓名: {selectedUser?.fullName}
          </DialogContentText>
          <TextField
            autoFocus
            margin="dense"
            label="新密码"
            type="password"
            fullWidth
            variant="outlined"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditDialogOpen(false)}>取消</Button>
          <Button onClick={handleResetPassword} variant="contained">确认重置</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default UserManagement;