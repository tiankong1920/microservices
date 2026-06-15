import React, { useState } from 'react';
import { Box, TextField, Stack, Typography, IconButton } from '@mui/material';
import { Clear as ClearIcon } from '@mui/icons-material';

export interface DateRangePickerProps {
  startDate?: string;
  endDate?: string;
  onStartDateChange: (date: string) => void;
  onEndDateChange: (date: string) => void;
  onClear?: () => void;
  label?: string;
  disabled?: boolean;
}

const DateRangePicker: React.FC<DateRangePickerProps> = ({
  startDate,
  endDate,
  onStartDateChange,
  onEndDateChange,
  onClear,
  label = '日期范围',
  disabled = false,
}) => {
  const [startDateInput, setStartDateInput] = useState<string>(startDate || '');
  const [endDateInput, setEndDateInput] = useState<string>(endDate || '');

  const handleStartDateChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const value = e.target.value;
    setStartDateInput(value);
    onStartDateChange(value);
  };

  const handleEndDateChange = (e: React.ChangeEvent<HTMLInputElement>): void => {
    const value = e.target.value;
    setEndDateInput(value);
    onEndDateChange(value);
  };

  const handleClear = (): void => {
    setStartDateInput('');
    setEndDateInput('');
    onStartDateChange('');
    onEndDateChange('');
    onClear?.();
  };

  return (
    <Box>
      <Typography variant="subtitle2" gutterBottom sx={{ fontWeight: 'medium' }}>
        {label}
      </Typography>
      <Stack direction="row" spacing={2} alignItems="center">
        <TextField
          type="date"
          label="开始日期"
          value={startDateInput}
          onChange={handleStartDateChange}
          disabled={disabled}
          InputLabelProps={{ shrink: true }}
          fullWidth
        />
        <TextField
          type="date"
          label="结束日期"
          value={endDateInput}
          onChange={handleEndDateChange}
          disabled={disabled}
          InputLabelProps={{ shrink: true }}
          fullWidth
        />
        {(startDateInput || endDateInput) && onClear && (
          <IconButton onClick={handleClear} color="error" title="清除日期">
            <ClearIcon />
          </IconButton>
        )}
      </Stack>
    </Box>
  );
};

export default DateRangePicker;