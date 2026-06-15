const fs = require('fs');
const p = 'microservices/project-root/core-services/order-service/src/test/java/com/inventory/orderservice/service/impl/OrderServiceImplTest.java';
fs.appendFileSync(p, 
    @BeforeEach
    void setUp() {
        testOrderItems = new ArrayList<>();
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setProductId(100L);
        item.setProductName('Test Product');
        item.setQuantity(10);
        item.setUnitPrice(100.0);
        item.setCreatedAt(LocalDateTime.now());
        testOrderItems.add(item);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNumber('ORD-2024-0001');
        testOrder.setCustomerName('Test Customer');
        testOrder.setStatus(Order.Status.PENDING);
        testOrder.setItems(testOrderItems);
        testOrder.setCreatedAt(LocalDateTime.now());
        testOrder.setUpdatedAt(LocalDateTime.now());
        testOrderItems.forEach(i -> i.setOrder(testOrder));

        testOrderItemDTOs = new ArrayList<>();
        OrderItemDTO itemDTO = new OrderItemDTO();
        itemDTO.setProductId(100L);
        itemDTO.setProductName('Test Product');
        itemDTO.setQuantity(10);
        itemDTO.setUnitPrice(100.0);
        testOrderItemDTOs.add(itemDTO);

        testOrderDTO = new OrderDTO();
        testOrderDTO.setId(1L);
        testOrderDTO.setOrderNumber('ORD-2024-0001');
        testOrderDTO.setCustomerName('Test Customer');
        testOrderDTO.setStatus(Order.Status.PENDING);
        testOrderDTO.setItems(testOrderItemDTOs);
    }
);console.log('done');