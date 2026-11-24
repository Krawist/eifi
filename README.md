# Eifi

Contactless payment for Cameroon: tap or scan, confirm with biometrics, type your Orange Money or MTN MoMo PIN. A single Android app, two modes: **Pay** and **Receive**.

The documentation is in [`docs/`](docs/) and the context for Claude Code in [`CLAUDE.md`](CLAUDE.md).

## Requirements

- A recent Android Studio (compatible with AGP 9.4), JDK 17
- An Android 8.0+ phone (NFC recommended)

## Getting started

```bash
./gradlew assembleDebug        # debug APK: app/build/outputs/apk/debug/
./gradlew installDebug         # installs on the connected phone
```

Checks run by CI on every pull request:

```bash
./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug
```

## Signing

- **debug**: Android Studio's local debug key, `applicationId` suffixed with `.debug` (both variants can be installed side by side on the phone).
- **release**: copy `keystore.properties.example` to `keystore.properties` and fill it in. This file and the `.jks` files are ignored by git. Without it, the release build is unsigned.

Create the key once:

```bash
mkdir -p keystore
keytool -genkeypair -v -keystore keystore/eifi-release.jks -alias eifi -keyalg RSA -keysize 4096 -validity 10000
```

## Structure

```
app/                  application, assembly of the modes
feature/onboarding    shared onboarding, mode choice, home
feature/pay           Pay mode
feature/receive       Receive mode
core/design           Compose theme and components
core/payload          payment URL format (pure Kotlin)
core/nfc              Type 4 HCE and NDEF reader
core/ussd             USSD templates and launch on the right SIM
core/sms              SMS notification reading
build-logic/          Gradle convention plugins
docs/                 product requirements, technical design, tickets, mockups
```
