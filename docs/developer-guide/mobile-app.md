# Mobile App

The mobile app is an Expo/React Native application targeting Android. CHWs use it to conduct home visits, register families, and sync data with the server.

## Running locally

Type: how-to.

1. In `services/app`, install locked dependencies:
   ```bash
   yarn install --frozen-lockfile
   ```
   Recovery: If installation fails, retain the lockfile and inspect the error. Check Node/Yarn versions in the [local setup guide](local-setup.md).
2. In `services/app`, select an isolated test API endpoint and start Expo:
   ```bash
   EXPO_PUBLIC_API_URL=http://YOUR_TEST_HOST:8080 npx expo start
   ```
   Recovery: Replace the example host with the actual device-reachable test origin. If connectivity fails, verify the transport with the operator; do not fall back to an existing production endpoint.

The device's `localhost` refers to the device itself. A loopback-only Docker API is not automatically reachable from a phone or emulator. Choose an explicit test transport. A compatible Android development build may be needed for this pinned Expo/native-module stack; do not assume the current Expo Go app can run it.

## Project Structure

```
services/app/
├── src/
│   ├── @types/          # TypeScript declarations
│   ├── actions.js       # Redux action creators
│   ├── assets/          # Images, fonts, splash screen
│   ├── cache/           # Offline data caching utilities
│   ├── components/      # Shared React Native components
│   ├── constants/       # App-wide constants and Config.js API endpoint
│   ├── locales/         # i18n translations (en/, zh/)
│   ├── models/          # Data model types
│   ├── navigation/      # React Navigation route definitions
│   ├── reducers/        # Redux reducers
│   ├── screens/         # Screen components
│   ├── store.js         # Redux store setup
│   ├── i18n.ts          # i18n initialization
│   ├── index.ts         # App entry point
│   └── utils/           # Utility functions
├── app.config.ts        # Expo configuration
├── eas.json             # EAS Build profiles
├── babel.config.js      # Babel configuration
├── package.json
└── tsconfig.json
```

## Key Screens

| Screen | File | Purpose |
|--------|------|---------|
| Sign In | `screens/SignIn.js` | CHW login |
| Home | `screens/Home/` | Dashboard with assigned babies and pending visits |
| Babies | `screens/Babies.js` | List of assigned families |
| Baby | `screens/Baby.js` | Family detail with visit history |
| Baby Form | `screens/BabyForm/` | Register or edit a family |
| Visits | `screens/Visits.js` | List of scheduled visits |
| Visit | `screens/Visit.js` | Active visit session |
| Create Visit | `screens/CreateVisit.js` | Start a new visit |
| Module | `screens/Module.jsx` | Curriculum module content viewer |
| Question | `screens/Question.jsx` | Questionnaire during visit |
| Me | `screens/Me.js` | CHW profile |
| Change Password | `screens/ChangePassword.js` | Password change |

## State Management (Redux)

The app uses Redux for global state:

| Reducer | File | Purpose |
|---------|------|---------|
| `user` | `reducers/user.js` | Auth state, current user, token |
| `net` | `reducers/net.js` | Network connectivity status |
| `lessons_update` | `reducers/lessons_update.js` | Curriculum/lesson sync state |
| `message` | `reducers/message.js` | User-facing messages and alerts |
| `modal` | `reducers/modal.js` | Modal dialog state |

Actions are defined in `actions.js` and dispatched from screen components.

## Offline support

Type: reference.

Sign in online and prepare the assigned family/curriculum data before leaving connectivity. Cached content supports the implemented offline tasks; initial setup, all editor operations and every media asset are not guaranteed offline.

