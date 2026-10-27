# Product requirements — Eifi

> English working copy of the reference document kept in Claude (October 26, 2026): https://claude.ai/artifact/32DZB7Fg15dCH5Payt8w2D. The reference is written in French; if the two differ, the Claude document prevails.

## Context and goals

Eifi (pronounced "eye-fye") is an Android app with two modes, Pay and Receive. The payee enters the amount; the person paying taps their phone (NFC) or scans a dynamic QR code, then confirms with their PIN in the USSD prompt of their operator, Orange Money or MTN MoMo. The payment can go to a merchant (merchant code) or to an individual (transfer to their phone number), for example between two friends.

Today, a merchant payment goes through a printed QR code or a merchant code: it does not contain the amount, which the customer has to type in. On top of that, each operator has its own app.

Product goals:

- Reduce payment to three gestures for the payer: tap or scan, confirm with biometrics, enter their PIN.
- Bring Orange Money and MTN MoMo together in a single tool, for paying as well as receiving.
- Never hold the funds: the money goes directly, at the operator, from the payer's account to the payee's.
- Confirm the payment on the payee's side from the operator's SMS, never from the payer's screen.

## Scope

The pilot covers face-to-face payment, between a merchant and their customer or between two individuals, through Orange Money and MTN MoMo, on Android and without a backend.

| In the pilot | Out of the pilot |
| --- | --- |
| A single Android app: onboarding and two modes, Pay and Receive | Holding, collecting or paying out funds through the app |
| Receive: amount entry, NFC and QR emission, reading credit SMS, history | Backend: backup, multiple devices, remote update of templates and parsers |
| Pay: NFC and QR reading, choice of account and SIM, biometrics, USSD launch, receipt | Bank card payment and banking integration |
| Merchant payment (merchant code) and person-to-person transfer (phone number) | Online and remote payment |
| Static hosting for App Links and a holding page | Full fallback web page, native iOS app |

Banks and cards will be studied separately, as they require a licence or an acquiring partner.

## Actors

The priority target remains the small merchant with a mid-range Android smartphone, but any user can receive payments, for example to receive money from a friend.

| Actor | Profile | Main expectation |
| --- | --- | --- |
| Merchant receiving payments | Kiosk, shop, street vendor; mid-range Android, often dual SIM; OM and/or MoMo merchant code | Get paid quickly, see an unambiguous confirmation, with no fees for their customers |
| Individual receiving payments | Receives on their Orange Money or MoMo number, without a merchant code | Receive money from a relative without dictating their number |
| Payer with the app | Android, OM or MoMo account, NFC depending on the model | Pay without typing anything but their PIN, knowing any fees |
| Payer without the app | Any phone with a camera or NFC | Be directed to install Eifi (full fallback page after the pilot) |
| Payer on iPhone | iOS does not allow launching a USSD code containing * or # | After the pilot: fallback page with the code to copy |
| Operators (Orange, MTN) | Execute the payment and send the SMS | No API integration planned for the pilot |

## Payment flow

1. The payee enters the amount and taps "Encaisser" (Collect).
2. Eifi creates a local session and shows the dynamic QR code while enabling NFC emission.
3. The payer taps their phone (NFC) **or** scans the QR code (branch).
4. Eifi, on the payer's side, shows the recipient's name, the amount and any fees.
5. The payer chooses their account (operator and SIM).
6. They confirm with biometrics.
7. Eifi launches the pre-filled USSD code on the right SIM.
8. The payer enters their PIN in the operator's prompt.
9. The operator debits and credits, then sends the SMS.
10. Eifi, on the payee's side, reads the credit SMS, matches the session and shows "Paiement reçu" (Payment received); the payer sees their receipt.

The payer's two branches meet at the operator's USSD prompt. The money goes directly to the payee, and the app only reads the confirmation. During the pilot, without a backend, matching happens on the payee's phone.

## Functional requirements

### Onboarding and home

| ID | Requirement |
| --- | --- |
| O1 | Shared onboarding: introduction to Eifi, then choice of the starting mode, Pay or Receive. |
| O2 | Home screen with both actions, Pay and Receive, and recent activity (payments sent and received). |
| O3 | Each permission is requested when it is needed, never during the shared onboarding. A refusal never blocks the flow. |

### Receive mode

