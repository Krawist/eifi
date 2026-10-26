# Eifi — Tickets mobile v1 (sans backend)

> Copie de travail du document de référence tenu dans Claude (26 octobre 2026). En cas d'écart, le document Claude fait foi ; mettre cette copie à jour quand il change.

Ce document découpe en tickets la première version de l'application Android Eifi. Une seule app réunit les modes Payer et Encaisser, et elle fonctionne sans backend : la session de paiement est entièrement contenue dans l'URL transmise par NFC ou QR, le rapprochement entre le SMS de crédit et la session se fait sur le téléphone de celui qui encaisse, et les gabarits USSD et parseurs de SMS sont embarqués. Seul un hébergement statique reste nécessaire pour le domaine de l'URL de paiement.

Tailles indicatives : S pour moins d'une journée, M pour deux ou trois jours, L pour une semaine environ.

## Ordre de réalisation

Le travail commence par trois explorations sur de vrais téléphones et de vrais comptes (EIFI-001 à 003), qui lèvent les inconnues capables de remettre en cause le produit. Viennent ensuite les fondations, le format de paiement et l'onboarding, dont dépendent les deux modes. Encaisser et Payer avancent alors en parallèle : Encaisser d'abord pour l'émission (NFC et QR), puis Payer pour la lecture et le paiement. La lecture des SMS et l'historique arrivent en dernier, car ils dépendent des formats relevés pendant les explorations.

## Explorations préalables

### EIFI-001 · Valider le paiement marchand et le transfert USSD en une ligne

Composer, depuis une app de test, un code USSD complet de paiement marchand (code marchand et montant), puis de transfert vers un numéro (numéro et montant), chez Orange Money et chez MTN MoMo. Vérifier que l'invite de l'opérateur ne demande plus que le code secret.

- La syntaxe exacte en une ligne est documentée pour chaque opérateur et chaque type d'opération, ou son absence est constatée, avec les frais de transfert annoncés par l'opérateur.
- Le lancement fonctionne avec `TelecomManager.placeCall` sur la SIM choisie d'un téléphone double SIM.
- Le comportement est noté sur au moins trois marques (Tecno, Infinix, Samsung).

Taille : M. Dépendances : comptes de test OM et MoMo, dont un compte marchand par opérateur.

### EIFI-002 · Relever le format des SMS de crédit et de débit

Collecter une vingtaine de SMS réels par opérateur : les SMS de crédit et de débit, pour un paiement marchand comme pour un transfert entre particuliers. Le but est d'écrire les premiers parseurs.

- Pour chaque opérateur et chaque type d'opération, l'expéditeur, le format du montant, le numéro de la contrepartie et la référence sont identifiés.
- Les SMS sont anonymisés et conservés comme jeux de tests.
- La notification correspondante est bien visible par un `NotificationListenerService` sur les marques testées.

Taille : M. Dépendances : EIFI-001.

### EIFI-003 · Valider l'émulation d'un tag NFC de type 4

Faire émuler par un Android un tag NFC Forum de type 4 contenant une URL, et vérifier sa lecture par un autre Android avec l'app, par un Android sans l'app et par un iPhone.

- L'URL s'ouvre dans le navigateur sur Android et iPhone sans app installée.
- La lecture se fait en moins de deux secondes, téléphones dos à dos.
- Les modèles qui refusent l'émulation ou la lecture sont listés.

Taille : M. Dépendances : aucune.

## Fondations

### EIFI-010 · Créer le dépôt multi-modules et la CI

Mettre en place le dépôt Gradle (Kotlin DSL, catalogue de versions) avec les modules `:app`, `:feature:onboarding`, `:feature:pay`, `:feature:collect`, `:core:design`, `:core:payload`, `:core:nfc`, `:core:ussd` et `:core:sms`. Le module `:core:network` est reporté avec le backend.

- L'app compile et se lance, avec un écran vide par mode.
- La CI exécute ktlint, detekt et les tests unitaires à chaque pull request.
- Les variantes debug et release sont signées séparément.

Taille : M.

### EIFI-011 · Implémenter le design system Compose

