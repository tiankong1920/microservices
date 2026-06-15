import React, { useState, useEffect } from 'react';
import { Typography, Box, Paper, Grid, Card, CardContent, CircularProgress, FormControl, InputLabel, Select, MenuItem, SelectChangeEvent, Button } from '@mui/material';
import { TrendingUp as TrendingUpIcon, MonetizationOn as MonetizationOnIcon, ShoppingCart as ShoppingCartIcon, Download as DownloadIcon, Print as PrintIcon } from '@mui/icons-material';
import { reportApi } from '../services/api';
import ExportService from '../services/exportService';
import Toast from '../components/Toast';
import type { ApiResponse, SalesReport as SalesReportType } from '../types';

const SalesReport: React.FC = () => {
  const [report, setReport] = useState<SalesReportType | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [_error, setError] = useState<string | null>(null);
  const [period, setPeriod] = useState<string>('month');
  const [startDate] = useState<string>('');
  const [endDate] = useState<string>('');
  const [toast, setToast] = useState<{ open: boolean; message: string; severity: 'success' | 'error' | 'info' | 'warning' }>({ open: false, message: '', severity: 'info' });

  const loadReport = async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const response: ApiResponse<SalesReportType> = await reportApi.getSalesReport({ startDate: startDate, endDate: endDate });
      setReport(response.data);
    } catch (err) {
      console.error('Failed to load sales report:', err);
      setError('加载销售报表失败');
      setToast({ open: true, message: '加载销售报表失败', severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadReport();
  }, [period]);

  const handlePeriodChange = (e: SelectChangeEvent<string>): void => {
    setPeriod(e.target.value);
  };

  const handleExportPDF = (): void => {
    if (!report) {
      setToast({ open: true, message: '没有可导出的数据', severity: 'warning' });
      return;
    }
    const fields = [
      { key: 'period', label: '报表期间' },
      { key: 'totalSales', label: '总销售额', format: (v: number) => `¥${v || 0}` },
      { key: 'totalOrders', label: '订单数量' },
      { key: 'averageOrderValue', label: '平均订单金额', format: (v: number) => `¥${v || 0}` },
    ];
    ExportService.exportToPDF([report], { filename: 'sales-report', fields });
    setToast({ open: true, message: 'PDF 导出成功', severity: 'success' });
  };

  const handlePrint = (): void => {
    window.print();
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 400 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Toast
        open={toast.open}
        message={toast.message}
        severity={toast.severity}
        onClose={() => setToast({ ...toast, open: false })}
      />

      <Box sx={{ p: 3 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
          <Typography variant="h4" gutterBottom>
            销售报表
          </Typography>
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
            <Button
              variant="outlined"
              size="small"
              startIcon={<DownloadIcon />}
              onClick={handleExportPDF}
            >
              导出 PDF
            </Button>
            <Button
              variant="outlined"
              size="small"
              startIcon={<PrintIcon />}
              onClick={handlePrint}
            >
              打印
            </Button>
            <FormControl sx={{ minWidth: 120 }}>
              <InputLabel>报表期间</InputLabel>
              <Select value={period} label="报表期间" onChange={handlePeriodChange}>
                <MenuItem value="week">本周</MenuItem>
                <MenuItem value="month">本月</MenuItem>
                <MenuItem value="quarter">本季度</MenuItem>
                <MenuItem value="year">本年</MenuItem>
              </Select>
            </FormControl>
          </Box>
        </Box>

        <Grid container spacing={3} sx={{ mb: 3 }}>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <MonetizationOnIcon color="success" sx={{ fontSize: 40, mr: 2 }} />
                  <Box>
                    <Typography color="textSecondary" gutterBottom>
                      总销售额
                    </Typography>
                    <Typography variant="h5">
                      ¥{report?.totalSales || 0}
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <ShoppingCartIcon color="primary" sx={{ fontSize: 40, mr: 2 }} />
                  <Box>
                    <Typography color="textSecondary" gutterBottom>
                      订单数量
                    </Typography>
                    <Typography variant="h5">
                      {report?.totalOrders || 0}
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <Card>
              <CardContent>
                <Box sx={{ display: 'flex', alignItems: 'center' }}>
                  <TrendingUpIcon color="info" sx={{ fontSize: 40, mr: 2 }} />
                  <Box>
                    <Typography color="textSecondary" gutterBottom>
                      平均订单金额
                    </Typography>
                    <Typography variant="h5">
                      ¥{report?.averageOrderValue || 0}
                    </Typography>
                  </Box>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>

        <Paper sx={{ p: 3 }}>
          <Typography variant="h6" gutterBottom>
            销售详情
          </Typography>
          <Typography variant="body1" color="textSecondary">
            报表期间: {report?.period || period}
          </Typography>
        </Paper>
      </Box>
    </Box>
  );
};

export default SalesReport;