import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  Grid,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  InputAdornment,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Divider,
  Tooltip,
  Autocomplete
} from '@mui/material';
import {
  Add as AddIcon,
  Delete as DeleteIcon,
  Save as SaveIcon,
  Cancel as CancelIcon,
  Search as SearchIcon,
  ShoppingCart as ShoppingCartIcon,
  Calculate as CalculateIcon
} from '@mui/icons-material';
import { orderApi, productApi, customerApi } from '../services/api';
import { useNavigate } from 'react-router-dom';
import type { Customer, Product, ApiResponse } from '../types';

interface OrderItemData {
  productId: number;
  productName: string;
  unit: string;
  price: number;
  quantity: number;
  discount: number;
  totalAmount: number;
}

interface FormErrors {
  [key: string]: string | undefined;
  customer?: string;
  items?: string;
  quantity?: string;
  product?: string;
  submit?: string;
}

const OrderCreate: React.FC = () => {
  const navigate = useNavigate();
  
  const [customers, setCustomers] = useState<Customer[]>([]);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [orderItems, setOrderItems] = useState<OrderItemData[]>([]);
  const [openProductDialog, setOpenProductDialog] = useState<boolean>(false);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [selectedCustomer, setSelectedCustomer] = useState<Customer | null>(null);
  const [quantity, setQuantity] = useState<number>(1);
  const [discount, setDiscount] = useState<number>(0);
  const [remark, setRemark] = useState<string>('');
  const [errors, setErrors] = useState<FormErrors>({});

  // 计算订单总额
  const calculateTotalAmount = (): number => {
    return orderItems.reduce((total, item) => {
      const itemTotal = (item.price * item.quantity) * (1 - item.discount / 100);
      return total + itemTotal;
    }, 0);
  };

  // 获取客户列表
  const fetchCustomers = async (): Promise<void> => {
    try {
      const response: ApiResponse<Customer[]> = await customerApi.getAllCustomers();
      setCustomers(response.data || []);
    } catch (error) {
      console.error('获取客户列表失败:', error);
    }
  };

  // 获取产品列表
  const fetchProducts = async (): Promise<void> => {
    try {
      const response: ApiResponse<Product[]> = await productApi.getAllProducts();
      setProducts(response.data || []);
    } catch (error) {
      console.error('获取产品列表失败:', error);
    }
  };

  useEffect(() => {
    fetchCustomers();
    fetchProducts();
  }, []);

  // 打开产品选择对话框
  const handleOpenProductDialog = (): void => {
    setOpenProductDialog(true);
  };

  // 关闭产品选择对话框
  const handleCloseProductDialog = (): void => {
    setOpenProductDialog(false);
    setSelectedProduct(null);
    setQuantity(1);
    setDiscount(0);
  };

  // 处理产品选择
  const handleProductSelect = (event: React.SyntheticEvent, product: Product | null): void => {
    setSelectedProduct(product);
    if (product) {
      setQuantity(1);
      setDiscount(0);
    }
  };

  // 添加产品到订单
  const handleAddProduct = (): void => {
    if (!selectedProduct) {
      setErrors({ product: '请选择产品' });
      return;
    }
    
    if (quantity <= 0) {
      setErrors({ quantity: '数量必须大于0' });
      return;
    }
    
    // 检查产品是否已在订单中
    const existingItemIndex = orderItems.findIndex(item => item.productId === selectedProduct.productId);
    
    if (existingItemIndex >= 0) {
      // 更新现有产品数量
      const updatedItems = [...orderItems];
      updatedItems[existingItemIndex].quantity += quantity;
      updatedItems[existingItemIndex].discount = discount;
      setOrderItems(updatedItems);
    } else {
      // 添加新产品
      const newItem: OrderItemData = {
        productId: selectedProduct.productId,
        productName: selectedProduct.productName,
        unit: selectedProduct.unit,
        price: selectedProduct.price || 0,
        quantity: quantity,
        discount: discount,
        totalAmount: ((selectedProduct.price || 0) * quantity) * (1 - discount / 100)
      };
      setOrderItems([...orderItems, newItem]);
    }
    
    // 关闭对话框并重置表单
    handleCloseProductDialog();
    setErrors({});
  };

  // 删除订单中的产品
  const handleDeleteItem = (index: number): void => {
    const updatedItems = [...orderItems];
    updatedItems.splice(index, 1);
    setOrderItems(updatedItems);
  };

  // 更新产品数量
  const handleUpdateQuantity = (index: number, newQuantity: number): void => {
    if (newQuantity <= 0) {
      return;
    }
    
    const updatedItems = [...orderItems];
    updatedItems[index].quantity = newQuantity;
    updatedItems[index].totalAmount = (updatedItems[index].price * newQuantity) * (1 - updatedItems[index].discount / 100);
    setOrderItems(updatedItems);
  };

  // 更新产品折扣
  const handleUpdateDiscount = (index: number, newDiscount: number): void => {
    if (newDiscount < 0 || newDiscount > 100) {
      return;
    }
    
    const updatedItems = [...orderItems];
    updatedItems[index].discount = newDiscount;
    updatedItems[index].totalAmount = (updatedItems[index].price * updatedItems[index].quantity) * (1 - newDiscount / 100);
    setOrderItems(updatedItems);
  };

  // 表单验证
  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};
    
    if (!selectedCustomer) {
      newErrors.customer = '请选择客户';
    }
    
    if (orderItems.length === 0) {
      newErrors.items = '订单至少需要包含一个产品';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // 处理创建订单
  const handleSubmit = async (): Promise<void> => {
    if (!validateForm()) {
      return;
    }
    
    setLoading(true);
    try {
      const orderData = {
        customerId: selectedCustomer?.id,
        remark: remark,
        orderItems: orderItems.map(item => ({
          productId: item.productId,
          quantity: item.quantity,
          unitPrice: item.price,
          discount: item.discount
        }))
      };
      
      await orderApi.createOrder(orderData);
      
      // 创建成功后跳转到订单管理页面
      navigate('/orders');
    } catch (error: unknown) {
      console.error('创建订单失败:', error);
      const err = error as { response?: { data?: { message?: string } } };
      if (err.response?.data?.message) {
        setErrors({ submit: err.response.data.message });
      } else {
        setErrors({ submit: '创建订单失败，请重试' });
      }
    } finally {
      setLoading(false);
    }
  };

  // 计算订单小计
  const calculateSubtotal = (): number => {
    return orderItems.reduce((total, item) => total + (item.price * item.quantity), 0);
  };

  // 计算订单折扣总额
  const calculateTotalDiscount = (): number => {
    const subtotal = calculateSubtotal();
    const totalAmount = calculateTotalAmount();
    return subtotal - totalAmount;
  };

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        创建订单
      </Typography>
      
      {/* 订单基本信息 */}
      <Paper sx={{ p: 3, mb: 3 }}>
        {errors.submit && (
          <Box sx={{ mb: 3, p: 2, backgroundColor: '#ffebee', borderRadius: 1, color: '#c62828' }}>
            {errors.submit}
          </Box>
        )}
        
        <Grid container spacing={3}>
          {/* 客户选择 */}
          <Grid size={{ xs: 12, sm: 6, md: 8 }}>
            <Typography variant="subtitle1" gutterBottom>
              选择客户
            </Typography>
            <Autocomplete
              fullWidth
              options={customers}
              getOptionLabel={(option: Customer) => `${option.customerName} (${option.contactPerson} - ${option.phone})`}
              value={selectedCustomer}
              onChange={(event: React.SyntheticEvent, newValue: Customer | null) => {
                setSelectedCustomer(newValue);
                if (errors.customer) {
                  setErrors(prev => {
                    const newErrors = { ...prev };
                    delete newErrors.customer;
                    return newErrors;
                  });
                }
              }}
              renderInput={(params) => (
                <TextField
                  {...params}
                  label="搜索客户..."
                  variant="outlined"
                  error={!!errors.customer}
                  helperText={errors.customer}
                  slotProps={{
                    input: {
                      ...params.InputProps,
                      startAdornment: (
                        <InputAdornment position="start">
                          <SearchIcon />
                        </InputAdornment>
                      )
                    }
                  }}
                />
              )}
            />
          </Grid>
          
          {/* 备注 */}
          <Grid size={{ xs: 12 }}>
            <TextField
              fullWidth
              label="备注"
              multiline
              rows={2}
              value={remark}
              onChange={(e: React.ChangeEvent<HTMLInputElement>) => setRemark(e.target.value)}
              variant="outlined"
            />
          </Grid>
        </Grid>
      </Paper>
      
      {/* 订单商品列表 */}
      <Paper sx={{ p: 3, mb: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="h6">
            订单商品
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleOpenProductDialog}
            sx={{ textTransform: 'none' }}
          >
            添加商品
          </Button>
        </Box>
        
        {errors.items && (
          <Box sx={{ mb: 2, p: 2, backgroundColor: '#ffebee', borderRadius: 1, color: '#c62828' }}>
            {errors.items}
          </Box>
        )}
        
        {orderItems.length === 0 ? (
          <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', py: 5, color: 'text.secondary' }}>
            <Box sx={{ textAlign: 'center' }}>
              <ShoppingCartIcon sx={{ fontSize: 64, mb: 2 }} />
              <Typography variant="h6">
                订单商品为空
              </Typography>
              <Typography variant="body2">
                点击"添加商品"按钮开始添加商品
              </Typography>
            </Box>
          </Box>
        ) : (
          <TableContainer sx={{ mb: 3 }}>
            <Table sx={{ minWidth: 650 }} aria-label="订单商品表格">
              <TableHead>
                <TableRow>
                  <TableCell>商品名称</TableCell>
                  <TableCell>单位</TableCell>
                  <TableCell align="right">单价</TableCell>
                  <TableCell align="right">数量</TableCell>
                  <TableCell align="right">折扣</TableCell>
                  <TableCell align="right">小计</TableCell>
                  <TableCell align="center">操作</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {orderItems.map((item, index) => (
                  <TableRow key={index}>
                    <TableCell component="th" scope="row">
                      {item.productName}
                    </TableCell>
                    <TableCell>{item.unit}</TableCell>
                    <TableCell align="right">
                      ¥{item.price.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
                    </TableCell>
                    <TableCell align="right">
                      <TextField
                        type="number"
                        size="small"
                        value={item.quantity}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleUpdateQuantity(index, parseInt(e.target.value) || 0)}
                        sx={{ width: 80 }}
                      />
                    </TableCell>
                    <TableCell align="right">
                      <TextField
                        type="number"
                        size="small"
                        value={item.discount}
                        onChange={(e: React.ChangeEvent<HTMLInputElement>) => handleUpdateDiscount(index, parseFloat(e.target.value) || 0)}
                        slotProps={{
                          input: {
                            endAdornment: <InputAdornment position="end">%</InputAdornment>
                          }
                        }}
                        sx={{ width: 100 }}
                      />
                    </TableCell>
                    <TableCell align="right" sx={{ fontWeight: 'bold' }}>
                      ¥{item.totalAmount.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
                    </TableCell>
                    <TableCell align="center">
                      <IconButton
                        size="small"
                        color="error"
                        onClick={() => handleDeleteItem(index)}
                        sx={{ ml: 1 }}
                      >
                        <DeleteIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}
        
        {/* 订单金额汇总 */}
        {orderItems.length > 0 && (
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', flexDirection: 'column', alignItems: 'flex-end' }}>
            <Grid container spacing={2} sx={{ justifyContent: 'flex-end', mb: 1 }}>
              <Grid size={{ xs: 12, sm: 6, md: 3 }} sx={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center' }}>
                <Typography variant="body1" sx={{ mr: 2 }}>
                  商品总计:
                </Typography>
                <Typography variant="body1" sx={{ fontWeight: 'bold' }}>
                  ¥{calculateSubtotal().toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
                </Typography>
              </Grid>
              <Grid size={{ xs: 12, sm: 6, md: 3 }} sx={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center' }}>
                <Typography variant="body1" sx={{ mr: 2 }}>
                  折扣:
                </Typography>
                <Typography variant="body1" sx={{ fontWeight: 'bold', color: '#4caf50' }}>
                  -¥{calculateTotalDiscount().toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
                </Typography>
              </Grid>
              <Grid size={{ xs: 12, sm: 6, md: 3 }} sx={{ display: 'flex', justifyContent: 'flex-end', alignItems: 'center' }}>
                <Typography variant="h6" sx={{ mr: 2, fontWeight: 'bold' }}>
                  订单总计:
                </Typography>
                <Typography variant="h6" sx={{ fontWeight: 'bold', color: 'primary.main' }}>
                  ¥{calculateTotalAmount().toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
                </Typography>
              </Grid>
            </Grid>
          </Box>
        )}
      </Paper>
      
      {/* 操作按钮 */}
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
        <Button
          variant="outlined"
          startIcon={<CancelIcon />}
          onClick={() => navigate('/orders')}
          sx={{ textTransform: 'none' }}
        >
          取消
        </Button>
        <Button
          variant="contained"
          startIcon={<SaveIcon />}
          onClick={handleSubmit}
          disabled={loading || orderItems.length === 0 || !selectedCustomer}
          sx={{ textTransform: 'none' }}
        >
          {loading ? '保存中...' : '保存订单'}
        </Button>
      </Box>
      
      {/* 产品选择对话框 */}
      <Dialog
        open={openProductDialog}
        onClose={handleCloseProductDialog}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <AddIcon />
            <span>添加商品</span>
          </Box>
        </DialogTitle>
        <DialogContent>
          <Grid container spacing={2}>
            {/* 产品搜索 */}
            <Grid size={{ xs: 12 }}>
              <Autocomplete
                fullWidth
                options={products}
                getOptionLabel={(option: Product) => `${option.productName} (${option.unit})`}
                value={selectedProduct}
                onChange={handleProductSelect}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="搜索商品..."
                    variant="outlined"
                    slotProps={{
                      input: {
                        ...params.InputProps,
                        startAdornment: (
                          <InputAdornment position="start">
                            <SearchIcon />
                          </InputAdornment>
                        )
                      }
                    }}
                  />
                )}
              />
            </Grid>
            
            {selectedProduct && (
              <>
                <Grid size={{ xs: 12 }}>
                  <Paper sx={{ p: 2 }}>
                    <Typography variant="h6" gutterBottom>
                      {selectedProduct.productName}
                    </Typography>
                    <Grid container spacing={2}>
                      <Grid size={{ xs: 12, sm: 6 }}>
                        <Typography variant="body2" color="text.secondary">
                          商品编号: {selectedProduct.productCode}
                        </Typography>
                      </Grid>
                      <Grid size={{ xs: 12, sm: 6 }}>
                        <Typography variant="body2" color="text.secondary">
                          分类: {selectedProduct.categoryName}
                        </Typography>
                      </Grid>
                      <Grid size={{ xs: 12, sm: 6 }}>
                        <Typography variant="body2" color="text.secondary">
                          单位: {selectedProduct.unit}
                        </Typography>
                      </Grid>
                      <Grid size={{ xs: 12, sm: 6 }}>
                        <Typography variant="body2" color="text.secondary">
                          当前库存: {selectedProduct.stockQuantity} {selectedProduct.unit}
                        </Typography>
                      </Grid>
                      <Grid size={{ xs: 12 }}>
                        <Divider sx={{ my: 1 }} />
                        <Typography variant="h6" color="primary">
                          单价: ¥{(selectedProduct.price || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
                        </Typography>
                      </Grid>
                    </Grid>
                  </Paper>
                </Grid>
                
                {/* 数量和折扣 */}
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    fullWidth
                    label="数量"
                    type="number"
                    value={quantity}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => setQuantity(parseInt(e.target.value) || 0)}
                    variant="outlined"
                    error={!!errors.quantity}
                    helperText={errors.quantity}
                    slotProps={{
                      input: {
                        startAdornment: (
                          <InputAdornment position="start">
                            <CalculateIcon />
                          </InputAdornment>
                        )
                      }
                    }}
                  />
                </Grid>
                
                <Grid size={{ xs: 12, sm: 6 }}>
                  <TextField
                    fullWidth
                    label="折扣"
                    type="number"
                    value={discount}
                    onChange={(e: React.ChangeEvent<HTMLInputElement>) => setDiscount(parseFloat(e.target.value) || 0)}
                    variant="outlined"
                    slotProps={{
                      input: {
                        endAdornment: <InputAdornment position="end">%</InputAdornment>
                      }
                    }}
                    helperText="0-100表示百分比折扣"
                  />
                </Grid>
                
                {/* 商品小计 */}
                <Grid size={{ xs: 12 }}>
                  <Paper sx={{ p: 2, display: 'flex', justifyContent: 'flex-end', alignItems: 'center' }}>
                    <Typography variant="h6" sx={{ mr: 2 }}>
                      小计: 
                    </Typography>
                    <Typography variant="h5" color="primary.main">
                      ¥{((selectedProduct.price || 0) * quantity * (1 - discount / 100)).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
                    </Typography>
                  </Paper>
                </Grid>
              </>
            )}
          </Grid>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button
            onClick={handleCloseProductDialog}
            startIcon={<CancelIcon />}
            sx={{ textTransform: 'none' }}
          >
            取消
          </Button>
          <Button
            onClick={handleAddProduct}
            variant="contained"
            startIcon={<AddIcon />}
            disabled={!selectedProduct}
            sx={{ textTransform: 'none' }}
          >
            添加到订单
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default OrderCreate;