Traduire la charte des maquettes en thème Compose : couleurs (#00DBAD, #FEC447, #DDFDBC et leurs encres), police Plus Jakarta Sans embarquée, échelle typographique, rayons et espacements. Composants partagés : bouton principal, bouton secondaire, carte, pastille, pavé numérique et affichage de montant.

- Un catalogue de prévisualisations Compose montre chaque composant.
- Les contrastes de texte atteignent au moins 4,5:1.
- Le thème suit la version unie des maquettes, sans dégradé.

Taille : L. Dépendances : EIFI-010.

### EIFI-012 · Mettre en place la navigation, la persistance et la localisation

Installer Hilt, Navigation Compose, Room, DataStore et les ressources en français et en anglais, avec une architecture ViewModel et état en `StateFlow`.

- L'app a un graphe de navigation par mode : onboarding, Payer et Encaisser.
- La langue suit celle du téléphone, avec le français par défaut.

Taille : M. Dépendances : EIFI-010.

## Format de paiement

### EIFI-020 · Spécifier et coder le format de l'URL de paiement

L'URL porte toute la session dans son fragment, jamais envoyé à un serveur : `https://pay.[domaine]/p#v1.{données}`. Les données, encodées en base64url, contiennent un identifiant de session aléatoire, le nom affiché (30 caractères au plus), le montant, le moyen de réception de chaque opérateur (code marchand ou numéro de téléphone), l'heure d'expiration et une somme de contrôle. Le module `:core:payload` fournit l'encodeur et le décodeur.

- L'aller-retour encodage puis décodage est couvert par des tests, cas limites compris (nom accentué, un seul opérateur, numéro au lieu d'un code marchand).
- Une URL expirée, mal formée ou de version inconnue est rejetée avec une erreur explicite.
- L'URL d'un cas courant tient dans un QR de version 6 au maximum.

Taille : M. Dépendances : EIFI-010.

### EIFI-021 · Configurer le domaine statique et les App Links

Réserver le domaine, l'héberger sur un service statique et publier le fichier `assetlinks.json`, pour que l'URL de paiement ouvre directement Eifi quand l'app est installée.

- Une URL de paiement tapée dans le navigateur ouvre Eifi sans boîte de choix.
- Sans l'app, l'URL affiche une page provisoire qui invite à installer Eifi.

Taille : S. Dépendances : EIFI-020.

## Onboarding et accueil

### EIFI-030 · Onboarding commun

Au premier lancement, des écrans présentent Eifi : le geste (approcher ou scanner), Orange Money et MTN MoMo réunis dans une seule app, et le fait qu'Eifi ne détient jamais l'argent ni le code secret. L'utilisateur choisit ensuite par quel mode commencer, Payer ou Encaisser (écrans « Bienvenue » et « Choix du mode »).

- L'onboarding commun ne demande aucune permission.
- Le mode choisi détermine l'écran ouvert après l'onboarding, sans fermer l'accès à l'autre mode.
- L'onboarding peut être revu depuis les réglages.

Taille : M. Dépendances : EIFI-011, EIFI-012.

### EIFI-031 · Écran d'accueil Payer et Encaisser

L'accueil propose les deux actions : « Payer », mise en avant avec l'état du NFC, et « Encaisser ». Il affiche aussi l'activité récente, qui mélange paiements envoyés et reçus (écran « Accueil »).

- Le premier passage par « Encaisser » ouvre le profil d'encaissement (EIFI-100) s'il n'est pas encore rempli.
- Quand l'app est ouverte sur l'accueil, approcher un téléphone qui encaisse lance directement le parcours Payer.

Taille : M. Dépendances : EIFI-030.

### EIFI-032 · Permissions demandées au moment où elles servent

Chaque permission est demandée juste avant la première action qui en a besoin, avec un écran d'explication : lecture des notifications et optimisation de batterie au premier encaissement, appel téléphonique et état du téléphone au premier paiement, appareil photo au premier scan. Un utilisateur qui ne fait que payer ne voit jamais la demande de lecture des notifications.

