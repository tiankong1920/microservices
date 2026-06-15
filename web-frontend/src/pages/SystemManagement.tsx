import React, { useState, useEffect } from 'react';
import { Typography, Box, TextField, Button, Grid, Card, CardContent, Switch, FormControlLabel, Divider, CircularProgress } from '@mui/material';
import { Save as SaveIcon, Refresh as RefreshIcon, Settings as SettingsIcon, Storage as StorageIcon, Security as SecurityIcon } from '@mui/icons-material';
import Toast from '../components/Toast';
import ConfirmDialog from '../components/ConfirmDialog';
import { systemConfigApi } from '../services/api';

interface SystemConfig {
  systemName: string;
  companyName: string;
  timezone: string;
  dateFormat: string;
  currency: string;
  language: string;
  enableAuditLog: boolean;
  enableDataBackup: boolean;
  backupFrequency: string;
  maxFileSize: number;
  sessionTimeout: number;
  passwordComplexity: boolean;
  twoFactorAuth: boolean;
}

const STORAGE_KEY = 'systemConfig';

const DEFAULT_CONFIG: SystemConfig = {
  systemName: '进销存管理系统',
  companyName: '示例公司',
  timezone: 'Asia/Shanghai',
  dateFormat: 'YYYY-MM-DD',
  currency: 'CNY',
  language: 'zh-CN',
  enableAuditLog: true,
  enableDataBackup: true,
  backupFrequency: 'daily',
  maxFileSize: 10,
  sessionTimeout: 30,
  passwordComplexity: true,
  twoFactorAuth: false,
};

const loadFromLocalStorage = (): SystemConfig | null => {
  try {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      return JSON.parse(stored) as SystemConfig;
    }
  } catch {
    // ignore parse errors
  }
  return null;
};

const saveToLocalStorage = (config: SystemConfig): void => {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(config));
  } catch {
    // ignore storage errors
  }
};

