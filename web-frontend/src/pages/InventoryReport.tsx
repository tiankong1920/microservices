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

interface InventorySummary {
  totalQuantity: number;
  totalValue: number;
  turnoverRate: number;
}

interface ProductData {
  productId: number;
  productName: string;
  warehouseId: number;
  warehouseName: string;
  stockQuantity: number;
  unit: string;
  totalValue: number;
  turnoverRate: number;
  reorderPoint: number;
  maxStock: number;
}

interface WarehouseData {
  warehouseId: number;
  warehouseName: string;
  totalQuantity: number;
}

interface MovementData {
  date: string;
  beginningStock: number;
  endingStock: number;
  inbound: number;
  outbound: number;
}

interface ReportData {
  inventorySummary?: InventorySummary[];
  productBreakdown?: ProductData[];
  warehouseBreakdown?: WarehouseData[];
  movementTrend?: MovementData[];
}

const InventoryReport: React.FC = () => {
  const [reportType, setReportType] = useState<string>('current');
  const [startDate, setStartDate] = useState<Dayjs>(dayjs().subtract(30, 'day'));
  const [endDate, setEndDate] = useState<Dayjs>(dayjs());
  const [inventoryData, setInventoryData] = useState<InventorySummary[]>([]);
  const [productData, setProductData] = useState<ProductData[]>([]);
  const [warehouseData, setWarehouseData] = useState<WarehouseData[]>([]);
  const [movementData, setMovementData] = useState<MovementData[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [toast, setToast] = useState<{ open: boolean; message: string; severity: 'success' | 'error' | 'info' | 'warning' }>({ open: false, message: '', severity: 'info' });

  const pieColors = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#A28DFF', '#FF6B6B'];

  const fetchInventoryReport = async (): Promise<void> => {
    setLoading(true);
    try {
      const response = await api.get('/reports/inventory', {
        params: {
          startDate: startDate.format('YYYY-MM-DD'),
          endDate: endDate.format('YYYY-MM-DD'),
          reportType
        }
      });
      
      const data: ReportData = response.data.data;
      setInventoryData(data.inventorySummary || []);
      setProductData(data.productBreakdown || []);
      setWarehouseData(data.warehouseBreakdown || []);
      setMovementData(data.movementTrend || []);
    } catch (error) {
      console.error('获取库存报表失败:', error);
      setToast({ open: true, message: '获取库存报表失败', severity: 'error' });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchInventoryReport();
  }, [startDate, endDate, reportType]);

  const handleDateRangeChange = (newStartDate: Dayjs | null, newEndDate: Dayjs | null): void => {
    if (newStartDate) setStartDate(newStartDate);
    if (newEndDate) setEndDate(newEndDate);
  };

  const handleReportTypeChange = (event: { target: { value: unknown } }): void => {
    setReportType(event.target.value as string);
  };

  const handleExportPDF = (): void => {
    try {
      ExportService.exportToPDF(productData, {
        filename: 'inventory-report',
        fields: [
          { key: 'productName', label: '产品名称' },
          { key: 'warehouseName', label: '仓库' },
          { key: 'stockQuantity', label: '库存数量' },
          { key: 'unit', label: '单位' },
          { key: 'totalValue', label: '库存价值', format: (v: number) => `¥${(v || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}` },
          { key: 'turnoverRate', label: '库存周转率', format: (v: number) => `${(v || 0).toFixed(2)}次` },
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

  const totalQuantity = inventoryData.reduce((sum, item) => sum + (item.totalQuantity || 0), 0);
  const totalValue = inventoryData.reduce((sum, item) => sum + (item.totalValue || 0), 0);
  const avgTurnover = inventoryData.reduce((sum, item) => sum + (item.turnoverRate || 0), 0) / Math.max(inventoryData.length, 1);
  const warningCount = productData.filter(item => item.stockQuantity < item.reorderPoint).length;

  return (
    <Box>
      <Toast
        open={toast.open}
        message={toast.message}
        severity={toast.severity}
        onClose={() => setToast({ ...toast, open: false })}
      />

      <Typography variant="h4" gutterBottom>
        库存报表
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
                <MenuItem value="current">当前库存</MenuItem>
                <MenuItem value="movement">库存变动</MenuItem>
                <MenuItem value="forecast">库存预测</MenuItem>
                <MenuItem value="valuation">库存估值</MenuItem>
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
              disabled={loading || productData.length === 0}
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
              onClick={fetchInventoryReport}
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
                总库存数量
              </Typography>
              <Typography variant="h5" color="primary" sx={{ mt: 1 }}>
                {totalQuantity}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                相比上月 <span style={{ color: '#4caf50', fontWeight: 'bold' }}>+3.5%</span>
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary">
                总库存估值
              </Typography>
              <Typography variant="h5" color="primary" sx={{ mt: 1 }}>
                ¥{totalValue.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                相比上月 <span style={{ color: '#4caf50', fontWeight: 'bold' }}>+5.2%</span>
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary">
                库存周转率
              </Typography>
              <Typography variant="h5" color="primary" sx={{ mt: 1 }}>
                {avgTurnover.toFixed(2)}次
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                相比上月 <span style={{ color: '#4caf50', fontWeight: 'bold' }}>+0.8%</span>
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid size={{ xs: 12, sm: 6, md: 3 }}>
          <Card>
            <CardContent>
              <Typography variant="subtitle2" color="text.secondary">
                预警产品数
              </Typography>
              <Typography variant="h5" color="warning.main" sx={{ mt: 1 }}>
                {warningCount}
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                需要及时补货
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
                库存趋势
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Box sx={{ height: 400 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={movementData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis />
                    <RechartsTooltip formatter={(value: number | undefined) => value !== undefined ? [value, '库存数量'] : ['', '库存数量']} />
                    <Legend />
                    <Line type="monotone" dataKey="beginningStock" stroke="#8884d8" strokeWidth={2} dot={{ r: 4 }} activeDot={{ r: 6 }} name="期初库存" />
                    <Line type="monotone" dataKey="endingStock" stroke="#82ca9d" strokeWidth={2} dot={{ r: 4 }} activeDot={{ r: 6 }} name="期末库存" />
                  </LineChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid size={{ xs: 12, md: 4 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                仓库库存分布
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Box sx={{ height: 400, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={warehouseData}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      outerRadius={120}
                      fill="#8884d8"
                      dataKey="totalQuantity"
                      nameKey="warehouseName"
                      label={({ name, percent }: { name?: string; percent?: number }) => `${name || ''} ${((percent || 0) * 100).toFixed(0)}%`}
                    >
                      {warehouseData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={pieColors[index % pieColors.length]} />
                      ))}
                    </Pie>
                    <RechartsTooltip formatter={(value: number | undefined) => value !== undefined ? [value, '库存数量'] : ['', '库存数量']} />
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
                库存变动统计
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Box sx={{ height: 300 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={movementData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="date" />
                    <YAxis />
                    <RechartsTooltip formatter={(value: number | undefined) => value !== undefined ? [value, '数量'] : ['', '数量']} />
                    <Legend />
                    <Bar dataKey="inbound" fill="#82ca9d" name="入库" />
                    <Bar dataKey="outbound" fill="#ff8042" name="出库" />
                  </BarChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                产品库存状况
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Box sx={{ height: 300 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={productData.slice(0, 10)}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="productName" />
                    <YAxis />
                    <RechartsTooltip formatter={(value: number | undefined) => value !== undefined ? [value, '数量'] : ['', '数量']} />
                    <Legend />
                    <Bar dataKey="stockQuantity" fill="#8884d8" name="当前库存" />
                    <Bar dataKey="reorderPoint" fill="#ffbb28" name="补货点" />
                  </BarChart>
                </ResponsiveContainer>
              </Box>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid size={{ xs: 12 }}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                库存明细
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <TableContainer>
                <Table sx={{ minWidth: 650 }} aria-label="库存明细表格">
                  <TableHead>
                    <TableRow>
                      <TableCell>产品名称</TableCell>
                      <TableCell>仓库</TableCell>
                      <TableCell>库存数量</TableCell>
                      <TableCell align="right">库存价值</TableCell>
                      <TableCell align="right">库存周转率</TableCell>
                      <TableCell align="right">状态</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {productData.map((row) => (
                      <TableRow
                        key={`${row.productId}-${row.warehouseId}`}
                        sx={{
                          '&:last-child td, &:last-child th': { border: 0 },
                          cursor: 'pointer',
                          '&:hover': { backgroundColor: 'rgba(0, 0, 0, 0.04)' }
                        }}
                      >
                        <TableCell component="th" scope="row">
                          {row.productName}
                        </TableCell>
                        <TableCell>{row.warehouseName}</TableCell>
                        <TableCell>
                          {row.stockQuantity} {row.unit}
                        </TableCell>
                        <TableCell align="right">
                          ¥{row.totalValue.toLocaleString('zh-CN', { minimumFractionDigits: 2 })}
                        </TableCell>
                        <TableCell align="right">
                          {row.turnoverRate.toFixed(2)}次
                        </TableCell>
                        <TableCell align="right">
                          <span style={{
                            color: row.stockQuantity < row.reorderPoint ? '#ff5722' : row.stockQuantity > row.maxStock ? '#ff9800' : '#4caf50',
                            fontWeight: 'bold'
                          }}>
                            {row.stockQuantity < row.reorderPoint ? '预警' : row.stockQuantity > row.maxStock ? '过剩' : '正常'}
                          </span>
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

export default InventoryReport;
