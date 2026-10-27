# Eifi — Technical design v1

> English working copy of the reference document kept in Claude (October 26,
> 2026): https://claude.ai/artifact/UCyVMkH69ju46QPDJ9fKfx. The reference is written in French; if the
> two differ, the Claude document prevails.

This document describes how to build Eifi: the Android app with its Pay and Receive modes, then,
after the pilot, the fallback web page and the backend. It also lists the technical choices that
remain open. Where it differs from the product requirements, this document prevails for technical
matters.

## Principle and architecture

Eifi never touches the money. The payee creates a payment session and passes it on by NFC or QR code
as a URL. The payer's phone then dials their operator's USSD code, and the payer confirms with their
PIN in the operator's prompt. The money goes directly to the payee's account. Confirmation comes
from the credit SMS the operator sends to the payee: Eifi reads it and matches it with the open
session.

| Component         | Role                                                                     | Technology                                  | Pilot |
|-------------------|--------------------------------------------------------------------------|---------------------------------------------|-------|
| Receive mode      | Amount entry, NFC and QR emission, reading credit SMS, history           | Android, Kotlin, Jetpack Compose            | Yes   |
| Pay mode          | NFC and QR reading, choice of account and SIM, biometrics, USSD, receipt | Android, Kotlin, Jetpack Compose (same app) | Yes   |
| Static hosting    | App Links (assetlinks.json) and holding page                             | Static file hosting                         | Yes   |
| Fallback web page | Payment without the app: USSD code and dialer                            | HTML rendered by the backend                | No    |
| Backend           | Backup, multiple devices, remote configuration, notifications            | Kotlin, Micronaut 4, PostgreSQL, Redis      | No    |

How a payment goes during the pilot:

1. The payee enters the amount; the app creates a local session and encodes the payment URL.
2. The app shows the QR code of the URL and enables the NFC emulation that serves the same URL.
3. The payer taps their phone or scans the QR code. With the app, the link opens in Eifi; without
   the app, it opens in the browser.
4. Eifi decodes the session, offers the compatible accounts, asks for biometrics and dials the USSD
   code on the right SIM.
5. The payer enters their PIN in the operator's prompt; the operator credits the payee's account and
   sends them an SMS.
6. The payee's phone reads the SMS notification, matches it with the pending session and shows
   "Paiement reçu" (Payment received).

## One app, two modes

Eifi is a single app with two modes, Pay and Receive. After a shared onboarding, the user chooses
where to start, then the home screen gives access to both. The "merchant" is not an account type: it
is a receiving profile, filled in the first time Receive mode is used. For each operator, this
profile says how to receive the money: with a merchant code or with a phone number.

The receiving method determines the operation the payer launches:

| Receiving method | USSD operation            | Fees for the payer         | Possible operators           |
|------------------|---------------------------|----------------------------|------------------------------|
| Merchant code    | Merchant payment          | None (at Orange Money)     | The merchant code's operator |
| Phone number     | Person-to-person transfer | Transfer fees and 0.2% tax | Same operator on both sides  |

The summary screen shows the fees before confirmation, and the app only offers compatible accounts.
Each operation type has its own USSD template and its own credit and debit SMS formats.

Permissions are requested when they are needed: notification access and disabling battery
optimisation the first time Receive mode is used, phone calls at the first payment, the camera at
the first scan. A user who only pays never has to grant access to their notifications.

## Running without a backend for the pilot

The pilot runs without an application server. The Fallback web page and Backend sections describe
the post-pilot target.

The payment URL carries the whole session in its fragment, which is never sent over the network:
`https://pay.[domain]/p#v1.{data}`. The data, encoded in base64url, contains a random identifier,
the display name, the amount, the receiving method for each operator, the expiry and a checksum.

Matching between the credit SMS and the session happens on the payee's phone, with the rule of a
single pending session per amount, enforced locally in Room. USSD templates and SMS parsers are
embedded in the app. All that remains is static hosting, to publish the App Links `assetlinks.json`
file and a holding page for people without the app.

The main risk of this mode is the lack of remote updates: if an operator changes its SMS format, a
new version will have to be released. A static, signed configuration file, downloaded from the same
hosting, could reduce this risk before the backend arrives.

## NFC: the payee emulates a tag, the payer reads

The payee's phone emulates in HCE (Host Card Emulation) and the payer's phone reads, because the
amount and the receiving method come from the payee.

The phone emulates an NFC Forum Type 4 tag holding an NDEF record of type URI, the payment URL: no
proprietary protocol. A phone without Eifi natively reads this tag and opens the URL in the browser,
on Android as well as on recent iPhones. With the app installed, the URL is a verified App Link and
opens Eifi directly.

A `HostApduService` answers the APDU commands used to read a Type 4 tag:

1. select the NDEF application (AID `D2760000850101`);
2. select and read the Capability Container file;
3. select and read the NDEF file holding the URL.

