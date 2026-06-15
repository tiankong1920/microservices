import React, { useState, useEffect } from 'react';
import { 
  Typography, Box, Paper, Button, IconButton, 
  Backdrop, CircularProgress, Divider, Grid
} from '@mui/material';
import { ArrowBack as ArrowBackIcon, Edit as EditIcon, Print as PrintIcon, Delete as DeleteIcon } from '@mui/icons-material';
import { productApi } from '../services/api';
import { useParams, useNavigate } from 'react-router-dom';
import { useToast } from '../contexts/ToastContext';
import ConfirmDialog from '../components/ConfirmDialog';
import type { Product, ApiResponse } from '../types';

interface ProductDetailData extends Product {
  id: number;
}

const ProductDetail: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [product, setProduct] = useState<ProductDetailData | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>('');
  const [deleteDialogOpen, setDeleteDialogOpen] = useState<boolean>(false);
  const [deleteLoading, setDeleteLoading] = useState<boolean>(false);
  const { showSuccess, showError } = useToast();

  useEffect(() => {
    loadProductDetail();
  }, [id]);

  const loadProductDetail = async (): Promise<void> => {
    if (!id) {
      setError('产品ID不能为空');
      setLoading(false);
      return;
    }

    setLoading(true);
    setError('');
    try {
      const response: ApiResponse<Product> = await productApi.getProductById(Number(id));
      const data = response.data;
      setProduct({
        id: data.productId,
        ...data
      });
    } catch (err) {
      console.error('Failed to load product detail:', err);
      setError('加载产品详情失败');
    } finally {
      setLoading(false);
    }
  };

  const handleEdit = (): void => {
    navigate(`/products/edit/${id}`);
  };

  const handleDelete = (): void => {
    setDeleteDialogOpen(true);
  };

  const confirmDelete = async (): Promise<void> => {
    if (!product) return;

    setDeleteLoading(true);
    setError('');
    try {
      await productApi.deleteProduct(product.productId);
      showSuccess('产品删除成功');
      setDeleteDialogOpen(false);
      navigate('/products');
    } catch (err) {
      console.error('Failed to delete product:', err);
      showError('删除产品失败');
    } finally {
      setDeleteLoading(false);
    }
  };

  const handlePrint = (): void => {
    if (!product) return;
    window.print();
  };

  const handleBack = (): void => {
    navigate('/products');
  };

  if (loading) {
    return (
      <Backdrop open={loading} sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}>
        <CircularProgress color="inherit" />
      </Backdrop>
    );
  }

  if (error && !product) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h6" color="error" gutterBottom>
          {error}
        </Typography>
      </Box>
    );
  }

  if (!product) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h6" color="info" gutterBottom>
          产品不存在
        </Typography>
      </Box>
    );
  }

  return (
    <Box>
      <ConfirmDialog
        open={deleteDialogOpen}
        title="确认删除"
        content="确定要删除这个产品吗？删除后无法恢复。"
        confirmText="删除"
        cancelText="取消"
        severity="warning"
        onConfirm={confirmDelete}
        onCancel={() => setDeleteDialogOpen(false)}
        loading={deleteLoading}
      />

      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <IconButton onClick={handleBack} sx={{ mr: 2 }}>
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4" gutterBottom>
          产品详情
        </Typography>
        <Box sx={{ flexGrow: 1 }} />
        <Button variant="outlined" startIcon={<EditIcon />} onClick={handleEdit} sx={{ mr: 2 }}>
          编辑
        </Button>
        <Button variant="contained" startIcon={<PrintIcon />} onClick={handlePrint} sx={{ mr: 2 }}>
          打印
        </Button>
        <Button variant="outlined" color="error" startIcon={<DeleteIcon />} onClick={handleDelete}>
          删除
        </Button>
      </Box>

      <Paper elevation={3} sx={{ p: 3 }}>
        <Grid container spacing={3}>
          <Grid size={{ xs: 12, md: 6 }}>
            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle1" color="textSecondary">产品ID</Typography>
              <Typography variant="h6">{product.productId}</Typography>
            </Box>
            
            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle1" color="textSecondary">产品编码</Typography>
              <Typography variant="h6">{product.productCode}</Typography>
            </Box>
            
            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle1" color="textSecondary">产品名称</Typography>
              <Typography variant="h5">{product.productName}</Typography>
            </Box>
            
            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle1" color="textSecondary">分类ID</Typography>
              <Typography variant="h6">{product.categoryId}</Typography>
            </Box>
            
            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle1" color="textSecondary">单位</Typography>
              <Typography variant="h6">{product.unit}</Typography>
            </Box>
          </Grid>
          
          <Grid size={{ xs: 12, md: 6 }}>
            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle1" color="textSecondary">创建时间</Typography>
              <Typography variant="h6">{product.createdAt ? new Date(product.createdAt).toLocaleString() : '-'}</Typography>
            </Box>
            
            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle1" color="textSecondary">更新时间</Typography>
              <Typography variant="h6">{product.updatedAt ? new Date(product.updatedAt).toLocaleString() : '-'}</Typography>
            </Box>
            
            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle1" color="textSecondary">删除状态</Typography>
              <Typography variant="h6">{product.deleted ? '已删除' : '正常'}</Typography>
            </Box>
          </Grid>
        </Grid>
        
        <Divider sx={{ my: 3 }} />
        
        <Box>
          <Typography variant="h6" gutterBottom>
            产品描述
          </Typography>
          <Typography variant="body1">
            {product.description}
          </Typography>
        </Box>
      </Paper>
    </Box>
  );
};

export default ProductDetail;