import React from 'react';
import { Box, Skeleton as MuiSkeleton, Stack, Typography, Paper, Grid } from '@mui/material';

export interface SkeletonProps {
  type?: 'list' | 'table' | 'card' | 'form';
  count?: number;
  rows?: number;
  columns?: number;
  height?: number;
  width?: number;
}

const Skeleton: React.FC<SkeletonProps> = ({
  type = 'list',
  count = 5,
  rows = 5,
  columns = 5,
  height = 40,
  width = '100%',
}) => {
  const renderListSkeleton = (): React.ReactNode => {
    return (
      <Stack spacing={2}>
        {Array.from({ length: count }).map((_, index) => (
          <Box key={index} sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <MuiSkeleton variant="circular" width={40} height={40} />
            <Box sx={{ flex: 1 }}>
              <MuiSkeleton variant="text" width="60%" height={20} />
              <MuiSkeleton variant="text" width="40%" height={16} sx={{ mt: 1 }} />
            </Box>
          </Box>
        ))}
      </Stack>
    );
  };

  const renderTableSkeleton = (): React.ReactNode => {
    return (
      <Box>
        <MuiSkeleton variant="rectangular" width="100%" height={60} sx={{ mb: 1 }} />
        {Array.from({ length: rows }).map((_, rowIndex) => (
          <Box key={rowIndex} sx={{ display: 'flex', gap: 1, mb: 1 }}>
            {Array.from({ length: columns }).map((_, colIndex) => (
              <MuiSkeleton
                key={`${rowIndex}-${colIndex}`}
                variant="rectangular"
                width={width}
                height={height}
              />
            ))}
          </Box>
        ))}
      </Box>
    );
  };

  const renderCardSkeleton = (): React.ReactNode => {
    return (
      <Grid container spacing={3}>
        {Array.from({ length: count }).map((_, index) => (
          <Grid size={{ xs: 12, sm: 6, md: 4 }} key={index}>
            <Paper elevation={1} sx={{ p: 2 }}>
              <MuiSkeleton variant="rectangular" width="100%" height={200} sx={{ mb: 2 }} />
              <MuiSkeleton variant="text" width="80%" height={24} sx={{ mb: 1 }} />
              <MuiSkeleton variant="text" width="60%" height={20} sx={{ mb: 1 }} />
              <MuiSkeleton variant="text" width="40%" height={20} />
            </Paper>
          </Grid>
        ))}
      </Grid>
    );
  };

  const renderFormSkeleton = (): React.ReactNode => {
    return (
      <Stack spacing={3}>
        <MuiSkeleton variant="rectangular" width="100%" height={60} />
        <Grid container spacing={3}>
          <Grid size={{ xs: 12, md: 6 }}>
            <MuiSkeleton variant="rectangular" width="100%" height={56} />
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <MuiSkeleton variant="rectangular" width="100%" height={56} />
          </Grid>
        </Grid>
        <Grid container spacing={3}>
          <Grid size={{ xs: 12 }}>
            <MuiSkeleton variant="rectangular" width="100%" height={56} />
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <MuiSkeleton variant="rectangular" width="100%" height={56} />
          </Grid>
          <Grid size={{ xs: 12, md: 6 }}>
            <MuiSkeleton variant="rectangular" width="100%" height={56} />
          </Grid>
        </Grid>
        <MuiSkeleton variant="rectangular" width="100%" height={120} />
        <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2, mt: 2 }}>
          <MuiSkeleton variant="rectangular" width={100} height={36} />
          <MuiSkeleton variant="rectangular" width={100} height={36} />
        </Box>
      </Stack>
    );
  };

  const renderContent = (): React.ReactNode => {
    switch (type) {
      case 'list':
        return renderListSkeleton();
      case 'table':
        return renderTableSkeleton();
      case 'card':
        return renderCardSkeleton();
      case 'form':
        return renderFormSkeleton();
      default:
        return renderListSkeleton();
    }
  };

  return (
    <Box>
      <Typography variant="h6" gutterBottom sx={{ fontWeight: 'medium' }}>
        加载中...
      </Typography>
      {renderContent()}
    </Box>
  );
};

export default Skeleton;