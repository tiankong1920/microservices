import React from 'react';
import { Box, Pagination as MuiPagination, PaginationItem, Stack, Typography } from '@mui/material';
import { ArrowBack as ArrowBackIcon, ArrowForward as ArrowForwardIcon, FirstPage as FirstPageIcon, LastPage as LastPageIcon } from '@mui/icons-material';

export interface PaginationProps {
  count: number;
  page: number;
  rowsPerPage: number;
  onPageChange: (event: React.ChangeEvent<unknown>, newPage: number) => void;
  onRowsPerPageChange: (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => void;
  rowsPerPageOptions?: number[];
  showFirstButton?: boolean;
  showLastButton?: boolean;
  disabled?: boolean;
}

const Pagination: React.FC<PaginationProps> = ({
  count,
  page,
  rowsPerPage,
  onPageChange,
  onRowsPerPageChange: _onRowsPerPageChange,
  rowsPerPageOptions: _rowsPerPageOptions = [10, 25, 50, 100],
  showFirstButton = true,
  showLastButton = true,
  disabled = false,
}) => {
  const handleChangePage = (event: React.ChangeEvent<unknown>, newPage: number): void => {
    onPageChange(event, newPage);
  };

  return (
    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', mt: 2, mb: 2 }}>
      <Stack spacing={2} alignItems="center">
        <Typography variant="body2" sx={{ mr: 2 }}>
          第 {page + 1} 页，共 {Math.ceil(count / rowsPerPage)} 页，总计 {count} 条
        </Typography>
        <MuiPagination
          count={Math.ceil(count / rowsPerPage)}
          page={page + 1}
          onChange={(event, newPage) => handleChangePage(event, newPage - 1)}
          disabled={disabled}
          showFirstButton={showFirstButton}
          showLastButton={showLastButton}
          getItemAriaLabel={(type) => {
            switch (type) {
              case 'first':
                return '第一页';
              case 'last':
                return '最后一页';
              case 'next':
                return '下一页';
              case 'previous':
                return '上一页';
              default:
                return '';
            }
          }}
          renderItem={(item) => (
            <PaginationItem
              {...item}
              components={{
                first: FirstPageIcon,
                last: LastPageIcon,
                next: ArrowForwardIcon,
                previous: ArrowBackIcon,
              }}
            />
          )}
        />
      </Stack>
    </Box>
  );
};

export default Pagination;