package com.inventory.reportservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 报告服务主应用类
 *
 * <p>报告服务微服务的入口类，负责启动和管理报表相关的所有业务功能。
 * 该服务提供销售报表、库存报表、采购报表、财务报表等核心报表功能。</p>
 *
 * <p>主要功能：
 * <ul>
 *   <li>销售报表：销售统计、销售趋势、客户分析</li>
 *   <li>库存报表：库存统计、库存预警、库存周转</li>
 *   <li>采购报表：采购统计、供应商分析、采购趋势</li>
 *   <li>财务报表：收支统计、利润分析、现金流分析</li>
 *   <li>自定义报表：支持用户自定义报表模板</li>
 *   <li>报表导出：支持PDF、Excel、Word等多种格式导出</li>
 * </ul></p>
 *
 * <p>技术特性：
 * <ul>
 *   <li>基于Spring Boot 3.4.2框架</li>
 *   <li>集成Spring Cloud服务发现</li>
 *   <li>支持Feign客户端调用其他服务</li>
 *   <li>支持多种报表格式导出</li>
 *   <li>支持报表缓存和预计算</li>
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
@EnableFeignClients
public class ReportServiceApplication {

    /**
     * 报告服务主方法
     *
     * <p>启动报告服务微服务，初始化Spring应用上下文。
     * 该方法会自动配置并启动嵌入式Tomcat服务器，监听默认端口。</p>
     *
     * <p>启动流程：
     * <ol>
     *   <li>加载Spring Boot配置文件</li>
     *   <li>初始化Spring应用上下文</li>
     *   <li>注册到服务注册中心</li>
     *   <li>启动嵌入式Web服务器</li>
     *   <li>初始化Feign客户端</li>
     *   <li>加载报表模板和配置</li>
     * </ol></p>
     *
     * @param args 命令行参数，支持Spring Boot标准参数，如：
     *               --server.port指定端口号
     *               --spring.profiles.active指定激活的配置文件
     * @see SpringApplication#run(Class, String...)
     * @since 3.0.0
     */
    public static void main(String[] args) {
        SpringApplication.run(ReportServiceApplication.class, args);
    }
}
