# Eifi — Dossier technique v1

> Copie de travail du document de référence tenu dans Claude (26 octobre 2026) : https://claude.ai/artifact/UCyVMkH69ju46QPDJ9fKfx. En cas d'écart, le document Claude fait foi.

Ce dossier décrit comment construire Eifi : l'application Android avec ses modes Payer et Encaisser, puis, après le pilote, la page web de secours et le backend. Il précise aussi les choix techniques qui restent ouverts. En cas d'écart avec le cahier des charges, ce document fait foi pour la partie technique.

## Principe et architecture

Eifi ne touche jamais l'argent. Celui qui encaisse crée une session de paiement et la transmet par NFC ou par QR code sous forme d'URL. Le téléphone de celui qui paie compose ensuite le code USSD de son opérateur, et le payeur valide avec son code secret dans l'invite de l'opérateur. L'argent part directement vers le compte de celui qui encaisse. La confirmation vient du SMS de crédit que l'opérateur lui envoie : Eifi le lit et le rapproche de la session ouverte.

| Composant | Rôle | Technologie | Pilote |
| --- | --- | --- | --- |
| Mode Encaisser | Saisie du montant, émission NFC et QR, lecture des SMS de crédit, historique | Android, Kotlin, Jetpack Compose | Oui |
| Mode Payer | Lecture NFC et QR, choix du compte et de la SIM, biométrie, USSD, reçu | Android, Kotlin, Jetpack Compose (même app) | Oui |
| Hébergement statique | App Links (assetlinks.json) et page d'attente | Hébergement de fichiers statiques | Oui |
| Page web de secours | Paiement sans l'app : code USSD et composeur | HTML rendu par le backend | Non |
| Backend | Sauvegarde, multi-appareil, configuration distante, notifications | Kotlin, Micronaut 4, PostgreSQL, Redis | Non |

Déroulé d'un paiement pendant le pilote :

1. Celui qui encaisse saisit le montant ; l'app crée une session locale et encode l'URL de paiement.
2. L'app affiche le QR de l'URL et active l'émulation NFC qui sert la même URL.
3. Celui qui paie approche son téléphone ou scanne le QR. Avec l'app, le lien s'ouvre dans Eifi ; sans l'app, il s'ouvre dans le navigateur.
4. Eifi décode la session, propose les comptes compatibles, demande la biométrie et compose le code USSD sur la bonne SIM.
5. Le payeur saisit son code secret dans l'invite de l'opérateur, qui crédite le compte de celui qui encaisse et lui envoie un SMS.
6. Le téléphone de celui qui encaisse lit la notification du SMS, la rapproche de la session en attente et affiche « Paiement reçu ».

## Une app, deux modes

Eifi est une seule application qui réunit deux modes, Payer et Encaisser. Après un onboarding commun, l'utilisateur choisit par où commencer, puis l'accueil donne accès aux deux. Le « marchand » n'est pas un type de compte : c'est un profil d'encaissement, rempli au premier passage en mode Encaisser. Ce profil indique, pour chaque opérateur, comment recevoir l'argent : avec un code marchand ou avec un numéro de téléphone.

Le moyen de réception détermine l'opération que lance celui qui paie :

| Moyen de réception | Opération USSD | Frais pour le payeur | Opérateurs possibles |
| --- | --- | --- | --- |
| Code marchand | Paiement marchand | Aucun (chez Orange Money) | Celui du code marchand |
| Numéro de téléphone | Transfert entre particuliers | Frais de transfert et taxe de 0,2 % | Même opérateur des deux côtés |

Le récapitulatif affiche les frais avant la validation, et l'app ne propose que les comptes compatibles. Chaque type d'opération a son propre gabarit USSD et ses propres formats de SMS de crédit et de débit.

Les permissions sont demandées au moment où elles servent : la lecture des notifications et la désactivation de l'optimisation de batterie au premier encaissement, l'appel téléphonique au premier paiement, l'appareil photo au premier scan. Un utilisateur qui ne fait que payer n'a jamais à autoriser la lecture de ses notifications.

## Fonctionnement sans backend pour le pilote

Le pilote fonctionne sans serveur applicatif. Les sections Page web de secours et Backend décrivent la cible d'après le pilote.

