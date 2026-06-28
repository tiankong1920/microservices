```markdown
# microservices Development Patterns

> Auto-generated skill from repository analysis

## Overview
This skill provides guidance on developing Java-based microservices following the conventions and patterns observed in the `microservices` repository. It covers coding standards, commit practices, and testing patterns to ensure consistency and maintainability across services. While no specific framework is enforced, the repository demonstrates a clear structure and disciplined workflow suitable for scalable microservices projects.

## Coding Conventions

### File Naming
- Use **camelCase** for file names.
  - Example: `orderService.java`, `userRepository.java`

### Import Style
- Use **relative imports** to reference classes and packages within the project.
  - Example:
    ```java
    import com.example.orders.orderService;
    ```

### Export Style
- Use **named exports** (Java's public classes).
  - Example:
    ```java
    public class OrderService {
        // class implementation
    }
    ```

### Commit Messages
- Follow **conventional commit** patterns.
- Prefixes used: `fix`, `chore`
- Example:
  ```
  fix: resolve null pointer exception in paymentService
  chore: update dependencies for user module
  ```

## Workflows

### Fixing a Bug
**Trigger:** When a bug or defect is identified in the codebase.
**Command:** `/fix-bug`

1. Identify and reproduce the bug.
2. Create a new branch for the fix.
3. Implement the fix following coding conventions.
4. Write or update relevant tests.
5. Commit with a message prefixed by `fix:`.
   - Example: `fix: correct calculation in invoiceService`
6. Open a pull request for review.

### Performing Maintenance or Chores
**Trigger:** When performing routine maintenance (e.g., dependency updates, code cleanup).
**Command:** `/chore-maintenance`

1. Create a new branch for the maintenance task.
2. Make the necessary changes (e.g., update dependencies).
3. Ensure all tests pass.
4. Commit with a message prefixed by `chore:`.
   - Example: `chore: remove unused imports from userService`
5. Open a pull request for review.

## Testing Patterns

- Test files follow the `*.test.*` naming convention.
  - Example: `orderService.test.java`
- The specific testing framework is not identified; use standard Java testing frameworks (e.g., JUnit) as appropriate.
- Place test files alongside or in a dedicated test directory, matching the naming pattern.

  ```java
  // Example: orderService.test.java
  import org.junit.Test;
  import static org.junit.Assert.*;

  public class OrderServiceTest {
      @Test
      public void testOrderCreation() {
          // test implementation
      }
  }
  ```

## Commands
| Command           | Purpose                                    |
|-------------------|--------------------------------------------|
| /fix-bug          | Start the bug fixing workflow              |
| /chore-maintenance| Start the maintenance/chore workflow       |
```
