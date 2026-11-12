# Contributing to CareLoom

Thank you for your interest in contributing to CareLoom! This document provides guidelines for contributing to the project.

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.

## Ways to Contribute

### Report Bugs
Open an issue on GitHub with:
- Steps to reproduce
- Expected vs actual behavior
- Screenshots if applicable
- Your environment (OS, browser, Docker version)

### Suggest Features
Open an issue with the "enhancement" label describing:
- The problem you're trying to solve
- Your proposed solution
- Alternative approaches you've considered

### Submit Code
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Make your changes following the coding conventions below
4. Write or update tests as needed
5. Run the test suite to ensure everything passes
6. Submit a pull request

## Development Setup

See [docs/developer-guide/local-setup.md](docs/developer-guide/local-setup.md) for detailed instructions.

Use the [testing runbook](docs/developer-guide/testing.md) for safe test commands, isolated synthetic data and cleanup. Keep development and preview builds pointed at your own test API. Do not use participant records, existing research services or credentials in tests, screenshots or public issues.

## Coding Conventions

### Backend (services/api)
- Java with Spring Boot conventions
- Flyway for database migrations (never modify existing migrations)
- Follow existing package structure (`dto`, `entity`, `service`, `web/rest`)

### Admin Dashboard (services/admin-web)
- TypeScript with React 18
- Ant Design components
- Zustand for state management
- Vitest for testing

### Mobile App (services/app)
- TypeScript with Expo/React Native
- Redux for state management
- Jest for testing

## Pull Request Process

1. Update documentation if your change affects user-facing behavior
2. Ensure CI passes
3. Request review from a maintainer
4. Squash and merge when approved

## Code of Conduct

See [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
