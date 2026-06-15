import React, { ReactNode } from 'react';
import { 
  Drawer, List, ListItem, ListItemButton, ListItemIcon, ListItemText, 
  AppBar, Toolbar, Typography, Box, IconButton, Menu, MenuItem, Divider
} from '@mui/material';
import { 
  Dashboard as DashboardIcon, Inventory2 as InventoryIcon, 
  ShoppingCart as ShoppingCartIcon, Store as StoreIcon, 
  TrendingUp as TrendingUpIcon, Settings as SettingsIcon,
  AccountCircle as AccountCircleIcon, Logout as LogoutIcon, 
  Menu as MenuIcon, People as PeopleIcon,
  BarChart as BarChartIcon, Business as BusinessIcon,
  LocalShipping as LocalShippingIcon, PointOfSale as PointOfSaleIcon,
  SwapHoriz as SwapHorizIcon, Receipt as ReceiptIcon,
  Payment as PaymentIcon, AccountBalance as AccountBalanceIcon,
  AttachMoney as AttachMoneyIcon
} from '@mui/icons-material';
import { Link, useNavigate } from 'react-router-dom';

const drawerWidth = 240;

interface LayoutProps {
  children: ReactNode;
}

interface MenuItem {
  text: string;
  icon: React.ReactElement;
  path: string;
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const [mobileOpen, setMobileOpen] = React.useState(false);
  const navigate = useNavigate();
  
  const [user] = React.useState(() => {
    try {
      const raw = localStorage.getItem('user');
      return raw ? JSON.parse(raw) : { username: '管理员' };
    } catch {
      return { username: '管理员' };
    }
  });

  const menuItems: MenuItem[] = [
    { text: '仪表盘', icon: <DashboardIcon />, path: '/' },
    { text: '产品管理', icon: <StoreIcon />, path: '/products' },
    { text: '客户管理', icon: <PeopleIcon />, path: '/customers' },
    { text: '供应商管理', icon: <BusinessIcon />, path: '/suppliers' },
    { text: '库存管理', icon: <InventoryIcon />, path: '/inventory' },
    { text: '订单管理', icon: <ShoppingCartIcon />, path: '/orders' },
    { text: '采购管理', icon: <LocalShippingIcon />, path: '/procurement' },
    { text: '销售管理', icon: <TrendingUpIcon />, path: '/sales' },
    { text: '零售管理', icon: <PointOfSaleIcon />, path: '/retail' },
    { text: '销售退货', icon: <SwapHorizIcon />, path: '/sales-return' },
    { text: '其他出入库', icon: <InventoryIcon />, path: '/other-stock' },
    { text: '库存调拨', icon: <SwapHorizIcon />, path: '/stock-transfers' },
    { text: '采购退货', icon: <LocalShippingIcon />, path: '/procurement-returns' },
    { text: '收款管理', icon: <ReceiptIcon />, path: '/receipts' },
    { text: '付款管理', icon: <PaymentIcon />, path: '/payments' },
    { text: '收入管理', icon: <AttachMoneyIcon />, path: '/incomes' },
    { text: '结算账户', icon: <AccountBalanceIcon />, path: '/settlement-accounts' },
    { text: '销售报表', icon: <BarChartIcon />, path: '/reports/sales' },
    { text: '采购报表', icon: <BarChartIcon />, path: '/reports/purchase' },
    { text: '库存报表', icon: <BarChartIcon />, path: '/reports/inventory' },
    { text: '财务报表', icon: <BarChartIcon />, path: '/reports/financial' },
    { text: '系统管理', icon: <SettingsIcon />, path: '/system' },
  ];

  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleLogout = () => {
    // 清除localStorage中的token和用户信息
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    // 跳转到登录页面
    navigate('/login');
  };

  // 处理移动端抽屉开关
  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  return (
    <Box sx={{ display: 'flex' }}>
      <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
        <Toolbar>
          {/* 移动端汉堡菜单 */}
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: 'none' } }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1, fontSize: { xs: '1.25rem', sm: '1.5rem' } }}>
            进销存管理系统
          </Typography>
          {/* 用户信息和菜单 */}
          <Box sx={{ display: 'flex', alignItems: 'center', gap: { xs: 1, sm: 2 } }}>
            <Typography variant="body2" noWrap sx={{ display: { xs: 'none', sm: 'block' } }}>
              欢迎, {user.username}
            </Typography>
            <IconButton
              size="large"
              aria-label="account of current user"
              aria-controls="menu-appbar"
              aria-haspopup="true"
              onClick={handleMenu}
              color="inherit"
            >
              <AccountCircleIcon />
            </IconButton>
            <Menu
              id="menu-appbar"
              anchorEl={anchorEl}
              anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
              keepMounted
              transformOrigin={{ vertical: 'top', horizontal: 'right' }}
              open={Boolean(anchorEl)}
              onClose={handleClose}
            >
              <MenuItem onClick={handleClose}>
                <AccountCircleIcon sx={{ mr: 1 }} />
                <Typography variant="inherit">个人中心</Typography>
              </MenuItem>
              <MenuItem onClick={handleClose}>
                <SettingsIcon sx={{ mr: 1 }} />
                <Typography variant="inherit">系统设置</Typography>
              </MenuItem>
              <Divider />
              <MenuItem onClick={handleLogout}>
                <LogoutIcon sx={{ mr: 1 }} />
                <Typography variant="inherit">登出</Typography>
              </MenuItem>
            </Menu>
          </Box>
        </Toolbar>
      </AppBar>
      
      {/* 移动端临时抽屉 */}
      <Box
        component="nav"
        sx={{ width: { sm: drawerWidth }, flexShrink: { sm: 0 } }}
        aria-label="mailbox folders"
      >
        <Drawer
          variant="temporary"
          open={mobileOpen}
          onClose={handleDrawerToggle}
          ModalProps={{
            keepMounted: true, // Better open performance on mobile.
          }}
          sx={{
            display: { xs: 'block', sm: 'none' },
            '& .MuiDrawer-paper': {
              boxSizing: 'border-box',
              width: drawerWidth,
            },
          }}
        >
          <Toolbar />
          <Box sx={{ overflow: 'auto' }}>
            <List>
              {menuItems.map((item) => (
                <ListItem key={item.text} disablePadding>
                  <ListItemButton component={Link} to={item.path} onClick={() => setMobileOpen(false)}>
                    <ListItemIcon>
                      {item.icon}
                    </ListItemIcon>
                    <ListItemText primary={item.text} />
                  </ListItemButton>
                </ListItem>
              ))}
            </List>
          </Box>
        </Drawer>
      </Box>
      
      {/* 主内容区域 */}
      <Box component="main" sx={{ 
        flexGrow: 1, 
        p: { xs: 1, sm: 3 },
        width: { xs: '100%', sm: `calc(100% - ${drawerWidth}px)` }
      }}>
        <Toolbar />
        {children}
      </Box>
    </Box>
  );
};

export default Layout;
