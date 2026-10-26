# CLAUDE.md — Eifi

Contexte pour Claude Code. Lire ce fichier en entier avant toute tâche, puis le document de `docs/`
utile au ticket en cours.

## Le produit en bref

Eifi (prononcé « aï-fi ») est une app Android de paiement sans contact pour le Cameroun. Une seule
app réunit deux modes :

- **Encaisser** : on saisit un montant, l'app affiche un QR dynamique et émet la même URL par NFC.
- **Payer** : on approche son téléphone ou on scanne. L'app montre le bénéficiaire, le montant et
  les frais éventuels, demande la biométrie, puis lance le code USSD pré-rempli d'Orange Money ou de
  MTN MoMo sur la bonne SIM. Le payeur tape son code secret dans l'invite de l'opérateur.

Eifi ne touche jamais l'argent. Le paiement est confirmé chez celui qui encaisse par la lecture du
SMS de crédit de l'opérateur, jamais par l'écran du payeur.

## Documents de référence

Les documents tenus dans Claude font foi. Les fichiers de `docs/` en sont des copies de travail du
26 octobre 2026.

| Document                              | Copie locale                 | Référence                                         |
|---------------------------------------|------------------------------|---------------------------------------------------|
| Cahier des charges (quoi et pourquoi) | `docs/cahier-des-charges.md` | https://claude.ai/artifact/32DZB7Fg15dCH5Payt8w2D |
| Dossier technique (comment)           | `docs/dossier-technique.md`  | https://claude.ai/artifact/UCyVMkH69ju46QPDJ9fKfx |
| Tickets mobile v1 (sans backend)      | `docs/tickets.md`            | https://claude.ai/artifact/9ojG4Ba9ksS7V8EAASASRR |
| Maquettes (canevas)                   | `docs/design/*.png`          | https://claude.ai/artifact/Kdt6ugqJn828TZ2fXQXK2g |

Avant d'implémenter un écran, ouvrir la maquette correspondante dans `docs/design/` (lire l'image).
Les crochets `[ ]` dans les maquettes marquent des contenus réels à fournir ; montants et heures
sont des exemples.

## Décisions prises (ne pas remettre en cause sans le demander)

- **Une seule app**, deux modes. Le « marchand » n'est pas un type de compte : c'est un *profil
  d'encaissement* (nom affiché + pour chaque opérateur un code marchand **ou** un numéro de
  téléphone).
- **Pas de backend pour le pilote.** La session voyage dans le fragment de l'URL :
  `https://pay.<domaine>/p#v1.{base64url}` (id aléatoire, nom ≤ 30 caractères, montant, moyen de
  réception par opérateur, expiration, somme de contrôle). Le rapprochement SMS ↔ session se fait
  localement dans Room. Gabarits USSD et parseurs SMS sont embarqués. Seul un hébergement statique
  sert `assetlinks.json` et une page d'attente.
- **NFC** : celui qui encaisse émule un tag NFC Forum de type 4 (NDEF URI) avec un
  `HostApduService`, AID `D2760000850101`. Celui qui paie lit avec `NfcAdapter.enableReaderMode`. Le
  QR et le NFC portent exactement la même URL.
- **USSD** : `TelecomManager.placeCall` avec le `PhoneAccountHandle` de la SIM choisie
  (`CALL_PHONE`, `READ_PHONE_STATE`), `#` encodé en `%23`. La syntaxe réelle par opérateur est
  inconnue tant que EIFI-001 n'est pas fait : ne jamais l'inventer, la garder en gabarit
  configurable.
- **Biométrie obligatoire** (`BiometricPrompt`, BIOMETRIC_STRONG ou DEVICE_CREDENTIAL) avant chaque
  lancement USSD. Le code secret opérateur n'est jamais demandé ni vu par Eifi.
- **SMS** : `NotificationListenerService` uniquement, **jamais** `READ_SMS` / `RECEIVE_SMS` (Play
  Store). Texte brut jamais stocké, seulement montant, numéro masqué, référence et une empreinte
  SHA-256.
- **Session** : PENDING → CONFIRMED / EXPIRED / CANCELLED, états finaux définitifs, expiration 2
  min, une seule session PENDING par montant, rapprochement possible jusqu'à 10 min après
  expiration, sinon « paiement non rattaché ». Confirmation manuelle possible (badge jaune «
  Manuel »).
- **Payer vs Envoyer** : le bouton dit « Payer » pour un code marchand, « Envoyer » pour un
  transfert vers un numéro. Un transfert coûte des frais + la taxe de 0,2 % au payeur, affichés
  avant la biométrie, et ne marche qu'entre comptes du même opérateur.