The service is only active during an open session. The waiting screen registers it as the preferred
service with `CardEmulation.setPreferredService` and removes it on exit; the screen must stay on and
unlocked while waiting for the payment. On the payer's side, Eifi reads the tag with
`NfcAdapter.enableReaderMode` (NFC-A and NFC-B) and extracts the URL from the NDEF message. Tests on
Tecno, Infinix, itel and Samsung are part of the prototype, as HCE behaviour varies between
manufacturers.

## QR code and payment URL

The QR code and the NFC tag carry exactly the same URL. The URL of a typical case must fit in a QR
code of version 8 at most, with error correction Q so that the Eifi icon can sit in the centre, so
it stays easy to scan on a mid-range screen in full sunlight; this is why the display name is
limited to 30 characters. The data is a compact binary layout (see `PayloadV1` in `:core:payload`)
ending with a CRC-32, which detects altered data but is not a signature. A typical URL takes about
90 bytes.

The QR code is always shown on the payee's screen and changes with each session, which makes it hard
to replace with a fake. The payer sees the name and the receiving method before confirming. Signing
the session data is to be studied for the backend version, where the token will become an opaque
22-character identifier resolved on the server.

## Android app

The app is built in a single multi-module Gradle repository, in Kotlin with Jetpack Compose. The
architecture follows unidirectional data flow: ViewModel, state exposed as `StateFlow`, Hilt
injection and coroutines. History is stored in Room and preferences in DataStore. The targeted
versions are Android 8.0 (API 26) at minimum and the latest stable API as target.

| Module                | Content                                                  |
|-----------------------|----------------------------------------------------------|
| `:app`                | Single application, navigation and assembly of the modes |
| `:feature:onboarding` | Shared onboarding, mode choice, home                     |
| `:feature:pay`        | Pay mode screens                                         |
| `:feature:receive`    | Receive mode screens                                     |
| `:core:design`        | Compose theme, colours, Plus Jakarta Sans, components    |
| `:core:payload`       | Encoding and decoding of the payment URL                 |
| `:core:nfc`           | Type 4 HCE service and NDEF reader                       |
| `:core:ussd`          | USSD templates, SIM selection, call launch               |
| `:core:sms`           | Notification listening and SMS parsers                   |

The `:core:network` module is postponed along with the backend.

### USSD launch and SIM choice

The code to dial comes from a template per operator and per operation type, embedded in the app, for
example `{prefix}*{merchantCode}*{amount}#` for a merchant payment. The real syntax for each
operator will be established during the prototype.

The app lists the active SIMs with `SubscriptionManager` (`READ_PHONE_STATE` permission) and
recognises the operator by its network code: MCC 624 for Cameroon, Orange and MTN MNC codes to be
checked. It finds the matching `PhoneAccountHandle` and places the call with
`TelecomManager.placeCall` (`CALL_PHONE` permission), encoding the `#` characters in the `tel:` URI.

Before the call, a mandatory `BiometricPrompt` asks for the fingerprint, face or unlock code
(BIOMETRIC_STRONG or DEVICE_CREDENTIAL). The Orange Money or MoMo PIN is never requested nor seen by
Eifi.

### Reading SMS

On the payee's side, a `NotificationListenerService`, enabled the first time Receive mode is used,
filters the notifications of the default SMS app whose sender matches Orange Money or MTN MoMo. The
embedded parsers, one per operator and per operation type, extract the amount, the counterparty's
number and the reference. An SMS from a known sender that is not recognised is recorded as
"unrecognised", which reveals a format change. The raw text is never kept; only a SHA-256 hash is
used for deduplication.

On the payer's side, the same component can detect the debit SMS if the option is enabled, to show
"Débit confirmé" (Debit confirmed) on the receipt. Onboarding guides the payee to disable battery
optimisation, to avoid missed confirmations on brands that kill background services.

## Fallback web page (after the pilot)

During the pilot, a person without the app lands on a simple holding page inviting them to install
Eifi. After the pilot, this page becomes the fallback page, rendered server-side without a
JavaScript framework so it loads fast on 3G. It shows the payee's name, the amount, the countdown,
an operator selector and the full USSD code. A `tel:` link opens the dialer on Android; on iPhone,
which blocks these links when they contain `*` or `#`, a button copies the code.

## Backend (after the pilot)

The backend is a Kotlin service on Micronaut 4 and Netty, packaged as a container. Persistence uses
Micronaut Data JDBC on PostgreSQL with Flyway migrations, and Redis holds the active sessions and
their expiry timeouts. Authentication uses a JWT (Micronaut Security), after verifying the phone
number with an SMS code. It brings history backup, use on several devices, remote update of USSD
templates and parsers, and the fallback page.

### API