L'URL de paiement porte toute la session dans son fragment, qui n'est jamais envoyé sur le réseau : `https://pay.[domaine]/p#v1.{données}`. Les données, encodées en base64url, contiennent un identifiant aléatoire, le nom affiché, le montant, le moyen de réception de chaque opérateur, l'expiration et une somme de contrôle.

Le rapprochement entre le SMS de crédit et la session se fait sur le téléphone de celui qui encaisse, avec la règle d'une seule session en attente par montant, garantie localement dans Room. Les gabarits USSD et les parseurs de SMS sont embarqués dans l'app. Il ne reste qu'un hébergement statique, pour publier le fichier `assetlinks.json` des App Links et une page d'attente destinée à ceux qui n'ont pas l'app.

Le principal risque de ce mode est l'absence de mise à jour à distance : si un opérateur change le format de ses SMS, il faudra publier une nouvelle version. Un fichier de configuration statique et signé, téléchargé depuis le même hébergement, pourra réduire ce risque avant l'arrivée du backend.

## NFC : celui qui encaisse émule un tag, celui qui paie lit

C'est le téléphone de celui qui encaisse qui émule en HCE (Host Card Emulation), et celui du payeur qui lit, car le montant et le moyen de réception partent de celui qui encaisse.

Le téléphone émule un tag NFC Forum de type 4 contenant un enregistrement NDEF de type URI, l'URL de paiement : aucun protocole propriétaire. Un téléphone sans Eifi lit nativement ce tag et ouvre l'URL dans le navigateur, sur Android comme sur les iPhone récents. Avec l'app installée, l'URL est un App Link vérifié et ouvre directement Eifi.

Un `HostApduService` répond aux commandes APDU de lecture d'un tag de type 4 :

1. sélection de l'application NDEF (AID `D2760000850101`) ;
2. sélection et lecture du fichier de capacité (Capability Container) ;
3. sélection et lecture du fichier NDEF qui contient l'URL.

Le service n'est actif que pendant une session ouverte. L'écran d'attente l'enregistre comme service préféré avec `CardEmulation.setPreferredService` et le retire à la sortie ; l'écran doit rester allumé et déverrouillé pendant l'encaissement. Côté payeur, Eifi lit le tag avec `NfcAdapter.enableReaderMode` (NFC-A et NFC-B) et extrait l'URL du message NDEF. Les tests sur Tecno, Infinix, itel et Samsung font partie du prototype, car le comportement HCE varie selon les fabricants.

## QR code et URL de paiement

Le QR et le tag NFC portent exactement la même URL. L'URL d'un cas courant doit tenir dans un QR de version 6 au maximum, pour rester facile à scanner sur un écran de milieu de gamme en plein soleil ; c'est pourquoi le nom affiché est limité à 30 caractères.

Le QR est toujours affiché sur l'écran de celui qui encaisse et change à chaque session, ce qui le rend difficile à remplacer par un faux. Le payeur voit le nom et le moyen de réception avant de valider. Une signature des données de session est à étudier pour la version avec backend, où le jeton deviendra un identifiant opaque de 22 caractères résolu côté serveur.

## Application Android

L'app est construite dans un seul dépôt Gradle multi-modules, en Kotlin avec Jetpack Compose. L'architecture suit le flux de données unidirectionnel : ViewModel, état exposé en `StateFlow`, injection par Hilt et coroutines. L'historique est stocké dans Room et les préférences dans DataStore. Les versions visées sont Android 8.0 (API 26) au minimum et la dernière API stable pour la cible.

| Module | Contenu |
| --- | --- |
| `:app` | Application unique, navigation et assemblage des modes |
| `:feature:onboarding` | Onboarding commun, choix du mode, accueil |
| `:feature:pay` | Écrans du mode Payer |
| `:feature:collect` | Écrans du mode Encaisser |
| `:core:design` | Thème Compose, couleurs, Plus Jakarta Sans, composants |
| `:core:payload` | Encodage et décodage de l'URL de paiement |
| `:core:nfc` | Service HCE de type 4 et lecteur NDEF |
| `:core:ussd` | Gabarits USSD, sélection de SIM, lancement de l'appel |
| `:core:sms` | Écoute des notifications et parseurs de SMS |

Le module `:core:network` est reporté avec le backend.

### Lancement du USSD et choix de la SIM

Le code à composer vient d'un gabarit par opérateur et par type d'opération, embarqué dans l'app, par exemple `{prefixe}*{codeMarchand}*{montant}#` pour un paiement marchand. La syntaxe réelle de chaque opérateur sera établie pendant le prototype.