| ID | Requirement |
| --- | --- |
| M1 | Receiving profile, filled in the first time Receive mode is used: display name and, for each operator, a merchant code or a receiving number. |
| M2 | Numeric keypad to enter the amount in FCFA, with an optional label. |
| M3 | Creation of a payment session: the dynamic QR code is shown and NFC emission is enabled at the same time. |
| M4 | Automatic session expiry after a configurable delay (2 minutes by default). |
| M5 | Only one pending session per amount at any given time, to remove ambiguity during matching. |
| M6 | Automatic confirmation by reading the credit SMS notification, with a distinct sound and vibration. |
| M7 | Manual confirmation is possible when automatic reading is disabled or fails. |
| M8 | History of the day's received payments and totals per operator. |

### Pay mode

| ID | Requirement |
| --- | --- |
| C1 | Reading of the NFC payload or the QR code, then display of the merchant and the amount before any action. |
| C2 | Choice of operator according to the available merchant codes and the SIMs present. |
| C3 | Automatic selection of the SIM matching the chosen operator on dual-SIM phones. |
| C4 | Launch of the pre-filled USSD code (merchant code or number, and amount); the PIN is still entered by the payer in the operator's prompt. |
| C5 | Mandatory biometric confirmation before launching the USSD code (fingerprint, face or phone unlock code). |
| C6 | Local history of initiated payments. |
| C7 | For a transfer to a number, the fees charged to the payer are shown before confirmation; only accounts with the same operator as the recipient are offered. |
| C8 | End-of-payment screen as a receipt: "Débit confirmé" (Debit confirmed) if the debit SMS is detected (option disabled by default), otherwise "Demande envoyée" (Request sent). Its layout differs from the payee's "Paiement reçu" screen. |

### Fallback web page (after the pilot)

| ID | Requirement |
| --- | --- |
| W1 | The QR code encodes a signed URL; without the app, it opens a lightweight page showing the merchant and the amount. |
| W2 | The page shows the full USSD code to dial, with a button that opens the dialer on Android and a copy of the code for iOS. |
| W3 | Link to install the app, never required to pay. |

## Technical requirements

The app is built in native Kotlin for Android. The pilot works without a backend; the Kotlin backend with Micronaut will come later.

**NFC transport.** The payee's phone emulates in HCE (`HostApduService`) an NFC Forum Type 4 tag holding the payment URL as NDEF; the payer reads it in reader mode (`NfcAdapter.enableReaderMode`). A phone without Eifi natively reads this tag and opens the URL in the browser. The payload is identical to the QR code's.

**Payload.** The QR code and NFC carry a URL of the form `https://pay.<domain>/p#v1.{data}`. Without a backend, the fragment, never sent over the network, carries the whole session encoded in base64url: random identifier, display name, amount, receiving method for each operator (merchant code or number), expiry and checksum.

**USSD launch.** The app builds the code from the operator's embedded template and the operation type (merchant payment or transfer to a number). It launches it on the right SIM with `TelecomManager.placeCall` and a `PhoneAccountHandle`, which requires `CALL_PHONE`; `#` characters are encoded as `%23`. The exact syntax for each operator is still to be confirmed (see Risks).

**Confirmation on the payee's side.** A `NotificationListenerService`, enabled the first time Receive mode is used, reads Orange and MTN SMS notifications. Embedded parsers, one per operator and per operation type, extract the amount, the counterparty's number and the reference. Matching with the pending session happens on the phone. The `READ_SMS` and `RECEIVE_SMS` permissions are ruled out because of Play Store restrictions.

**Backend (after the pilot).** A Micronaut service with PostgreSQL and Redis will handle backup, multiple devices and remote update of USSD templates and parsers. Details are in the technical design document.

**iOS.** There is no iOS app in the MVP. An iPhone can pay through the fallback web page by copying the USSD code.

## Security and fraud prevention

The core rule is: a payment is only confirmed from the SMS received by the payee, never from what the payer shows.

| Threat | Measure |
| --- | --- |
| Fake "payment successful" screenshot | The confirmation is shown only on the merchant's phone, with a distinct signal; the payer's end screen is a receipt, designed so it cannot be confused with the "Paiement reçu" screen. |
| Forged or re-stuck QR code (redirect to another merchant code) | QR code shown on the payee's screen and renewed at each session, short-lived; name and receiving method shown to the payer before any confirmation. Signing the data is to be studied. |
| Replay of an old session | 2-minute expiry and a terminal state that cannot be reused. |
| Matching ambiguity (two identical payments) | Only one pending session per amount; manual confirmation remains as a fallback. |
| Change in the operators' SMS format | Versioned parsers updated remotely, with automatic fallback to manual confirmation. |
| Data leak | No PIN or balance stored; SMS content reduced to amount, number and reference; encryption in transit. |
| NFC relay | Low stakes, since the customer confirms with their PIN in the operator prompt anyway. |