- **Reçu payeur** : « Débit confirmé » si le SMS de débit est détecté (option désactivée par
  défaut), sinon « Demande envoyée ». Sa mise en page doit rester différente de l'écran « Paiement
  reçu » de celui qui encaisse (anti-fraude par capture d'écran).
- **Permissions demandées au moment où elles servent**, jamais pendant l'onboarding commun ; un
  refus ne bloque jamais le parcours.
- **Design** : version **unie** uniquement (la variante en dégradé est abandonnée pour le pilote).
  Style sobre inspiré d'iOS.

## Stack et conventions

- Kotlin, Jetpack Compose, Material 3 thémé. minSdk 26, compile/target 36. JDK 17.
- Architecture UDF : ViewModel + état en `StateFlow`, coroutines. Hilt, Room, DataStore et
  Navigation Compose arrivent avec EIFI-012.
- Gradle Kotlin DSL, catalogue `gradle/libs.versions.toml`, plugins de convention dans
  `build-logic/` :
    - `eifi.android.application`, `eifi.android.library`, `eifi.android.compose`,
      `eifi.android.feature` (library + compose + `:core:design`), `eifi.jvm.library` (Kotlin pur).
- Sources Kotlin dans `src/main/kotlin` et `src/test/kotlin`.
- Accès typés aux modules : `implementation(projects.core.design)`.
- Logique métier pure (payload, parseurs, rapprochement, gabarits) en Kotlin sans dépendance Android
  dès que possible, couverte par des tests JVM.
- Textes visibles : ressources `strings.xml` en français (défaut) **et** anglais (`values-en`).
  Jamais de texte en dur dans un composable.
- Montants : entiers en FCFA (`Long`), affichés avec `formatFcfa()` (espace fine U+202F pour les
  milliers).
- Qualité : `./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug` doit passer avant de
  considérer un ticket terminé. Corriger le formatage avec `./gradlew ktlintFormat`.
- Code et identifiants en anglais ; commentaires, KDoc et messages de commit en anglais.

### Modules

| Module                | Contenu                                             | Tickets       |
|-----------------------|-----------------------------------------------------|---------------|
| `:app`                | Application, assemblage des modes, signature        | 010, 012      |
| `:feature:onboarding` | Onboarding commun, choix du mode, accueil           | 030, 031, 032 |
| `:feature:pay`        | Écrans du mode Payer                                | 200–208       |
| `:feature:collect`    | Écrans du mode Encaisser                            | 100–109       |
| `:core:design`        | Thème, couleurs, typo, composants, formatage        | 011           |
| `:core:payload`       | Encodage/décodage de l'URL de paiement (Kotlin pur) | 020           |
| `:core:nfc`           | HCE type 4 et lecteur NDEF                          | 104, 200      |
| `:core:ussd`          | Gabarits USSD, SIM, lancement de l'appel            | 202, 204      |
| `:core:sms`           | Écoute des notifications, parseurs SMS              | 105, 206      |

`:core:network` est reporté avec le backend.

### Jetons de design (version unie)

| Jeton      | Valeur    | Usage                                    |
|------------|-----------|------------------------------------------|
| primary    | `#00DBAD` | Actions principales                      |
| on-primary | `#04362B` | Texte sur primary (jamais de blanc)      |
| accent     | `#FEC447` | Attente, frais, confirmation manuelle    |
| soft       | `#DDFDBC` | Bandeau NFC, notes, pastilles opérateurs |
| background | `#F6F8F7` | Fond des écrans                          |
| surface    | `#FFFFFF` | Cartes, touches du pavé                  |
| ink        | `#0E1A17` | Texte principal                          |
| muted      | `#56665F` | Texte secondaire                         |

Police Plus Jakarta Sans (OFL, à embarquer en EIFI-011). Montant 68/800, corps 15/500, étiquettes
13/600. Rayons : cartes 22 à 28, boutons 18. Zones tactiles ≥ 44 dp. Le logo n'est pas encore
choisi.

## État d'avancement

- [x] **EIFI-010** — squelette multi-modules, CI, signature debug/release séparée. Les versions des
  dépendances ont été fixées hors ligne : le premier sync peut demander des ajustements (voir
  README).
- [ ] **Prochain ticket : EIFI-011** — design system Compose (police embarquée, composants : bouton
  principal, bouton secondaire, carte, pastille, pavé numérique, affichage de montant ; catalogue de
  previews ; contraste ≥ 4,5:1).
- [ ] EIFI-012, puis EIFI-020, puis EIFI-030… (ordre dans `docs/tickets.md`).
- Explorations EIFI-001 à 003 : faites par Krawist sur de vrais téléphones et comptes. Tant qu'elles
  ne sont pas faites, gabarits USSD, expéditeurs SMS, codes MNC et barème des frais restent des
  valeurs à fournir, jamais inventées.

Mettre à jour cette section à la fin de chaque ticket.

## Façon de travailler

- Un ticket à la fois, sur une branche `eifi-0xx-description`, avec des commits petits et lisibles.
- Relire les critères d'acceptation du ticket dans `docs/tickets.md` avant de coder et les vérifier
  à la fin.
- Si un choix produit n'est pas tranché par les documents, poser la question plutôt que de décider
  seul.
