# Developer Guide

This guide is for software developers who want to contribute to CareLoom or run it locally for development.

## Quick Start

1. [Local Setup](local-setup.md) -- get the three services running on your machine
2. [Architecture](architecture.md) -- understand how the services communicate
3. Pick the service you want to work on:
   - [Backend (API)](backend.md) -- Spring Boot, Java 8, Flyway
   - [Admin Dashboard](admin-web.md) -- Vite, React 18, TypeScript, Ant Design
   - [Mobile App](mobile-app.md) -- Expo, React Native, Redux
4. [Database](database.md) -- MySQL schema, Flyway migrations
5. [Testing](testing.md) -- how to run tests for each service

## Sections

| Guide | What It Covers |
|-------|---------------|
| [Local Setup](local-setup.md) | Prerequisites, cloning, running each service locally |
| [Architecture](architecture.md) | How the three services communicate, package structure |
| [Backend](backend.md) | Spring Boot conventions, adding endpoints, Flyway migrations |
| [Admin Dashboard](admin-web.md) | Vite/React project structure, Ant Design, Zustand state |
| [Mobile App](mobile-app.md) | Expo/React Native structure, Redux, building APKs |
| [Database](database.md) | MySQL schema, migration conventions, key tables |
| [Testing](testing.md) | Running tests per service, testing conventions |

## Related

- [Architecture Overview](../getting-started/overview.md) -- high-level system design and data model
- [Environment Variables Reference](../reference/environment-variables.md) -- all config variables across services
- [API Reference](../reference/api-reference.md) -- key REST endpoints
