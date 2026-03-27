# Testing

Each service has its own test setup. This page describes how to run tests and the testing conventions for each.

## API (Spring Boot)

### Running Tests

```bash
cd services/api

# Run all tests
./gradlew test

# Run tests with coverage report
./gradlew test jacocoTestReport
```

Coverage reports are generated in `build/jacoco/`.

### Test Stack

- **JUnit 5** (via `spring-boot-starter-test`)
- **Spring Security Test** for authentication testing
- **H2 in-memory database** for tests (no MySQL required)

### Conventions

- Test classes go in `src/test/java/` mirroring the main source structure.
- Use `@SpringBootTest` for integration tests that need the full application context.
- Use `@WebMvcTest` for controller-only tests.
- The H2 database is used automatically in tests, so no Docker MySQL is needed.
- Coverage excludes `config/`, `jwt/`, `security/`, `converter/`, `logging/`, `pojo/`, `entity/`, `dto/` packages (see `build.gradle` JaCoCo config).

### Example

```java
@SpringBootTest
class ExampleServiceTest {

    @Autowired
    private ExampleService exampleService;

    @Test
    void shouldCreateExample() {
        // arrange
        CreateExampleDTO dto = new CreateExampleDTO("test");

        // act
        ExampleDTO result = exampleService.create(dto);

        // assert
        assertNotNull(result.getId());
        assertEquals("test", result.getName());
    }
}
```

## Admin Dashboard (Vite/React)

### Running Tests

```bash
cd services/admin-web

# Run tests in watch mode
yarn test

# Run tests once with coverage (CI mode)
yarn test:ci
```

### Test Stack

- **Vitest** as the test runner
- **React Testing Library** (`@testing-library/react`) for component tests
- **MSW** (Mock Service Worker) for API mocking
- **jsdom** as the test environment

### Conventions

- Test files go next to the component they test, named `*.test.tsx` or `*.test.ts`.
- Use React Testing Library's `render`, `screen`, and `userEvent` for component interaction tests.
- Mock API calls with MSW handlers rather than mocking Axios directly.
- Setup file: `vitest-setup.js` (configures jsdom and Testing Library matchers).

### Example

```tsx
import { render, screen } from "@testing-library/react";
import { describe, it, expect } from "vitest";
import MyComponent from "./MyComponent";

describe("MyComponent", () => {
  it("renders the title", () => {
    render(<MyComponent title="Hello" />);
    expect(screen.getByText("Hello")).toBeInTheDocument();
  });
});
```

## Mobile App (Expo/React Native)

### Running Tests

```bash
cd services/app

# Run tests in watch mode
yarn test

# Run tests once with coverage (CI mode)
yarn test:ci
```

### Test Stack

- **Jest** with `jest-expo` preset
- **React Native Testing Library** (`@testing-library/react-native`)
- **React Test Renderer** for snapshot tests

### Conventions

- Test files go in `src/screens/__tests__/` or next to the component.
- Setup file: `jest.setup.js` (mocks native modules and environment).
- The `transformIgnorePatterns` in `package.json` are configured to handle React Native's module structure.

### Example

```jsx
import { render, screen } from "@testing-library/react-native";
import MyScreen from "../MyScreen";

describe("MyScreen", () => {
  it("renders correctly", () => {
    render(<MyScreen />);
    expect(screen.getByText("Welcome")).toBeTruthy();
  });
});
```

## Running All Tests

From the repository root, you can run tests for all services:

```bash
# API
cd services/api && ./gradlew test && cd ../..

# Admin Dashboard
cd services/admin-web && yarn test:ci && cd ../..

# Mobile App
cd services/app && yarn test:ci && cd ../..
```

## Code Quality Tools

| Service | Linting | Formatting | Pre-commit |
|---------|---------|-----------|------------|
| API | Spotless (Google Java Format) | `./gradlew spotlessApply` | Lefthook |
| Admin Web | ESLint | Prettier (`yarn format`) | Husky + lint-staged |
| Mobile App | ESLint (Expo config) | Prettier | Husky + lint-staged |
