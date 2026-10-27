# CLAUDE.md — Eifi

Context for Claude Code. Read this file in full before any task, then the `docs/` document relevant
to the current ticket.

## The product in short

Eifi (pronounced "eye-fye") is a contactless payment Android app for Cameroon. A single app brings
together two modes:

- **Receive** ("Encaisser", "Collect" in the English UI): you enter an amount, the app shows a dynamic QR code and emits the
  same URL over NFC.
- **Pay** ("Payer"): you tap your phone or scan. The app shows the recipient, the amount and any
  fees, asks for biometrics, then launches the pre-filled Orange Money or MTN MoMo USSD code on the
  right SIM. The payer types their PIN in the operator's prompt.

Eifi never touches the money. The payment is confirmed on the payee's side by reading the
operator's credit SMS, never from the payer's screen.

## Reference documents

The documents kept in Claude prevail; they are written in French. The files in `docs/` are English
working copies from October 26, 2026.

| Document                              | Local copy                   | Reference                                         |
|---------------------------------------|------------------------------|---------------------------------------------------|
| Product requirements (what and why)   | `docs/cahier-des-charges.md` | https://claude.ai/artifact/32DZB7Fg15dCH5Payt8w2D |
| Technical design (how)                | `docs/dossier-technique.md`  | https://claude.ai/artifact/UCyVMkH69ju46QPDJ9fKfx |
| Mobile tickets v1 (no backend)        | `docs/tickets.md`            | https://claude.ai/artifact/9ojG4Ba9ksS7V8EAASASRR |
| Mockups (canvas)                      | `docs/design/*.png`          | https://claude.ai/artifact/Kdt6ugqJn828TZ2fXQXK2g |

Before implementing a screen, open the matching mockup in `docs/design/` (read the image). Square
brackets `[ ]` in the mockups mark real content to be provided; amounts and times are examples.

## Decisions taken (do not question them unless asked)

- **A single app**, two modes. The "merchant" is not an account type: it is a *receiving profile*
  (display name + for each operator a merchant code **or** a phone number).
- **No backend for the pilot.** The session travels in the URL fragment:
  `https://pay.<domain>/p#v1.{base64url}` (random id, name ≤ 30 characters, amount, receiving
  method per operator, expiry, checksum). SMS ↔ session matching happens locally in Room. USSD
  templates and SMS parsers are embedded. Only static hosting serves `assetlinks.json` and a
  holding page.
- **NFC**: the payee emulates an NFC Forum Type 4 tag (NDEF URI) with a `HostApduService`, AID
  `D2760000850101`. The payer reads with `NfcAdapter.enableReaderMode`. The QR code and NFC carry
  exactly the same URL.
- **USSD**: `TelecomManager.placeCall` with the `PhoneAccountHandle` of the chosen SIM
  (`CALL_PHONE`, `READ_PHONE_STATE`), `#` encoded as `%23`. The real syntax per operator is unknown
  until EIFI-001 is done: never invent it, keep it as a configurable template.
- **Mandatory biometrics** (`BiometricPrompt`, BIOMETRIC_STRONG or DEVICE_CREDENTIAL) before each
  USSD launch. The operator PIN is never requested nor seen by Eifi.
- **SMS**: `NotificationListenerService` only, **never** `READ_SMS` / `RECEIVE_SMS` (Play Store).
  Raw text is never stored, only amount, masked number, reference and a SHA-256 hash.
- **Session**: PENDING → CONFIRMED / EXPIRED / CANCELLED, final states are permanent, 2 min expiry,
  only one PENDING session per amount, matching possible up to 10 min after expiry, otherwise
  "unmatched payment". Manual confirmation possible (yellow "Manuel" badge).
- **Pay vs Send**: the button says "Payer" for a merchant code, "Envoyer" for a transfer to a
  number. A transfer costs the payer fees + the 0.2% tax, shown before biometrics, and only works
  between accounts of the same operator.
- **Payer receipt**: "Débit confirmé" (Debit confirmed) if the debit SMS is detected (option
  disabled by default), otherwise "Demande envoyée" (Request sent). Its layout must stay different
  from the payee's "Paiement reçu" (Payment received) screen (screenshot fraud prevention).
- **Permissions requested when they are needed**, never during the shared onboarding; a refusal
  never blocks the flow.
- **Design**: **plain** version only (the gradient variant is dropped for the pilot). Restrained
  iOS-inspired style.

## Stack and conventions

