# Admin Dashboard (admin-web)

The admin dashboard is a React 18 single-page application built with Vite, TypeScript, and Ant Design. Program managers use it to manage curricula, CHWs, and monitor visits.

## Running Locally

```bash
cd services/admin-web
yarn install
yarn start
```

The dev server starts on **http://localhost:5173** by default. It proxies API requests to `http://localhost:8080`.

Make sure the API service is running before using the dashboard.

## Project Structure

```
services/admin-web/
├── etc/nginx/           # Nginx config for production container
├── public/              # Static assets served as-is
├── src/
│   ├── assets/          # Images, SVGs
│   ├── components/      # Shared components
│   ├── constants/       # App-wide constants
│   ├── hooks/           # Custom React hooks
│   ├── icons/           # Icon components
│   ├── locales/         # i18n translations (en/, zh/)
│   ├── models/          # TypeScript interfaces and types
│   ├── pages/           # Route-level page components
│   │   ├── Babies/      # Family management
│   │   ├── Baby/        # Baby detail view
│   │   ├── Curriculum/  # Curriculum editor
│   │   ├── Module/      # Module content editor
│   │   ├── Projects/    # Project management
│   │   ├── User/        # User detail
│   │   └── Users/       # User list
│   ├── store/           # Zustand state stores
│   ├── stories/         # Storybook stories
│   ├── tests/           # Test utilities
│   ├── utils/           # Utility functions
│   ├── App.tsx          # Root component
│   ├── config.ts        # Runtime config
│   ├── i18n.ts          # i18n setup (i18next)
│   ├── Layout.tsx       # Shell layout (sidebar + header)
│   ├── main.tsx         # Entry point
│   ├── Router.tsx       # Route definitions
│   └── theme.ts         # Ant Design theme tokens
├── Dockerfile           # Production build (multi-stage: build + Nginx)
├── package.json
├── vite.config.ts       # Vite configuration
├── vitest.config.ts     # Vitest configuration
└── tsconfig.json        # TypeScript config
```

## Adding a New Page

1. **Create a page directory** in `src/pages/`:

```
src/pages/MyFeature/
├── MyFeature.tsx        # Main page component
├── MyFeature.test.tsx   # Tests
└── index.ts             # Re-export
```

2. **Add a route** in `src/Router.tsx`:

```tsx
<Route path="/my-feature" element={<MyFeature />} />
```

3. **Add navigation** in `src/Layout.tsx` to include the page in the sidebar menu.

4. **Add translations** for any user-facing strings in both `src/locales/en/` and `src/locales/zh/`.

## Ant Design Components

The dashboard uses [Ant Design 5.x](https://ant.design/) for all UI components. Common patterns:

```tsx
import { Table, Button, Form, Input, message } from "antd";
```

- **Tables** for data lists with sorting and pagination
- **Forms** with Formik for data entry (not Ant Design's built-in Form)
- **Modals** for confirmation dialogs
- **Messages** for success/error notifications

Theme customization is in `src/theme.ts`, which overrides Ant Design's design tokens.

## State Management (Zustand)

Global state is managed with [Zustand](https://github.com/pmndrs/zustand) stores in `src/store/`:

| Store | File | Purpose |
|-------|------|---------|
| User | `user.ts` | Authentication state, current user |
| Network | `network.ts` | Axios instance, request interceptors, API helpers |
| Module | `module.ts` | Module editor state |

Example usage:

```tsx
import { useUserStore } from "@/store/user";

function MyComponent() {
  const user = useUserStore((state) => state.user);
  // ...
}
```

## API Communication

API calls go through the network store (`src/store/network.ts`), which provides an Axios instance with:

- Base URL configuration
- JWT token injection via request interceptor
- Error handling and response parsing

```tsx
import { useNetworkStore } from "@/store/network";

const { get, post } = useNetworkStore.getState();
const data = await get("/api/admin/users");
```

## Styling

The project uses a mix of:

- **Ant Design** component props for layout and theming
- **Emotion CSS** for custom styles (`@emotion/css`)
- **Styled Components** for component-level styles

## Running Tests

```bash
# Run tests in watch mode
yarn test

# Run tests once with coverage
yarn test:ci
```

Tests use:
- [Vitest](https://vitest.dev/) as the test runner
- [React Testing Library](https://testing-library.com/react) for component tests
- [MSW](https://mswjs.io/) (Mock Service Worker) for API mocking

## Storybook

The project includes [Storybook](https://storybook.js.org/) for component development:

```bash
# Start Storybook
yarn storybook

# Build Storybook
yarn build-storybook
```

Stories live in `src/stories/` and alongside components.

## Building for Production

```bash
yarn build
```

The production build is output to `dist/`. In the Docker container, Nginx serves these files and proxies API requests to the backend.

## Linting and Formatting

```bash
# Lint
yarn lint

# Format
yarn format
```

ESLint and Prettier run automatically via Husky pre-commit hooks.
