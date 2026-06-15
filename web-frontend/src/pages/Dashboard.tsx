import React, { useEffect, useRef, useState, useMemo } from 'react';
import { Typography, Grid, Paper, Box } from '@mui/material';
import { TrendingUp as TrendingUpIcon, ShoppingCart as ShoppingCartIcon, Inventory2 as InventoryIcon, Store as StoreIcon, Person as PersonIcon } from '@mui/icons-material';
import type { ECharts, EChartsOption } from 'echarts';
import { dashboardApi, procurementApi } from '../services/api';
import type { DashboardStats, InventoryReport, ProcurementOrder } from '../types';
import Skeleton from '../components/Skeleton';
import { useToast } from '../contexts/ToastContext';

interface StatItem {
  title: string;
  value: string;
  icon: React.ReactElement;
  color: string;
}

interface SalesData {
  months: string[];
  values: number[];
}

interface InventoryItem {
  name: string;
  value: number;
}

interface ProcurementData {
  suppliers: string[];
  amounts: number[];
}

const Dashboard: React.FC = () => {
  // API data state
  const [statsData, setStatsData] = useState<DashboardStats | null>(null);
  const [salesTrend, setSalesTrend] = useState<{ date: string; value: number }[]>([]);
  const [inventoryReport, setInventoryReport] = useState<InventoryReport | null>(null);
  const [procurementOrders, setProcurementOrders] = useState<ProcurementOrder[]>([]);
  const { showError, showWarning } = useToast();
  const [loading, setLoading] = useState(true);

  // 图表容器引用
  const salesChartRef = useRef<HTMLDivElement>(null);
  const inventoryChartRef = useRef<HTMLDivElement>(null);
  const procurementChartRef = useRef<HTMLDivElement>(null);
  const cleanupRef = useRef<(() => void) | null>(null);

  // 获取所有仪表盘数据
  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const results = await Promise.allSettled([
          dashboardApi.getDashboardStats(),
          dashboardApi.getSalesTrend('yearly'),
          dashboardApi.getInventoryReport(),
          procurementApi.getAllProcurements(),
        ]);

        const [statsResult, trendResult, invResult, procResult] = results;

        if (statsResult.status === 'fulfilled' && statsResult.value.success && statsResult.value.data) {
          setStatsData(statsResult.value.data);
        } else {
          console.error('Stats fetch failed:', statsResult);
        }

        if (trendResult.status === 'fulfilled' && trendResult.value.success && trendResult.value.data) {
          setSalesTrend(trendResult.value.data);
        } else {
          console.error('Sales trend fetch failed:', trendResult);
        }

        if (invResult.status === 'fulfilled' && invResult.value.success && invResult.value.data) {
          setInventoryReport(invResult.value.data);
        } else {
          console.error('Inventory report fetch failed:', invResult);
        }

        if (procResult.status === 'fulfilled' && procResult.value.success && procResult.value.data) {
          setProcurementOrders(procResult.value.data);
        } else {
          console.error('Procurement fetch failed:', procResult);
        }

        const hasFailures = results.some(r => r.status === 'rejected');
        if (hasFailures) {
          showWarning('部分数据加载失败，请稍后重试');
        }
      } catch (err) {
        console.error('Dashboard data fetch error:', err);
        showError('加载仪表盘数据失败');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // 构建统计卡片数据
  const statItems: StatItem[] = useMemo(() => {
    if (!statsData) return [];
    const inventoryValue = inventoryReport?.totalValue ?? 0;
    return [
      { title: '总产品数', value: statsData.totalProducts.toLocaleString(), icon: <StoreIcon />, color: '#1976d2' },
      { title: '库存总量', value: `¥${inventoryValue.toLocaleString()}`, icon: <InventoryIcon />, color: '#388e3c' },
      { title: '今日订单', value: statsData.totalOrders.toLocaleString(), icon: <ShoppingCartIcon />, color: '#f57c00' },
      { title: '本月销售额', value: `¥${statsData.totalSales.toLocaleString()}`, icon: <TrendingUpIcon />, color: '#d32f2f' },
      { title: '活跃客户', value: statsData.totalCustomers.toLocaleString(), icon: <PersonIcon />, color: '#7b1fa2' },
    ];
  }, [statsData, inventoryReport]);

  // 销售趋势图数据
  const salesData: SalesData = useMemo(() => {
    if (salesTrend.length === 0) return { months: [], values: [] };
    return {
      months: salesTrend.map(item => item.date),
      values: salesTrend.map(item => item.value),
    };
  }, [salesTrend]);

  // 库存分布图数据
  const inventoryItems: InventoryItem[] = useMemo(() => {
    const breakdown = inventoryReport?.categoryBreakdown;
    if (!breakdown || breakdown.length === 0) return [];
    return breakdown.map(item => ({
      name: item.category,
      value: item.productCount,
    }));
  }, [inventoryReport]);

  // 采购报表数据（按供应商聚合）
  const procurementData: ProcurementData = useMemo(() => {
    if (procurementOrders.length === 0) return { suppliers: [], amounts: [] };
    const supplierMap = new Map<string, number>();
    procurementOrders.forEach(order => {
      const name = order.supplierName;
      supplierMap.set(name, (supplierMap.get(name) || 0) + order.totalAmount);
    });
    return {
      suppliers: Array.from(supplierMap.keys()),
      amounts: Array.from(supplierMap.values()),
    };
  }, [procurementOrders]);

  // 初始化所有图表
  useEffect(() => {
    if (loading) return;

    let cancelled = false;

    const initCharts = async () => {
      try {
        const echarts = await import('echarts');

        if (cancelled) return;

        // 初始化销售趋势图
        if (salesChartRef.current && salesData.months.length > 0) {
          const existingSales = echarts.getInstanceByDom(salesChartRef.current);
          if (existingSales) existingSales.dispose();
          const salesChart: ECharts = echarts.init(salesChartRef.current);
          const salesOption: EChartsOption = {
            tooltip: {
              trigger: 'axis',
              formatter: '{b}: ¥{c}',
            },
            grid: {
              left: '3%',
              right: '4%',
              bottom: '3%',
              containLabel: true,
            },
            xAxis: {
              type: 'category',
              boundaryGap: false,
              data: salesData.months,
            },
            yAxis: {
              type: 'value',
              axisLabel: {
                formatter: '¥{value}',
              },
            },
            series: [
              {
                name: '销售额',
                type: 'line',
                smooth: true,
                data: salesData.values,
                areaStyle: {
                  color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: 'rgba(25, 118, 210, 0.5)' },
                    { offset: 1, color: 'rgba(25, 118, 210, 0.1)' },
                  ]),
                },
                lineStyle: {
                  color: '#1976d2',
                },
                itemStyle: {
                  color: '#1976d2',
                },
              },
            ],
          };
          salesChart.setOption(salesOption);
        }

        // 初始化库存分布图
        if (inventoryChartRef.current && inventoryItems.length > 0) {
          const existingInventory = echarts.getInstanceByDom(inventoryChartRef.current);
          if (existingInventory) existingInventory.dispose();
          const inventoryChart: ECharts = echarts.init(inventoryChartRef.current);
          const inventoryOption: EChartsOption = {
            tooltip: {
              trigger: 'item',
              formatter: '{b}: {c} ({d}%)',
            },
            legend: {
              orient: 'vertical',
              right: 10,
              top: 'center',
            },
            series: [
              {
                name: '库存分布',
                type: 'pie',
                radius: ['40%', '70%'],
                avoidLabelOverlap: false,
                itemStyle: {
                  borderRadius: 10,
                  borderColor: '#fff',
                  borderWidth: 2,
                },
                label: {
                  show: false,
                  position: 'center',
                },
                emphasis: {
                  label: {
                    show: true,
                    fontSize: 20,
                    fontWeight: 'bold',
                  },
                },
                labelLine: {
                  show: false,
                },
                data: inventoryItems,
              },
            ],
          };
          inventoryChart.setOption(inventoryOption);
        }

        // 初始化采购报表图
        if (procurementChartRef.current && procurementData.suppliers.length > 0) {
          const existingProcurement = echarts.getInstanceByDom(procurementChartRef.current);
          if (existingProcurement) existingProcurement.dispose();
          const procurementChart: ECharts = echarts.init(procurementChartRef.current);
          const procurementOption: EChartsOption = {
            tooltip: {
              trigger: 'axis',
              formatter: '{b}: ¥{c}',
            },
            grid: {
              left: '3%',
              right: '4%',
              bottom: '3%',
              containLabel: true,
            },
            xAxis: {
              type: 'category',
              data: procurementData.suppliers,
              axisLabel: {
                rotate: 45,
              },
            },
            yAxis: {
              type: 'value',
              axisLabel: {
                formatter: '¥{value}',
              },
            },
            series: [
              {
                name: '采购金额',
                type: 'bar',
                data: procurementData.amounts,
                itemStyle: {
                  color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: '#388e3c' },
                    { offset: 1, color: '#66bb6a' },
                  ]),
                },
              },
            ],
          };
          procurementChart.setOption(procurementOption);
        }

        if (cancelled) return;

        // 统一的响应式处理
        const handleResize = () => {
          if (salesChartRef.current) {
            const chart = echarts.getInstanceByDom(salesChartRef.current);
            chart?.resize();
          }
          if (inventoryChartRef.current) {
            const chart = echarts.getInstanceByDom(inventoryChartRef.current);
            chart?.resize();
          }
          if (procurementChartRef.current) {
            const chart = echarts.getInstanceByDom(procurementChartRef.current);
            chart?.resize();
          }
        };
        window.addEventListener('resize', handleResize);

        cleanupRef.current = () => {
          window.removeEventListener('resize', handleResize);
          [salesChartRef, inventoryChartRef, procurementChartRef].forEach(ref => {
            if (ref.current) {
              const chart = echarts.getInstanceByDom(ref.current);
              chart?.dispose();
            }
          });
        };
      } catch (error) {
        console.error('Failed to load echarts:', error);
      }
    };

    initCharts();

    return () => {
      cancelled = true;
      cleanupRef.current?.();
      cleanupRef.current = null;
    };
  }, [loading, salesData, inventoryItems, procurementData]);

  // 加载状态
  if (loading) {
    return (
      <Box>
        <Typography variant="h4" gutterBottom>
          仪表盘
        </Typography>
        <Skeleton type="card" count={5} />
        <Box sx={{ mt: 3 }}>
          <Skeleton type="table" rows={4} columns={4} />
        </Box>
      </Box>
    );
  }

  // 无数据状态
  if (!statsData && statItems.length === 0) {
    return (
      <Box>
        <Typography variant="h4" gutterBottom>
          仪表盘
        </Typography>
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="h6" color="text.secondary">
            暂无数据
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            请确保后端服务已启动且数据已加载
          </Typography>
        </Paper>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        仪表盘
      </Typography>
      <Grid container spacing={3}>
        {statItems.map((stat, index) => (
          <Grid size={{ xs: 12, sm: 6, md: 4, lg: 2 }} key={index}>
            <Paper
              sx={{
                p: 2,
                display: 'flex',
                flexDirection: 'column',
                height: 150,
                backgroundColor: stat.color,
                color: 'white',
                borderRadius: 2,
                boxShadow: 3,
              }}
            >
              <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 1 }}>
                {stat.icon}
              </Box>
              <Box sx={{ flexGrow: 1 }}>
                <Typography variant="body2" gutterBottom>
                  {stat.title}
                </Typography>
                <Typography variant="h4" component="div">
                  {stat.value}
                </Typography>
              </Box>
            </Paper>
          </Grid>
        ))}
      </Grid>
      <Box sx={{ mt: 3 }}>
        <Grid container spacing={3}>
          {/* 销售趋势图 */}
          <Grid size={{ xs: 12, md: 8 }}>
            <Paper sx={{ p: 2, height: 400 }}>
              <Typography variant="h6" gutterBottom>
                销售趋势
              </Typography>
              <div ref={salesChartRef} style={{ width: '100%', height: '350px' }} />
            </Paper>
          </Grid>
          {/* 库存分布图 */}
          <Grid size={{ xs: 12, md: 4 }}>
            <Paper sx={{ p: 2, height: 400 }}>
              <Typography variant="h6" gutterBottom>
                库存分布
              </Typography>
              <div ref={inventoryChartRef} style={{ width: '100%', height: '350px' }} />
            </Paper>
          </Grid>
          {/* 采购报表 */}
          <Grid size={{ xs: 12 }}>
            <Paper sx={{ p: 2, height: 400 }}>
              <Typography variant="h6" gutterBottom>
                供应商采购金额
              </Typography>
              <div ref={procurementChartRef} style={{ width: '100%', height: '350px' }} />
            </Paper>
          </Grid>
        </Grid>
      </Box>
    </Box>
  );
};

export default Dashboard;
