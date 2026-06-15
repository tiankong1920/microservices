package com.inventory.financeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 财务服务主应用类
 *
 * <p>财务服务微服务的入口类，负责启动和管理财务相关的所有业务功能。
 * 该服务提供凭证管理、账户管理、收支管理、结算管理等核心财务功能。</p>
 *
 * <p>主要功能：
 * <ul>
 *   <li>凭证管理：凭证的创建、查询、更新、删除</li>
 *   <li>账户管理：账户的增删改查、账户余额管理</li>
 *   <li>收支管理：收入管理、支出管理、收支统计</li>
 *   <li>结算管理：结算账户管理、结算单管理</li>
 *   <li>财务报表：资产负债表、利润表、现金流量表</li>
 *   <li>财务审计：财务审计日志、审计报告</li>
 * </ul></p>
 *
 * <p>技术特性：
 * <ul>
 *   <li>基于Spring Boot 3.4.2框架</li>
 *   <li>集成Spring Cloud服务发现</li>
 *   <li>支持事务管理</li>
 *   <li>支持财务报表生成</li>
 *   <li>支持多币种管理</li>
 * </ul></p>
 *
 * @author Inventory Team
 * @since 3.0.0
 * @version 5.0
 * @see org.springframework.boot.SpringApplication
 * @see org.springframework.boot.autoconfigure.SpringBootApplication
 */
@SpringBootApplication
public class FinanceServiceApplication {

    /**
     * 财务服务主方法
     *
     * <p>启动财务服务微服务，初始化Spring应用上下文。
     * 该方法会自动配置并启动嵌入式Tomcat服务器，监听默认端口。</p>
     *
     * <p>启动流程：
     * <ol>
     *   <li>加载Spring Boot配置文件</li>
     *   <li>初始化Spring应用上下文</li>
     *   <li>注册到服务注册中心</li>
     *   <li>启动嵌入式Web服务器</li>
     *   <li>初始化财务配置和账户数据</li>
     * </ol></p>
     *
     * @param args 命令行参数，支持Spring Boot标准参数，如：
     *               --server.port指定端口号
     *               --spring.profiles.active指定激活的配置文件
     * @see SpringApplication#run(Class, String...)
     * @since 3.0.0
     */
    public static void main(final String[] args) {
        SpringApplication.run(FinanceServiceApplication.class, args);
    }
}
