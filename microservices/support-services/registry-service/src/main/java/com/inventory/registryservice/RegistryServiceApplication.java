package com.inventory.registryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 注册服务主应用类.
 *
 * <p>注册服务微服务的入口类，封装 Nacos 服务注册发现功能，
 * 提供服务列表查询、健康检查、服务实例管理等 REST API。</p>
 *
 * <p>主要功能：
 * <ul>
 *   <li>服务发现：通过 Nacos 查询已注册的微服务列表</li>
 *   <li>健康检查：检查微服务的健康状态和实例可用性</li>
 *   <li>服务实例管理：查询服务实例详情、IP、端口、权重</li>
 *   <li>服务元数据管理：支持自定义服务元数据标签</li>
 * </ul></p>
 *
 * <p>技术特性：
 * <ul>
 *   <li>基于 Spring Boot 3.4.4 框架</li>
 *   <li>集成 Spring Cloud Alibaba Nacos Discovery</li>
 *   <li>支持服务健康检查与负载均衡</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 3.0
 */
@SpringBootApplication
public class RegistryServiceApplication {

    /**
     * 注册服务主方法
     *
     * <p>启动注册服务微服务，初始化Spring应用上下文。
     * 该方法会自动配置并启动嵌入式Tomcat服务器，监听默认端口。</p>
     *
     * <p>启动流程：
     * <ol>
     *   <li>加载Spring Boot配置文件</li>
     *   <li>初始化Spring应用上下文</li>
     *   <li>初始化Eureka服务器</li>
     *   <li>启动嵌入式Web服务器</li>
     *   <li>等待微服务注册</li>
     * </ol></p>
     *
     * @param args 命令行参数，支持Spring Boot标准参数，如：
     *               --server.port指定端口号
     *               --spring.profiles.active指定激活的配置文件
     * @see SpringApplication#run(Class, String...)
     * @since 3.0.0
     */
    public static void main(final String[] args) {
        SpringApplication.run(RegistryServiceApplication.class, args);
    }
}
