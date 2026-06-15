package com.inventory.adminservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 管理服务主应用类
 *
 * <p>管理服务微服务的入口类，负责启动和管理系统管理相关的所有业务功能。
 * 该服务提供用户管理、权限管理、系统配置管理等核心管理功能。</p>
 *
 * <p>主要功能：
 * <ul>
 *   <li>用户管理：用户的创建、查询、更新、删除</li>
 *   <li>权限管理：角色管理、权限分配、访问控制</li>
 *   <li>系统配置管理：系统参数配置、字典管理</li>
 *   <li>日志管理：操作日志、系统日志查询</li>
 * </ul></p>
 *
 * <p>技术特性：
 * <ul>
 *   <li>基于Spring Boot 3.4.2框架</li>
 *   <li>集成Spring Cloud服务发现</li>
 *   <li>支持RESTful API接口</li>
 *   <li>支持权限验证和访问控制</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 */
@SpringBootApplication
public class AdminServiceApplication {

    /**
     * 管理服务主方法
     *
     * <p>启动管理服务微服务，初始化Spring应用上下文。
     * 该方法会自动配置并启动嵌入式Tomcat服务器，监听默认端口。</p>
     *
     * <p>启动流程：
     * <ol>
     *   <li>加载Spring Boot配置文件</li>
     *   <li>初始化Spring应用上下文</li>
     *   <li>注册到服务注册中心</li>
     *   <li>启动嵌入式Web服务器</li>
     * </ol></p>
     *
     * @param args 命令行参数，支持Spring Boot标准参数，如：
     *               --server.port指定端口号
     *               --spring.profiles.active指定激活的配置文件
     * @see SpringApplication#run(Class, String...)
     * @since 3.0.0
     */
    public static void main(final String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }
}
