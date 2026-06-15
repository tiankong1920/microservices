package com.inventory.gatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关服务主应用类
 *
 * <p>网关服务微服务的入口类，负责启动和管理API网关相关的所有功能。
 * 该服务提供路由转发、负载均衡、权限验证、限流熔断等核心网关功能。</p>
 *
 * <p>主要功能：
 * <ul>
 *   <li>路由转发：请求路由到后端微服务</li>
 *   <li>负载均衡：请求负载均衡分发</li>
 *   <li>权限验证：统一认证和授权</li>
 *   <li>限流熔断：流量控制和熔断降级</li>
 *   <li>日志记录：请求日志和响应日志</li>
 *   <li>监控统计：请求统计和性能监控</li>
 * </ul></p>
 *
 * <p>技术特性：
 * <ul>
 *   <li>基于Spring Boot 3.4.2框架</li>
 *   <li>集成Spring Cloud Gateway</li>
 *   <li>支持动态路由配置</li>
 *   <li>支持过滤器链</li>
 *   <li>支持限流和熔断</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 */
@SpringBootApplication
public class GatewayServiceApplication {

    /**
     * 网关服务主方法
     *
     * <p>启动网关服务微服务，初始化Spring应用上下文。
     * 该方法会自动配置并启动嵌入式Tomcat服务器，监听默认端口。</p>
     *
     * <p>启动流程：
     * <ol>
     *   <li>加载Spring Boot配置文件</li>
     *   <li>初始化Spring应用上下文</li>
     *   <li>注册到服务注册中心</li>
     *   <li>启动嵌入式Web服务器</li>
     *   <li>初始化路由配置</li>
     * </ol></p>
     *
     * @param args 命令行参数，支持Spring Boot标准参数，如：
     *               --server.port指定端口号
     *               --spring.profiles.active指定激活的配置文件
     * @see SpringApplication#run(Class, String...)
     * @since 3.0.0
     */
    public static void main(final String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
