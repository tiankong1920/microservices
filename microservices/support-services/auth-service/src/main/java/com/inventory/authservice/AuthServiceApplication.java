package com.inventory.authservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 认证服务主应用类
 *
 * <p>认证服务微服务的入口类，负责启动和管理认证授权相关的所有业务功能。
 * 该服务提供用户认证、授权管理、令牌管理、多因素认证等核心安全功能。</p>
 *
 * <p>主要功能：
 * <ul>
 *   <li>用户认证：用户名密码认证、OAuth2认证</li>
 *   <li>授权管理：角色授权、权限管理</li>
 *   <li>令牌管理：JWT令牌生成、验证、刷新</li>
 *   <li>多因素认证：短信验证码、邮箱验证码</li>
 *   <li>会话管理：会话创建、销毁、超时管理</li>
 *   <li>安全审计：登录日志、操作审计</li>
 * </ul></p>
 *
 * <p>技术特性：
 * <ul>
 *   <li>基于Spring Boot 3.4.2框架</li>
 *   <li>集成Spring Cloud服务发现</li>
 *   <li>支持JWT令牌认证</li>
 *   <li>支持OAuth2授权</li>
 *   <li>支持多因素认证</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 * @see org.springframework.cloud.client.discovery.EnableDiscoveryClient
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
public class AuthServiceApplication {

    /**
     * 认证服务主方法
     *
     * <p>启动认证服务微服务，初始化Spring应用上下文。
     * 该方法会自动配置并启动嵌入式Tomcat服务器，监听默认端口。</p>
     *
     * <p>启动流程：
     * <ol>
     *   <li>加载Spring Boot配置文件</li>
     *   <li>初始化Spring应用上下文</li>
     *   <li>注册到服务注册中心</li>
     *   <li>启动嵌入式Web服务器</li>
     *   <li>初始化JWT密钥和认证配置</li>
     * </ol></p>
     *
     * @param args 命令行参数，支持Spring Boot标准参数，如：
     *               --server.port指定端口号
     *               --spring.profiles.active指定激活的配置文件
     * @see SpringApplication#run(Class, String...)
     * @since 3.0.0
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
