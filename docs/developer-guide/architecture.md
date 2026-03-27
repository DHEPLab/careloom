# Architecture

CareLoom is a three-service mono-repo. This page describes how the services communicate and how each one is structured internally.

## Service Communication

```
+------------------+          +------------------+
| Admin Dashboard  |   REST   |   API Service    |
| (React/Vite)     +--------->| (Spring Boot)    |
| Port 3000 (prod) |   JSON   | Port 8080        |
| Port 5173 (dev)  |          +--------+---------+
+------------------+                   |
                                  JDBC / JPA
+------------------+                   |
|   Mobile App     |   REST   +--------+---------+
| (Expo/RN)        +--------->|   MySQL 5.7      |
| Android APK      |   JSON   |   Port 3306      |
+------------------+          +------------------+
```

### How it works

- The **API service** is the single source of truth. Both the admin dashboard and mobile app communicate with it over REST (JSON).
- The **admin dashboard** is a static React app served by Nginx in production. In production, Nginx serves the built files and proxies `/api/` requests to the API service. In development, Vite handles proxying.
- The **mobile app** hits the API directly using the `EXPO_PUBLIC_API_URL` configured at build time. It supports offline operation with local caching and syncs when connectivity is restored.
- **MySQL** is the only database. The API connects via JDBC/JPA.

### Authentication Flow

1. Client sends `POST /api/authenticate` with username and password.
2. API validates credentials and returns a JWT.
3. Client includes the JWT in all subsequent requests via `Authorization: Bearer <token>`.
4. Tokens are valid for 30 days (configurable via `application.token-validity-day`).

## API Service Structure

```
services/api/src/main/java/edu/stanford/fsi/reap/
├── config/              # Spring configuration (security, web, etc.)
├── converter/           # JPA attribute converters (JSON to entity mapping)
├── dto/                 # Data Transfer Objects (request/response shapes)
├── entity/              # JPA entities (database table mappings)
│   └── enumerations/    # Enum types (BabyStage, VisitStatus, etc.)
├── handler/             # Exception handlers
├── jwt/                 # JWT token creation and validation
├── logging/             # Request/response logging
├── pojo/                # Plain old Java objects (non-entity models)
├── repository/          # Spring Data JPA repositories
├── security/            # Security configuration, user details
├── service/             # Business logic layer
├── task/                # Scheduled tasks (e.g., visit expiration cron)
├── utils/               # Utility classes
└── web/
    └── rest/            # REST controllers
        ├── admin/       # Admin-only endpoints (/api/admin/*)
        └── errors/      # Error response classes
```

### Key conventions

- **Controllers** define endpoints, delegate to **Services** for business logic.
- **Services** use **Repositories** (Spring Data JPA) for database access.
- **DTOs** define the shape of request and response bodies, separate from entities.
- **Entities** map 1:1 to database tables via JPA annotations.

## Admin Dashboard Structure

```
services/admin-web/src/
├── assets/          # Static assets (images, SVGs)
├── components/      # Shared React components
├── constants/       # Application constants
├── hooks/           # Custom React hooks
├── icons/           # Icon components
├── locales/         # i18n translation files
│   ├── en/          # English translations
│   └── zh/          # Chinese translations
├── models/          # TypeScript type definitions
├── pages/           # Page components (one directory per route)
│   ├── Babies/      # Family management pages
│   ├── Baby/        # Individual baby detail
│   ├── Curriculum/  # Curriculum editor
│   ├── Module/      # Module editor
│   ├── Projects/    # Project management
│   ├── User/        # User detail
│   └── Users/       # User management list
├── store/           # Zustand state management
│   ├── module.ts    # Module state
│   ├── network.ts   # API client and request handling
│   └── user.ts      # Authentication state
├── tests/           # Test utilities
├── utils/           # Utility functions
├── App.tsx          # Root component
├── Layout.tsx       # Main layout (sidebar, header)
├── Router.tsx       # Route definitions
├── config.ts        # Runtime configuration
├── i18n.ts          # i18n initialization
├── main.tsx         # Entry point
└── theme.ts         # Ant Design theme customization
```

### Key conventions

- Pages map to routes defined in `Router.tsx`.
- Zustand stores in `store/` manage global state (auth, network, module data).
- Ant Design components are used throughout. Theme customization is in `theme.ts`.
- API calls go through the network store (`store/network.ts`), which wraps Axios.

## Mobile App Structure

```
services/app/src/
├── @types/          # TypeScript type declarations
├── actions.js       # Redux action creators
├── assets/          # Images, fonts, splash screen
├── cache/           # Offline data caching
├── components/      # Shared React Native components
├── config.js        # API URL and app configuration
├── constants/       # Application constants
├── locales/         # i18n translation files
│   ├── en/          # English translations
│   └── zh/          # Chinese translations
├── models/          # Data models
├── navigation/      # React Navigation setup
├── reducers/        # Redux reducers
│   ├── index.js     # Root reducer
│   ├── lessons_update.js  # Lesson/curriculum state
│   ├── message.js   # Notification messages
│   ├── modal.js     # Modal state
│   ├── net.js       # Network state
│   └── user.js      # Authentication state
├── screens/         # Screen components (one per view)
│   ├── Babies.js    # Family list
│   ├── Baby.js      # Baby detail
│   ├── BabyForm/    # Baby registration form
│   ├── CreateVisit.js  # Start a new visit
│   ├── Home/        # Home screen
│   ├── Module.jsx   # Module content viewer
│   ├── Question.jsx # Questionnaire screen
│   ├── SignIn.js    # Login screen
│   ├── Visit.js     # Visit execution
│   └── Visits.js    # Visit list
├── store.js         # Redux store configuration
├── i18n.ts          # i18n initialization
└── index.ts         # App entry point
```

### Key conventions

- Redux for state management (actions, reducers, store).
- React Navigation for screen routing.
- Offline-first: visits can be conducted without connectivity and synced later.
- Android-only: the app currently targets Android devices.