L'app liste les SIM actives avec `SubscriptionManager` (permission `READ_PHONE_STATE`) et reconnaît l'opérateur par son code réseau : MCC 624 pour le Cameroun, codes MNC d'Orange et de MTN à vérifier. Elle retrouve le `PhoneAccountHandle` correspondant et passe l'appel avec `TelecomManager.placeCall` (permission `CALL_PHONE`), en encodant les `#` dans l'URI `tel:`.

Avant l'appel, une `BiometricPrompt` obligatoire demande l'empreinte, le visage ou le code de déverrouillage (BIOMETRIC_STRONG ou DEVICE_CREDENTIAL). Le code secret Orange Money ou MoMo n'est jamais demandé ni vu par Eifi.

### Lecture des SMS

Chez celui qui encaisse, un `NotificationListenerService`, activé au premier encaissement, filtre les notifications de l'application SMS par défaut dont l'expéditeur correspond à Orange Money ou MTN MoMo. Les parseurs embarqués, un par opérateur et par type d'opération, extraient le montant, le numéro de la contrepartie et la référence. Un SMS d'un expéditeur connu mais non reconnu est enregistré comme « non reconnu », ce qui révèle un changement de format. Le texte brut n'est jamais conservé ; seule une empreinte SHA-256 sert à dédoublonner.

Chez le payeur, la même brique peut détecter le SMS de débit si l'option est activée, pour afficher « Débit confirmé » sur le reçu. L'onboarding guide celui qui encaisse vers la désactivation de l'optimisation de batterie, pour éviter les confirmations manquées sur les marques qui ferment les services en arrière-plan.

## Page web de secours (après le pilote)

Pendant le pilote, une personne sans l'app arrive sur une simple page d'attente qui l'invite à installer Eifi. Après le pilote, cette page devient la page de secours, rendue côté serveur sans framework JavaScript pour s'afficher vite en 3G. Elle montre le nom de celui qui encaisse, le montant, le compte à rebours, un sélecteur d'opérateur et le code USSD complet. Un lien `tel:` ouvre le composeur sur Android ; sur iPhone, qui bloque ces liens quand ils contiennent `*` ou `#`, un bouton copie le code.

## Backend (après le pilote)

Le backend est un service Kotlin sur Micronaut 4 et Netty, packagé en conteneur. La persistance utilise Micronaut Data JDBC sur PostgreSQL avec des migrations Flyway, et Redis porte les sessions actives et leurs délais d'expiration. L'authentification se fait par jeton JWT (Micronaut Security), après vérification du numéro de téléphone par code SMS. Il apporte la sauvegarde de l'historique, l'usage sur plusieurs appareils, la mise à jour à distance des gabarits USSD et des parseurs, et la page de secours.

### API

| Méthode et route | Appelant | Rôle |
| --- | --- | --- |
| `POST /v1/auth/otp` et `/v1/auth/verify` | Utilisateur | Connexion par numéro de téléphone et code SMS |
| `PUT /v1/profile/collect` | Celui qui encaisse | Nom affiché, moyen de réception par opérateur, expéditeurs SMS |
| `POST /v1/sessions` | Celui qui encaisse | Crée une session ; renvoie jeton, URL et expiration |
| `POST /v1/sessions/{id}/cancel` | Celui qui encaisse | Annule une session en attente |
| `POST /v1/sessions/{id}/confirm-manual` | Celui qui encaisse | Confirmation manuelle |
| `GET /v1/sessions?date=` | Celui qui encaisse | Historique et totaux du jour par opérateur |
| `POST /v1/sms-events` | Celui qui encaisse | Remonte un SMS de crédit analysé |
| `GET /v1/config` | App | Gabarits USSD, parseurs SMS, expéditeurs, versions minimales |
| `WS /v1/events` | Celui qui encaisse | Mises à jour des sessions en temps réel |
| `GET /p/{token}` | Public | Page de secours (HTML) ou session (JSON) |

### Modèle de données