- Un refus ne bloque jamais le parcours : l'app propose une alternative (confirmation manuelle, code USSD à composer soi-même, lecture NFC au lieu du scan).
- Les réglages montrent l'état de chaque permission et permettent de la modifier.

Taille : M. Dépendances : EIFI-030.

## Mode Encaisser

### EIFI-100 · Profil d'encaissement

Au premier passage en mode Encaisser, l'utilisateur saisit le nom affiché à ceux qui le paient, puis le moyen de réception de chaque opérateur : un code marchand ou son numéro de téléphone (au moins un opérateur). L'app lui demande ensuite d'autoriser la lecture des notifications et de désactiver l'optimisation de batterie, en expliquant pourquoi (écrans « Où recevoir l'argent » et « Confirmation automatique »).

- Les informations sont stockées localement et modifiables dans les réglages.
- Si la lecture des notifications est refusée, l'app fonctionne en confirmation manuelle et le signale.
- Le guide d'optimisation de batterie couvre au moins Tecno, Infinix et itel.

Taille : M. Dépendances : EIFI-031, EIFI-032.

### EIFI-101 · Écran de saisie du montant

Implémenter l'écran « Saisie du montant » : pavé numérique, touche 000, effacement, libellé facultatif, rappel des opérateurs acceptés et bouton « Encaisser ».

- Le montant est un entier en FCFA, de 100 à 2 000 000, et le bouton reste inactif hors de cette plage.
- Le montant s'affiche avec une espace fine pour les milliers.

Taille : M. Dépendances : EIFI-011.

### EIFI-102 · Créer et gérer la session locale

« Encaisser » crée une session locale (identifiant, montant, libellé, création, expiration) au statut PENDING, enregistrée dans Room. Les transitions sont CONFIRMED, EXPIRED et CANCELLED, toutes définitives.

- Il ne peut exister qu'une session PENDING par montant.
- Une session expire après deux minutes, même si l'app est en arrière-plan.
- Les transitions sont couvertes par des tests unitaires.

Taille : M. Dépendances : EIFI-020, EIFI-012.

### EIFI-103 · Afficher le QR code de paiement

Écran « QR et NFC actifs » : QR de l'URL de paiement avec le pictogramme Eifi au centre, montant, compte à rebours et bouton d'annulation.

- Le QR est lisible par l'appareil photo d'un Android et d'un iPhone à 30 cm.
- La luminosité passe au maximum pendant l'affichage, puis revient à sa valeur initiale.
- À l'expiration, l'écran le signale et propose de relancer avec le même montant.

Taille : M. Dépendances : EIFI-102.

### EIFI-104 · Émettre l'URL par NFC (HCE type 4)

Implémenter le `HostApduService` qui émule un tag NFC de type 4 contenant l'URL de paiement en NDEF, d'après les conclusions de EIFI-003. Le service n'est actif que pendant la session.

- Un téléphone qui paie lit l'URL en approchant son dos de celui qui encaisse.
- Le service ne répond plus après la fin de la session.
- Si le NFC est absent ou désactivé, l'écran le signale sans bloquer le QR.

Taille : L. Dépendances : EIFI-003, EIFI-102.

### EIFI-105 · Lire les SMS de crédit

Implémenter le `NotificationListenerService` et les parseurs embarqués issus de EIFI-002, pour les paiements marchands comme pour les transferts reçus. Un SMS reconnu produit un événement local : opérateur, type d'opération, montant, numéro masqué de la contrepartie, référence et heure.

- Les parseurs passent tous les SMS du jeu de tests de EIFI-002.
- Un SMS d'un expéditeur non configuré est ignoré.
- Un SMS d'un expéditeur configuré mais non reconnu est enregistré comme « non reconnu ».
- Le texte brut du SMS n'est pas conservé.

Taille : L. Dépendances : EIFI-002, EIFI-100.

### EIFI-106 · Rapprocher le SMS de la session

À l'arrivée d'un SMS de crédit, chercher la session PENDING du même montant, ou expirée depuis moins de dix minutes, créée avant le SMS, et la passer en CONFIRMED. Un SMS sans session devient un « paiement non rattaché » dans l'historique.

- Le doublon d'un même SMS (même référence) n'est traité qu'une fois.
- Les cas « session trouvée », « session expirée récemment » et « aucune session » sont couverts par des tests.

Taille : M. Dépendances : EIFI-102, EIFI-105.

### EIFI-107 · Écran « Paiement reçu »

À la confirmation, l'écran d'attente passe à « Paiement reçu » avec un son et une vibration distincts, le montant, l'opérateur, le numéro masqué, la référence et l'heure.

- La confirmation s'affiche moins de deux secondes après la notification du SMS.
- Le son et la vibration se déclenchent même en arrière-plan, avec une notification qui rouvre l'écran.

Taille : M. Dépendances : EIFI-106.

### EIFI-108 · Confirmation manuelle

Depuis l'écran d'attente, celui qui encaisse peut confirmer lui-même après avoir vérifié son SMS. La session passe en CONFIRMED avec la source « manuelle ».

- La confirmation manuelle demande une validation explicite en deux gestes.
- L'historique distingue ces confirmations par un badge jaune « Manuel ».

Taille : S. Dépendances : EIFI-102.

### EIFI-109 · Historique du jour

Écran « Historique du jour » : total encaissé, totaux et nombre de paiements par opérateur, et liste des transactions avec leur statut (Confirmé, Manuel, Expiré, Non rattaché).

- Les totaux comptent les paiements confirmés et non rattachés.
- Les jours précédents sont consultables.

Taille : M. Dépendances : EIFI-106, EIFI-108.

## Mode Payer

### EIFI-200 · Lire l'URL de paiement

Recevoir l'URL de trois façons : par le lecteur NFC (`enableReaderMode`) quand l'app est ouverte, par un App Link quand le tag ou le QR est lu hors de l'app, et par un scanner QR intégré.

- Les trois entrées mènent au même écran récapitulatif.
- Une URL invalide ou expirée affiche un message clair, sans lancer de paiement.

Taille : M. Dépendances : EIFI-020, EIFI-021.

### EIFI-201 · Écran récapitulatif

Implémenter l'écran « Récapitulatif » : nom de celui qui encaisse, montant, compte choisi avec le lien « Changer », rappel que le code secret se saisit dans l'invite de l'opérateur, frais éventuels (EIFI-208) et bouton de validation. Le bouton dit « Payer » pour un code marchand et « Envoyer » pour un transfert vers un numéro.

- Le compte proposé par défaut est le dernier utilisé, s'il est compatible.
- Le compte à rebours de la session est visible, et le paiement est bloqué après l'expiration.

Taille : M. Dépendances : EIFI-011, EIFI-200.

### EIFI-202 · Choix de l'opérateur et de la SIM

Lister les SIM actives, reconnaître leur opérateur et ne proposer que les combinaisons acceptées par celui qui encaisse (écran « Choix de l'opérateur »).

- Sur un téléphone double SIM, chaque option indique la SIM utilisée.
- Si aucune SIM ne correspond aux opérateurs du bénéficiaire, l'écran l'explique (un transfert ne fonctionne qu'entre deux comptes du même opérateur).
- Le choix « Se souvenir » est enregistré localement.

Taille : M. Dépendances : EIFI-001, EIFI-201.

### EIFI-203 · Confirmation biométrique

La validation déclenche une `BiometricPrompt` obligatoire : empreinte, visage ou code de déverrouillage du téléphone.

- Un échec ou une annulation ramène au récapitulatif sans lancer d'appel.
- Sur un téléphone sans verrouillage configuré, l'app demande d'en activer un.

Taille : S. Dépendances : EIFI-201.

### EIFI-204 · Lancer le USSD

Construire le code depuis le gabarit embarqué de l'opérateur et du type d'opération (paiement marchand ou transfert vers un numéro), et le lancer sur la bonne SIM avec `TelecomManager.placeCall`.

- Le code composé correspond exactement à la syntaxe validée en EIFI-001.
- Les permissions `CALL_PHONE` et `READ_PHONE_STATE` sont demandées au premier paiement, avec une explication.
- Le paiement n'est jamais lancé deux fois pour la même session.

Taille : M. Dépendances : EIFI-001, EIFI-202, EIFI-203.

### EIFI-205 · Écran de fin de paiement

Quand l'invite USSD se ferme et que l'app reprend la main, afficher l'écran « Paiement effectué » sous forme de reçu. Si la détection du SMS de débit est activée (EIFI-206), il indique « Débit confirmé par votre SMS » ; sinon, « Demande envoyée », avec l'invitation à vérifier le SMS de l'opérateur. Dans les deux cas, il rappelle que celui qui encaisse reçoit sa propre confirmation.

- L'écran ne reprend pas la mise en page « Paiement reçu » de celui qui encaisse, pour qu'on ne puisse pas les confondre.
- Le reçu est enregistré dans l'historique local et peut être partagé en image.

Taille : M. Dépendances : EIFI-204.

### EIFI-206 · Détection facultative du SMS de débit

Proposer au payeur d'autoriser la lecture des notifications pour confirmer le paiement à partir du SMS de débit de son opérateur. L'option est désactivée par défaut et réutilise `:core:sms` avec les parseurs de débit.

- Le SMS de débit est rattaché au paiement en cours par le montant et l'heure.
- Sans autorisation, le parcours reste complet grâce à l'état « Demande envoyée ».

Taille : M. Dépendances : EIFI-002, EIFI-205.

### EIFI-207 · Historique des paiements envoyés

Liste locale des paiements effectués, avec bénéficiaire, montant, opérateur, date et statut.

- Chaque ligne rouvre le reçu correspondant.

Taille : S. Dépendances : EIFI-205.

### EIFI-208 · Frais et compatibilité des opérateurs

Quand le bénéficiaire reçoit par numéro de téléphone, le paiement est un transfert entre particuliers : il est payant pour celui qui paie et soumis à la taxe de 0,2 %. Le récapitulatif l'annonce avant la biométrie, avec le montant des frais quand il est connu (écran « Payer · Transfert à un ami »). Un transfert ne fonctionne qu'entre deux comptes du même opérateur : l'app ne propose que les comptes compatibles et explique l'absence des autres.

- Un paiement vers un code marchand affiche « Sans frais pour vous ».
- Un transfert affiche « Frais de transfert à votre charge » avant la validation, avec le barème embarqué issu de EIFI-001.
- Si aucun compte du payeur n'est compatible avec ceux du bénéficiaire, le paiement est bloqué avec une explication.

Taille : M. Dépendances : EIFI-001, EIFI-201, EIFI-202.

## Qualité et lancement du pilote

### EIFI-300 · Tests sur appareils réels

Dérouler les parcours complets, paiement marchand et transfert entre amis, sur une matrice d'au moins six téléphones courants (Tecno, Infinix, itel, Samsung, simple et double SIM), avec les deux opérateurs.

- Le taux de succès et le temps du parcours sont mesurés pour chaque combinaison.
- Les anomalies propres à un fabricant sont documentées avec leur contournement.

Taille : L. Dépendances : EIFI-107, EIFI-205, EIFI-208.

### EIFI-301 · Distribution du pilote

Publier l'APK signé auprès de 10 à 20 commerçants à Yaoundé et de leurs clients, via un canal de test (Firebase App Distribution ou piste de test interne du Play Store), avec un formulaire de retour.

- Chaque commerçant pilote est installé et formé en moins de quinze minutes.
- Les plantages sont remontés via Crashlytics.

Taille : M. Dépendances : EIFI-300.

## Reporté après le pilote

Ces sujets sont volontairement hors de cette première version : le backend (sauvegarde, plusieurs appareils), la mise à jour à distance des gabarits USSD et des parseurs de SMS, la page web de secours complète pour ceux qui n'ont pas l'app, l'app iOS, le paiement par carte et un vrai statut de marchand vérifié. Pour l'instant, un marchand n'est qu'un profil d'encaissement doté d'un code marchand.

Sans mise à jour à distance, tout changement de format de SMS chez un opérateur demandera une nouvelle version de l'app. C'est le principal risque du fonctionnement sans backend, à surveiller pendant le pilote.