- Family and visit queues retain entries until their corresponding upload is acknowledged. A failed upload leaves that entry and remaining work for retry.
- Sync calls share an in-progress operation. Families are uploaded before queued visits; successful entries are removed individually rather than clearing the whole queue at startup.
- Home submits one retained visit status per refresh. Repeat refresh as needed and verify each expected visit on the server; this path does not drain all pending statuses in one operation.
- Module progress belongs to its visit. Acknowledging that visit clears only its progress; unrelated pending work cannot mark a new visit complete. Unassigned legacy progress is retained for recovery and is not applied to another visit.
- Pending work is bound to the authenticated account and API endpoint. Signing out preserves it. Another account/backend cannot adopt that pending queue.
- Old nonempty queues without recorded ownership are held for supervised recovery. Do not guess an owner, clear app storage or reinstall as a workaround.
- A lost response after a server commit is still ambiguous. Client serialization does not provide server-side exactly-once processing. Reconcile suspected duplicates before submitting the same record again.

After reconnecting, confirm the records reached the server. A network-connected icon or a dismissed message is not confirmation of completed synchronization. Keep the original account until pending work is resolved; contact your operator if the app reports an ownership/recovery hold.

Local cache data uses AsyncStorage. These changes do not establish encrypted storage, backup, clinical performance or complete offline/reconnect readiness. Test the required workflow on the actual Android build with synthetic records before operational use.

## Navigation

Screen navigation uses [React Navigation](https://reactnavigation.org/) v6:

- **Stack Navigator** (`@react-navigation/stack`) for screen-to-screen navigation
- **Bottom Tab Navigator** (`@react-navigation/bottom-tabs`) for main app sections

Navigation configuration lives in `src/navigation/`.

## Building APKs

Type: explanation.

Android builds are deployment-specific. Before invoking a hosted EAS build, configure your own Expo owner/project identity and review `app.config.ts` and `eas.json`. The checked-in identities and production profile belong to the original project; a fork must not accidentally submit a build to that project or connect to its backend.

No prebuilt APK or platform-wide versioned release is established by these instructions. Record the source commit, profile, API endpoint and artifact checksum for each candidate you build. Component version strings are build labels, not an adopted release policy.

### Build profiles

Type: reference.

| Profile | Purpose | Endpoint requirement |
|---------|---------|----------------------|
| `preview` | Sideloadable test APK | Explicit isolated API origin; no production fallback |
| `development` | Development/test Android build | Explicit isolated API origin; no production fallback |
| `production` | Operator-reviewed release configuration | Review the configured production endpoint and app identity for your deployment |

Different Android package names permit side-by-side installation but do not isolate data when profiles point to the same backend. Public `EXPO_PUBLIC_*` values are embedded in the app and must not contain credentials.

### EAS commands

Type: how-to.

1. In your own EAS project and selected build environment, configure `EXPO_PUBLIC_API_URL` to your isolated device-reachable API origin for a preview or development build. Confirm the value is available both during local config evaluation and on the remote builder.
   Recovery: If the endpoint is missing or selects the original production service, stop and correct the selected environment. Local shell variables alone do not establish remote-build configuration.
2. In `services/app`, invoke the intended build profile only after the project identity and environment are verified:
   ```bash
   npx eas-cli build --profile preview --platform android
   ```
   Recovery: If EAS selects the wrong account/project or config validation fails, stop the build and resolve that identity/configuration error.
3. For development work, use the same preparation with:
   ```bash
   npx eas-cli build --profile development --platform android
   ```
   Recovery: If the installed build cannot run the pinned native modules, inspect its build logs and runtime compatibility rather than changing dependency versions blindly.

Operational production builds use `--profile production` only after the operator has reviewed the endpoint, account, application identity, permissions and release artifact. Building and distributing are separate actions.

## Device checks

Type: reference.

Use a compatible Android emulator or test device and the deployment-specific build. Confirm sign-in, prepared curriculum display, offline record creation, failure/retry, reconnect confirmation and account-switch holds with synthetic records. Development tooling must not connect to a real research deployment for convenience.

The local Compose ports bind to loopback. A real device requires an operator-reviewed test transport and a matching API URL; changing the database bind address is not a solution.

## Running Tests

```bash
# Run tests in watch mode
yarn test

# Run tests once with coverage (CI mode)
yarn test:ci --runInBand
```

Tests use Jest with the `jest-expo` preset and React Native Testing Library.

## Linting

```bash
yarn lint
```

Uses ESLint with the `expo` config and Prettier for formatting.
