import React, { useState, useEffect } from 'react';
import { 
  Typography, Box, Paper, Button, IconButton, 
  Backdrop, CircularProgress, TextField,
  FormControl, InputLabel, Select, MenuItem, SelectChangeEvent
} from '@mui/material';
import { ArrowBack as ArrowBackIcon, Save as SaveIcon } from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { useToast } from '../contexts/ToastContext';

interface ProductFormData {
  productCode: string;
  productName: string;
  categoryId: number;
  description: string;
  unit: string;
}

interface FormErrors {
  [key: string]: string | undefined;
  productCode?: string;
  productName?: string;
  categoryId?: string;
  unit?: string;
  description?: string;
}

const ProductEdit: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [product, setProduct] = useState<ProductFormData>({
    productCode: '',
    productName: '',
    categoryId: 1,
    description: '',
    unit: '件'
  });
  const [loading, setLoading] = useState<boolean>(true);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const { showSuccess, showError } = useToast();
  const [errors, setErrors] = useState<FormErrors>({});
  const [touched, setTouched] = useState<Record<string, boolean>>({});

  const categories = [
    { value: 1, label: '分类1' },
    { value: 2, label: '分类2' },
    { value: 3, label: '分类3' },
    { value: 4, label: '分类4' }
  ];

  const units = ['件', '个', '台', '千克', '米', '升'];

  useEffect(() => {
    if (id && id !== 'new') {
      loadProductData();
    } else {
      setLoading(false);
    }
  }, [id]);

  const loadProductData = async (): Promise<void> => {
    if (!id) return;
    
    setLoading(true);
    try {
      setProduct({
        productCode: `PROD${id.padStart(5, '0')}`,
        productName: `产品${id}`,
        categoryId: 1,
        description: `这是产品${id}的详细描述。`,
        unit: '件'
      });
    } catch (err) {
      console.error('Failed to load product data:', err);
      showError('加载产品数据失败');
    } finally {
      setLoading(false);
    }
  };

  const validateForm = (values: ProductFormData): FormErrors => {
    const newErrors: FormErrors = {};
    
    if (!values.productCode.trim()) {
      newErrors.productCode = '产品编码不能为空';
    } else if (values.productCode.length < 5) {
      newErrors.productCode = '产品编码长度不能少于5个字符';
    } else if (values.productCode.length > 20) {
      newErrors.productCode = '产品编码长度不能超过20个字符';
    } else if (!/^[A-Za-z0-9_-]+$/.test(values.productCode)) {
      newErrors.productCode = '产品编码只能包含字母、数字、下划线和连字符';
    }
    
    if (!values.productName.trim()) {
      newErrors.productName = '产品名称不能为空';
    } else if (values.productName.length < 2) {
      newErrors.productName = '产品名称长度不能少于2个字符';
    } else if (values.productName.length > 50) {
      newErrors.productName = '产品名称长度不能超过50个字符';
    }
    
    if (!values.unit.trim()) {
      newErrors.unit = '单位不能为空';
    }
    
    if (!values.categoryId || values.categoryId <= 0) {
      newErrors.categoryId = '请选择有效的分类';
    }
    
    if (values.description && values.description.length > 200) {
      newErrors.description = '描述长度不能超过200个字符';
    }
    
    return newErrors;
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const { name, value } = e.target;
    const updatedProduct: ProductFormData = {
      ...product,
      [name]: name === 'categoryId' ? parseInt(value) : value
    };
    setProduct(updatedProduct);
    
    const fieldErrors = validateForm(updatedProduct);
    setErrors(prev => ({
      ...prev,
      [name]: fieldErrors[name]
    }));
  };

  const handleCategoryChange = (e: SelectChangeEvent<number>): void => {
    const value = e.target.value as number;
    setProduct(prev => ({
      ...prev,
      categoryId: value
    }));
  };

  const _handleUnitChange = (e: SelectChangeEvent<string>): void => {
    const value = e.target.value as string;
    setProduct(prev => ({
      ...prev,
      unit: value
    }));
  };

  const handleBlur = (e: React.FocusEvent<HTMLInputElement>): void => {
    const { name } = e.target;
    setTouched(prev => ({
      ...prev,
      [name]: true
    }));
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement> | React.MouseEvent<HTMLButtonElement>): Promise<void> => {
    e.preventDefault();
    setSubmitting(true);
    
    const allTouched: Record<string, boolean> = {};
    Object.keys(product).forEach(key => {
      allTouched[key] = true;
    });
    setTouched(allTouched);
    
    try {
      const validationErrors = validateForm(product);
      if (Object.keys(validationErrors).length > 0) {
        setErrors(validationErrors);
        showError('表单验证失败，请检查输入信息');
        throw new Error('表单验证失败，请检查输入信息');
      }
      
      if (id && id !== 'new') {
        showSuccess('产品更新成功');
        setTimeout(() => {
          navigate(`/products/${id}`);
        }, 1500);
      } else {
        showSuccess('产品创建成功');
        setProduct({
          productCode: '',
          productName: '',
          categoryId: 1,
          description: '',
          unit: '件'
        });
        setErrors({});
        setTouched({});
      }
    } catch (err: unknown) {
      const errorMessage = err instanceof Error ? err.message : '操作失败';
      showError(errorMessage);
      console.error('Failed to save product:', err);
    } finally {
      setSubmitting(false);
    }
  };

  const handleBack = (): void => {
    if (id && id !== 'new') {
      navigate(`/products/${id}`);
    } else {
      navigate('/products');
    }
  };

  if (loading) {
    return (
      <Backdrop open={loading} sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}>
        <CircularProgress color="inherit" />
      </Backdrop>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
        <IconButton onClick={handleBack} sx={{ mr: 2 }}>
          <ArrowBackIcon />
        </IconButton>
        <Typography variant="h4" gutterBottom>
          {id && id !== 'new' ? '编辑产品' : '创建新产品'}
        </Typography>
        <Box sx={{ flexGrow: 1 }} />
        <Button 
          variant="contained" 
          startIcon={<SaveIcon />} 
          onClick={handleSubmit} 
          disabled={submitting}
        >
          {submitting ? '保存中...' : '保存'}
        </Button>
      </Box>
      
      <Paper elevation={3} sx={{ p: 3 }}>
        <form onSubmit={handleSubmit}>
          <Box sx={{ display: 'grid', gap: 3, maxWidth: 800 }}>
            <TextField
              fullWidth
              label="产品编码"
              name="productCode"
              value={product.productCode}
              onChange={handleChange}
              onBlur={handleBlur}
              required
              disabled={!!(id && id !== 'new')}
              error={touched.productCode && Boolean(errors.productCode)}
              helperText={touched.productCode && errors.productCode}
              slotProps={{
                htmlInput: {
                  maxLength: 20,
                  pattern: '[A-Za-z0-9_-]+'
                }
              }}
            />
            
            <TextField
              fullWidth
              label="产品名称"
              name="productName"
              value={product.productName}
              onChange={handleChange}
              onBlur={handleBlur}
              required
              error={touched.productName && Boolean(errors.productName)}
              helperText={touched.productName && errors.productName}
              slotProps={{
                htmlInput: {
                  maxLength: 50
                }
              }}
            />
            
            <FormControl fullWidth required error={touched.categoryId && Boolean(errors.categoryId)}>
              <InputLabel>分类</InputLabel>
              <Select
                name="categoryId"
                value={product.categoryId}
                label="分类"
                onChange={handleCategoryChange}
              >
                {categories.map(category => (
                  <MenuItem key={category.value} value={category.value}>
                    {category.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            
            <TextField
              fullWidth
              label="单位"
              name="unit"
              value={product.unit}
              onChange={handleChange}
              onBlur={handleBlur}
              select
              required
              error={touched.unit && Boolean(errors.unit)}
              helperText={touched.unit && errors.unit}
            >
              {units.map(unit => (
                <MenuItem key={unit} value={unit}>
                  {unit}
                </MenuItem>
              ))}
            </TextField>
            
            <TextField
              fullWidth
              label="描述"
              name="description"
              value={product.description}
              onChange={handleChange}
              onBlur={handleBlur}
              multiline
              rows={4}
              error={touched.description && Boolean(errors.description)}
              helperText={touched.description && errors.description ? errors.description : `${product.description.length}/200`}
              slotProps={{
                htmlInput: {
                  maxLength: 200
                }
              }}
            />
            
            <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end', mt: 2 }}>
              <Button variant="outlined" onClick={handleBack}>
                取消
              </Button>
              <Button 
                variant="contained" 
                startIcon={<SaveIcon />} 
                type="submit" 
                disabled={submitting}
              >
                {submitting ? '保存中...' : '保存'}
              </Button>
            </Box>
          </Box>
        </form>
      </Paper>
    </Box>
  );
};

export default ProductEdit;