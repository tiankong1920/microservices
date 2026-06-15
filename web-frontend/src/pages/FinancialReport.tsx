import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Paper,
  Grid,
  Button,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Card,
  CardContent,
  Divider,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow
} from '@mui/material';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as RechartsTooltip,
  Legend,
  ResponsiveContainer,
  LineChart,
  Line,
  PieChart,
  Pie,
  Cell
} from 'recharts';
import { Download as DownloadIcon, Print as PrintIcon } from '@mui/icons-material';
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { DatePicker as MuiDatePicker } from '@mui/x-date-pickers/DatePicker';
import dayjs, { Dayjs } from 'dayjs';
import api from '../services/api';
import ExportService from '../services/exportService';
import Toast from '../components/Toast';

interface FinancialData {
  roa?: number;
  incomeBreakdown?: IncomeItem[];
  expenseBreakdown?: ExpenseItem[];
  cashFlowTrend?: CashFlowItem[];
  accountBalances?: AccountBalance[];
}

interface IncomeItem {
  id: number;
  date: string;
  category: string;
  description: string;
  amount: number;
}

interface ExpenseItem {
  id: number;
  date: string;
  category: string;
  description: string;
  amount: number;
}

interface CashFlowItem {
  date: string;
  income: number;
  expense: number;
  netCashFlow: number;
}

interface AccountBalance {
  accountId: number;
  accountName: string;
  accountType: string;
  beginningBalance: number;
  endingBalance: number;
}

