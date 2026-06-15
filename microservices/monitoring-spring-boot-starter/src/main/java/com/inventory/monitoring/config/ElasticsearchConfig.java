/*
 * Copyright (c) 2026 Inventory Management System. All rights reserved.
 */

package com.inventory.monitoring.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Elasticsearch配置类，用于配置和初始化Elasticsearch 8.x客户端.
 * <p>
 * 该类提供了Elasticsearch Java API Client的配置和初始化功能.
 * 支持通过配置文件设置Elasticsearch的连接参数.
 * </p>
 */
@Configuration
@ConditionalOnProperty(prefix = "inventory.monitoring.elk",
                        name = "enabled",
                        havingValue = "true",
                        matchIfMissing = false)
public class ElasticsearchConfig {

    /**
 * 默认Elasticsearch端口.
     */
    private static final int DEFAULT_ELASTICSEARCH_PORT = 9200;

    /**
 * 默认连接超时时间（秒）.
     */
    private static final int DEFAULT_CONNECTION_TIMEOUT_SECONDS = 30;

    /**
 * Elasticsearch配置属性.
     */
    private final ElasticsearchProperties elasticsearchProperties;

    /**
 * 构造方法，注入Elasticsearch配置属性.
     *
 * @param elasticsearchProperties Elasticsearch配置属性
     */
    public ElasticsearchConfig(final ElasticsearchProperties elasticsearchProperties) {
        this.elasticsearchProperties = elasticsearchProperties;
    }

    /**
 * 创建并配置Elasticsearch Java API Client.
     *
 * @return Elasticsearch客户端
     */
    @Bean(destroyMethod = "close")
    public ElasticsearchClient elasticsearchClient() {
        final ElasticsearchProperties.Host[] hosts = elasticsearchProperties.getHosts();
        final HttpHost[] httpHosts = new HttpHost[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            httpHosts[i] = new HttpHost(hosts[i].getHost(), hosts[i].getPort(), hosts[i].getScheme());
        }
        final RestClientBuilder builder = RestClient.builder(httpHosts);

        // 配置认证
        if (elasticsearchProperties.getUsername() != null
                && elasticsearchProperties.getPassword() != null) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(
                            elasticsearchProperties.getUsername(),
                            elasticsearchProperties.getPassword()));
            builder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        // 配置连接超时
        builder.setRequestConfigCallback(requestConfigBuilder -> {
            requestConfigBuilder.setConnectTimeout(
                    (int) elasticsearchProperties.getConnectTimeout().toMillis());
            requestConfigBuilder.setSocketTimeout(
                    (int) elasticsearchProperties.getSocketTimeout().toMillis());
            requestConfigBuilder.setConnectionRequestTimeout(
                    (int) elasticsearchProperties.getConnectionRequestTimeout().toMillis());
            return requestConfigBuilder;
        });

        // 配置连接池
        builder.setHttpClientConfigCallback(httpClientBuilder -> {
            httpClientBuilder.setMaxConnTotal(elasticsearchProperties.getMaxConnTotal());
            httpClientBuilder.setMaxConnPerRoute(elasticsearchProperties.getMaxConnPerRoute());
            return httpClientBuilder;
        });

