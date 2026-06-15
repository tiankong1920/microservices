import React, { useState, useEffect } from 'react';
import {
  Typography, Box, Paper, TextField, Button, IconButton, CircularProgress,
  Grid, FormControl, InputLabel, Select, MenuItem, FormHelperText, SelectChangeEvent
} from '@mui/material';
import {
  ArrowBack as ArrowBackIcon, Save as SaveIcon, Clear as ClearIcon
} from '@mui/icons-material';
import { customerApi } from '../services/api';
import { useNavigate, useParams } from 'react-router-dom';
import type { Customer, CustomerFormData, FormErrors } from '../types';
import { useToast } from '../contexts/ToastContext';

const CustomerEdit: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isEditMode = id !== 'new';

  const [formData, setFormData] = useState<CustomerFormData>({
    customerName: '',
    contactPerson: '',
    phone: '',
    email: '',
    address: '',
    status: 'ACTIVE',
    remark: ''
  });

  const [errors, setErrors] = useState<FormErrors>({});
  const [loading, setLoading] = useState<boolean>(false);
  const [submitting, setSubmitting] = useState<boolean>(false);
  const { showSuccess, showError } = useToast();

  const loadCustomerData = async (): Promise<void> => {
    if (isEditMode && id) {
      setLoading(true);
      try {
        const response = await customerApi.getCustomerById(Number(id));
        const data = response.data;
        setFormData({
          customerName: data.customerName,
          contactPerson: data.contactPerson,
          phone: data.phone,
          email: data.email,
          address: data.address || '',
          status: data.status,
          remark: data.remark || ''
        });
      } catch (err) {
        console.error('Failed to load customer data:', err);
        showError('加载客户数据失败');
      } finally {
        setLoading(false);
      }
    }
  };

  useEffect(() => {
    loadCustomerData();
  }, [id, isEditMode]);

  const validateForm = (): boolean => {
    const newErrors: FormErrors = {};

    if (!formData.customerName.trim()) {
      newErrors.customerName = '客户名称不能为空';
    }

    if (!formData.contactPerson.trim()) {
      newErrors.contactPerson = '联系人不能为空';
    }

    if (!formData.phone.trim()) {
      newErrors.phone = '联系电话不能为空';
    } else if (!/^1[3-9]\d{9}$/.test(formData.phone)) {
      newErrors.phone = '请输入有效的手机号码';
    }

    if (!formData.email.trim()) {
      newErrors.email = '电子邮箱不能为空';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = '请输入有效的电子邮箱';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));

    if (errors[name]) {
      setErrors(prev => {
        const newErrors = { ...prev };
        delete newErrors[name];
        return newErrors;
      });
    }
  };

  const handleStatusChange = (e: SelectChangeEvent<'ACTIVE' | 'INACTIVE'>): void => {
    setFormData(prev => ({
      ...prev,
      status: e.target.value as 'ACTIVE' | 'INACTIVE'
    }));
  };

  const handleSave = async (): Promise<void> => {
    if (!validateForm()) {
      return;
    }

    setSubmitting(true);
    setErrors({});

    try {
      if (isEditMode && id) {
        await customerApi.updateCustomer(Number(id), formData);
        showSuccess('客户信息更新成功');
        setTimeout(() => navigate('/customers'), 1500);
      } else {
        await customerApi.createCustomer(formData);
        showSuccess('客户创建成功');
        setTimeout(() => navigate('/customers'), 1500);
      }
    } catch (err) {
      console.error('Failed to save customer:', err);
      showError(isEditMode ? '更新客户失败' : '创建客户失败');
    } finally {
      setSubmitting(false);
    }
  };

  const handleReset = (): void => {
    if (isEditMode) {
      loadCustomerData();
    } else {
      setFormData({
        customerName: '',
        contactPerson: '',
        phone: '',
        email: '',
        address: '',
        status: 'ACTIVE',
        remark: ''
      });
    }
    setErrors({});
    // toast state removed, using useToast hook
  };

  const handleBack = (): void => {
    navigate('/customers');
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 200 }}>
        <CircularProgress />
      </Box>
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
            {isEditMode ? '编辑客户' : '新增客户'}
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<ClearIcon />}
            onClick={handleReset}
            disabled={submitting}
          >
            重置
          </Button>
          <Button
            variant="contained"
            startIcon={<SaveIcon />}
            onClick={handleSave}
            disabled={submitting}
          >
            {submitting ? '保存中...' : '保存'}
          </Button>
        </Box>
      </Box>

      <Paper sx={{ p: 3 }}>
        <Grid container spacing={3}>
          <Grid size={{ xs: 12, md: 6 }}>
            <TextField
              fullWidth
              label="客户名称"
              name="customerName"
              value={formData.customerName}
              onChange={handleInputChange}
              error={!!errors.customerName}
              helperText={errors.customerName}
              sx={{ mb: 2 }}
              disabled={submitting}
            />

            <TextField
              fullWidth
              label="联系人"
              name="contactPerson"
              value={formData.contactPerson}
              onChange={handleInputChange}
              error={!!errors.contactPerson}
              helperText={errors.contactPerson}
              sx={{ mb: 2 }}
              disabled={submitting}
            />

            <TextField
              fullWidth
              label="联系电话"
              name="phone"
              value={formData.phone}
              onChange={handleInputChange}
              error={!!errors.phone}
              helperText={errors.phone}
              sx={{ mb: 2 }}
              disabled={submitting}
            />

            <TextField
              fullWidth
              label="电子邮箱"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleInputChange}
              error={!!errors.email}
              helperText={errors.email}
              sx={{ mb: 2 }}
              disabled={submitting}
            />
          </Grid>

          <Grid size={{ xs: 12, md: 6 }}>
            <TextField
              fullWidth
              label="地址"
              name="address"
              value={formData.address}
              onChange={handleInputChange}
              multiline
              rows={3}
              sx={{ mb: 2 }}
              disabled={submitting}
            />

            <FormControl fullWidth sx={{ mb: 2 }} error={!!errors.status}>
              <InputLabel>状态</InputLabel>
              <Select
                name="status"
                value={formData.status}
                label="状态"
                onChange={handleStatusChange}
                disabled={submitting}
              >
                <MenuItem value="ACTIVE">启用</MenuItem>
                <MenuItem value="INACTIVE">禁用</MenuItem>
              </Select>
              {errors.status && <FormHelperText>{errors.status}</FormHelperText>}
            </FormControl>

            <TextField
              fullWidth
              label="备注"
              name="remark"
              value={formData.remark}
              onChange={handleInputChange}
              multiline
              rows={3}
              sx={{ mb: 2 }}
              disabled={submitting}
            />
          </Grid>
        </Grid>
      </Paper>
    </Box>
  );
};

export default CustomerEdit;