const FinancialReport: React.FC = () => {
  const [reportType, setReportType] = useState<string>('profitLoss');
  const [startDate, setStartDate] = useState<Dayjs>(dayjs().subtract(30, 'day'));
  const [endDate, setEndDate] = useState<Dayjs>(dayjs());
  const [financialData, setFinancialData] = useState<FinancialData>({});
  const [incomeData, setIncomeData] = useState<IncomeItem[]>([]);
  const [expenseData, setExpenseData] = useState<ExpenseItem[]>([]);
  const [cashFlowData, setCashFlowData] = useState<CashFlowItem[]>([]);
  const [_accountData, setAccountData] = useState<AccountBalance[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [toast, setToast] = useState<{ open: boolean; message: string; severity: 'success' | 'error' | 'info' | 'warning' }>({ open: false, message: '', severity: 'info' });

  const pieColors = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#A28DFF', '#FF6B6B'];

  const fetchFinancialReport = React.useCallback(async (): Promise<void> => {
    setLoading(true);
    try {
      const response = await api.get('/reports/financial', {
        params: {
          startDate: startDate.format('YYYY-MM-DD'),
          endDate: endDate.format('YYYY-MM-DD'),
          reportType
        }
      });
      
      const data: FinancialData = response.data.data;
      setFinancialData(data);
      setIncomeData(data.incomeBreakdown || []);
      setExpenseData(data.expenseBreakdown || []);
      setCashFlowData(data.cashFlowTrend || []);
      setAccountData(data.accountBalances || []);
    } catch (error) {
      console.error('获取财务报表失败:', error);
      setToast({ open: true, message: '获取财务报表失败', severity: 'error' });
    } finally {
      setLoading(false);
    }
  }, [startDate, endDate, reportType]);

  useEffect(() => {
    fetchFinancialReport();
  }, [fetchFinancialReport]);

  const handleDateRangeChange = (newStartDate: Dayjs | null, newEndDate: Dayjs | null): void => {
    if (newStartDate) setStartDate(newStartDate);
    if (newEndDate) setEndDate(newEndDate);
  };

  const handleReportTypeChange = (event: { target: { value: unknown } }): void => {
    setReportType(event.target.value as string);
  };

  const handleExportPDF = (): void => {
    try {
      ExportService.exportToPDF(incomeData, {
        filename: 'financial-report',
        fields: [
          { key: 'date', label: '日期' },
          { key: 'category', label: '类别' },
          { key: 'description', label: '描述' },
          { key: 'amount', label: '金额', format: (v: number) => `¥${(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}` },
        ],
      });
      setToast({ open: true, message: 'PDF 导出成功', severity: 'success' });
    } catch (err) {
      console.error('Export PDF failed:', err);
      setToast({ open: true, message: 'PDF 导出失败', severity: 'error' });
    }
  };

  const handlePrint = (): void => {
    window.print();
  };

  const totalIncome = incomeData.reduce((sum, item) => sum + (item.amount || 0), 0);
  const totalExpense = expenseData.reduce((sum, item) => sum + (item.amount || 0), 0);
  const netProfit = totalIncome - totalExpense;

  return (
    <Box>
      <Toast
        open={toast.open}
        message={toast.message}
        severity={toast.severity}
        onClose={() => setToast({ ...toast, open: false })}
      />

      <Typography variant="h4" gutterBottom>
        财务报表
      </Typography>
      
      <Paper sx={{ p: 2, mb: 3, backgroundColor: '#f5f5f5' }}>
        <Grid container spacing={3} alignItems="center">
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <FormControl fullWidth>
              <InputLabel id="report-type-label">报表类型</InputLabel>
              <Select
                labelId="report-type-label"
                id="report-type"
                value={reportType}
                label="报表类型"
                onChange={handleReportTypeChange}
              >
                <MenuItem value="profitLoss">利润表</MenuItem>
                <MenuItem value="balanceSheet">资产负债表</MenuItem>
                <MenuItem value="cashFlow">现金流量表</MenuItem>
                <MenuItem value="trialBalance">试算平衡表</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <LocalizationProvider dateAdapter={AdapterDayjs}>
              <MuiDatePicker
                label="开始日期"
                value={startDate}
                onChange={(newValue) => handleDateRangeChange(newValue, endDate)}
              />
            </LocalizationProvider>
          </Grid>
          
          <Grid size={{ xs: 12, sm: 6, md: 3 }}>
            <LocalizationProvider dateAdapter={AdapterDayjs}>
              <MuiDatePicker
                label="结束日期"
                value={endDate}
                onChange={(newValue) => handleDateRangeChange(startDate, newValue)}
              />
            </LocalizationProvider>
          </Grid>
          
          <Grid size={{ xs: 12, sm: 6, md: 3 }} sx={{ display: 'flex', gap: 1 }}>
            <Button
              variant="outlined"
              size="small"
              startIcon={<DownloadIcon />}
              onClick={handleExportPDF}
              disabled={loading || incomeData.length === 0}
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
            <Button
              variant="contained"
              color="primary"
              onClick={fetchFinancialReport}
              disabled={loading}
            >
              查询
            </Button>
            <Button
              variant="outlined"
              color="secondary"
              onClick={() => {
                setStartDate(dayjs().subtract(30, 'day'));
                setEndDate(dayjs());
              }}
            >
              重置
            </Button>
          </Grid>
        </Grid>
      </Paper>

      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary">
                总收入
              </Typography>
              <Typography variant="h5" color="primary" sx={{ mt: 1 }}>
                ¥{totalIncome.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                相比上月 <span style={{ color: '#4caf50', fontWeight: 'bold' }}>+12.5%</span>
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary">
                总支出
              </Typography>
              <Typography variant="h5" color="error" sx={{ mt: 1 }}>
                ¥{totalExpense.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                相比上月 <span style={{ color: '#ff5722', fontWeight: 'bold' }}>+8.2%</span>
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary">
                净利润
              </Typography>
              <Typography variant="h5" color={netProfit >= 0 ? '#4caf50' : '#ff5722'} sx={{ mt: 1 }}>
                ¥{netProfit.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                相比上月 <span style={{ color: '#4caf50', fontWeight: 'bold' }}>+18.3%</span>
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary">
                净资产收益率
              </Typography>
              <Typography variant="h5" color="primary" sx={{ mt: 1 }}>
                {(financialData.roa || 0).toFixed(2)}%
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                行业平均水平 <span style={{ color: '#ff9800', fontWeight: 'bold' }}>8.5%</span>
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid size={{ xs: 12, md: 8 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                收入支出对比
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Box sx={{ height: 400 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={cashFlowData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis />
                    <RechartsTooltip formatter={(value: number | undefined) => value !== undefined ? [`¥${value}`, '金额'] : ['', '金额']} />
                    <Legend />
                    <Bar dataKey="income" fill="#4caf50" name="收入" />
                    <Bar dataKey="expense" fill="#ff5722" name="支出" />
                    <Bar dataKey="netCashFlow" fill="#2196f3" name="净现金流" />
                  </BarChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid size={{ xs: 12, md: 4 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                收入来源占比
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Box sx={{ height: 400, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={incomeData}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      outerRadius={120}
                      fill="#8884d8"
                      dataKey="amount"
                      nameKey="category"
                      label={({ name, percent }: { name?: string; percent?: number }) => `${name || ''} ${((percent || 0) * 100).toFixed(0)}%`}
                    >
                      {incomeData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={pieColors[index % pieColors.length]} />
                      ))}
                    </Pie>
                    <RechartsTooltip formatter={(value: number | undefined) => value !== undefined ? [`¥${value}`, '金额'] : ['', '金额']} />
                  </PieChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid size={{ xs: 12, md: 4 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                支出构成分析
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Box sx={{ height: 400, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={expenseData}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      outerRadius={120}
                      fill="#8884d8"
                      dataKey="amount"
                      nameKey="category"
                      label={({ name, percent }: { name?: string; percent?: number }) => `${name || ''} ${((percent || 0) * 100).toFixed(0)}%`}
                    >
                      {expenseData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={pieColors[index % pieColors.length]} />
                      ))}
                    </Pie>
                    <RechartsTooltip formatter={(value: number | undefined) => value !== undefined ? [`¥${value}`, '金额'] : ['', '金额']} />
                  </PieChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                现金流趋势
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Box sx={{ height: 300 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={cashFlowData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis />
                    <RechartsTooltip formatter={(value: number | undefined) => value !== undefined ? [`¥${value}`, '金额'] : ['', '金额']} />
                    <Legend />
                    <Line type="monotone" dataKey="netCashFlow" stroke="#2196f3" strokeWidth={2} dot={{ r: 4 }} activeDot={{ r: 6 }} name="净现金流" />
                  </LineChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                收入明细
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <TableContainer sx={{ maxHeight: 400 }}>
                <Table sx={{ minWidth: 300 }} aria-label="收入明细表格">
                  <TableHead>
                    <TableRow>
                      <TableCell>日期</TableCell>
                      <TableCell>类别</TableCell>
                      <TableCell>描述</TableCell>
                      <TableCell align="right">金额</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {incomeData.map((row) => (
                      <TableRow
                        key={row.id}
                        sx={{
                          '&:last-child td, &:last-child th': { border: 0 },
                          cursor: 'pointer',
                          '&:hover': { backgroundColor: 'rgba(0, 0, 0, 0.04)' }
                        }}
                      >
                        <TableCell>{row.date}</TableCell>
                        <TableCell>{row.category}</TableCell>
                        <TableCell>{row.description}</TableCell>
                        <TableCell align="right" style={{ color: '#4caf50', fontWeight: 'bold' }}>
                          +¥{row.amount.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                支出明细
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <TableContainer sx={{ maxHeight: 400 }}>
                <Table sx={{ minWidth: 300 }} aria-label="支出明细表格">
                  <TableHead>
                    <TableRow>
                      <TableCell>日期</TableCell>
                      <TableCell>类别</TableCell>
                      <TableCell>描述</TableCell>
                      <TableCell align="right">金额</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {expenseData.map((row) => (
                      <TableRow
                        key={row.id}
                        sx={{
                          '&:last-child td, &:last-child th': { border: 0 },
                          cursor: 'pointer',
                          '&:hover': { backgroundColor: 'rgba(0, 0, 0, 0.04)' }
                        }}
                      >
                        <TableCell>{row.date}</TableCell>
                        <TableCell>{row.category}</TableCell>
                        <TableCell>{row.description}</TableCell>
                        <TableCell align="right" style={{ color: '#ff5722', fontWeight: 'bold' }}>
                          -¥{row.amount.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default FinancialReport;
