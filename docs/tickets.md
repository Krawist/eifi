# Eifi — Mobile tickets v1 (no backend)

> English working copy of the reference document kept in Claude (October 26, 2026). The reference is written in French; if the two differ, the Claude document prevails. Update this copy when it changes.

This document splits the first version of the Eifi Android app into tickets. A single app brings together the Pay and Receive modes, and it works without a backend: the payment session is entirely contained in the URL passed by NFC or QR code, matching between the credit SMS and the session happens on the payee's phone, and USSD templates and SMS parsers are embedded. Only static hosting is still needed, for the domain of the payment URL.

Indicative sizes: S for less than a day, M for two or three days, L for about a week.

User-facing texts are quoted in French, the app's default language, with an English translation in brackets where useful. Screen names match the mockups in `design/`.

## Order of work

Work starts with three explorations on real phones and real accounts (EIFI-001 to 003), which clear the unknowns that could call the product into question. Next come the foundations, the payment format and the onboarding, on which both modes depend. Receive and Pay then move forward in parallel: Receive first for emission (NFC and QR), then Pay for reading and payment. SMS reading and history come last, as they depend on the formats recorded during the explorations.

## Preliminary explorations

### EIFI-001 · Validate one-line USSD merchant payment and transfer

From a test app, dial a complete USSD code for a merchant payment (merchant code and amount), then for a transfer to a number (number and amount), at Orange Money and at MTN MoMo. Check that the operator's prompt then only asks for the PIN.

- The exact one-line syntax is documented for each operator and each operation type, or its absence is recorded, along with the transfer fees announced by the operator.
- The launch works with `TelecomManager.placeCall` on the chosen SIM of a dual-SIM phone.
- The behaviour is recorded on at least three brands (Tecno, Infinix, Samsung).

Size: M. Dependencies: OM and MoMo test accounts, including one merchant account per operator.

### EIFI-002 · Record the format of credit and debit SMS

Collect about twenty real SMS per operator: credit and debit SMS, for a merchant payment as well as for a person-to-person transfer. The goal is to write the first parsers.

- For each operator and each operation type, the sender, the amount format, the counterparty's number and the reference are identified.
- The SMS are anonymised and kept as test sets.
- The matching notification is visible to a `NotificationListenerService` on the tested brands.

Size: M. Dependencies: EIFI-001.

### EIFI-003 · Validate NFC Type 4 tag emulation

Have an Android phone emulate an NFC Forum Type 4 tag holding a URL, and check that it is read by another Android with the app, by an Android without the app and by an iPhone.

- The URL opens in the browser on Android and iPhone without the app installed.
- Reading takes less than two seconds, phones back to back.
- Models that refuse emulation or reading are listed.

Size: M. Dependencies: none.

## Foundations

### EIFI-010 · Create the multi-module repository and CI

Set up the Gradle repository (Kotlin DSL, version catalog) with the `:app`, `:feature:onboarding`, `:feature:pay`, `:feature:receive`, `:core:design`, `:core:payload`, `:core:nfc`, `:core:ussd` and `:core:sms` modules. The `:core:network` module is postponed along with the backend.

- The app compiles and launches, with an empty screen per mode.
- CI runs ktlint, detekt and unit tests on every pull request.
- Debug and release variants are signed separately.

Size: M.

### EIFI-011 · Implement the Compose design system

Turn the mockups' design guidelines into a Compose theme: colours (#00DBAD, #FEC447, #DDFDBC and their inks), embedded Plus Jakarta Sans font, type scale, radii and spacing. Shared components: primary button, secondary button, card, pill, numeric keypad and amount display.

- A catalogue of Compose previews shows every component.
- Text contrast reaches at least 4.5:1.
- The theme follows the plain version of the mockups, without gradient.

Size: L. Dependencies: EIFI-010.

### EIFI-012 · Set up navigation, persistence and localisation

Install Hilt, Navigation Compose, Room, DataStore and French and English resources, with a ViewModel architecture and state in `StateFlow`.

- The app has one navigation graph per mode: onboarding, Pay and Receive.
- The language follows the phone's, with French by default.

Size: M. Dependencies: EIFI-010.

### EIFI-013 · Animate the transitions between screens

Opening a screen slides it in from the right while the previous one moves slightly to the left and fades, as on iOS; going back plays the same movement in reverse. The onboarding screens slide from one to the next, and the steps shown inside a screen (permission explanations, Pay placeholder) use the same movement. The transitions are defined once in the design system.

