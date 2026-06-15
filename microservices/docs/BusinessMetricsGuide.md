# 业务级监控指标文档

## 文档概述

本文档提供企业级分布式应用业务级监控指标的完整定义，涵盖订单量、用户活跃度、转化率、交易额等业务指标的监控、采集方法、告警阈值和优化建议。

## 目录

1. [业务级监控概述](#业务级监控概述)
2. [用户指标](#用户指标)
3. [订单指标](#订单指标)
4. [商品指标](#商品指标)
5. [交易指标](#交易指标)
6. [营销指标](#营销指标)
7. [告警阈值配置](#告警阈值配置)

## 业务级监控概述

### 业务级监控定义

业务级监控是对业务关键指标（KPI）进行监控，包括订单量、用户活跃度、转化率、交易额等业务数据。

### 业务级监控重要性

- **业务健康监控**：实时监控业务健康状况
- **业务决策支持**：为业务决策提供数据支持
- **异常检测**：快速发现业务异常情况
- **趋势分析**：分析业务发展趋势

### 业务级监控范围

```yaml
business_monitoring_scope:
  metrics:
    - "用户指标"
    - "订单指标"
    - "商品指标"
    - "交易指标"
    - "营销指标"
  
  dimensions:
    - "时间维度"
    - "地域维度"
    - "渠道维度"
    - "用户维度"
  
  aggregations:
    - "实时指标"
    - "小时指标"
    - "日指标"
    - "周指标"
    - "月指标"
```

## 用户指标

### 用户活跃度指标

#### 1. 日活跃用户数（DAU）

**指标名称**：`business.users.dau`

**指标描述**：日活跃用户数

**采集方法**：

```java
@Component
public class UserMetricsCollector {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Scheduled(cron = "0 0 * * * ?")
    public void collectDau() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        long dau = userActivityRepository.countByActivityTimeBetween(
                startOfDay, endOfDay);
        
        Metrics.gauge("business.users.dau", dau)
                .tag("host", getHostname())
                .tag("date", today.toString())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
business_users_dau_alerts:
  warning:
    threshold: 1000
    duration: "1h"
    message: "日活跃用户数低于1000"
  critical:
    threshold: 500
    duration: "30m"
    message: "日活跃用户数低于500"
```

#### 2. 周活跃用户数（WAU）

**指标名称**：`business.users.wau`

**指标描述**：周活跃用户数

**采集方法**：

```java
@Scheduled(cron = "0 0 0 * * ?")
public void collectWau() {
    LocalDate today = LocalDate.now();
    LocalDate weekAgo = today.minusWeeks(1);
    LocalDateTime startOfWeek = weekAgo.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(23, 59, 59);
    
    long wau = userActivityRepository.countByActivityTimeBetween(
            startOfWeek, endOfDay);
    
    Metrics.gauge("business.users.wau", wau)
            .tag("host", getHostname())
            .tag("week", today.format(DateTimeFormatter.ofPattern("yyyy-ww")))
            .register(Metrics.globalRegistry);
}
```

**告警阈值**：

```yaml
business_users_wau_alerts:
  warning:
    threshold: 5000
    duration: "1d"
    message: "周活跃用户数低于5000"
  critical:
    threshold: 2500
    duration: "12h"
    message: "周活跃用户数低于2500"
```

#### 3. 月活跃用户数（MAU）

**指标名称**：`business.users.mau`

**指标描述**：月活跃用户数

**采集方法**：

```java
@Scheduled(cron = "0 0 0 1 * ?")
public void collectMau() {
    LocalDate today = LocalDate.now();
    LocalDate monthAgo = today.minusMonths(1);
    LocalDateTime startOfMonth = monthAgo.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(23, 59, 59);
    
    long mau = userActivityRepository.countByActivityTimeBetween(
            startOfMonth, endOfDay);
    
    Metrics.gauge("business.users.mau", mau)
            .tag("host", getHostname())
            .tag("month", today.format(DateTimeFormatter.ofPattern("yyyy-MM")))
            .register(Metrics.globalRegistry);
}
```

**告警阈值**：

```yaml
business_users_mau_alerts:
  warning:
    threshold: 10000
    duration: "1d"
    message: "月活跃用户数低于10000"
  critical:
    threshold: 5000
    duration: "12h"
    message: "月活跃用户数低于5000"
```

### 用户增长指标

#### 1. 日新增用户数

**指标名称**：`business.users.new.daily`

**指标描述**：日新增用户数

**采集方法**：

```java
@Scheduled(cron = "0 0 * * * ?")
public void collectDailyNewUsers() {
    LocalDate today = LocalDate.now();
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(23, 59, 59);
    
    long dailyNewUsers = userRepository.countByCreatedAtBetween(
            startOfDay, endOfDay);
    
    Metrics.gauge("business.users.new.daily", dailyNewUsers)
            .tag("host", getHostname())
            .tag("date", today.toString())
            .register(Metrics.globalRegistry);
}
```

**告警阈值**：

```yaml
business_users_new_daily_alerts:
  warning:
    threshold: 50
    duration: "1d"
    message: "日新增用户数低于50"
  critical:
    threshold: 20
    duration: "12h"
    message: "日新增用户数低于20"
```

#### 2. 用户留存率

**指标名称**：`business.users.retention.rate`

**指标描述**：用户留存率

**采集方法**：

```java
@Scheduled(cron = "0 0 0 * * ?")
public void collectUserRetentionRate() {
    LocalDate today = LocalDate.now();
    LocalDate day7Ago = today.minusDays(7);
    LocalDate day30Ago = today.minusDays(30);
    
    long day7Users = userRepository.countByCreatedAtBefore(
            day7Ago.atStartOfDay());
    long day30Users = userRepository.countByCreatedAtBefore(
            day30Ago.atStartOfDay());
    
    long day7Active = userActivityRepository.countByCreatedAtBeforeAndActivityTimeAfter(
            day7Ago.atStartOfDay(), day7Ago.atTime(23, 59, 59));
    long day30Active = userActivityRepository.countByCreatedAtBeforeAndActivityTimeAfter(
            day30Ago.atStartOfDay(), day30Ago.atTime(23, 59, 59));
    
    double day7Retention = day7Users > 0 ? 
            (double) day7Active / day7Users * 100 : 0;
    double day30Retention = day30Users > 0 ? 
            (double) day30Active / day30Users * 100 : 0;
    
    Metrics.gauge("business.users.retention.rate.7d", day7Retention)
            .tag("host", getHostname())
            .tag("date", today.toString())
            .register(Metrics.globalRegistry);
    
    Metrics.gauge("business.users.retention.rate.30d", day30Retention)
            .tag("host", getHostname())
            .tag("date", today.toString())
            .register(Metrics.globalRegistry);
}
```

**告警阈值**：

```yaml
business_users_retention_rate_alerts:
  warning:
    threshold: 30
    duration: "1d"
    message: "7日用户留存率低于30%"
  critical:
    threshold: 20
    duration: "12h"
    message: "7日用户留存率低于20%"
```

## 订单指标

### 订单量指标

#### 1. 日订单量

**指标名称**：`business.orders.daily`

**指标描述**：日订单量

**采集方法**：

```java
@Component
public class OrderMetricsCollector {

    @Autowired
    private OrderRepository orderRepository;

    @Scheduled(cron = "0 0 * * * ?")
    public void collectDailyOrders() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        long dailyOrders = orderRepository.countByCreatedAtBetween(
                startOfDay, endOfDay);
        
        Metrics.gauge("business.orders.daily", dailyOrders)
                .tag("host", getHostname())
                .tag("date", today.toString())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
business_orders_daily_alerts:
  warning:
    threshold: 100
    duration: "1h"
    message: "日订单量低于100"
  critical:
    threshold: 50
    duration: "30m"
    message: "日订单量低于50"
```

#### 2. 小时订单量

**指标名称**：`business.orders.hourly`

**指标描述**：小时订单量

**采集方法**：

```java
@Scheduled(cron = "0 0 * * * ?")
public void collectHourlyOrders() {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startOfHour = now.withMinute(0).withSecond(0).withNano(0);
    LocalDateTime endOfHour = startOfHour.plusHours(1);
    
    long hourlyOrders = orderRepository.countByCreatedAtBetween(
            startOfHour, endOfHour);
    
    Metrics.gauge("business.orders.hourly", hourlyOrders)
            .tag("host", getHostname())
            .tag("hour", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd:HH")))
            .register(Metrics.globalRegistry);
}
```

**告警阈值**：

```yaml
business_orders_hourly_alerts:
  warning:
    threshold: 10
    duration: "1h"
    message: "小时订单量低于10"
  critical:
    threshold: 5
    duration: "30m"
    message: "小时订单量低于5"
```

### 订单金额指标

#### 1. 日订单总额

**指标名称**：`business.orders.amount.daily`

**指标描述**：日订单总额

**采集方法**：

```java
@Scheduled(cron = "0 0 * * * ?")
public void collectDailyOrderAmount() {
    LocalDate today = LocalDate.now();
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(23, 59, 59);
    
    BigDecimal dailyOrderAmount = orderRepository.sumTotalAmountByCreatedAtBetween(
            startOfDay, endOfDay);
    
    Metrics.gauge("business.orders.amount.daily", dailyOrderAmount.doubleValue())
            .tag("host", getHostname())
            .tag("date", today.toString())
            .register(Metrics.globalRegistry);
}
```

**告警阈值**：

```yaml
business_orders_amount_daily_alerts:
  warning:
    threshold: 10000
    duration: "1h"
    message: "日订单总额低于10000"
  critical:
    threshold: 5000
    duration: "30m"
    message: "日订单总额低于5000"
```

#### 2. 平均订单金额

**指标名称**：`business.orders.amount.average`

**指标描述**：平均订单金额

**采集方法**：

```java
@Scheduled(cron = "0 0 * * * ?")
public void collectAverageOrderAmount() {
    LocalDate today = LocalDate.now();
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(23, 59, 59);
    
    long dailyOrders = orderRepository.countByCreatedAtBetween(
            startOfDay, endOfDay);
    BigDecimal dailyOrderAmount = orderRepository.sumTotalAmountByCreatedAtBetween(
            startOfDay, endOfDay);
    
    BigDecimal averageOrderAmount = dailyOrders > 0 ? 
            dailyOrderAmount.divide(BigDecimal.valueOf(dailyOrders), 2, 
                    RoundingMode.HALF_UP) : BigDecimal.ZERO;
    
    Metrics.gauge("business.orders.amount.average", averageOrderAmount.doubleValue())
            .tag("host", getHostname())
            .tag("date", today.toString())
            .register(Metrics.globalRegistry);
}
```

**告警阈值**：

```yaml
business_orders_amount_average_alerts:
  warning:
    threshold: 50
    duration: "1h"
    message: "平均订单金额低于50"
  critical:
    threshold: 30
    duration: "30m"
    message: "平均订单金额低于30"
```

### 订单状态指标

#### 1. 订单完成率

**指标名称**：`business.orders.completed.rate`

**指标描述**：订单完成率

**采集方法**：

```java
@Scheduled(cron = "0 0 * * * ?")
public void collectOrderCompletionRate() {
    LocalDate today = LocalDate.now();
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(23, 59, 59);
    
    long totalOrders = orderRepository.countByCreatedAtBetween(
            startOfDay, endOfDay);
    long completedOrders = orderRepository.countByCreatedAtBetweenAndStatus(
            startOfDay, endOfDay, OrderStatus.COMPLETED);
    
    double completionRate = totalOrders > 0 ? 
            (double) completedOrders / totalOrders * 100 : 0;
    
    Metrics.gauge("business.orders.completed.rate", completionRate)
            .tag("host", getHostname())
            .tag("date", today.toString())
            .register(Metrics.globalRegistry);
}
```

**告警阈值**：

```yaml
business_orders_completed_rate_alerts:
  warning:
    threshold: 80
    duration: "1h"
    message: "订单完成率低于80%"
  critical:
    threshold: 70
    duration: "30m"
    message: "订单完成率低于70%"
```

#### 2. 订单取消率

**指标名称**：`business.orders.cancelled.rate`

**指标描述**：订单取消率

**采集方法**：

```java
@Scheduled(cron = "0 0 * * * ?")
public void collectOrderCancellationRate() {
    LocalDate today = LocalDate.now();
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(23, 59, 59);
    
    long totalOrders = orderRepository.countByCreatedAtBetween(
            startOfDay, endOfDay);
    long cancelledOrders = orderRepository.countByCreatedAtBetweenAndStatus(
            startOfDay, endOfDay, OrderStatus.CANCELLED);
    
    double cancellationRate = totalOrders > 0 ? 
            (double) cancelledOrders / totalOrders * 100 : 0;
    
    Metrics.gauge("business.orders.cancelled.rate", cancellationRate)
            .tag("host", getHostname())
            .tag("date", today.toString())
            .register(Metrics.globalRegistry);
}
```

**告警阈值**：

```yaml
business_orders_cancelled_rate_alerts:
  warning:
    threshold: 10
    duration: "1h"
    message: "订单取消率超过10%"
  critical:
    threshold: 20
    duration: "30m"
    message: "订单取消率超过20%"
```

## 商品指标

### 商品浏览指标

#### 1. 日商品浏览量

**指标名称**：`business.products.views.daily`

**指标描述**：日商品浏览量

**采集方法**：

```java
@Component
public class ProductMetricsCollector {

    @Autowired
    private ProductViewRepository productViewRepository;

    @Scheduled(cron = "0 0 * * * ?")
    public void collectDailyProductViews() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        long dailyProductViews = productViewRepository.countByViewTimeBetween(
                startOfDay, endOfDay);
        
        Metrics.gauge("business.products.views.daily", dailyProductViews)
                .tag("host", getHostname())
                .tag("date", today.toString())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
business_products_views_daily_alerts:
  warning:
    threshold: 1000
    duration: "1h"
    message: "日商品浏览量低于1000"
  critical:
    threshold: 500
    duration: "30m"
    message: "日商品浏览量低于500"
```

#### 2. 商品转化率

**指标名称**：`business.products.conversion.rate`

**指标描述**：商品转化率（浏览到下单）

**采集方法**：

```java
@Scheduled(cron = "0 0 * * * ?")
public void collectProductConversionRate() {
    LocalDate today = LocalDate.now();
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(23, 59, 59);
    
    long dailyProductViews = productViewRepository.countByViewTimeBetween(
            startOfDay, endOfDay);
    long dailyOrders = orderRepository.countByCreatedAtBetween(
            startOfDay, endOfDay);
    
    double conversionRate = dailyProductViews > 0 ? 
            (double) dailyOrders / dailyProductViews * 100 : 0;
    
    Metrics.gauge("business.products.conversion.rate", conversionRate)
            .tag("host", getHostname())
            .tag("date", today.toString())
            .register(Metrics.globalRegistry);
}
```

**告警阈值**：

```yaml
business_products_conversion_rate_alerts:
  warning:
    threshold: 5
    duration: "1h"
    message: "商品转化率低于5%"
  critical:
    threshold: 3
    duration: "30m"
    message: "商品转化率低于3%"
```

### 商品库存指标

#### 1. 库存预警商品数

**指标名称**：`business.products.stock.warning`

**指标描述**：库存预警商品数

**采集方法**：

```java
@Component
public class StockMetricsCollector {

    @Autowired
    private ProductRepository productRepository;

    @Scheduled(cron = "0 0 * * * ?")
    public void collectStockWarningProducts() {
        long stockWarningProducts = productRepository.countByStockLessThan(10);
        
        Metrics.gauge("business.products.stock.warning", stockWarningProducts)
                .tag("host", getHostname())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
business_products_stock_warning_alerts:
  warning:
    threshold: 50
    duration: "1h"
    message: "库存预警商品数超过50"
  critical:
    threshold: 100
    duration: "30m"
    message: "库存预警商品数超过100"
```

#### 2. 缺货商品数

**指标名称**：`business.products.stock.out`

**指标描述**：缺货商品数

**采集方法**：

```java
@Scheduled(cron = "0 0 * * * ?")
public void collectOutOfStockProducts() {
    long outOfStockProducts = productRepository.countByStockEquals(0);
    
    Metrics.gauge("business.products.stock.out", outOfStockProducts)
            .tag("host", getHostname())
            .register(Metrics.globalRegistry);
}
```

**告警阈值**：

```yaml
business_products_stock_out_alerts:
  warning:
    threshold: 10
    duration: "1h"
    message: "缺货商品数超过10"
  critical:
    threshold: 20
    duration: "30m"
    message: "缺货商品数超过20"
```

## 交易指标

### 交易量指标

#### 1. 日交易量

**指标名称**：`business.transactions.daily`

**指标描述**：日交易量

**采集方法**：

```java
@Component
public class TransactionMetricsCollector {

    @Autowired
    private TransactionRepository transactionRepository;

    @Scheduled(cron = "0 0 * * * ?")
    public void collectDailyTransactions() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        long dailyTransactions = transactionRepository.countByCreatedAtBetween(
                startOfDay, endOfDay);
        
        Metrics.gauge("business.transactions.daily", dailyTransactions)
                .tag("host", getHostname())
                .tag("date", today.toString())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
business_transactions_daily_alerts:
  warning:
    threshold: 100
    duration: "1h"
    message: "日交易量低于100"
  critical:
    threshold: 50
    duration: "30m"
    message: "日交易量低于50"
```

#### 2. 交易成功率

**指标名称**：`business.transactions.success.rate`

**指标描述**：交易成功率

**采集方法**：

```java
@Scheduled(cron = "0 0 * * * ?")
public void collectTransactionSuccessRate() {
    LocalDate today = LocalDate.now();
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(23, 59, 59);
    
    long totalTransactions = transactionRepository.countByCreatedAtBetween(
            startOfDay, endOfDay);
    long successTransactions = transactionRepository.countByCreatedAtBetweenAndStatus(
            startOfDay, endOfDay, TransactionStatus.SUCCESS);
    
    double successRate = totalTransactions > 0 ? 
            (double) successTransactions / totalTransactions * 100 : 0;
    
    Metrics.gauge("business.transactions.success.rate", successRate)
            .tag("host", getHostname())
            .tag("date", today.toString())
            .register(Metrics.globalRegistry);
}
```

**告警阈值**：

```yaml
business_transactions_success_rate_alerts:
  warning:
    threshold: 95
    duration: "1h"
    message: "交易成功率低于95%"
  critical:
    threshold: 90
    duration: "30m"
    message: "交易成功率低于90%"
```

### 交易金额指标

#### 1. 日交易总额

**指标名称**：`business.transactions.amount.daily`

**指标描述**：日交易总额

**采集方法**：

```java
@Scheduled(cron = "0 0 * * * ?")
public void collectDailyTransactionAmount() {
    LocalDate today = LocalDate.now();
    LocalDateTime startOfDay = today.atStartOfDay();
    LocalDateTime endOfDay = today.atTime(23, 59, 59);
    
    BigDecimal dailyTransactionAmount = transactionRepository.sumAmountByCreatedAtBetween(
            startOfDay, endOfDay);
    
    Metrics.gauge("business.transactions.amount.daily", dailyTransactionAmount.doubleValue())
            .tag("host", getHostname())
            .tag("date", today.toString())
            .register(Metrics.globalRegistry);
}
```

**告警阈值**：

```yaml
business_transactions_amount_daily_alerts:
  warning:
    threshold: 10000
    duration: "1h"
    message: "日交易总额低于10000"
  critical:
    threshold: 5000
    duration: "30m"
    message: "日交易总额低于5000"
```

## 营销指标

### 营销活动指标

#### 1. 活动参与人数

**指标名称**：`business.marketing.participants`

**指标描述**：营销活动参与人数

**采集方法**：

```java
@Component
public class MarketingMetricsCollector {

    @Autowired
    private MarketingActivityRepository marketingActivityRepository;

    @Autowired
    private MarketingParticipantRepository marketingParticipantRepository;

    @Scheduled(cron = "0 0 * * * ?")
    public void collectMarketingParticipants() {
        List<MarketingActivity> activeActivities = 
                marketingActivityRepository.findByStatusAndEndTimeAfter(
                        MarketingActivityStatus.ACTIVE, LocalDateTime.now());
        
        for (MarketingActivity activity : activeActivities) {
            long participants = marketingParticipantRepository
                    .countByActivityId(activity.getId());
            
            Metrics.gauge("business.marketing.participants", participants)
                    .tag("host", getHostname())
                    .tag("activity", activity.getName())
                    .register(Metrics.globalRegistry);
        }
    }
}
```

**告警阈值**：

```yaml
business_marketing_participants_alerts:
  warning:
    threshold: 100
    duration: "1h"
    message: "营销活动参与人数低于100"
  critical:
    threshold: 50
    duration: "30m"
    message: "营销活动参与人数低于50"
```

#### 2. 活动转化率

**指标名称**：`business.marketing.conversion.rate`

**指标描述**：营销活动转化率

**采集方法**：

```java
@Scheduled(cron = "0 0 * * * ?")
public void collectMarketingConversionRate() {
    List<MarketingActivity> activeActivities = 
            marketingActivityRepository.findByStatusAndEndTimeAfter(
                    MarketingActivityStatus.ACTIVE, LocalDateTime.now());
    
    for (MarketingActivity activity : activeActivities) {
        long participants = marketingParticipantRepository
                .countByActivityId(activity.getId());
        long conversions = marketingParticipantRepository
                .countByActivityIdAndStatus(activity.getId(), 
                        ParticipantStatus.CONVERTED);
        
        double conversionRate = participants > 0 ? 
                (double) conversions / participants * 100 : 0;
        
        Metrics.gauge("business.marketing.conversion.rate", conversionRate)
                .tag("host", getHostname())
                .tag("activity", activity.getName())
                .register(Metrics.globalRegistry);
    }
}
```

**告警阈值**：

```yaml
business_marketing_conversion_rate_alerts:
  warning:
    threshold: 5
    duration: "1h"
    message: "营销活动转化率低于5%"
  critical:
    threshold: 3
    duration: "30m"
    message: "营销活动转化率低于3%"
```

## 告警阈值配置

### 告警阈值配置文件

```yaml
business_alerts:
  users:
    dau:
      warning:
        threshold: 1000
        duration: "1h"
        message: "日活跃用户数低于1000"
      critical:
        threshold: 500
        duration: "30m"
        message: "日活跃用户数低于500"
    wau:
      warning:
        threshold: 5000
        duration: "1d"
        message: "周活跃用户数低于5000"
      critical:
        threshold: 2500
        duration: "12h"
        message: "周活跃用户数低于2500"
    mau:
      warning:
        threshold: 10000
        duration: "1d"
        message: "月活跃用户数低于10000"
      critical:
        threshold: 5000
        duration: "12h"
        message: "月活跃用户数低于5000"
    new_daily:
      warning:
        threshold: 50
        duration: "1d"
        message: "日新增用户数低于50"
      critical:
        threshold: 20
        duration: "12h"
        message: "日新增用户数低于20"
    retention_rate:
      warning:
        threshold: 30
        duration: "1d"
        message: "7日用户留存率低于30%"
      critical:
        threshold: 20
        duration: "12h"
        message: "7日用户留存率低于20%"
  
  orders:
    daily:
      warning:
        threshold: 100
        duration: "1h"
        message: "日订单量低于100"
      critical:
        threshold: 50
        duration: "30m"
        message: "日订单量低于50"
    hourly:
      warning:
        threshold: 10
        duration: "1h"
        message: "小时订单量低于10"
      critical:
        threshold: 5
        duration: "30m"
        message: "小时订单量低于5"
    amount_daily:
      warning:
        threshold: 10000
        duration: "1h"
        message: "日订单总额低于10000"
      critical:
        threshold: 5000
        duration: "30m"
        message: "日订单总额低于5000"
    amount_average:
      warning:
        threshold: 50
        duration: "1h"
        message: "平均订单金额低于50"
      critical:
        threshold: 30
        duration: "30m"
        message: "平均订单金额低于30"
    completed_rate:
      warning:
        threshold: 80
        duration: "1h"
        message: "订单完成率低于80%"
      critical:
        threshold: 70
        duration: "30m"
        message: "订单完成率低于70%"
    cancelled_rate:
      warning:
        threshold: 10
        duration: "1h"
        message: "订单取消率超过10%"
      critical:
        threshold: 20
        duration: "30m"
        message: "订单取消率超过20%"
  
  products:
    views_daily:
      warning:
        threshold: 1000
        duration: "1h"
        message: "日商品浏览量低于1000"
      critical:
        threshold: 500
        duration: "30m"
        message: "日商品浏览量低于500"
    conversion_rate:
      warning:
        threshold: 5
        duration: "1h"
        message: "商品转化率低于5%"
      critical:
        threshold: 3
        duration: "30m"
        message: "商品转化率低于3%"
    stock_warning:
      warning:
        threshold: 50
        duration: "1h"
        message: "库存预警商品数超过50"
      critical:
        threshold: 100
        duration: "30m"
        message: "库存预警商品数超过100"
    stock_out:
      warning:
        threshold: 10
        duration: "1h"
        message: "缺货商品数超过10"
      critical:
        threshold: 20
        duration: "30m"
        message: "缺货商品数超过20"
  
  transactions:
    daily:
      warning:
        threshold: 100
        duration: "1h"
        message: "日交易量低于100"
      critical:
        threshold: 50
        duration: "30m"
        message: "日交易量低于50"
    success_rate:
      warning:
        threshold: 95
        duration: "1h"
        message: "交易成功率低于95%"
      critical:
        threshold: 90
        duration: "30m"
        message: "交易成功率低于90%"
    amount_daily:
      warning:
        threshold: 10000
        duration: "1h"
        message: "日交易总额低于10000"
      critical:
        threshold: 5000
        duration: "30m"
        message: "日交易总额低于5000"
  
  marketing:
    participants:
      warning:
        threshold: 100
        duration: "1h"
        message: "营销活动参与人数低于100"
      critical:
        threshold: 50
        duration: "30m"
        message: "营销活动参与人数低于50"
    conversion_rate:
      warning:
        threshold: 5
        duration: "1h"
        message: "营销活动转化率低于5%"
      critical:
        threshold: 3
        duration: "30m"
        message: "营销活动转化率低于3%"
```

## 相关文档

- [系统级监控指标文档](SystemMetricsGuide.md)
- [应用级监控指标文档](ApplicationMetricsGuide.md)
- [数据库监控指标文档](DatabaseMetricsGuide.md)
- [缓存监控指标文档](CacheMetricsGuide.md)

## 版本历史

| 版本 | 日期 | 作者 | 变更说明 |
|------|------|------|---------|
| 1.0.0 | 2026-01-19 | System | 初始版本 |