package com.inventory.supplierservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 供应商服务主应用类
 *
 * <p>供应商服务微服务的入口类，负责启动和管理供应商相关的所有业务功能。
 * 该服务提供供应商的增删改查功能，包括供应商信息管理、供应商统计、供应商评级等核心业务逻辑。</p>
 *
 * <p>主要功能：
 * <ul>
 *   <li>供应商信息管理：供应商的创建、查询、更新、删除</li>
 *   <li>供应商统计：供应商采购统计、供应商供货统计</li>
 *   <li>供应商评级：供应商等级评定、供应商绩效分析</li>
 *   <li>供应商查询：按名称查询、按等级查询、按状态查询</li>
 * </ul></p>
 *
 * <p>技术特性：
 * <ul>
 *   <li>基于Spring Boot 3.4.2框架</li>
 *   <li>集成Spring Cloud服务发现</li>
 *   <li>启用缓存支持，提升查询性能</li>
 *   <li>支持RESTful API接口</li>
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
public final class SupplierServiceApplication {

    /**
     * 私有构造函数
     *
     * <p>防止实例化，该类只包含静态方法，不需要创建实例。</p>
     */
    private SupplierServiceApplication() {
        // Prevent instantiation
    }

    /**
     * 供应商服务主方法
     *
     * <p>启动供应商服务微服务，初始化Spring应用上下文。
     * 该方法会自动配置并启动嵌入式Tomcat服务器，监听默认端口。</p>
     *
     * <p>启动流程：
     * <ol>
     *   <li>加载Spring Boot配置文件</li>
     *   <li>初始化Spring应用上下文</li>
     *   <li>注册到服务注册中心</li>
     *   <li>启动嵌入式Web服务器</li>
     *   <li>初始化缓存管理器</li>
     * </ol></p>
     *
     * @param args 命令行参数，支持Spring Boot标准参数，如：
     *               --server.port指定端口号
     *               --spring.profiles.active指定激活的配置文件
     * @throws IllegalArgumentException 当args为null时抛出
     * @see SpringApplication#run(Class, String...)
     * @since 3.0.0
     */
    public static void main(final String[] args) {
        SpringApplication.run(SupplierServiceApplication.class, args);
    }
}