- Every navigation between existing screens is animated, forward and back.
- A transition lasts at most 350 ms, so that it never slows the payment down.

Size: S. Dependencies: EIFI-012, EIFI-030.

## Payment format

### EIFI-020 · Specify and code the payment URL format

The URL carries the whole session in its fragment, never sent to a server: `https://pay.[domain]/p#v1.{data}`. The data, encoded in base64url, contains a random session identifier, the display name (30 characters at most), the amount, the receiving method for each operator (merchant code or phone number), the expiry time and a checksum. The `:core:payload` module provides the encoder and the decoder.

- The encode-then-decode round trip is covered by tests, including edge cases (accented name, a single operator, a number instead of a merchant code).
- An expired, malformed or unknown-version URL is rejected with an explicit error.
- The URL of a typical case fits in a QR code of version 8 at most with error correction Q, which the centred icon of EIFI-103 needs (decided on October 27, 2026; the reference document still says version 6).

Size: M. Dependencies: EIFI-010.

### EIFI-021 · Configure the static domain and App Links

Register the domain, host it on a static service and publish the `assetlinks.json` file, so that the payment URL opens Eifi directly when the app is installed.

- A payment URL typed in the browser opens Eifi without a chooser dialog.
- Without the app, the URL shows a temporary page inviting the user to install Eifi.

Size: S. Dependencies: EIFI-020.

## Onboarding and home

### EIFI-030 · Shared onboarding

On first launch, screens introduce Eifi: the gesture (tap or scan), Orange Money and MTN MoMo together in a single app, and the fact that Eifi never holds the money nor the PIN. The user then chooses which mode to start with, Pay or Receive ("Bienvenue" and "Choix du mode" screens).

- The shared onboarding requests no permission.
- The chosen mode determines the screen opened after onboarding, without closing access to the other mode.
- The onboarding can be viewed again from the settings.

Decided on October 27, 2026: a last step asks for the personal name, the business name, or both (at least one, 30 characters at most). They are kept in DataStore; the home screen greets with the business name first.

Size: M. Dependencies: EIFI-011, EIFI-012.

### EIFI-031 · Pay and Receive home screen

The home screen offers both actions: "Payer" (Pay), highlighted with the NFC status, and "Encaisser" (Collect). It also shows recent activity, mixing payments sent and received ("Accueil" screen).

- The first use of "Encaisser" opens the receiving profile (EIFI-100) if it is not filled in yet.
- When the app is open on the home screen, tapping a receiving phone directly starts the Pay flow.

Decided on October 27, 2026: once the onboarding is done, the app always opens on the home screen; the chosen mode only opens right after the onboarding. What depends on later tickets goes through fixed interfaces with fake implementations, so that navigation already works: recent activity (`ActivityRepository`, EIFI-102 and EIFI-207), receiving profile (`ReceivingProfileRepository`, EIFI-100), NFC reading (`NfcPaymentReader`, EIFI-200), and placeholder screens for the history and the settings.

Size: M. Dependencies: EIFI-030.

### EIFI-032 · Permissions requested when they are needed

Each permission is requested just before the first action that needs it, with an explanation screen: notification access and battery optimisation the first time Receive mode is used, phone calls and phone state at the first payment, camera at the first scan. A user who only pays never sees the notification access request.

- A refusal never blocks the flow: the app offers an alternative (manual confirmation, USSD code to dial yourself, NFC reading instead of scanning).
- The settings show the status of each permission and allow changing it.

