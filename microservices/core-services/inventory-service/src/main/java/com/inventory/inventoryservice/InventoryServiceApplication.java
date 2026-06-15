package com.inventory.inventoryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 库存服务主应用类
 *
 * <p>库存服务微服务的入口类，负责启动和管理库存相关的所有业务功能。
 * 该服务提供库存的增删改查功能，包括库存信息管理、库存转移、批次管理等核心业务逻辑。</p>
 *
 * <p>主要功能：
 * <ul>
 *   <li>库存信息管理：库存的创建、查询、更新、删除</li>
 *   <li>库存转移：库存在不同仓库之间的转移</li>
 *   <li>批次管理：库存批次的创建、查询、更新、删除</li>
 *   <li>库存锁定：支持库存锁定和解锁</li>
 *   <li>库存盘点：支持库存盘点功能</li>
 * </ul></p>
 *
 * <p>技术特性：
 * <ul>
 *   <li>基于Spring Boot 3.4.2框架</li>
 *   <li>集成Spring Cloud服务发现</li>
 *   <li>启用缓存支持，提升查询性能</li>
 *   <li>支持RESTful API接口</li>
 *   <li>支持Kafka消息队列，实现异步处理</li>
 *   <li>使用Redis分布式锁，保证并发安全</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.cache.annotation.EnableCaching
 */
@SpringBootApplication
@EnableCaching
public final class InventoryServiceApplication {

    /**
     * 库存服务主方法
     *
     * <p>启动库存服务微服务，初始化Spring应用上下文。
     * 该方法会自动配置并启动嵌入式Tomcat服务器，监听默认端口。</p>
     *
     * <p>启动流程：
     * <ol>
     *   <li>加载Spring Boot配置文件</li>
     *   <li>初始化Spring应用上下文</li>
     *   <li>注册到服务注册中心</li>
     *   <li>启动嵌入式Web服务器</li>
     *   <li>初始化缓存和分布式锁</li>
     * </ol></p>
     *
     * @param args 命令行参数，支持Spring Boot标准参数，如：
     *               --server.port指定端口号
     *               --spring.profiles.active指定激活的配置文件
     * @throws IllegalArgumentException 当args为null时抛出
     * @see SpringApplication#run(Class, String...)
     * @since 3.0.0
     */
    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }
}
