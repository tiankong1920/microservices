# Config Service Cross-Service Testing Framework

## 1. Overview

This testing framework provides comprehensive cross-service testing capabilities for the Config Service. It enables testing of:

- Core configuration service functionality
- Integration with other services
- Configuration client behavior
- Multi-environment configuration support
- Configuration format support (YAML/JSON)
- Error handling and edge cases

## 2. Framework Structure

```
src/test/
├── java/com/inventory/configservice/test/
│   ├── ConfigServiceTestBase.java         # Base test class with common utilities
│   ├── ConfigServiceIntegrationTest.java   # Core functionality tests
│   ├── ConfigServiceCrossServiceTest.java  # Cross-service integration tests
│   └── TestConfig.java                     # Test configuration beans
├── resources/
│   └── application-test.yml               # Test environment configuration
└── README.md                               # This documentation
```

## 3. Test Coverage

### 3.1 Core Functionality Tests

| Test Case | Description |
|-----------|-------------|
| `testGetProductServiceDevConfig` | Tests fetching product-service's dev environment configuration |
| `testGetProductServiceJsonConfig` | Tests fetching configuration in JSON format |
| `testGetNonExistentConfig` | Tests handling of non-existent configuration requests |
| `testGetDifferentProfiles` | Tests multi-environment configuration support |
| `testHealthCheck` | Tests health check endpoint |
| `testInfoEndpoint` | Tests info endpoint |

### 3.2 Cross-Service Integration Tests

| Test Case | Description |
|-----------|-------------|
| `testConfigClientPropertySource` | Tests ConfigServicePropertySourceLocator functionality |
| `testConfigClientProperties` | Tests ConfigClientProperties configuration |
| `testSimulateServiceConfigFetch` | Simulates a complete service configuration fetch flow |
| `testConfigServiceHighAvailability` | Tests handling of multiple simultaneous service requests |

## 4. Testing Utilities

### 4.1 Base Test Class (`ConfigServiceTestBase`)

Provides common utilities for all tests:

- **Setup/Teardown**: Automatic test environment setup
- **URL Building**: Methods to construct configuration service URLs
- **Request Helpers**: Pre-configured RestTemplate with JSON support
- **Validation Methods**: Utilities to validate configuration content

### 4.2 Configuration Management

- **Test Profile**: Uses `test` profile for isolated testing
- **Separate Port**: Runs on port 8889 to avoid conflict with production
- **Disabled Eureka**: Does not register with Eureka during testing
- **Local Git Repo**: Uses local Git repository for configuration storage

## 5. Test Execution

### 5.1 Prerequisites

- JDK 17 or higher
- Maven 3.6+ (for running tests)
- Local Git repository at `/e:/101/config-repo` (can be configured in `application-test.yml`)

### 5.2 Running Tests

#### Run All Tests

```bash
mvn test
```

#### Run Specific Test Class

```bash
mvn test -Dtest=ConfigServiceIntegrationTest
```

#### Run Specific Test Method

```bash
mvn test -Dtest=ConfigServiceIntegrationTest#testGetProductServiceDevConfig
```

### 5.3 Running Tests with Coverage

```bash
mvn test jacoco:report
```

Coverage report will be available at `target/site/jacoco/index.html`

## 6. Test Execution Flow

```
1. Test initialization (JUnit 5 extension)
2. Spring Boot context startup with test profile
3. Test method execution:
   a. Build configuration service URL
   b. Send HTTP request to config service
   c. Validate response status and content
   d. Assert expected configuration values
4. Test cleanup
5. Spring Boot context shutdown
```

## 7. Test Configuration

### 7.1 Application Configuration (`application-test.yml`)

```yaml
spring:
  application:
    name: config-service-test
  profiles:
    active: test
  cloud:
    config:
      server:
        git:
          uri: file:///e:/101/config-repo
          search-paths:
            - '{application}'
          clone-on-start: true

server:
  port: 8889

# Disable Eureka registration
eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

### 7.2 Test Config Beans (`TestConfig.java`)

- Configures `ConfigClientProperties` with test settings
- Configures `ConfigServicePropertySourceLocator` for testing
- Provides primary beans to override default configurations

## 8. Test Result Interpretation

### 8.1 Success Criteria

- All tests pass with `BUILD SUCCESS`
- No compilation errors
- No runtime exceptions
- Expected configuration values are present
- Correct HTTP status codes are returned

### 8.2 Common Failure Scenarios

| Failure | Possible Cause | Resolution |
|---------|----------------|------------|
| Git repository not found | Invalid Git URI | Check `spring.cloud.config.server.git.uri` |
| Port already in use | Port 8889 is occupied | Change port in `application-test.yml` |
| Configuration not found | Missing configuration files | Add required configuration files to Git repo |
| Eureka connection failure | Eureka server not running | Ensure Eureka is disabled in test profile |

## 9. Extending the Framework

### 9.1 Adding New Test Cases

1. Create a new test method in an existing test class
2. Use `ConfigServiceTestBase` utilities to build URLs and send requests
3. Validate responses using assertions
4. Follow naming convention: `test[Description]`

### 9.2 Adding New Test Classes

1. Extend `ConfigServiceTestBase`
2. Add `@ExtendWith(SpringExtension.class)`, `@SpringBootTest`, and `@ActiveProfiles("test")` annotations
3. Implement test methods following the same pattern

### 9.3 Adding New Test Utilities

1. Add utility methods to `ConfigServiceTestBase`
2. Ensure methods are generic and reusable
3. Document methods with Javadoc

## 10. Best Practices

1. **Isolation**: Tests should not depend on external services (except local Git repo)
2. **Reusability**: Use the base class utilities instead of duplicating code
3. **Clear Naming**: Test methods should clearly describe what they test
4. **Comprehensive Coverage**: Test both success and failure scenarios
5. **Maintainability**: Keep tests simple and focused
6. **Documentation**: Document test cases and their expected behavior

## 11. CI/CD Integration

This test framework can be easily integrated into CI/CD pipelines:

```yaml
# Example GitHub Actions workflow
name: Config Service Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Run tests
        run: cd microservices/config-service && mvn test
```

## 12. Troubleshooting

### 12.1 Common Issues

- **Test Context Failed to Load**: Check Spring Boot dependencies and configuration
- **Git Repository Issues**: Ensure Git repo exists and is accessible
- **Port Conflicts**: Change test port in `application-test.yml`
- **Configuration Not Found**: Verify configuration files exist in the Git repository

### 12.2 Debugging Tips

- Enable debug logging: Add `-Dlogging.level.org.springframework.cloud=DEBUG` to test command
- Use IDE debugging features to step through tests
- Check target/test-classes directory for generated test resources
- Review Maven output for dependency resolution issues

## 13. Conclusion

This testing framework provides a comprehensive solution for testing the Config Service's functionality and integration with other services. It follows best practices for test design, maintainability, and extensibility, making it a valuable tool for ensuring the reliability and correctness of the configuration service.
