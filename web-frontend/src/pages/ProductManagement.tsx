import React, { useState, useEffect, ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { 
  Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, 
  Button, TextField, IconButton, Tooltip, CircularProgress, Checkbox, 
  FormControlLabel, Switch, Toolbar, Menu, MenuItem, Chip 
} from '@mui/material';
import { 
  Add as AddIcon, Edit as EditIcon, Delete as DeleteIcon, 
  Search as SearchIcon, DeleteForever as DeleteForeverIcon, 
  Visibility as VisibilityIcon, Download as DownloadIcon, FilterList as FilterIcon 
} from '@mui/icons-material';
import { productApi } from '../services/api';
import ExportService from '../services/exportService';
import Pagination from '../components/Pagination';
import DateRangePicker from '../components/DateRangePicker';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import EmptyState from '../components/EmptyState';
import Skeleton from '../components/Skeleton';
import type { Product } from '../types';

const ProductManagement: React.FC = () => {
  const navigate = useNavigate();
  const [products, setProducts] = useState<Product[]>([]);
  const [filteredProducts, setFilteredProducts] = useState<Product[]>([]);
  const [selectedProducts, setSelectedProducts] = useState<number[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [_error, setError] = useState<string | null>(null);
  const [isAllSelected, setIsAllSelected] = useState<boolean>(false);
  const [page, setPage] = useState<number>(0);
  const [rowsPerPage, setRowsPerPage] = useState<number>(10);
  const [startDate, setStartDate] = useState<string>('');
  const [endDate, setEndDate] = useState<string>('');
  const [statusFilter, setStatusFilter] = useState<string>('all');
  const [sortField, setSortField] = useState<string>('productName');
  const [sortOrder, setSortOrder] = useState<'asc' | 'desc'>('asc');
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [batchDeleteLoading, setBatchDeleteLoading] = useState<boolean>(false);
  const { showSuccess, showError, showWarning } = useToast();

  const loadProducts = async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const response = await productApi.getAllProducts();
      setProducts(response.data || []);
    } catch (err) {
      console.error('Failed to load products:', err);
      setError('加载产品数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProducts();
  }, []);

  useEffect(() => {
    applyFiltersAndSort();
  }, [products, searchTerm, startDate, endDate, statusFilter, sortField, sortOrder, page, rowsPerPage]);

  const applyFiltersAndSort = (): void => {
    let filtered = [...products];

    if (searchTerm) {
      filtered = filtered.filter(product => 
        product.productName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        product.productCode?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        product.categoryId?.toString().includes(searchTerm)
      );
    }

    if (startDate) {
      filtered = filtered.filter(product => {
        const createdAt = product.createdAt || '';
        return createdAt >= startDate;
      });
    }

    if (endDate) {
      filtered = filtered.filter(product => {
        const createdAt = product.createdAt || '';
        return createdAt <= endDate;
      });
    }

    if (statusFilter !== 'all') {
      filtered = filtered.filter(product => 
        statusFilter === 'active' ? !product.deleted : product.deleted
      );
    }

    filtered.sort((a, b) => {
      const aValue = (a as any)[sortField];
      const bValue = (b as any)[sortField];
      
      if (aValue === undefined || bValue === undefined) return 0;
      if (aValue < bValue) return sortOrder === 'asc' ? -1 :1;
      if (aValue > bValue) return sortOrder === 'asc' ?1 : -1;
      return 0;
    });

    setFilteredProducts(filtered);
  };

  const handleSearch = (e: ChangeEvent<HTMLInputElement>): void => {
    setSearchTerm(e.target.value);
  };

  const handleAddProduct = (): void => {
    navigate('/products/edit/new');
  };

  const handleEditProduct = (productId: number): void => {
    navigate(`/products/edit/${productId}`);
  };

  const handleDeleteProduct = async (_productId: number): Promise<void> => {
    setDeleteDialogOpen(true);
  };

  const confirmDeleteProduct = async (productId: number): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      await productApi.deleteProduct(productId);
      showSuccess('产品删除成功');
      loadProducts();
      setDeleteDialogOpen(false);
    } catch (err) {
      console.error('Failed to delete product:', err);
      showError('删除产品失败');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectProduct = (productId: number): void => {
    setSelectedProducts(prev => {
      if (prev.includes(productId)) {
        return prev.filter(id => id !== productId);
      } else {
        return [...prev, productId];
      }
    });
  };

  const handleSelectAllProducts = (): void => {
    setIsAllSelected(!isAllSelected);
    if (!isAllSelected) {
      setSelectedProducts(filteredProducts.map(product => product.productId));
    } else {
      setSelectedProducts([]);
    }
  };

  const handleBatchDelete = async (): Promise<void> => {
    if (selectedProducts.length === 0) {
      showWarning('请先选择要删除的产品');
      return;
    }
    setBatchDeleteLoading(true);
    try {
      await Promise.all(selectedProducts.map(id => productApi.deleteProduct(id)));
      showSuccess(`成功删除 ${selectedProducts.length} 个产品`);
      setSelectedProducts([]);
      setIsAllSelected(false);
      loadProducts();
    } catch (err) {
      console.error('Failed to batch delete products:', err);
      showError('批量删除失败');
    } finally {
      setBatchDeleteLoading(false);
    }
  };

  const handleExport = (format: 'csv' | 'excel' | 'json' | 'pdf'): void => {
    if (filteredProducts.length === 0) {
      showWarning('没有可导出的数据');
      return;
    }
    const fields = [
      { key: 'productId', label: '产品ID' },
      { key: 'productCode', label: '产品编码' },
      { key: 'productName', label: '产品名称' },
      { key: 'categoryId', label: '分类ID' },
      { key: 'unit', label: '单位' },
      { key: 'price', label: '价格', format: (v) => `¥${v}` },
      { key: 'stock', label: '库存' },
      { key: 'deleted', label: '状态' },
      { key: 'createdAt', label: '创建时间' },
    ];
    
    if (format === 'csv') {
      ExportService.exportToCSV(filteredProducts, { filename: 'products', fields });
    } else if (format === 'excel') {
      ExportService.exportToExcel(filteredProducts, { filename: 'products', fields });
    } else if (format === 'pdf') {
      ExportService.exportToPDF(filteredProducts, { filename: 'products', fields });
    } else {
      ExportService.exportToJSON(filteredProducts, { filename: 'products', fields });
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

  const paginatedProducts = filteredProducts.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

  const getStatusLabel = (product: Product): string => {
    return product.deleted ? '已禁用' : '启用';
  };

  return (
    <Box>
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个产品吗？删除后无法恢复。"
        confirmText="删除"
        cancelText="取消"
        severity="warning"
        onConfirm={() => selectedProducts.length > 0 ? handleBatchDelete() : confirmDeleteProduct(selectedProducts[0])}
        onCancel={() => setDeleteDialogOpen(false)}
        loading={loading || batchDeleteLoading}
      />

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          产品管理
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleAddProduct}
          >
            新增产品
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
            placeholder="搜索产品名称、编码或分类..."
            variant="outlined"
            size="small"
            value={searchTerm}
            onChange={handleSearch}
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
              label={statusFilter === 'all' ? '全部状态' : statusFilter === 'active' ? '启用' : '已禁用'} 
              onClick={() => setStatusFilter(statusFilter === 'all' ? 'active' : 'all')}
              color={statusFilter === 'all' ? 'default' : statusFilter === 'active' ? 'success' : 'error'}
              size="small"
            />
          </Box>
        </Toolbar>
      </Paper>

      {loading ? (
        <Skeleton type="table" rows={rowsPerPage} columns={7} />
      ) : filteredProducts.length === 0 ? (
        <EmptyState type="no-results" />
      ) : (
        <Box>
          <Box sx={{ display: 'flex', gap: 2, alignItems: 'center', mb: 2 }}>
            <Checkbox
              checked={isAllSelected}
              onChange={handleSelectAllProducts}
              indeterminate={selectedProducts.length > 0 && selectedProducts.length < paginatedProducts.length}
            />
            <Typography variant="body2">
              已选择 {selectedProducts.length} 项
            </Typography>
            {selectedProducts.length > 0 && (
              <Button
                variant="contained"
                color="error"
                startIcon={<DeleteForeverIcon />}
                onClick={() => setDeleteDialogOpen(true)}
                disabled={batchDeleteLoading}
              >
                批量删除
              </Button>
            )}
          </Box>

          <Box sx={{ overflowX: 'auto' }}>
            <TableContainer component={Paper} sx={{ minWidth: '100%' }}>
              <Table sx={{ minWidth: 1000 }} aria-label="product table">
                <TableHead>
                  <TableRow>
                    <TableCell padding="checkbox">
                      <Checkbox checked={isAllSelected} onChange={handleSelectAllProducts} />
                    </TableCell>
                    <TableCell>
                      <Button
                        size="small"
                        onClick={() => handleSort('productName')}
                        endIcon={sortField === 'productName' ? (sortOrder === 'asc' ? '↑' : '↓') : null}
                      >
                        产品名称
                      </Button>
                    </TableCell>
                    <TableCell>产品编码</TableCell>
                    <TableCell>分类ID</TableCell>
                    <TableCell>单位</TableCell>
                    <TableCell>价格</TableCell>
                    <TableCell>库存</TableCell>
                    <TableCell>状态</TableCell>
                    <TableCell>操作</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {paginatedProducts.map((product) => (
                    <TableRow
                      key={product.productId}
                      sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                      hover
                    >
                      <TableCell padding="checkbox">
                        <Checkbox
                          checked={selectedProducts.includes(product.productId)}
                          onChange={() => handleSelectProduct(product.productId)}
                          color="primary"
                        />
                      </TableCell>
                      <TableCell component="th" scope="row">
                        {product.productId}
                      </TableCell>
                      <TableCell>{product.productCode}</TableCell>
                      <TableCell 
                        sx={{ cursor: 'pointer', fontWeight: 'medium' }}
                        onClick={() => navigate(`/products/${product.productId}`)}
                      >
                        {product.productName}
                      </TableCell>
                      <TableCell>{product.categoryId}</TableCell>
                      <TableCell>{product.unit}</TableCell>
                      <TableCell sx={{ color: '#d32f2f', fontWeight: 'bold' }}>¥{product.price}</TableCell>
                      <TableCell>{product.stock}</TableCell>
                      <TableCell>
                        <FormControlLabel
                          control={
                            <Switch
                              checked={!product.deleted}
                              disabled
                              color="success"
                            />
                          }
                          label={getStatusLabel(product)}
                          labelPlacement="start"
                        />
                      </TableCell>
                      <TableCell>
                        <Tooltip title="查看详情">
                          <IconButton size="small" onClick={() => navigate(`/products/${product.productId}`)}>
                            <VisibilityIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="编辑">
                          <IconButton size="small" onClick={() => handleEditProduct(product.productId)}>
                            <EditIcon />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="删除">
                          <IconButton size="small" onClick={() => handleDeleteProduct(product.productId)} color="error">
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
            count={filteredProducts.length}
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

export default ProductManagement;