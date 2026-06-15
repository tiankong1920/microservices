# Monitoring and Observability Guide

This document provides guidance for monitoring Spring Cloud services in the inventory management system.

## Metrics endpoints
- /actuator/metrics
- /actuator/prometheus
- /actuator/health
- /actuator/info

## Grafana setup
- Install Grafana and add Prometheus as a data source
- Import dashboards for application, JVM, and HTTP metrics
- Configure time range and alerting rules in Prometheus/Grafana

## Alert configuration
- Prometheus alerts defined in docs/monitoring/prometheus-alerts.yml
- Alertmanager routing to on-call channels

## Log analysis
- Centralized logging to ELK (Elasticsearch, Logstash, Kibana)
- Logs are emitted in JSON format for easy parsing

## Notes
- Ensure Spring Profiles set to expose production-friendly endpoints securely
- Verify network policies allow Prometheus and Grafana to reach /actuator/prometheus
