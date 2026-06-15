# UAT Test Plan - Inventory Management System v3.0

## Test Scope

| Module | Test Cases | Priority |
|--------|------------|----------|
| Authentication | 12 | P0 |
| Product Management | 15 | P0 |
| Order Processing | 20 | P0 |
| Inventory Management | 18 | P0 |
| Procurement | 16 | P1 |
| Business Partner | 12 | P1 |
| Frontend | 10 | P2 |

---

## P0 - Critical Path Tests

### 1. Authentication Module

| TC ID | Test Case | Steps | Expected Result |
|-------|-----------|-------|------------------|
| AUTH-01 | User Login | 1. Navigate to login page<br>2. Enter valid credentials<br>3. Click login | Redirect to dashboard, JWT token stored |
| AUTH-02 | Invalid Login | 1. Enter invalid credentials<br>2. Click login | Error message displayed, no token stored |
| AUTH-03 | Token Refresh | 1. Login<br>2. Wait for token expiry<br>3. Make API request | Token automatically refreshed |
| AUTH-04 | Logout | 1. Login<br>2. Click logout | Redirect to login, token cleared |

### 2. Product Management

| TC ID | Test Case | Steps | Expected Result |
|-------|-----------|-------|------------------|
| PROD-01 | Create Product | 1. Navigate to products<br>2. Click add new<br>3. Fill form<br>4. Submit | Product created, appears in list |
| PROD-02 | Search Products | 1. Enter search keyword<br>2. Click search | Matching products displayed |
| PROD-03 | Update Product | 1. Select product<br>2. Click edit<br>3. Modify fields<br>4. Save | Changes persisted |
| PROD-04 | Delete Product | 1. Select product<br>2. Click delete<br>3. Confirm | Product removed from list |

### 3. Order Processing

| TC ID | Test Case | Steps | Expected Result |
|-------|-----------|-------|------------------|
| ORD-01 | Create Order | 1. Navigate to orders<br>2. Click create<br>3. Add items<br>4. Submit | Order created with PENDING status |
| ORD-02 | Process Order | 1. Select PENDING order<br>2. Click process | Status changes to PROCESSING |
| ORD-03 | Cancel Order | 1. Select PENDING order<br>2. Click cancel<br>3. Enter reason | Status changes to CANCELLED |
| ORD-04 | Complete Order | 1. Select PROCESSING order<br>2. Ship order<br>3. Deliver order | Status changes to DELIVERED |

### 4. Inventory Management

| TC ID | Test Case | Steps | Expected Result |
|-------|-----------|-------|------------------|
| INV-01 | Check Stock | 1. View inventory levels<br>2. Compare with thresholds | Low stock alerts triggered correctly |
| INV-02 | Reserve Stock | 1. Create order with items<br>2. Check inventory | Reserved quantity updated |
| INV-03 | Release Stock | 1. Cancel order<br>2. Check inventory | Stock returned to available |
| INV-04 | Stock Alert Email | 1. Set low threshold<br>2. Reduce stock below threshold | Alert email sent |

---

## P1 - Business Workflow Tests

### 5. Procurement Workflow

| TC ID | Test Case | Steps | Expected Result |
|-------|-----------|-------|------------------|
| PROC-01 | Create PO | 1. Navigate to procurement<br>2. Create new PO<br>3. Add items<br>4. Submit | PO created with PENDING status |
| PROC-02 | Confirm PO | 1. Select PENDING PO<br>2. Click confirm | Status changes to CONFIRMED |
| PROC-03 | 4-Step Process | 1. Confirm<br>2. Start processing<br>3. Ship<br>4. Deliver | All transitions succeed |
| PROC-04 | Cancel PO | 1. Select IN_PROGRESS PO<br>2. Cancel | Status changes to CANCELLED |

### 6. Business Partner

| TC ID | Test Case | Steps | Expected Result |
|-------|-----------|-------|------------------|
| BP-01 | Create Partner | 1. Navigate to partners<br>2. Create new partner<br>3. Fill details<br>4. Save | Partner created |
| BP-02 | Search Partners | 1. Use search/filter<br>2. View results | Correct results displayed |
| BP-03 | Activate/Deactivate | 1. Select partner<br>2. Toggle status | Status updated |
| BP-04 | View Statistics | 1. Navigate to stats<br>2. View metrics | Correct data displayed |

---

## P2 - UI/UX Tests

### 7. Frontend Functionality

| TC ID | Test Case | Steps | Expected Result |
|-------|-----------|-------|------------------|
| UI-01 | Page Navigation | 1. Click menu items<br>2. Verify routing | Correct pages load |
| UI-02 | RBAC Enforcement | 1. Login as USER<br>2. Access admin page | 403 Forbidden shown |
| UI-03 | Responsive Design | 1. Resize window<br>2. Check layout | Adapts correctly |
| UI-04 | Form Validation | 1. Submit invalid form<br>2. Check errors | Validation messages shown |

---

## Test Execution

### Pre-conditions
- All services running
- Test user accounts created
- Database seeded with test data

### Test Accounts

| Role | Username | Password |
|------|----------|----------|
| Admin | admin | Admin@123 |
| Manager | manager | Manager@123 |
| User | user | User@123 |

### Defect Reporting

| Severity | Definition | Example |
|----------|------------|---------|
| Critical | System unusable | Login broken, data loss |
| High | Major feature broken | Order creation fails |
| Medium | Feature partially working | Validation issues |
| Low | Minor UI issues | Spacing, colors |

### Sign-off Criteria

- [ ] All P0 test cases pass
- [ ] All P1 test cases pass
- [ ] No Critical/High defects open
- [ ] Performance within SLA

---

## Test Summary

| Status | Count |
|--------|-------|
| Total Test Cases | 103 |
| Executed | 0 |
| Passed | 0 |
| Failed | 0 |
| Blocked | 0 |

### Pass Criteria
- 95%+ pass rate
- Zero Critical defects
- All P0 tests passing
