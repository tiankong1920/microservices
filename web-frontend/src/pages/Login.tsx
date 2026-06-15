import React, { useState, useRef, useEffect, ChangeEvent, FormEvent } from 'react';
import { Box, Button, Container, TextField, Typography, Paper, Link } from '@mui/material';
import { LockOutlined as LockOutlinedIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useToast } from '../contexts/ToastContext';

interface Credentials {
  username: string;
  password: string;
}

const Login: React.FC = () => {
  const [credentials, setCredentials] = useState<Credentials>({
    username: '',
    password: ''
  });
  const { showSuccess, showError } = useToast();
  const [loading, setLoading] = useState<boolean>(false);
  const navigate = useNavigate();
  const navigateTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const { login: authLogin } = useAuth();

  useEffect(() => {
    return () => {
      if (navigateTimerRef.current !== null) {
        clearTimeout(navigateTimerRef.current);
      }
    };
  }, []);

  const handleChange = (e: ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setCredentials(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setLoading(true);

    try {
      await authLogin(credentials.username, credentials.password);
      showSuccess('登录成功');
      navigateTimerRef.current = setTimeout(() => navigate('/'), 1500);
    } catch (err) {
      showError('登录失败，请检查用户名和密码');
      console.error('Login error:', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container component="main" maxWidth="xs">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Paper elevation={3} sx={{ p: 4, width: '100%' }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 3 }}>
            <LockOutlinedIcon sx={{ fontSize: 40, color: '#1976d2' }} />
            <Typography component="h1" variant="h5" sx={{ mt: 2 }}>
              进销存管理系统
            </Typography>
          </Box>

          <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1 }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="username"
              label="用户名"
              name="username"
              autoComplete="username"
              autoFocus
              onChange={handleChange}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="密码"
              type="password"
              id="password"
              autoComplete="current-password"
              onChange={handleChange}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
              disabled={loading}
            >
              登录
            </Button>
            <Box sx={{ display: 'flex', justifyContent: 'center' }}>
              <Typography variant="body2">
                没有账号？ <Link href="/register" variant="body2">联系管理员</Link>
              </Typography>
            </Box>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default Login;