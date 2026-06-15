import React from 'react';
import { Box, Typography, Button, Container, Paper } from '@mui/material';
import { Inbox as InboxIcon, SearchOff as SearchOffIcon, ErrorOutline as ErrorIcon } from '@mui/icons-material';

export interface EmptyStateProps {
  type?: 'no-data' | 'no-results' | 'error' | 'loading';
  title?: string;
  description?: string;
  actionText?: string;
  onAction?: () => void;
  icon?: React.ReactNode;
}

const EmptyState: React.FC<EmptyStateProps> = ({
  type = 'no-data',
  title,
  description,
  actionText,
  onAction,
  icon,
}) => {
  const getDefaultContent = (): { title: string; description: string; icon: React.ReactNode } => {
    switch (type) {
      case 'no-data':
        return {
          title: title || '暂无数据',
          description: description || '当前没有可显示的数据',
          icon: icon || <InboxIcon sx={{ fontSize: 64, color: 'text.secondary' }} />,
        };
      case 'no-results':
        return {
          title: title || '未找到结果',
          description: description || '没有找到匹配的搜索结果',
          icon: icon || <SearchOffIcon sx={{ fontSize: 64, color: 'text.secondary' }} />,
        };
      case 'error':
        return {
          title: title || '加载失败',
          description: description || '数据加载失败，请稍后重试',
          icon: icon || <ErrorIcon sx={{ fontSize: 64, color: 'error' }} />,
        };
      default:
        return {
          title: title || '暂无数据',
          description: description || '当前没有可显示的数据',
          icon: icon || <InboxIcon sx={{ fontSize: 64, color: 'text.secondary' }} />,
        };
    }
  };

  const content = getDefaultContent();

  return (
    <Container maxWidth="md" sx={{ py: 8 }}>
      <Paper
        elevation={0}
        sx={{
          py: 8,
          px: 4,
          textAlign: 'center',
          borderRadius: 2,
        }}
      >
        <Box sx={{ mb: 3 }}>
          {content.icon}
        </Box>
        <Typography variant="h5" gutterBottom sx={{ fontWeight: 'medium' }}>
          {content.title}
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 4 }}>
          {content.description}
        </Typography>
        {actionText && onAction && (
          <Button variant="contained" onClick={onAction} size="large">
            {actionText}
          </Button>
        )}
      </Paper>
    </Container>
  );
};

export default EmptyState;