const SystemManagement: React.FC = () => {
  const [config, setConfig] = useState<SystemConfig>(DEFAULT_CONFIG);
  const [loading, setLoading] = useState<boolean>(true);
  const [saving, setSaving] = useState<boolean>(false);
  const [toast, setToast] = useState<{ open: boolean; message: string; severity: 'success' | 'error' | 'info' | 'warning' }>({ open: false, message: '', severity: 'info' });
  const [resetDialogOpen, setResetDialogOpen] = useState<boolean>(false);

  // Load config on mount: localStorage first, then try API
  useEffect(() => {
    // Instant load from localStorage
    const local = loadFromLocalStorage();
    if (local) {
      setConfig(local);
    }

    // Try API fetch in background
    systemConfigApi.getSettings()
      .then((res) => {
        if (res.data) {
          const apiConfig: SystemConfig = {
            systemName: (res.data.systemName as string) ?? DEFAULT_CONFIG.systemName,
            companyName: (res.data.companyName as string) ?? DEFAULT_CONFIG.companyName,
            timezone: (res.data.timezone as string) ?? DEFAULT_CONFIG.timezone,
            dateFormat: (res.data.dateFormat as string) ?? DEFAULT_CONFIG.dateFormat,
            currency: (res.data.currency as string) ?? DEFAULT_CONFIG.currency,
            language: (res.data.language as string) ?? DEFAULT_CONFIG.language,
            enableAuditLog: (res.data.enableAuditLog as boolean) ?? DEFAULT_CONFIG.enableAuditLog,
            enableDataBackup: (res.data.enableDataBackup as boolean) ?? DEFAULT_CONFIG.enableDataBackup,
            backupFrequency: (res.data.backupFrequency as string) ?? DEFAULT_CONFIG.backupFrequency,
            maxFileSize: (res.data.maxFileSize as number) ?? DEFAULT_CONFIG.maxFileSize,
            sessionTimeout: (res.data.sessionTimeout as number) ?? DEFAULT_CONFIG.sessionTimeout,
            passwordComplexity: (res.data.passwordComplexity as boolean) ?? DEFAULT_CONFIG.passwordComplexity,
            twoFactorAuth: (res.data.twoFactorAuth as boolean) ?? DEFAULT_CONFIG.twoFactorAuth,
          };
          setConfig(apiConfig);
          saveToLocalStorage(apiConfig);
        }
      })
      .catch(() => {
        // API not available — localStorage data is already loaded (or defaults)
      })
      .finally(() => {
        setLoading(false);
      });
  }, []);

  const handleSave = async (): Promise<void> => {
    setSaving(true);

    // Persist to localStorage immediately
    saveToLocalStorage(config);

    // Try API save in background
    try {
      await systemConfigApi.updateSettings(config as unknown as Record<string, unknown>);
      setToast({ open: true, message: '系统配置保存成功', severity: 'success' });
    } catch {
      // API not available — data is safe in localStorage
      setToast({ open: true, message: '已保存到本地，服务器同步失败', severity: 'warning' });
    } finally {
      setSaving(false);
      setTimeout(() => setToast((prev) => ({ ...prev, open: false })), 3000);
    }
  };

  const handleReset = (): void => {
    setResetDialogOpen(true);
  };

  const confirmReset = (): void => {
    setConfig(DEFAULT_CONFIG);
    saveToLocalStorage(DEFAULT_CONFIG);
    setToast({ open: true, message: '配置已重置为默认值', severity: 'success' });
    setResetDialogOpen(false);
    setTimeout(() => setToast((prev) => ({ ...prev, open: false })), 3000);
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 400 }}>
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

      <ConfirmDialog
        open={resetDialogOpen}
        title="确认重置"
        content="确定要重置为默认配置吗？此操作将覆盖当前所有设置。"
        confirmText="重置"
        cancelText="取消"
        severity="warning"
        onConfirm={confirmReset}
        onCancel={() => setResetDialogOpen(false)}
      />

      <Typography variant="h4" gutterBottom>
        系统管理
      </Typography>

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                <SettingsIcon color="primary" />
                <Typography variant="h6">基本设置</Typography>
              </Box>
              <Divider sx={{ mb: 2 }} />
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <TextField
                  label="系统名称"
                  fullWidth
                  value={config.systemName}
                  onChange={(e) => setConfig({ ...config, systemName: e.target.value })}
                />
                <TextField
                  label="公司名称"
                  fullWidth
                  value={config.companyName}
                  onChange={(e) => setConfig({ ...config, companyName: e.target.value })}
                />
                <TextField
                  label="时区"
                  fullWidth
                  select
                  value={config.timezone}
                  onChange={(e) => setConfig({ ...config, timezone: e.target.value })}
                >
                  <option value="Asia/Shanghai">中国标准时间 (UTC+8)</option>
                  <option value="Asia/Tokyo">日本标准时间 (UTC+9)</option>
                  <option value="America/New_York">美国东部时间 (UTC-5)</option>
                  <option value="Europe/London">格林威治时间 (UTC+0)</option>
                </TextField>
                <TextField
                  label="日期格式"
                  fullWidth
                  select
                  value={config.dateFormat}
                  onChange={(e) => setConfig({ ...config, dateFormat: e.target.value })}
                >
                  <option value="YYYY-MM-DD">YYYY-MM-DD</option>
                  <option value="DD/MM/YYYY">DD/MM/YYYY</option>
                  <option value="MM/DD/YYYY">MM/DD/YYYY</option>
                </TextField>
                <TextField
                  label="货币"
                  fullWidth
                  select
                  value={config.currency}
                  onChange={(e) => setConfig({ ...config, currency: e.target.value })}
                >
                  <option value="CNY">人民币 (CNY)</option>
                  <option value="USD">美元 (USD)</option>
                  <option value="EUR">欧元 (EUR)</option>
                  <option value="JPY">日元 (JPY)</option>
                </TextField>
                <TextField
                  label="语言"
                  fullWidth
                  select
                  value={config.language}
                  onChange={(e) => setConfig({ ...config, language: e.target.value })}
                >
                  <option value="zh-CN">简体中文</option>
                  <option value="zh-TW">繁体中文</option>
                  <option value="en-US">English</option>
                  <option value="ja-JP">日本語</option>
                </TextField>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                <StorageIcon color="primary" />
                <Typography variant="h6">数据设置</Typography>
              </Box>
              <Divider sx={{ mb: 2 }} />
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={config.enableAuditLog}
                      onChange={(e) => setConfig({ ...config, enableAuditLog: e.target.checked })}
                    />
                  }
                  label="启用审计日志"
                />
                <FormControlLabel
                  control={
                    <Switch
                      checked={config.enableDataBackup}
                      onChange={(e) => setConfig({ ...config, enableDataBackup: e.target.checked })}
                    />
                  }
                  label="启用数据备份"
                />
                <TextField
                  label="备份频率"
                  fullWidth
                  select
                  value={config.backupFrequency}
                  onChange={(e) => setConfig({ ...config, backupFrequency: e.target.value })}
                  disabled={!config.enableDataBackup}
                >
                  <option value="hourly">每小时</option>
                  <option value="daily">每天</option>
                  <option value="weekly">每周</option>
                  <option value="monthly">每月</option>
                </TextField>
                <TextField
                  label="最大文件大小 (MB)"
                  fullWidth
                  type="number"
                  value={config.maxFileSize}
                  onChange={(e) => setConfig({ ...config, maxFileSize: Number(e.target.value) })}
                />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 6 }}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                <SecurityIcon color="primary" />
                <Typography variant="h6">安全设置</Typography>
              </Box>
              <Divider sx={{ mb: 2 }} />
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={config.passwordComplexity}
                      onChange={(e) => setConfig({ ...config, passwordComplexity: e.target.checked })}
                    />
                  }
                  label="启用密码复杂度验证"
                />
                <FormControlLabel
                  control={
                    <Switch
                      checked={config.twoFactorAuth}
                      onChange={(e) => setConfig({ ...config, twoFactorAuth: e.target.checked })}
                    />
                  }
                  label="启用双因素认证"
                />
                <TextField
                  label="会话超时时间 (分钟)"
                  fullWidth
                  type="number"
                  value={config.sessionTimeout}
                  onChange={(e) => setConfig({ ...config, sessionTimeout: Number(e.target.value) })}
                />
              </Box>
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12 }}>
          <Box sx={{ display: 'flex', gap: 2, justifyContent: 'flex-end', mt: 2 }}>
            <Button
              variant="outlined"
              startIcon={<RefreshIcon />}
              onClick={handleReset}
            >
              重置默认
            </Button>
            <Button
              variant="contained"
              startIcon={<SaveIcon />}
              onClick={handleSave}
              disabled={saving}
            >
              {saving ? '保存中...' : '保存配置'}
            </Button>
          </Box>
        </Grid>
      </Grid>
    </Box>
  );
};

export default SystemManagement;