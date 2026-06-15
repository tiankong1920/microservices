package com.inventory.salesservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 销售服务主应用类
 *
 * <p>销售服务微服务的入口类，负责启动和管理销售相关的所有业务功能。
 * 该服务提供销售订单的增删改查功能，包括销售订单管理、退货管理、零售管理等核心业务逻辑。</p>
 *
 * <p>主要功能：
 * <ul>
 *   <li>销售订单管理：销售订单的创建、查询、更新、删除</li>
 *   <li>销售退货管理：退货订单的创建、查询、更新、删除</li>
 *   <li>零售管理：零售订单的创建、查询、更新、删除</li>
 *   <li>客户集成：调用客户服务获取客户信息</li>
 *   <li>产品集成：调用产品服务获取产品信息</li>
 *   <li>库存集成：调用库存服务进行库存操作</li>
 * </ul></p>
 *
 * <p>技术特性：
 * <ul>
 *   <li>基于Spring Boot 3.4.2框架</li>
 *   <li>集成Spring Cloud服务发现</li>
 *   <li>支持Feign客户端调用其他服务</li>
 *   <li>支持RESTful API接口</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.cloud.openfeign.EnableFeignClients
 */
@SpringBootApplication
@EnableFeignClients(basePackages = {"com.inventory.common.client", "com.inventory.salesservice.client"})
public final class SalesServiceApplication {

    /**
     * 销售服务主方法
     *
     * <p>启动销售服务微服务，初始化Spring应用上下文。
     * 该方法会自动配置并启动嵌入式Tomcat服务器，监听默认端口。</p>
     *
     * <p>启动流程：
     * <ol>
     *   <li>加载Spring Boot配置文件</li>
     *   <li>初始化Spring应用上下文</li>
     *   <li>注册到服务注册中心</li>
     *   <li>启动嵌入式Web服务器</li>
     *   <li>初始化Feign客户端</li>
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
        SpringApplication.run(SalesServiceApplication.class, args);
    }
}
