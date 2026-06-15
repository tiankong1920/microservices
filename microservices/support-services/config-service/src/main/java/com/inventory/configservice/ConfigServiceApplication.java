package com.inventory.configservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 配置服务主应用类.
 *
 * <p>配置服务微服务的入口类，封装 Nacos 配置中心管理功能，
 * 提供集中化配置管理、动态配置更新、配置版本控制等核心配置功能。</p>
 *
 * <p>主要功能：
 * <ul>
 *   <li>集中化配置管理：通过 Nacos 统一管理所有微服务的配置</li>
 *   <li>动态配置更新：支持配置的动态更新和热加载</li>
 *   <li>配置版本控制：配置的历史版本管理和回滚</li>
 *   <li>环境管理：支持多环境配置（开发、测试、生产）</li>
 * </ul></p>
 *
 * <p>技术特性：
 * <ul>
 *   <li>基于 Spring Boot 3.4.4 框架</li>
 *   <li>集成 Spring Cloud Alibaba Nacos Config</li>
 *   <li>支持 YAML/Properties 格式配置</li>
 *   <li>支持配置的动态刷新</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 3.0
 */
@SpringBootApplication
public class ConfigServiceApplication {

    /**
     * 配置服务主方法
     *
     * <p>启动配置服务微服务，初始化Spring应用上下文。
     * 该方法会自动配置并启动嵌入式Tomcat服务器，监听默认端口。</p>
     *
     * <p>启动流程：
     * <ol>
     *   <li>加载Spring Boot配置文件</li>
     *   <li>初始化Spring应用上下文</li>
     *   <li>初始化配置仓库连接</li>
     *   <li>启动嵌入式Web服务器</li>
     *   <li>加载所有微服务的配置</li>
     * </ol></p>
     *
     * @param args 命令行参数，支持Spring Boot标准参数，如：
     *               --server.port指定端口号
     *               --spring.profiles.active指定激活的配置文件
     * @see SpringApplication#run(Class, String...)
     * @since 3.0.0
     */
    public static void main(final String[] args) {
        SpringApplication.run(ConfigServiceApplication.class, args);
    }
}
