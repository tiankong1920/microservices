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
import { supplierApi } from '../services/api';
import { useNavigate, useParams } from 'react-router-dom';
import type { Supplier } from '../types';

const SupplierDetail: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [supplier, setSupplier] = useState<Supplier | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const loadSupplierDetail = async (): Promise<void> => {
    if (!id) {
      setError('供应商ID不能为空');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const response = await supplierApi.getSupplierById(Number(id));
      setSupplier(response.data);
    } catch (err) {
      console.error('Failed to load supplier detail:', err);
      setError('加载供应商详情失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSupplierDetail();
  }, [id]);

  const handleEditSupplier = (): void => {
    navigate(`/suppliers/edit/${id}`);
  };

  const handleBack = (): void => {
    navigate('/suppliers');
  };

  const handlePrint = (): void => {
    if (!supplier) return;
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

  if (!supplier) {
    return (
      <Typography variant="body1" sx={{ textAlign: 'center', py: 4 }}>
        供应商不存在
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
            供应商详情
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
            onClick={handleEditSupplier}
          >
            编辑供应商
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
                <ListItemText primary="供应商ID" secondary={supplier.supplierId} />
              </ListItem>
              <ListItem>
                <ListItemText primary="供应商名称" secondary={supplier.supplierName} />
              </ListItem>
              <ListItem>
                <ListItemText primary="联系人" secondary={supplier.contactPerson} />
              </ListItem>
              <ListItem>
                <ListItemText 
                  primary={
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                      <PhoneIcon fontSize="small" />
                      <span>联系电话</span>
                    </Box>
                  } 
                  secondary={supplier.phone} 
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
                  secondary={supplier.email} 
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
                  secondary={supplier.address || '无'} 
                />
              </ListItem>
              <ListItem>
                <ListItemText primary="状态" secondary={
                  <Chip 
                    label={supplier.status === 'ACTIVE' ? '启用' : '禁用'} 
                    color={supplier.status === 'ACTIVE' ? 'success' : 'default'} 
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
                  secondary={new Date(supplier.createdAt).toLocaleString()} 
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
                  secondary={new Date(supplier.updatedAt).toLocaleString()} 
                />
              </ListItem>
              <ListItem>
                <ListItemText primary="备注" secondary={supplier.remark || '无'} />
              </ListItem>
            </List>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <Box sx={{ textAlign: 'center', p: 2 }}>
              <Typography variant="h6" color="textSecondary">
                总采购次数
              </Typography>
              <Typography variant="h4" color="primary" sx={{ mt: 1 }}>
                {supplier.totalProcurements || 0}
              </Typography>
            </Box>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <Box sx={{ textAlign: 'center', p: 2 }}>
              <Typography variant="h6" color="textSecondary">
                总采购金额
              </Typography>
              <Typography variant="h4" color="primary" sx={{ mt: 1 }}>
                ¥{(supplier.totalProcurementAmount || 0).toFixed(2)}
              </Typography>
            </Box>
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <Box sx={{ textAlign: 'center', p: 2 }}>
              <Typography variant="h6" color="textSecondary">
                最近采购
              </Typography>
              <Typography variant="h4" color="primary" sx={{ mt: 1 }}>
                {supplier.lastProcurementDate ? new Date(supplier.lastProcurementDate).toLocaleDateString() : '无'}
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
                总采购次数
              </Typography>
              <Typography variant="h4" color="primary" sx={{ mt: 1 }}>
                {supplier.totalProcurements || 0}
              </Typography>
            </Box>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Box sx={{ textAlign: 'center', p: 2 }}>
              <Typography variant="h6" color="textSecondary">
                总采购金额
              </Typography>
              <Typography variant="h4" color="primary" sx={{ mt: 1 }}>
                ¥{(supplier.totalProcurementAmount || 0).toFixed(2)}
              </Typography>
            </Box>
          </Grid>
          <Grid size={{ xs: 12, md: 4 }}>
            <Box sx={{ textAlign: 'center', p: 2 }}>
              <Typography variant="h6" color="textSecondary">
                最近采购
              </Typography>
              <Typography variant="h4" color="primary" sx={{ mt: 1 }}>
                {supplier.lastProcurementDate ? new Date(supplier.lastProcurementDate).toLocaleDateString() : '无'}
              </Typography>
            </Box>
          </Grid>
        </Grid>
      </Paper>
    </Box>
  );
};

export default SupplierDetail;