# 进销存系统开发指南

## 概述

本文档集合为进销存系统的开发提供了全面的指导和规范，涵盖了从架构设计到具体实现的各个方面。文档基于现代化微服务架构的最佳实践，旨在帮助开发团队构建高质量、可维护和可扩展的企业级应用。

## 文档结构

### 1. [微服务架构升级指南](microservices-architecture-upgrade-guide.md)
- 当前系统架构分析
- 改进计划和实施路线图
- 各阶段实施优先级
- 最佳实践和注意事项

### 2. [微服务配置管理指南](microservices-configuration-management.md)
- 配置管理架构设计
- 配置文件详解和示例
- 环境配置管理策略
- 敏感信息安全管理
- 配置刷新机制
- 配置最佳实践

### 3. [微服务开发规范](microservices-development-standards.md)
- 微服务设计原则
- 服务拆分规范
- API设计规范
- 代码开发规范
- 数据库设计规范
- 安全规范
- 测试规范
- 部署规范
- 监控规范

### 4. [微服务通信与事件驱动架构指南](microservices-communication-event-driven.md)
- 微服务通信模式（同步/异步）
- 事件驱动架构设计
- 分布式事务处理
- 服务熔断与降级
- 负载均衡与服务发现
- API网关配置
- 监控与追踪
- 安全通信

### 5. [Kotlin开发指南](kotlin-development-guide.md)
- Kotlin语言基础
- Kotlin与Java互操作性
- 项目中Kotlin的使用规范
- Lombok替代方案
- 开发环境配置
- 最佳实践
- 迁移指南
- 测试Kotlin代码

## 使用说明

### 目标读者
- 后端开发工程师
- 系统架构师
- DevOps工程师
- 技术负责人

### 阅读建议
1. 首先阅读[微服务架构升级指南](microservices-architecture-upgrade-guide.md)，了解整体架构设计和实施计划
2. 学习[微服务开发规范](microservices-development-standards.md)，掌握编码标准和最佳实践
3. 参考[微服务配置管理指南](microservices-configuration-management.md)，了解配置管理策略
4. 深入学习[微服务通信与事件驱动架构指南](microservices-communication-event-driven.md)，掌握服务间通信机制
5. 学习[Kotlin开发指南](kotlin-development-guide.md)，掌握Kotlin语言基础和项目使用规范（可选，用于Java到Kotlin的迁移）

## 技术栈概览

- **核心框架**: Spring Boot 3.4.2, Spring Cloud 2024.0.0
- **编程语言**: Java 21 LTS (默认), Kotlin
- **数据库**: PostgreSQL
- **消息队列**: RabbitMQ, Apache Kafka
- **缓存**: Redis, Ehcache
- **安全框架**: Spring Security, JWT
- **监控**: Prometheus, Grafana, Zipkin
- **容器化**: Docker
- **编排**: Kubernetes (计划中)

## 贡献指南

如果您发现文档中的任何问题或有改进建议，请：
1. 创建GitHub Issue说明问题
2. 提交Pull Request进行修改
3. 联系技术负责人进行讨论

## 更新日志

- 2025-11-09: 创建初始版本的开发文档集合

---
*本文档将持续更新，以反映系统的最新设计和实现细节。*