| Method and route                          | Caller | Role                                                     |
|-------------------------------------------|--------|----------------------------------------------------------|
| `POST /v1/auth/otp` and `/v1/auth/verify` | User   | Sign-in with phone number and SMS code                   |
| `PUT /v1/profile/receive`                 | Payee  | Display name, receiving method per operator, SMS senders |
| `POST /v1/sessions`                       | Payee  | Creates a session; returns token, URL and expiry         |
| `POST /v1/sessions/{id}/cancel`           | Payee  | Cancels a pending session                                |
| `POST /v1/sessions/{id}/confirm-manual`   | Payee  | Manual confirmation                                      |
| `GET /v1/sessions?date=`                  | Payee  | History and daily totals per operator                    |
| `POST /v1/sms-events`                     | Payee  | Uploads a parsed credit SMS                              |
| `GET /v1/config`                          | App    | USSD templates, SMS parsers, senders, minimum versions   |
| `WS /v1/events`                           | Payee  | Real-time session updates                                |
| `GET /p/{token}`                          | Public | Fallback page (HTML) or session (JSON)                   |

### Data model

| Table             | Main columns                                                                                                                                      |
|-------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| `app_user`        | id, phone, display_name, created_at                                                                                                               |
| `receive_method`  | user_id, operator (OM, MOMO), kind (MERCHANT_CODE, PHONE), value, sms_sender                                                                      |
| `payment_session` | id, user_id, token, amount, label, status, chosen_operator, created_at, expires_at, confirmed_at, confirmation_source (SMS, MANUAL), sms_event_id |
| `sms_event`       | id, user_id, operator, operation_kind, amount, counterpart_masked, operator_ref, received_at, text_hash, parser_version, matched_session_id       |
| `remote_config`   | key, version, payload (JSON), active                                                                                                              |

Amounts are integers in FCFA. The counterparty's number is stored masked, except for the last two
digits.

### Session lifecycle and matching

| Transition          | Trigger                               |
|---------------------|---------------------------------------|
| creation → PENDING  | Amount entered and "Encaisser" tapped |
| PENDING → CONFIRMED | SMS matched, or manual confirmation   |
| PENDING → EXPIRED   | Delay elapsed (2 minutes by default)  |
| PENDING → CANCELLED | Cancelled by the payee                |

Final states are permanent and each transition is idempotent. Only one session can be pending per
amount, enforced on the server by a Redis key `pending:{userId}:{amount}` set with SETNX. When an
SMS arrives, we look for the PENDING session, or one that expired less than ten minutes ago, with
the same amount and created before the SMS. An SMS without a session becomes an "unmatched payment"
visible in the history, and deduplication uses the operator reference and the text hash. During the
pilot, the same rules apply locally in Room.

## Security

Payment confirmation never relies on what the payer's phone shows. Its end screen is a receipt whose
layout deliberately differs from the payee's "Paiement reçu" screen. It shows "Débit confirmé" only
if the operator's debit SMS was detected; otherwise it says "Demande envoyée" (Request sent).

The QR code is dynamic, short-lived and shown only on the payee's screen. Eifi stores no PIN, no
balance and no operator account identifier, and never keeps the raw SMS text. With the backend,
traffic will go over HTTPS with certificate pinning.

The main residual risk is a fake SMS sent to the payee. It is limited by filtering on the operators'
official senders and by manual confirmation in case of doubt, and will be assessed with the real SMS
collected during the prototype.

## Design guidelines

The interface follows a restrained iOS-inspired style: large amounts with tabular figures, wide
titles, very rounded white cards on a light grey background. A gradient variant, drawn on the
canvas, is set aside: the pilot uses only the plain version, to keep the code simple.

| Token      | Value     | Usage                                                 |
|------------|-----------|-------------------------------------------------------|
| primary    | `#00DBAD` | Main actions, confirmation                            |
| on-primary | `#04362B` | Text and icons on primary (white would be unreadable) |
| accent     | `#FEC447` | Pending states, fees, manual confirmation             |
| soft       | `#DDFDBC` | Soft backgrounds: NFC banner, notes, operator pills   |
| background | `#F6F8F7` | Screen background                                     |
| surface    | `#FFFFFF` | Cards, keypad keys                                    |
| ink        | `#0E1A17` | Main text                                             |
| muted      | `#56665F` | Secondary text                                        |

The font is Plus Jakarta Sans (OFL licence), embedded in the app. The type scale goes from 68 px at
weight 800 for the main amount down to 13 px at weight 600 for labels, with body text at 15 px
weight 500. Cards have a 22 to 28 px radius, buttons 18 px, and touch targets are at least 44 px.
The logo has not yet been chosen among the three options on the design canvas.

The rendered mockups are in [`design/`](design/).

## Open questions

- One-line USSD syntax for merchant payment and transfer at Orange and at MTN.
- Transfer fee schedule of each operator, and exact format of credit and debit SMS.
- Orange and MTN MNC codes in Cameroon, and the sender names of their SMS.
- Reliability of Type 4 tag emulation on common models, and background reading by iPhone.
- Domain name of the payment URL and static hosting.
- Final logo choice.