- Kotlin, Jetpack Compose, themed Material 3. minSdk 26, compile/target 36. JDK 17.
- UDF architecture: ViewModel + state in `StateFlow`, coroutines. Hilt, Room, DataStore and
  Navigation Compose arrive with EIFI-012.
- Gradle Kotlin DSL, `gradle/libs.versions.toml` catalog, convention plugins in `build-logic/`:
    - `eifi.android.application`, `eifi.android.library`, `eifi.android.compose`,
      `eifi.android.feature` (library + compose + `:core:design`), `eifi.jvm.library` (pure Kotlin).
- Kotlin sources in `src/main/kotlin` and `src/test/kotlin`.
- Type-safe module accessors: `implementation(projects.core.design)`.
- Pure business logic (payload, parsers, matching, templates) in Kotlin without Android
  dependencies whenever possible, covered by JVM tests.
- User-facing texts: `strings.xml` resources in French (default) **and** English (`values-en`).
  Never hard-coded text in a composable.
- Amounts: integers in FCFA (`Long`), displayed with `formatFcfa()` (thin space U+202F as
  thousands separator).
- Quality: `./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug` must pass before a ticket
  is considered done. Fix formatting with `./gradlew ktlintFormat`.
- Naming: the two modes are `Pay` and `Receive` in code and documentation, and the person who gets
  the money is the *payee*. The UI keeps « Encaisser » in French and "Collect" in English.
- Everything in English: code, identifiers, comments, KDoc, commit messages and documentation.
  Only the user-facing French strings stay in French.

### Modules

| Module                | Content                                             | Tickets       |
|-----------------------|-----------------------------------------------------|---------------|
| `:app`                | Application, assembly of the modes, signing         | 010, 012      |
| `:feature:onboarding` | Shared onboarding, mode choice, home                | 030, 031, 032 |
| `:feature:pay`        | Pay mode screens                                    | 200–208       |
| `:feature:receive`    | Receive mode screens                                | 100–109       |
| `:core:design`        | Theme, colours, type, components, formatting        | 011           |
| `:core:payload`       | Payment URL encoding/decoding (pure Kotlin)         | 020           |
| `:core:nfc`           | Type 4 HCE and NDEF reader                          | 104, 200      |
| `:core:ussd`          | USSD templates, SIM, call launch                    | 202, 204      |
| `:core:sms`           | Notification listening, SMS parsers                 | 105, 206      |

`:core:network` is postponed along with the backend.

### Design tokens (plain version)

| Token      | Value     | Usage                                    |
|------------|-----------|------------------------------------------|
| primary    | `#00DBAD` | Main actions                             |
| on-primary | `#04362B` | Text on primary (never white)            |
| accent     | `#FEC447` | Waiting, fees, manual confirmation       |
| soft       | `#DDFDBC` | NFC banner, notes, operator pills        |
| background | `#F6F8F7` | Screen background                        |
| surface    | `#FFFFFF` | Cards, keypad keys                       |
| ink        | `#0E1A17` | Main text                                |
| muted      | `#56665F` | Secondary text                           |

Plus Jakarta Sans font (OFL, variable font in `core/design/src/main/res/font`). Amount 68/800, body
15/500, labels 13/600. Radii: cards 22 to 28, buttons 18. Touch targets ≥ 44 dp. The logo has not
been chosen yet.

## Progress

- [x] **EIFI-010** — multi-module skeleton, CI, separate debug/release signing. Dependency versions
  were pinned offline: the first sync may need adjustments (see README).
- [x] **EIFI-011** — Compose design system in `:core:design`: embedded font, tokens `EifiColors` /
  `EifiSpacing` / `EifiDimens` / `EifiRadius`, components `EifiPrimaryButton`,
  `EifiSecondaryButton`, `EifiCard`, `EifiPill` (+ `EifiPillTone`), `NumericKeypad`,
  `AmountDisplay`; catalogue `catalog/DesignSystemCatalog.kt`; contrast checked by
  `ContrastTest`. Use these components in screens rather than raw Material.
- [ ] **Next ticket: EIFI-012**, then EIFI-020, then EIFI-030… (order in `docs/tickets.md`).
- Explorations EIFI-001 to 003: done by Krawist on real phones and accounts. Until they are done,
  USSD templates, SMS senders, MNC codes and the fee schedule remain values to be provided, never
  invented.

Update this section at the end of each ticket.

## Way of working

- One ticket at a time, on an `eifi-0xx-description` branch, with small, readable commits.
- Reread the ticket's acceptance criteria in `docs/tickets.md` before coding and check them at the
  end.
- If a product choice is not settled by the documents, ask rather than decide alone.
