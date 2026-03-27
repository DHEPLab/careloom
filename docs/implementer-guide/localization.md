# Localization

CareLoom supports multiple languages for the admin dashboard and the mobile app. This page describes current language support and how to add new languages.

## Current Language Support

CareLoom currently supports two languages:

| Language | Code | Coverage |
|----------|------|----------|
| **English** | `en` | Full (admin dashboard, mobile app, API messages) |
| **Chinese (Mandarin)** | `zh` | Full (admin dashboard, mobile app, API messages) |

English is the default language. The admin dashboard and mobile app detect the user's browser or device language and switch automatically if a matching translation is available.

## How Language Detection Works

### Admin Dashboard

The admin dashboard uses [i18next](https://www.i18next.com/) with browser language detection. It checks:

1. The browser's language setting
2. Falls back to English if no matching translation is found

Users can also switch languages manually from the dashboard interface.

### Mobile App

The mobile app uses the device's locale setting via `expo-localization`. It checks:

1. The device's system language
2. Falls back to English if no matching translation is found

## Where Translation Files Live

Translation files are plain JSON files organized by language code:

| Service | Translation Directory |
|---------|-----------------------|
| Admin Dashboard | `services/admin-web/src/locales/en/` and `services/admin-web/src/locales/zh/` |
| Mobile App | `services/app/src/locales/en/` and `services/app/src/locales/zh/` |
| API (error messages) | `services/api/src/main/resources/messages_en.properties` and `messages_zh.properties` |

## Adding a New Language

Adding a new language requires creating translation files in each service. This is a task for a developer or translator working with the codebase.

### What Is Needed

1. **Translation files** for the admin dashboard (JSON format)
2. **Translation files** for the mobile app (JSON format)
3. **Message properties** for the API (Java properties format)
4. **Code changes** to register the new language in the i18n configuration

### Process

1. Copy the English translation directory as a starting point:
   - `services/admin-web/src/locales/en/` to `services/admin-web/src/locales/{language-code}/`
   - `services/app/src/locales/en/` to `services/app/src/locales/{language-code}/`
2. Translate every string in the copied files.
3. Copy `services/api/src/main/resources/messages_en.properties` to `messages_{language-code}.properties` and translate.
4. Register the new language in the i18n configuration files:
   - `services/admin-web/src/i18n.ts`
   - `services/app/src/i18n.ts`
5. Test the translations by switching your browser or device language.
6. Submit the changes as a pull request.

### Translation Tips

- **Do not translate variable placeholders.** Strings may contain placeholders like `{{count}}` or `{0}` that are replaced at runtime. Keep these exactly as they are.
- **Respect string length.** Some UI elements have limited space. Test that translated strings fit in buttons, labels, and menu items.
- **Use formal register.** CareLoom is used in professional health program contexts. Use formal language appropriate for health workers and program managers.

## Curriculum Content Language

The language of curriculum content (module text, media captions) is independent of the interface language. Curriculum content is entered directly by program managers in whatever language is appropriate for their CHWs and families. The system does not translate curriculum content automatically.

If you need the same curriculum in multiple languages, create separate curricula for each language.
