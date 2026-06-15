package com.inventory.common.resilience;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 降级策略.
 *
 * <p>定义服务降级的策略，包括失败阈值、成功阈值和恢复超时时间。</p>
 *
 * @author Inventory Team
 * @version 5.0
 * @since 3.0.0
 */
public class FallbackStrategy {

    /** 默认失败阈值. */
    private static final int DEFAULT_FAILURE_THRESHOLD = 5;

    /** 默认成功阈值. */
    private static final int DEFAULT_SUCCESS_THRESHOLD = 3;

    /** 默认恢复超时时间（毫秒）. */
    private static final long DEFAULT_RECOVERY_TIMEOUT_MS = 30000;

    /** 失败阈值. */
    private final int failureThreshold;

    /** 成功阈值. */
    private final int successThreshold;

    /** 恢复超时时间（毫秒）. */
    private final long recoveryTimeoutMs;

    /** 失败计数. */
    private final AtomicInteger failureCount = new AtomicInteger(0);

    /** 成功计数. */
    private final AtomicInteger successCount = new AtomicInteger(0);

    /** 降级状态. */
    private final AtomicBoolean degraded = new AtomicBoolean(false);

    /** 最后失败时间. */
    private volatile long lastFailureTime = 0;

    /**
     * 默认构造函数.
     */
    public FallbackStrategy() {
        this(DEFAULT_FAILURE_THRESHOLD, DEFAULT_SUCCESS_THRESHOLD, DEFAULT_RECOVERY_TIMEOUT_MS);
    }

    /**
     * 构造函数.
     *
     * @param failureThreshold 失败阈值
     * @param successThreshold 成功阈值
     * @param recoveryTimeoutMs 恢复超时时间（毫秒）
     */
    public FallbackStrategy(final int failureThreshold,
            final int successThreshold, final long recoveryTimeoutMs) {
        this.failureThreshold = failureThreshold;
        this.successThreshold = successThreshold;
        this.recoveryTimeoutMs = recoveryTimeoutMs;
    }

    /**
     * 记录失败.
     */
    public void recordFailure() {
        lastFailureTime = System.currentTimeMillis();
        failureCount.incrementAndGet();
        successCount.set(0);

        if (failureCount.get() >= failureThreshold) {
            degraded.set(true);
        }
    }

    /**
     * 记录成功.
     */
    public void recordSuccess() {
        successCount.incrementAndGet();

        if (degraded.get() && successCount.get() >= successThreshold) {
            degraded.set(false);
            failureCount.set(0);
        }
    }

    /**
     * 检查是否降级.
     *
     * @return 是否降级
     */
    public boolean isDegraded() {
        if (degraded.get() && System.currentTimeMillis() - lastFailureTime > recoveryTimeoutMs) {
            degraded.set(false);
            failureCount.set(0);
            successCount.set(0);
            return false;
        }
        return degraded.get();
    }

    /**
     * 设置降级状态.
     *
     * @param degradedValue 降级状态
     */
    public void setDegraded(final boolean degradedValue) {
        this.degraded.set(degradedValue);
        if (degradedValue) {
            lastFailureTime = System.currentTimeMillis();
        }
    }

    /**
     * 获取失败计数.
     *
     * @return 失败计数
     */
    public int getFailureCount() {
        return failureCount.get();
    }

    /**
     * 获取成功计数.
     *
     * @return 成功计数
     */
    public int getSuccessCount() {
        return successCount.get();
    }

    /**
     * 获取成功率.
     *
     * @return 成功率
     */
    public double getSuccessRate() {
        final int total = failureCount.get() + successCount.get();
        return total == 0 ? 1.0 : (double) successCount.get() / total;
    }

    /**
     * 重置状态.
     */
    public void reset() {
        failureCount.set(0);
        successCount.set(0);
        degraded.set(false);
        lastFailureTime = 0;
    }
}
