name: Datasource API Contract
description: Contract for Datasource Service REST API
priority: 1

request:
  method: GET
  urlPath: /api/v1/datasources/1
  headers:
    Content-Type: application/json
    Accept: application/json

response:
  status: 200
  headers:
    Content-Type: application/json
  body:
    code: 200
    message: "数据源详情获取成功"
    data:
      id: 1
      name: "test-mysql"
      type: "MYSQL"
      host: "localhost"
      port: 3306
      databaseName: "test_db"

---
name: Create Datasource Contract
description: Contract for creating a new datasource
priority: 1

request:
  method: POST
  urlPath: /api/v1/datasources
  headers:
    Content-Type: application/json
    Accept: application/json
  body:
    name: "test-mysql"
    type: "MYSQL"
    host: "localhost"
    port: 3306
    databaseName: "test_db"
    username: "root"
    password: "password"

response:
  status: 200
  headers:
    Content-Type: application/json
  body:
    code: 200
    message: "数据源创建成功"
    data:
      id: 1
      name: "test-mysql"
      type: "MYSQL"