| Table | Colonnes principales |
| --- | --- |
| `app_user` | id, phone, display_name, created_at |
| `collect_method` | user_id, operator (OM, MOMO), kind (MERCHANT_CODE, PHONE), value, sms_sender |
| `payment_session` | id, user_id, token, amount, label, status, chosen_operator, created_at, expires_at, confirmed_at, confirmation_source (SMS, MANUAL), sms_event_id |
| `sms_event` | id, user_id, operator, operation_kind, amount, counterpart_masked, operator_ref, received_at, text_hash, parser_version, matched_session_id |
| `remote_config` | key, version, payload (JSON), active |

Les montants sont des entiers en FCFA. Le numéro de la contrepartie est conservé masqué, sauf les deux derniers chiffres.

### Cycle de vie d'une session et rapprochement

| Transition | Déclencheur |
| --- | --- |
| création → PENDING | Saisie du montant et « Encaisser » |
| PENDING → CONFIRMED | SMS rapproché, ou confirmation manuelle |
| PENDING → EXPIRED | Délai écoulé (2 minutes par défaut) |
| PENDING → CANCELLED | Annulation par celui qui encaisse |

Les états finaux sont définitifs et chaque transition est idempotente. Une seule session peut être en attente par montant, garanti côté serveur par une clé Redis `pending:{userId}:{amount}` posée avec SETNX. À la réception d'un SMS, on cherche la session PENDING, ou expirée depuis moins de dix minutes, du même montant et créée avant le SMS. Un SMS sans session devient un « paiement non rattaché » visible dans l'historique, et le dédoublonnage se fait sur la référence opérateur et l'empreinte du texte. Pendant le pilote, ces mêmes règles s'appliquent localement dans Room.

## Sécurité

La confirmation d'un paiement ne repose jamais sur ce que montre le téléphone de celui qui paie. Son écran de fin est un reçu dont la mise en page diffère volontairement de l'écran « Paiement reçu » de celui qui encaisse. Il n'affiche « Débit confirmé » que si le SMS de débit de l'opérateur a été détecté ; sinon, il indique « Demande envoyée ».

Le QR est dynamique, de courte durée et affiché uniquement sur l'écran de celui qui encaisse. Eifi ne stocke ni code secret, ni solde, ni identifiant de compte opérateur, et ne conserve jamais le texte brut des SMS. Avec le backend, les échanges passeront en HTTPS avec épinglage de certificat.

Le principal risque résiduel est un faux SMS envoyé à celui qui encaisse. Il est limité par le filtrage sur les expéditeurs officiels des opérateurs et par la confirmation manuelle en cas de doute, et sera évalué avec les SMS réels collectés pendant le prototype.

## Charte de design

L'interface suit un style sobre inspiré d'iOS : grands montants en chiffres alignés, titres larges, cartes blanches très arrondies sur fond gris clair. Une variante en dégradé, dessinée sur le canevas, est mise de côté : le pilote utilise uniquement la version unie, pour garder le code simple.

| Jeton | Valeur | Usage |
| --- | --- | --- |
| primary | `#00DBAD` | Actions principales, validation |
| on-primary | `#04362B` | Texte et icônes sur primary (le blanc y serait illisible) |
| accent | `#FEC447` | États en attente, frais, confirmation manuelle |
| soft | `#DDFDBC` | Fonds doux : bandeau NFC, notes, pastilles opérateurs |
| background | `#F6F8F7` | Fond des écrans |
| surface | `#FFFFFF` | Cartes, touches du pavé |
| ink | `#0E1A17` | Texte principal |
| muted | `#56665F` | Texte secondaire |

La police est Plus Jakarta Sans (licence OFL), embarquée dans l'app. L'échelle typographique va de 68 px en graisse 800 pour le montant principal à 13 px en graisse 600 pour les étiquettes, avec un corps de texte à 15 px en graisse 500. Les cartes ont un rayon de 22 à 28 px, les boutons de 18 px, et les zones tactiles font au moins 44 px. Le logo n'est pas encore choisi parmi les trois pistes du canevas de design.

Les maquettes rendues sont dans [`design/`](design/).

## Questions ouvertes

- Syntaxe USSD en une ligne du paiement marchand et du transfert chez Orange et chez MTN.
- Barème des frais de transfert de chaque opérateur, et format exact des SMS de crédit et de débit.
- Codes MNC d'Orange et de MTN au Cameroun, et noms d'expéditeur de leurs SMS.
- Fiabilité de l'émulation de tag de type 4 sur les modèles courants, et lecture par iPhone en arrière-plan.
- Nom de domaine de l'URL de paiement et hébergement statique.
- Choix final du logo.