Decided on October 27, 2026: battery optimisation goes through the system list (no `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, restricted by the Play Store). Each permission is asked once (`PermissionRequestHistory`): after a refusal, the alternative is offered directly and the settings allow changing it. The battery exemption is only asked once notification access is granted. Until EIFI-200 and EIFI-204, the phone and camera steps sit on the Pay placeholder ("Payer", "Scanner un QR code").

Size: M. Dependencies: EIFI-030.

## Receive mode

### EIFI-100 · Receiving profile

The first time Receive mode is used, the user enters the name shown to those who pay them, then the receiving method for each operator: a merchant code or their phone number (at least one operator). The app then asks them to allow notification access and to disable battery optimisation, explaining why ("Où recevoir l'argent" and "Confirmation automatique" screens).

- The information is stored locally and can be edited in the settings.
- If notification access is refused, the app works with manual confirmation and says so.
- The battery optimisation guide covers at least Tecno, Infinix and itel.

Decided on October 27, 2026: the phone number must be a valid Cameroonian mobile number according to Google's libphonenumber (the country is picked with its flag and calling code, Cameroon only for now; the number is stored and carried in the payment URL in E.164 format, e.g. +237690123456; no check of the operator behind the prefix before EIFI-001); the display name is prefilled with the business name, otherwise the personal name, given in the onboarding. The brand is detected from `Build`, and the guide gives the most likely menu paths of HiOS, XOS and itel OS; they are to verify on real phones in EIFI-300. The "SIM 1" pill of the mockup comes with SIM detection (EIFI-202).

Size: M. Dependencies: EIFI-031, EIFI-032.

### EIFI-101 · Amount entry screen

Implement the "Saisie du montant" (amount entry) screen: numeric keypad, 000 key, delete, optional label, reminder of the accepted operators and "Encaisser" button.

- The amount is an integer in FCFA, from 100 to 2,000,000, and the button stays disabled outside this range.
- The amount is displayed with a thin space as thousands separator.

Decided on October 27, 2026: a back button replaces "Aujourd'hui" (top left, the logo on the right), as the history is not needed from this screen. A key that would go past 2,000,000 is ignored, and a line under the amount explains the minimum or the maximum; the label (30 characters at most) stays on the phone, as the payment URL does not carry it; "Encaisser" opens a placeholder of the "QR et NFC actifs" screen until EIFI-102 and EIFI-103. A "Confirmation manuelle" pill shows when notification access is refused.

Size: M. Dependencies: EIFI-011.

### EIFI-102 · Create and manage the local session

"Encaisser" creates a local session (identifier, amount, label, creation, expiry) with status PENDING, saved in Room. The transitions are CONFIRMED, EXPIRED and CANCELLED, all final.

- There can be only one PENDING session per amount.
- A session expires after two minutes, even if the app is in the background.
- The transitions are covered by unit tests.

Decided on October 27, 2026: "Encaisser" with an amount already pending reopens that session, with its countdown. CONFIRMED and CANCELLED are final; EXPIRED too, except for the operator's SMS arriving less than 10 minutes after the expiry (EIFI-106), and the manual confirmation only applies to a pending session. The expiry needs no background job: the status follows the expiry time, and the overdue sessions are stored as EXPIRED before each new session and when the countdown ends.

Size: M. Dependencies: EIFI-020, EIFI-012.

### EIFI-103 · Show the payment QR code

"QR et NFC actifs" screen: QR code of the payment URL with the Eifi icon in the centre (error correction Q, see EIFI-020), amount, countdown and cancel button.

- The QR code can be read by the camera of an Android phone and an iPhone at 30 cm.
- Brightness goes to maximum while it is shown, then returns to its initial value.
- On expiry, the screen says so and offers to restart with the same amount.

Decided on October 27, 2026: until the domain is chosen (EIFI-021), the base URL is a placeholder in `BuildConfig.PAYMENT_BASE_URL`. The "NFC actif" banner and the mention of approaching the phone come with EIFI-104, when the emulation really runs; "En attente du SMS de l'opérateur" comes with EIFI-105.

Size: M. Dependencies: EIFI-102.

### EIFI-104 · Emit the URL over NFC (HCE Type 4)

Implement the `HostApduService` that emulates an NFC Type 4 tag holding the payment URL as NDEF, based on the findings of EIFI-003. The service is only active during the session.

- A paying phone reads the URL by bringing its back close to the payee's phone.
- The service no longer responds after the session ends.
- If NFC is missing or disabled, the screen says so without blocking the QR code.

Decided on October 27, 2026: built before EIFI-003 on the standard NFC Forum Type 4 tag protocol; the tap between real phones is checked as part of EIFI-003. The service is a disabled component, enabled only while a pending payment is shown, so that other apps keep the NDEF application the rest of the time. Samsung's built-in tag ("Tag intégré") claims the same AID: without priority, Android asks which app to use and then reports a conflict. While the QR screen is resumed, `CardEmulation.setPreferredService` gives Eifi priority (`NfcPaymentPriority`).

Size: L. Dependencies: EIFI-003, EIFI-102.

### EIFI-105 · Read credit SMS

Implement the `NotificationListenerService` and the embedded parsers from EIFI-002, for merchant payments as well as received transfers. A recognised SMS produces a local event: operator, operation type, amount, masked counterparty number, reference and time.

- The parsers pass all the SMS of the EIFI-002 test set.
- An SMS from an unconfigured sender is ignored.
- An SMS from a configured sender that is not recognised is recorded as "unrecognised".
- The raw SMS text is not kept.

Size: L. Dependencies: EIFI-002, EIFI-100.

### EIFI-106 · Match the SMS with the session

When a credit SMS arrives, look for the PENDING session with the same amount, or one that expired less than ten minutes ago, created before the SMS, and move it to CONFIRMED. An SMS without a session becomes an "unmatched payment" in the history.

- A duplicate of the same SMS (same reference) is processed only once.
- The "session found", "recently expired session" and "no session" cases are covered by tests.

Size: M. Dependencies: EIFI-102, EIFI-105.

### EIFI-107 · "Paiement reçu" screen

On confirmation, the waiting screen switches to "Paiement reçu" (Payment received) with a distinct sound and vibration, the amount, the operator, the masked number, the reference and the time.

- The confirmation is shown less than two seconds after the SMS notification.
- The sound and vibration fire even in the background, with a notification that reopens the screen.

Size: M. Dependencies: EIFI-106.

### EIFI-108 · Manual confirmation

From the waiting screen, the payee can confirm themselves after checking their SMS. The session moves to CONFIRMED with the "manual" source.

- Manual confirmation requires an explicit two-step validation.
- The history marks these confirmations with a yellow "Manuel" (Manual) badge.

Size: S. Dependencies: EIFI-102.

### EIFI-109 · Today's history

"Historique du jour" (today's history) screen: total received, totals and number of payments per operator, and list of transactions with their status (Confirmé, Manuel, Expiré, Non rattaché — Confirmed, Manual, Expired, Unmatched).

- Totals count confirmed and unmatched payments.
- Previous days can be viewed.

Size: M. Dependencies: EIFI-106, EIFI-108.

## Pay mode

### EIFI-200 · Read the payment URL

Receive the URL in three ways: through the NFC reader (`enableReaderMode`) when the app is open, through an App Link when the tag or QR code is read outside the app, and through a built-in QR scanner.

- All three entry points lead to the same summary screen.
- An invalid or expired URL shows a clear message, without launching a payment.

Decided on October 27, 2026: the scanner uses CameraX with ML Kit barcode scanning, model bundled in the app (works without Play services). The App Link is registered on the placeholder host of `PAYMENT_BASE_URL` without `autoVerify`; EIFI-021 switches to the real domain and adds the verification. A tag read while Eifi is not in the foreground opens Eifi through an `NDEF_DISCOVERED` filter. A link opened before the onboarding is done is not kept.

Size: M. Dependencies: EIFI-020, EIFI-021.

### EIFI-201 · Summary screen

Implement the "Récapitulatif" (summary) screen: payee's name, amount, chosen account with the "Changer" (Change) link, reminder that the PIN is entered in the operator's prompt, any fees (EIFI-208) and confirmation button. The button says "Payer" (Pay) for a merchant code and "Envoyer" (Send) for a transfer to a number.

- The default account is the last one used, if it is compatible.
- The session countdown is visible, and payment is blocked after expiry.

Decided on October 27, 2026: the payee's badge stays neutral ("Code marchand" or "Transfert à un particulier") instead of "Marchand vérifié", since nothing verifies a merchant without a backend. Until EIFI-202, "Changer" opens a simple list of the payee's operators, without SIMs. The last account used is saved when the payer confirms.

Size: M. Dependencies: EIFI-011, EIFI-200.

### EIFI-202 · Choice of operator and SIM

List the active SIMs, recognise their operator and only offer the combinations accepted by the payee ("Choix de l'opérateur" screen).

- On a dual-SIM phone, each option shows the SIM used.
- If no SIM matches the recipient's operators, the screen explains it (a transfer only works between two accounts of the same operator).
- The "Se souvenir" (Remember) choice is saved locally.

Size: M. Dependencies: EIFI-001, EIFI-201.

### EIFI-203 · Biometric confirmation

Confirming triggers a mandatory `BiometricPrompt`: fingerprint, face or phone unlock code.

- A failure or cancellation returns to the summary without placing a call.
- On a phone without a configured screen lock, the app asks the user to set one up.

Size: S. Dependencies: EIFI-201.

### EIFI-204 · Launch the USSD code

Build the code from the embedded template of the operator and the operation type (merchant payment or transfer to a number), and launch it on the right SIM with `TelecomManager.placeCall`.

- The dialled code matches exactly the syntax validated in EIFI-001.
- The `CALL_PHONE` and `READ_PHONE_STATE` permissions are requested at the first payment, with an explanation.
- The payment is never launched twice for the same session.

Size: M. Dependencies: EIFI-001, EIFI-202, EIFI-203.

### EIFI-205 · End-of-payment screen

When the USSD prompt closes and the app is back in the foreground, show the "Paiement effectué" (payment done) screen as a receipt. If debit SMS detection is enabled (EIFI-206), it says "Débit confirmé par votre SMS" (Debit confirmed by your SMS); otherwise "Demande envoyée" (Request sent), inviting the user to check the operator's SMS. In both cases, it reminds the user that the payee receives their own confirmation.

- The screen does not reuse the payee's "Paiement reçu" layout, so the two cannot be confused.
- The receipt is saved in the local history and can be shared as an image.

Size: M. Dependencies: EIFI-204.

### EIFI-206 · Optional debit SMS detection

Offer the payer to allow notification access to confirm the payment from their operator's debit SMS. The option is disabled by default and reuses `:core:sms` with the debit parsers.

- The debit SMS is linked to the current payment by amount and time.
- Without permission, the flow stays complete thanks to the "Demande envoyée" state.

Size: M. Dependencies: EIFI-002, EIFI-205.

### EIFI-207 · History of payments sent

Local list of payments made, with recipient, amount, operator, date and status.

- Each row reopens the matching receipt.

Size: S. Dependencies: EIFI-205.

### EIFI-208 · Operator fees and compatibility

When the recipient receives on a phone number, the payment is a person-to-person transfer: it costs the payer a fee and is subject to the 0.2% tax. The summary announces it before biometrics, with the fee amount when known ("Payer · Transfert à un ami" screen). A transfer only works between two accounts of the same operator: the app only offers compatible accounts and explains why the others are missing.

- A payment to a merchant code shows "Sans frais pour vous" (No fees for you).
- A transfer shows "Frais de transfert à votre charge" (Transfer fees paid by you) before confirmation, with the embedded fee schedule from EIFI-001.
- If none of the payer's accounts is compatible with the recipient's, the payment is blocked with an explanation.

Design note (October 27, 2026): the screens must not know each receiving method. Three levels:

- `ReceivingMethod` (`:core:payload`) only carries what travels in the URL: the method and its value. Fees and rules never come from the URL, so a payee cannot forge a URL announcing "no fees" for a paid transfer.
- A specification per operator and method, embedded in the payer's app (pure Kotlin module, for example `:core:operator`): fees (none, or schedule + 0.2% tax), same-operator constraint, USSD operation, SMS parsers.
- A `ReceivingMethodUi` built from the specification (Android module, string resources): button label ("Payer" or "Envoyer"), fee notice, icon. Screens only display it, so a new method needs a specification, its strings and a URL wire code (free codes: 0 and 3 to 7), but no screen change.

Size: M. Dependencies: EIFI-001, EIFI-201, EIFI-202.

## Quality and pilot launch

### EIFI-300 · Tests on real devices

Run the complete flows, merchant payment and transfer between friends, on a matrix of at least six common phones (Tecno, Infinix, itel, Samsung, single and dual SIM), with both operators.

- The success rate and flow duration are measured for each combination.
- Manufacturer-specific anomalies are documented with their workaround.

Size: L. Dependencies: EIFI-107, EIFI-205, EIFI-208.

### EIFI-301 · Pilot distribution

Publish the signed APK to 10 to 20 merchants in Yaoundé and their customers, through a test channel (Firebase App Distribution or Play Store internal test track), with a feedback form.

- Each pilot merchant is set up and trained in less than fifteen minutes.
- Crashes are reported through Crashlytics.

Size: M. Dependencies: EIFI-300.

## Postponed until after the pilot

These topics are deliberately out of this first version: the backend (backup, multiple devices), remote update of USSD templates and SMS parsers, the full fallback web page for people without the app, the iOS app, card payment and a real verified merchant status. For now, a merchant is just a receiving profile with a merchant code.

Without remote updates, any change to an operator's SMS format will require a new app version. This is the main risk of running without a backend, to be monitored during the pilot.
