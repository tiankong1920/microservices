import React, { useState, useEffect } from 'react';
import { Typography, Box, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Button, TextField, IconButton, Tooltip, Modal, FormControl, InputLabel, Select, MenuItem, InputAdornment, CircularProgress, Alert, Backdrop, Chip } from '@mui/material';
import { Search as SearchIcon, Sync as SyncIcon, Edit as EditIcon, SwapVert as SwapVertIcon } from '@mui/icons-material';
import { inventoryApi } from '../services/api';
import type { Inventory } from '../types';

const InventoryManagement: React.FC = () => {
  const [inventories, setInventories] = useState<Inventory[]>([]);
  const [searchTerm, setSearchTerm] = useState<string>('');
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string>('');
  
  const [adjustmentModalOpen, setAdjustmentModalOpen] = useState<boolean>(false);
  const [selectedInventory, setSelectedInventory] = useState<Inventory | null>(null);
  const [adjustmentQuantity, setAdjustmentQuantity] = useState<number>(0);
  const [adjustmentReason, setAdjustmentReason] = useState<string>('');
  
  const [transferModalOpen, setTransferModalOpen] = useState<boolean>(false);
  const [transferFrom, setTransferFrom] = useState<Inventory | null>(null);
  const [transferTo, setTransferTo] = useState<string>('');
  const [transferQuantity, setTransferQuantity] = useState<number>(0);
  const [transferReason, setTransferReason] = useState<string>('');
  
  const warehouses: string[] = ['仓库1', '仓库2', '仓库3', '仓库4', '仓库5'];

  const loadInventory = async (): Promise<void> => {
    setLoading(true);
    setError(null);
    try {
      const response = await inventoryApi.getAllInventory();
      setInventories(response.data);
    } catch (err) {
      console.error('Failed to load inventory:', err);
      setError('加载库存数据失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInventory();
  }, []);

  const handleRefresh = (): void => {
    loadInventory();
  };

  const filteredInventories = inventories.filter((inventory: Inventory) => 
    inventory.productName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    inventory.sku?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    inventory.warehouse?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    inventory.batchNumber?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleOpenAdjustmentModal = (inventory: Inventory): void => {
    setSelectedInventory(inventory);
    setAdjustmentQuantity(0);
    setAdjustmentReason('');
    setAdjustmentModalOpen(true);
  };

  const handleCloseAdjustmentModal = (): void => {
    setAdjustmentModalOpen(false);
  };

  const handleSaveAdjustment = async (): Promise<void> => {
    if (!selectedInventory) return;
    
    setLoading(true);
    setError(null);
    try {
      await inventoryApi.adjustInventory(selectedInventory.id, {
        quantity: adjustmentQuantity,
        reason: adjustmentReason
      });
      setSuccess('库存调整成功');
      setAdjustmentModalOpen(false);
      loadInventory();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      console.error('Failed to adjust inventory:', err);
      setError('库存调整失败');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenTransferModal = (inventory: Inventory): void => {
    setTransferFrom(inventory);
    setTransferTo('');
    setTransferQuantity(0);
    setTransferReason('');
    setTransferModalOpen(true);
  };

  const handleCloseTransferModal = (): void => {
    setTransferModalOpen(false);
  };

  const handleSaveTransfer = async (): Promise<void> => {
    if (!transferFrom) return;
    
    setLoading(true);
    setError(null);
    try {
      await inventoryApi.transferInventory({
        fromInventoryId: transferFrom.id,
        toWarehouseId: transferTo,
        quantity: transferQuantity,
        reason: transferReason
      });
      setSuccess('库存调拨成功');
      setTransferModalOpen(false);
      loadInventory();
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      console.error('Failed to transfer inventory:', err);
      setError('库存调拨失败');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status: string): 'success' | 'warning' | 'error' => {
    switch (status) {
      case '正常': return 'success';
      case '预警': return 'warning';
      case '缺货': return 'error';
      default: return 'success';
    }
  };

  return (
    <Box>
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}
      
      {success && (
        <Alert severity="success" sx={{ mb: 3 }}>
          {success}
        </Alert>
      )}
      
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h4" gutterBottom>
          库存管理
        </Typography>
        <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
          <TextField
            placeholder="搜索产品名称、SKU、仓库或批次号..."
            variant="outlined"
            size="small"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              endAdornment: <SearchIcon />,
            }}
            sx={{ minWidth: 300 }}
          />
          <Button
            variant="outlined"
            startIcon={<SyncIcon />}
            onClick={handleRefresh}
          >
            刷新库存
          </Button>
          <Button
            variant="contained"
            startIcon={<SwapVertIcon />}
            onClick={() => inventories[0] && handleOpenTransferModal(inventories[0])}
          >
            库存调拨
          </Button>
        </Box>
      </Box>
      
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: 200 }}>
          <CircularProgress />
        </Box>
      ) : (
        <Box sx={{ overflowX: 'auto' }}>
          <TableContainer component={Paper} sx={{ minWidth: '100%' }}>
            <Table sx={{ minWidth: 800 }} aria-label="inventory table">
              <TableHead>
                <TableRow>
                  <TableCell>产品名称</TableCell>
                  <TableCell>SKU</TableCell>
                  <TableCell>仓库</TableCell>
                  <TableCell>批次号</TableCell>
                  <TableCell>库存数量</TableCell>
                  <TableCell>单位</TableCell>
                  <TableCell>状态</TableCell>
                  <TableCell>操作</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {filteredInventories.map((inventory: Inventory) => (
                  <TableRow
                    key={inventory.id}
                    sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                    hover
                  >
                    <TableCell>{inventory.productName}</TableCell>
                    <TableCell>{inventory.sku}</TableCell>
                    <TableCell>{inventory.warehouse}</TableCell>
                    <TableCell>{inventory.batchNumber}</TableCell>
                    <TableCell>{inventory.quantity}</TableCell>
                    <TableCell>{inventory.unit}</TableCell>
                    <TableCell>
                      <Chip 
                        label={inventory.status || '正常'} 
                        color={getStatusColor(inventory.status || '正常')} 
                        size="small" 
                      />
                    </TableCell>
                    <TableCell>
                      <Tooltip title="库存调整">
                        <IconButton size="small" onClick={() => handleOpenAdjustmentModal(inventory)}>
                          <EditIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="库存调拨">
                        <IconButton size="small" onClick={() => handleOpenTransferModal(inventory)}>
                          <SwapVertIcon />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </Box>
      )}
      
      <Modal
        open={adjustmentModalOpen}
        onClose={handleCloseAdjustmentModal}
        aria-labelledby="inventory-adjustment-modal"
        sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}
      >
        <Paper sx={{ p: 4, width: 500, maxWidth: '100%' }}>
          <Typography variant="h5" gutterBottom>
            库存调整
          </Typography>
          {selectedInventory && (
            <Box sx={{ mb: 3 }}>
              <Typography variant="body1">
                产品：{selectedInventory.productName} ({selectedInventory.sku})
              </Typography>
              <Typography variant="body1">
                仓库：{selectedInventory.warehouse}
              </Typography>
              <Typography variant="body1">
                当前库存：{selectedInventory.quantity} {selectedInventory.unit}
              </Typography>
            </Box>
          )}
          <TextField
            fullWidth
            label="调整数量"
            type="number"
            value={adjustmentQuantity}
            onChange={(e) => setAdjustmentQuantity(Number(e.target.value))}
            InputProps={{
              startAdornment: <InputAdornment position="start">{adjustmentQuantity >= 0 ? '+' : ''}</InputAdornment>,
            }}
            sx={{ mb: 2 }}
          />
          <TextField
            fullWidth
            label="调整原因"
            multiline
            rows={3}
            value={adjustmentReason}
            onChange={(e) => setAdjustmentReason(e.target.value)}
            sx={{ mb: 3 }}
          />
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
            <Button onClick={handleCloseAdjustmentModal}>取消</Button>
            <Button variant="contained" onClick={handleSaveAdjustment}>保存</Button>
          </Box>
        </Paper>
      </Modal>
      
      <Modal
        open={transferModalOpen}
        onClose={handleCloseTransferModal}
        aria-labelledby="inventory-transfer-modal"
        sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center' }}
      >
        <Paper sx={{ p: 4, width: 500, maxWidth: '100%' }}>
          <Typography variant="h5" gutterBottom>
            库存调拨
          </Typography>
          {transferFrom && (
            <Box sx={{ mb: 3 }}>
              <Typography variant="body1">
                产品：{transferFrom.productName} ({transferFrom.sku})
              </Typography>
              <Typography variant="body1">
                当前仓库：{transferFrom.warehouse}
              </Typography>
              <Typography variant="body1">
                当前库存：{transferFrom.quantity} {transferFrom.unit}
              </Typography>
            </Box>
          )}
          <FormControl fullWidth sx={{ mb: 2 }}>
            <InputLabel>目标仓库</InputLabel>
            <Select
              value={transferTo}
              label="目标仓库"
              onChange={(e) => setTransferTo(e.target.value)}
            >
              {warehouses.filter(warehouse => warehouse !== transferFrom?.warehouse).map((warehouse) => (
                <MenuItem key={warehouse} value={warehouse}>
                  {warehouse}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            fullWidth
            label="调拨数量"
            type="number"
            value={transferQuantity}
            onChange={(e) => setTransferQuantity(Number(e.target.value))}
            sx={{ mb: 2 }}
          />
          <TextField
            fullWidth
            label="调拨原因"
            multiline
            rows={3}
            value={transferReason}
            onChange={(e) => setTransferReason(e.target.value)}
            sx={{ mb: 3 }}
          />
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 2 }}>
            <Button onClick={handleCloseTransferModal}>取消</Button>
            <Button variant="contained" onClick={handleSaveTransfer}>保存</Button>
          </Box>
        </Paper>
      </Modal>
      
      <Backdrop
        sx={{ color: '#fff', zIndex: (theme) => theme.zIndex.drawer + 1 }}
        open={loading}
      >
        <CircularProgress color="inherit" />
      </Backdrop>
    </Box>
  );
};

export default InventoryManagement;