## Non-functional requirements

The targets below are starting goals, to be adjusted after field tests.

| Criterion | Target |
| --- | --- |
| Time between amount entry and QR display | Under 1 s, with local generation possible if the network is slow |
| Time between NFC tap or scan and the USSD prompt opening | Under 3 s, excluding the operator's own delay |
| Confirmation delay after the SMS is received | Under 2 s |
| Compatibility | Android 8.0 (API 26) and above; NFC optional, QR mandatory |
| App size | Under 15 MB, suited to limited data plans |
| Degraded network | Works on unstable 3G; USSD itself requires no mobile data |
| Languages | French and English |
| Offline operation | Complete flow without an Internet connection during the pilot |

## Regulatory and operator constraints

Since the app neither holds nor transfers funds, it positions itself as a technical tool and not as a payment service provider. This positioning must be validated by a lawyer before launch.

- In the CEMAC zone, collecting or holding funds on behalf of third parties falls under the payment service provider status, which requires a licence ([2018 regulation](https://droitmediasfinance.com/index.php/actualites/droit-tech-fintech/370-cameroun-la-filiale-fin-tech-d-orange-obtient-un-agrement-d-etablissement-de-paiement-comprendre)). The MVP stays outside this framework.
- Orange Money merchant payment is [free for the customer and exempt from the 0.2% tax](https://lefisk.cm/blog/orange-money-cameroun-fonctionnement-comptes-frais-taxe-guide). The app keeps this channel without adding fees.
- Orange's and MTN's terms of use must be checked regarding automated USSD launch and SMS reading.
- Personal data (numbers, history) falls under Cameroon's data protection law. A privacy policy is required for the Play Store.
- The use of `CALL_PHONE` and of the `NotificationListenerService` must be justified in the Play Store listing.

A person-to-person transfer costs the sender a fee and is subject to the 0.2% tax: the app announces it before each transfer.

## Risks and unknowns to validate

Two unknowns are blocking and must be cleared by the prototype before any other development.

| Risk | Impact | Validation |
| --- | --- | --- |
| No single-line USSD merchant payment string at an operator | Blocking: the customer has to navigate menus | Real tests on OM and MoMo merchant accounts |
| Unstable or incomplete merchant credit SMS format | Blocking for automatic confirmation | Collection of about twenty real SMS per operator |
| Orange or MTN launch a dynamic QR code with amount | The differentiator shrinks to aggregation | Watch MaxIt and the MoMo app |
| Play Store refusal for `CALL_PHONE` or the notification listener | Publication delay | Early declaration and APK distribution during the pilot |
| Low share of NFC phones among customers | QR becomes the main channel | Measured during the pilot |
| Android manufacturers that kill background services (Tecno, Infinix, Itel) | Missed confirmations | Battery optimisation tests per brand |
| Regulatory requalification as a payment provider | Shutdown or mandatory partnership | Legal opinion before public launch |
| Transfer fees poorly understood by payers | Loss of trust in Eifi | Fees shown before confirmation (C7) and tests with pilot payers |

## Delivery plan

The project moves forward in three phases, and each phase starts only if the previous one has met its exit criterion.

1. **Validation prototype.** A bare app to generate a QR code, launch the USSD code (merchant payment and transfer) and read SMS notifications, tested on a dual-SIM Tecno phone with real OM and MoMo merchant accounts.
   - Exit criterion: one successful real payment per operator and per operation type, without typing the amount manually, and one correctly parsed credit SMS.
2. **Pilot without a backend.** The complete Eifi app (onboarding, Pay, Receive), without a backend, deployed with 10 to 20 merchants in Yaoundé, with APK distribution outside the Play Store.
   - Exit criterion: payment time shorter than the current channel, and a measured automatic confirmation rate.
3. **Public launch and improvements.** Play Store release, backend (backup, multiple devices, remote update), full fallback web page, then study of the iOS app and of card payment through a licensed partner.
