# Eifi

Paiement sans contact pour le Cameroun : on approche ou on scanne, on valide par biométrie, on tape son code secret Orange Money ou MTN MoMo. Une seule app Android, deux modes : **Payer** et **Encaisser**.

La documentation est dans [`docs/`](docs/) et le contexte pour Claude Code dans [`CLAUDE.md`](CLAUDE.md).

## Prérequis

- Android Studio récent (compatible AGP 8.13), JDK 17
- Un téléphone Android 8.0+ (NFC conseillé)

## Démarrer

```bash
./gradlew assembleDebug        # APK debug : app/build/outputs/apk/debug/
./gradlew installDebug         # installe sur le téléphone branché
```

Vérifications lancées par la CI à chaque pull request :

```bash
./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug
```

## Signature

- **debug** : clé de debug locale d'Android Studio, `applicationId` suffixé `.debug` (les deux variantes cohabitent sur le téléphone).
- **release** : copier `keystore.properties.example` en `keystore.properties` et le compléter. Ce fichier et les `.jks` sont ignorés par git. Sans lui, la release est construite non signée.

Créer la clé une fois :

```bash
mkdir -p keystore
keytool -genkeypair -v -keystore keystore/eifi-release.jks -alias eifi -keyalg RSA -keysize 4096 -validity 10000
```

## Premier sync

Les versions de `gradle/libs.versions.toml` ont été fixées sans accès aux dépôts Maven. Si le sync échoue sur une version introuvable ou incompatible (AGP, Kotlin, Compose BOM, ktlint, detekt), ajuster le catalogue : Android Studio propose les versions disponibles.

## Structure

```
app/                  application, assemblage des modes
feature/onboarding    onboarding commun, choix du mode, accueil
feature/pay           mode Payer
feature/collect       mode Encaisser
core/design           thème Compose et composants
core/payload          format de l'URL de paiement (Kotlin pur)
core/nfc              HCE type 4 et lecteur NDEF
core/ussd             gabarits USSD et lancement sur la bonne SIM
core/sms              lecture des notifications SMS
build-logic/          plugins Gradle de convention
docs/                 cahier des charges, dossier technique, tickets, maquettes
```
