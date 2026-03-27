# Mobile App

The mobile app is an Expo/React Native application targeting Android. CHWs use it to conduct home visits, register families, and sync data with the server.

## Running Locally

```bash
cd services/app
yarn install

# Start Expo dev server
npx expo start
```

Options for testing:
- **Android Emulator:** Press `a` to launch in a connected emulator (requires Android Studio)
- **Physical Device:** Install Expo Go on an Android phone, scan the QR code
- **Development Build:** Build a custom dev client for testing native modules

Set the API URL to your local machine's network IP:

```bash
EXPO_PUBLIC_API_URL=http://192.168.1.100:8080 npx expo start
```

Do not use `localhost` -- the mobile device needs a network-accessible address.

## Project Structure

```
services/app/
├── src/
│   ├── @types/          # TypeScript declarations
│   ├── actions.js       # Redux action creators
│   ├── assets/          # Images, fonts, splash screen
│   ├── cache/           # Offline data caching utilities
│   ├── components/      # Shared React Native components
│   ├── config.js        # API URL, app configuration
│   ├── constants/       # App-wide constants
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

## Offline Support

The app is designed for use in areas with limited or no connectivity:

1. **Curriculum data** is downloaded and cached locally when the CHW syncs.
2. **Visits can be completed offline** -- all visit data is stored locally.
3. **Data syncs automatically** when the device regains connectivity.
4. Local storage uses `@react-native-async-storage/async-storage` and the cache utilities in `src/cache/`.

## Navigation

Screen navigation uses [React Navigation](https://reactnavigation.org/) v6:

- **Stack Navigator** (`@react-navigation/stack`) for screen-to-screen navigation
- **Bottom Tab Navigator** (`@react-navigation/bottom-tabs`) for main app sections

Navigation configuration lives in `src/navigation/`.

## Building APKs

### Preview Build (for testing)

Uses EAS Build to create a distributable APK:

```bash
npx eas-cli build --profile preview --platform android
```

This produces an APK (not AAB) suitable for sideloading onto test devices. See `eas.json` for profile configuration.

### Development Build

```bash
npx eas-cli build --profile development --platform android
```

Creates a debug APK with dev tools enabled.

### Production Build

```bash
npx eas-cli build --profile production --platform android
```

Creates a release build. The production profile uses:
- ProGuard for code shrinking
- Resource shrinking
- Production API URL (`EXPO_PUBLIC_API_URL`)

### Build Environment Variables

Build-time variables are set in `eas.json` under each profile:

| Variable | Description |
|----------|-------------|
| `EXPO_PUBLIC_APP_NAME` | App display name |
| `EXPO_PUBLIC_APP_VERSION` | Version string |
| `EXPO_PUBLIC_API_URL` | Backend API URL |
| `EXPO_PUBLIC_ANDROID_PACKAGE` | Android package name (different per profile to allow side-by-side installs) |

## Running on a Device/Emulator

### Android Emulator

1. Install Android Studio and create an AVD (Android Virtual Device).
2. Start the emulator.
3. Run `npx expo start` and press `a`.

### Physical Device

1. Enable Developer Options and USB Debugging on the device.
2. Connect via USB.
3. Run `npx expo start` and press `a`.

Or use Expo Go:
1. Install Expo Go from the Google Play Store.
2. Run `npx expo start`.
3. Scan the QR code with the Expo Go app.

## Running Tests

```bash
# Run tests in watch mode
yarn test

# Run tests once with coverage (CI mode)
yarn test:ci
```

Tests use Jest with the `jest-expo` preset and React Native Testing Library.

## Linting

```bash
yarn lint
```

Uses ESLint with the `expo` config and Prettier for formatting.