        final RestClient restClient = builder.build();
        final ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }

    /**
 * Elasticsearch配置属性类，用于读取Elasticsearch相关的配置.
     */
    @Configuration
    @ConfigurationProperties(prefix = "inventory.monitoring.elasticsearch")
    public static class ElasticsearchProperties {

        /**
 * Elasticsearch主机配置.
         */
        private Host[] hosts = new Host[]{
                new Host("localhost", DEFAULT_ELASTICSEARCH_PORT, "http")};

        /**
 * 用户名（用于认证）.
         */
        private String username;

        /**
 * 密码（用于认证）.
         */
        private String password;

        /**
 * 连接超时时间.
         */
        private Duration connectTimeout = Duration.ofSeconds(10);

        /**
 * Socket超时时间.
         */
        private Duration socketTimeout = Duration.ofSeconds(
                DEFAULT_CONNECTION_TIMEOUT_SECONDS);

        /**
 * 连接请求超时时间.
         */
        private Duration connectionRequestTimeout = Duration.ofSeconds(5);

        /**
 * 最大连接总数.
         */
    private static final int DEFAULT_MAX_CONN_TOTAL = 30;

    /**
 * 每个路由的最大连接数.
         */
    private static final int DEFAULT_MAX_CONN_PER_ROUTE = 10;

    /**
 * 最大连接总数.
         */
    private int maxConnTotal = DEFAULT_MAX_CONN_TOTAL;

    /**
 * 每个路由的最大连接数.
         */
    private int maxConnPerRoute = DEFAULT_MAX_CONN_PER_ROUTE;

        /**
 * 获取Elasticsearch主机配置.
         *
 * @return Elasticsearch主机配置
         */
        public Host[] getHosts() {
            return hosts;
        }

        /**
 * 设置Elasticsearch主机配置.
         *
 * @param hosts Elasticsearch主机配置
         */
        public void setHosts(final Host[] hosts) {
            this.hosts = hosts;
        }

        /**
 * 获取用户名.
         *
 * @return 用户名
         */
        public String getUsername() {
            return username;
        }

        /**
 * 设置用户名.
         *
 * @param username 用户名
         */
        public void setUsername(final String username) {
            this.username = username;
        }

        /**
 * 获取密码.
         *
 * @return 密码
         */
        public String getPassword() {
            return password;
        }

        /**
 * 设置密码.
         *
 * @param password 密码
         */
        public void setPassword(final String password) {
            this.password = password;
        }

        /**
 * 获取连接超时时间.
         *
 * @return 连接超时时间
         */
        public Duration getConnectTimeout() {
            return connectTimeout;
        }

        /**
 * 设置连接超时时间.
         *
 * @param connectTimeout 连接超时时间
         */
        public void setConnectTimeout(final Duration connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        /**
 * 获取Socket超时时间.
         *
 * @return Socket超时时间
         */
        public Duration getSocketTimeout() {
            return socketTimeout;
        }

        /**
 * 设置Socket超时时间.
         *
 * @param socketTimeout Socket超时时间
         */
        public void setSocketTimeout(final Duration socketTimeout) {
            this.socketTimeout = socketTimeout;
        }

        /**
 * 获取连接请求超时时间.
         *
 * @return 连接请求超时时间
         */
        public Duration getConnectionRequestTimeout() {
            return connectionRequestTimeout;
        }

        /**
 * 设置连接请求超时时间.
         *
 * @param connectionRequestTimeout 连接请求超时时间
         */
        public void setConnectionRequestTimeout(final Duration connectionRequestTimeout) {
            this.connectionRequestTimeout = connectionRequestTimeout;
        }

        /**
 * 获取最大连接总数.
         *
 * @return 最大连接总数
         */
        public int getMaxConnTotal() {
            return maxConnTotal;
        }

        /**
 * 设置最大连接总数.
         *
 * @param maxConnTotal 最大连接总数
         */
        public void setMaxConnTotal(final int maxConnTotal) {
            this.maxConnTotal = maxConnTotal;
        }

        /**
 * 获取每个路由的最大连接数.
         *
 * @return 每个路由的最大连接数
         */
        public int getMaxConnPerRoute() {
            return maxConnPerRoute;
        }

        /**
 * 设置每个路由的最大连接数.
         *
 * @param maxConnPerRoute 每个路由的最大连接数
         */
        public void setMaxConnPerRoute(final int maxConnPerRoute) {
            this.maxConnPerRoute = maxConnPerRoute;
        }

        /**
 * Elasticsearch主机配置类.
         */
        public static class Host {

            /**
 * 主机名.
             */
            private String host = "localhost";

            /**
 * 端口号.
             */
            private int port = DEFAULT_ELASTICSEARCH_PORT;

            /**
 * 协议（http或https）.
             */
            private String scheme = "http";

            /**
 * 构造方法.
             */
            public Host() {
            }

            /**
 * 构造方法.
             *
 * @param host   主机名
 * @param port   端口号
 * @param scheme 协议
             */
            public Host(final String host, final int port, final String scheme) {
                this.host = host;
                this.port = port;
                this.scheme = scheme;
            }

            /**
 * 获取主机名.
             *
 * @return 主机名
             */
            public String getHost() {
                return host;
            }

            /**
 * 设置主机名.
             *
 * @param host 主机名
             */
            public void setHost(final String host) {
                this.host = host;
            }

            /**
 * 获取端口号.
             *
 * @return 端口号
             */
            public int getPort() {
                return port;
            }

            /**
 * 设置端口号.
             *
 * @param port 端口号
             */
            public void setPort(final int port) {
                this.port = port;
            }

            /**
 * 获取协议.
             *
 * @return 协议
             */
            public String getScheme() {
                return scheme;
            }

            /**
 * 设置协议.
             *
 * @param scheme 协议
             */
            public void setScheme(final String scheme) {
                this.scheme = scheme;
            }
        }
    }
}
