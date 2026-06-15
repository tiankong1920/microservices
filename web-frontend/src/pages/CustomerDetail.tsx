import React, { useState, useEffect } from 'react';
import {
  Typography, Box, Paper, Divider, Button, IconButton, Tooltip, CircularProgress,
  Grid, List, ListItem, ListItemText, Chip
} from '@mui/material';
import {
  Edit as EditIcon, ArrowBack as ArrowBackIcon,
  Phone as PhoneIcon, Mail as MailIcon, Map as MapIcon,
  CalendarToday as CalendarIcon, Print as PrintIcon
} from '@mui/icons-material';
import { customerApi } from '../services/api';
import { useNavigate, useParams } from 'react-router-dom';

import type { Customer } from '../types';

const CustomerDetail: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [customer, setCustomer] = useState<Customer | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const loadCustomerDetail = async (): Promise<void> => {
    if (!id) {
      setError('客户ID不能为空');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const response = await customerApi.getCustomerById(Number(id));
      setCustomer(response.data);
    } catch (err) {
      console.error('Failed to load customer detail:', err);
      setError('加载客户详情失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCustomerDetail();
  }, [id]);

  const handleEditCustomer = (): void => {
    navigate(`/customers/edit/${id}`);
  };

  const handleBack = (): void => {
    navigate('/customers');
  };

  const handlePrint = (): void => {
    if (!customer) return;
    window.print();
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 200 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <Typography variant="h6" color="error" gutterBottom>
          {error}
        </Typography>
      </Box>
    );
  }

  if (!customer) {
    return (
      <Typography variant="body1" sx={{ textAlign: 'center', py: 4 }}>
        客户不存在
      </Typography>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <IconButton onClick={handleBack} color="primary">
            <ArrowBackIcon />
          </IconButton>
          <Typography variant="h4" gutterBottom>
            客户详情
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="contained"
            startIcon={<PrintIcon />}
            onClick={handlePrint}
          >
            打印
          </Button>
          <Button
            variant="contained"
            startIcon={<EditIcon />}
            onClick={handleEditCustomer}
          >
            编辑客户
          </Button>
        </Box>
      </Box>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h5" gutterBottom>
          基本信息
        </Typography>
        <Divider sx={{ mb: 3 }} />

        <Grid container spacing={3}>
          <Grid size={{ xs: 12, md: 6 }}>
            <List>
              <ListItem>
                <ListItemText primary="客户ID" secondary={customer.customerId} />
              </ListItem>
              <ListItem>
                <ListItemText primary="客户名称" secondary={customer.customerName} />
              </ListItem>
              <ListItem>
                <ListItemText primary="联系人" secondary={customer.contactPerson} />
              </ListItem>
              <ListItem>
                <ListItemText 
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <PhoneIcon fontSize="small" />
                      <span>联系电话</span>
                    </Box>
                  } 
                  secondary={customer.phone} 
                />
              </ListItem>
              <ListItem>
                <ListItemText 
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <MailIcon fontSize="small" />
                      <span>电子邮箱</span>
                    </Box>
                  } 
                  secondary={customer.email} 
                />
              </ListItem>
              <ListItem>
                <ListItemText 
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <MapIcon fontSize="small" />
                      <span>地址</span>
                    </Box>
                  } 
                  secondary={customer.address || '无'} 
                />
              </ListItem>
              <ListItem>
                <ListItemText primary="状态" secondary={
                  <Chip 
                    label={customer.status === 'ACTIVE' ? '启用' : '禁用'} 
                    color={customer.status === 'ACTIVE' ? 'success' : 'default'} 
                    size="small" 
                  />
                } />
              </ListItem>
              <ListItem>
                <ListItemText 
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <CalendarIcon fontSize="small" />
                      <span>创建时间</span>
                    </Box>
                  } 
                  secondary={new Date(customer.createdAt).toLocaleString()} 
                />
              </ListItem>
              <ListItem>
                <ListItemText 
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <CalendarIcon fontSize="small" />
                      <span>更新时间</span>
                    </Box>
                  } 
                  secondary={new Date(customer.updatedAt).toLocaleString()} 
                />
              </ListItem>
              <ListItem>
                <ListItemText primary="备注" secondary={customer.remark || '无'} />
              </ListItem>
            </List>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <Box sx={{ textAlign: 'center', p: 2 }}>
              <Typography variant="h6" color="textSecondary">
                总订单数
              </Typography>
              <Typography variant="h4" color="primary" sx={{ mt: 1 }}>
                {customer.totalOrders || 0}
              </Typography>
            </Box>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <Box sx={{ textAlign: 'center', p: 2 }}>
              <Typography variant="h6" color="textSecondary">
                总销售额
              </Typography>
              <Typography variant="h4" color="primary" sx={{ mt: 1 }}>
                ¥{(customer.totalSalesAmount || 0).toFixed(2)}
              </Typography>
            </Box>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <Box sx={{ textAlign: 'center', p: 2 }}>
              <Typography variant="h6" color="textSecondary">
                最近订单
              </Typography>
              <Typography variant="h4" color="primary" sx={{ mt: 1 }}>
                {customer.lastOrderDate ? new Date(customer.lastOrderDate).toLocaleDateString() : '无'}
              </Typography>
            </Box>
          </Grid>
        </Grid>
      </Paper>

      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h5" gutterBottom>
          业务信息
        </Typography>
        <Divider sx={{ mb: 3 }} />

        <Grid container spacing={3}>
          <Grid size={{ xs: 12, md: 4 }}>
            <Box sx={{ textAlign: 'center', p: 2 }}>
              <Typography variant="h6" color="textSecondary">
                总订单数
              </Typography>
              <Typography variant="h4" color="primary" sx={{ mt: 1 }}>
                {customer.totalOrders || 0}
              </Typography>
            </Box>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Box sx={{ textAlign: 'center', p: 2 }}>
              <Typography variant="h6" color="textSecondary">
                总销售额
              </Typography>
              <Typography variant="h4" color="primary" sx={{ mt: 1 }}>
                ¥{(customer.totalSalesAmount || 0).toFixed(2)}
              </Typography>
            </Box>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Box sx={{ textAlign: 'center', p: 2 }}>
              <Typography variant="h6" color="textSecondary">
                最近订单
              </Typography>
              <Typography variant="h4" color="primary" sx={{ mt: 1 }}>
                {customer.lastOrderDate ? new Date(customer.lastOrderDate).toLocaleDateString() : '无'}
              </Typography>
            </Box>
          </Grid>
        </Grid>
      </Paper>
    </Box>
  );
};

export default CustomerDetail;