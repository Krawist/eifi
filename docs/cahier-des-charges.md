# Cahier des charges — Eifi

> Copie de travail du document de référence tenu dans Claude (26 octobre 2026) : https://claude.ai/artifact/32DZB7Fg15dCH5Payt8w2D. En cas d'écart, le document Claude fait foi.

## Contexte et objectifs

Eifi (prononcé « aï-fi ») est une application Android qui réunit deux modes, Payer et Encaisser. Celui qui encaisse saisit le montant ; celui qui paie approche son téléphone (NFC) ou scanne un QR dynamique, puis valide avec son code secret dans l'invite USSD de son opérateur, Orange Money ou MTN MoMo. Le paiement peut aller à un commerçant (code marchand) ou à un particulier (transfert vers son numéro), par exemple entre deux amis.

Aujourd'hui, le paiement marchand passe par un QR imprimé ou un code marchand : il ne contient pas le montant, que le client doit saisir lui-même. Chaque opérateur a en plus sa propre application.

Objectifs du produit :

- Réduire le paiement à trois gestes pour celui qui paie : approcher ou scanner, valider par biométrie, saisir son code secret.
- Réunir Orange Money et MTN MoMo dans un seul outil, pour payer comme pour encaisser.
- Ne jamais détenir les fonds : l'argent va directement, chez l'opérateur, du compte de celui qui paie vers celui qui encaisse.
- Confirmer le paiement chez celui qui encaisse à partir du SMS de l'opérateur, jamais à partir de l'écran de celui qui paie.

## Périmètre

Le pilote couvre le paiement en face à face, entre un commerçant et son client ou entre deux particuliers, via Orange Money et MTN MoMo, sur Android et sans backend.

| Inclus dans le pilote | Exclu du pilote |
| --- | --- |
| App Android unique : onboarding et deux modes, Payer et Encaisser | Détention, collecte ou reversement de fonds par l'application |
| Encaisser : saisie du montant, émission NFC et QR, lecture des SMS de crédit, historique | Backend : sauvegarde, plusieurs appareils, mise à jour à distance des gabarits et parseurs |
| Payer : lecture NFC et QR, choix du compte et de la SIM, biométrie, lancement du USSD, reçu | Paiement par carte bancaire et intégration bancaire |
| Paiement marchand (code marchand) et transfert entre particuliers (numéro de téléphone) | Paiement en ligne et paiement à distance |
| Hébergement statique pour les App Links et une page d'attente | Page web de secours complète, application iOS native |

La banque et les cartes feront l'objet d'une étude séparée, car elles exigent un agrément ou un partenaire acquéreur.

## Acteurs

La cible prioritaire reste le petit commerçant équipé d'un smartphone Android milieu de gamme, mais tout utilisateur peut encaisser, par exemple pour recevoir l'argent d'un ami.

| Acteur | Profil | Attente principale |
| --- | --- | --- |
| Commerçant qui encaisse | Kiosque, boutique, vendeur de rue ; Android milieu de gamme, souvent double SIM ; code marchand OM et/ou MoMo | Encaisser vite, voir la confirmation sans doute possible, sans frais pour ses clients |
| Particulier qui encaisse | Reçoit sur son numéro Orange Money ou MoMo, sans code marchand | Recevoir l'argent d'un proche sans dicter son numéro |
| Payeur avec l'app | Android, compte OM ou MoMo, NFC selon le modèle | Payer sans rien saisir à part son code secret, en connaissant les frais éventuels |
| Payeur sans l'app | Tout téléphone avec appareil photo ou NFC | Être orienté vers l'installation d'Eifi (page de secours complète après le pilote) |
| Payeur sur iPhone | iOS ne permet pas de lancer un USSD contenant * ou # | Après le pilote : page de secours avec le code à copier |
| Opérateurs (Orange, MTN) | Exécutent le paiement et envoient les SMS | Aucune intégration API prévue au pilote |

## Parcours de paiement

1. Celui qui encaisse saisit le montant et appuie sur « Encaisser ».
2. Eifi crée une session locale et affiche le QR dynamique tout en activant l'émission NFC.
3. Celui qui paie approche son téléphone (NFC) **ou** scanne le QR (embranchement).
4. Eifi, côté payeur, affiche le nom du bénéficiaire, le montant et les frais éventuels.
5. Le payeur choisit son compte (opérateur et SIM).
6. Il valide par biométrie.
7. Eifi lance le code USSD pré-rempli sur la bonne SIM.
8. Le payeur saisit son code secret dans l'invite de l'opérateur.
9. L'opérateur débite et crédite, puis envoie les SMS.
10. Eifi, côté encaisseur, lit le SMS de crédit, rapproche la session et affiche « Paiement reçu » ; le payeur voit son reçu.

Les deux branches de celui qui paie se rejoignent sur l'invite USSD de l'opérateur. L'argent va directement à celui qui encaisse, et l'application ne fait que lire la confirmation. Pendant le pilote, sans backend, le rapprochement se fait sur le téléphone de celui qui encaisse.

## Exigences fonctionnelles

### Onboarding et accueil

| ID | Exigence |
| --- | --- |
| O1 | Onboarding commun : présentation d'Eifi, puis choix du mode de départ, Payer ou Encaisser. |
| O2 | Accueil avec les deux actions, Payer et Encaisser, et l'activité récente (paiements envoyés et reçus). |
| O3 | Chaque permission est demandée au moment où elle sert, jamais pendant l'onboarding commun. Un refus ne bloque jamais le parcours. |

### Mode Encaisser

| ID | Exigence |
| --- | --- |
| M1 | Profil d'encaissement, rempli au premier passage en mode Encaisser : nom affiché et, pour chaque opérateur, un code marchand ou un numéro de réception. |
| M2 | Pavé numérique de saisie du montant en FCFA, avec un libellé facultatif. |
| M3 | Création d'une session de paiement : affichage du QR dynamique et activation de l'émission NFC en simultané. |
| M4 | Expiration automatique de la session après un délai configurable (2 minutes par défaut). |
| M5 | Une seule session en attente par montant à un instant donné, pour lever l'ambiguïté lors du rapprochement. |
| M6 | Confirmation automatique par lecture de la notification SMS de crédit, avec un signal sonore et une vibration distincts. |
| M7 | Confirmation manuelle possible lorsque la lecture automatique est désactivée ou échoue. |
| M8 | Historique des encaissements du jour et totaux par opérateur. |

### Mode Payer

| ID | Exigence |
| --- | --- |
| C1 | Lecture du payload NFC ou du QR, puis affichage du marchand et du montant avant toute action. |
| C2 | Choix de l'opérateur selon les codes marchands disponibles et les SIM présentes. |
| C3 | Sélection automatique de la SIM correspondant à l'opérateur choisi sur les téléphones double SIM. |
| C4 | Lancement du code USSD pré-rempli (code marchand ou numéro, et montant), le code secret restant saisi par celui qui paie dans l'invite de l'opérateur. |
| C5 | Confirmation biométrique obligatoire avant le lancement du USSD (empreinte, visage ou code de déverrouillage du téléphone). |
| C6 | Historique local des paiements initiés. |
| C7 | Pour un transfert vers un numéro, affichage des frais à la charge du payeur avant la validation ; seuls les comptes du même opérateur que le bénéficiaire sont proposés. |
| C8 | Écran de fin de paiement sous forme de reçu : « Débit confirmé » si le SMS de débit est détecté (option désactivée par défaut), sinon « Demande envoyée ». Sa mise en page diffère de l'écran « Paiement reçu » de celui qui encaisse. |

### Page web de secours (après le pilote)

| ID | Exigence |
| --- | --- |
| W1 | Le QR encode une URL signée ; sans l'app, elle ouvre une page légère affichant le marchand et le montant. |
| W2 | La page affiche le code USSD complet à composer, avec un bouton qui ouvre le composeur sur Android et une copie du code pour iOS. |
| W3 | Lien vers l'installation de l'app, jamais obligatoire pour payer. |

## Exigences techniques

L'app est développée en Kotlin natif pour Android. Le pilote fonctionne sans backend ; le backend en Kotlin avec Micronaut viendra ensuite.

**Transport NFC.** Le téléphone de celui qui encaisse émule en HCE (`HostApduService`) un tag NFC Forum de type 4 contenant l'URL de paiement en NDEF ; celui qui paie le lit en reader mode (`NfcAdapter.enableReaderMode`). Un téléphone sans Eifi lit nativement ce tag et ouvre l'URL dans le navigateur. Le payload transporté est identique à celui du QR.

**Payload.** Le QR et le NFC transportent une URL de la forme `https://pay.<domaine>/p#v1.{données}`. Sans backend, le fragment, jamais envoyé sur le réseau, porte toute la session encodée en base64url : identifiant aléatoire, nom affiché, montant, moyen de réception de chaque opérateur (code marchand ou numéro), expiration et somme de contrôle.

**Lancement USSD.** L'app construit le code à partir du gabarit embarqué de l'opérateur et du type d'opération (paiement marchand ou transfert vers un numéro). Elle le lance sur la bonne SIM avec `TelecomManager.placeCall` et un `PhoneAccountHandle`, ce qui requiert `CALL_PHONE` ; les `#` sont encodés en `%23`. La syntaxe exacte de chaque opérateur est à confirmer (voir Risques).

**Confirmation chez celui qui encaisse.** Un `NotificationListenerService`, activé au premier encaissement, lit les notifications SMS d'Orange et de MTN. Des parseurs embarqués, un par opérateur et par type d'opération, extraient le montant, le numéro de la contrepartie et la référence. Le rapprochement avec la session en attente se fait sur le téléphone. Les permissions `READ_SMS` et `RECEIVE_SMS` sont écartées à cause des restrictions du Play Store.

**Backend (après le pilote).** Un service Micronaut avec PostgreSQL et Redis prendra en charge la sauvegarde, le multi-appareil et la mise à jour à distance des gabarits USSD et des parseurs. Le détail figure dans le dossier technique.

**iOS.** Il n'y a pas d'app iOS au MVP. Un iPhone peut payer via la page web de secours en copiant le code USSD.

## Sécurité et lutte contre la fraude

La règle centrale est la suivante : un paiement n'est confirmé qu'à partir du SMS reçu par celui qui encaisse, jamais à partir de ce que montre celui qui paie.

| Menace | Mesure |
| --- | --- |
| Fausse capture d'écran « paiement réussi » | La confirmation s'affiche uniquement sur le téléphone du marchand, avec un signal distinct ; l'écran de fin de celui qui paie est un reçu, conçu pour ne pas pouvoir être confondu avec l'écran « Paiement reçu ». |
| QR falsifié ou recollé (redirection vers un autre code marchand) | QR affiché sur l'écran de celui qui encaisse et renouvelé à chaque session, courte durée ; nom et moyen de réception montrés au payeur avant toute validation. Une signature des données est à étudier. |
| Rejeu d'une ancienne session | Expiration à 2 minutes et état terminal non réutilisable. |
| Ambiguïté de rapprochement (deux paiements identiques) | Une seule session en attente par montant ; la confirmation manuelle reste en secours. |
| Changement du format des SMS opérateur | Parseurs versionnés et mis à jour à distance, avec repli automatique sur la confirmation manuelle. |
| Fuite de données | Aucun code secret ni solde stocké ; le contenu SMS est réduit au montant, au numéro et à la référence ; chiffrement en transit. |
| Relais NFC | Faible enjeu, car le client valide de toute façon avec son code secret dans l'invite opérateur. |

## Exigences non fonctionnelles

Les cibles ci-dessous sont des objectifs de départ, à ajuster après les tests terrain.

| Critère | Cible |
| --- | --- |
| Temps entre la saisie du montant et l'affichage du QR | Moins de 1 s, avec génération locale possible si le réseau est lent |
| Temps entre le tap NFC ou le scan et l'ouverture de l'invite USSD | Moins de 3 s, hors délai propre à l'opérateur |
| Délai de confirmation après réception du SMS | Moins de 2 s |
| Compatibilité | Android 8.0 (API 26) et plus ; NFC facultatif, QR obligatoire |
| Taille de l'app | Moins de 15 Mo, adaptée aux forfaits data limités |
| Réseau dégradé | Fonctionnement en 3G instable ; le USSD lui-même ne requiert pas de données mobiles |
| Langues | Français et anglais |
| Fonctionnement hors ligne | Parcours complet sans connexion Internet pendant le pilote |

## Contraintes réglementaires et opérateurs

Comme l'application ne détient ni ne transfère de fonds, elle se positionne comme un outil technique et non comme un prestataire de services de paiement. Ce positionnement doit être validé par un juriste avant le lancement.

- En zone CEMAC, encaisser ou détenir des fonds pour le compte de tiers relève du statut de prestataire de services de paiement, soumis à agrément ([règlement de 2018](https://droitmediasfinance.com/index.php/actualites/droit-tech-fintech/370-cameroun-la-filiale-fin-tech-d-orange-obtient-un-agrement-d-etablissement-de-paiement-comprendre)). Le MVP reste hors de ce cadre.
- Le paiement marchand Orange Money est [gratuit pour le client et hors taxe de 0,2 %](https://lefisk.cm/blog/orange-money-cameroun-fonctionnement-comptes-frais-taxe-guide). L'app conserve ce circuit sans ajouter de frais.
- Les conditions d'utilisation d'Orange et de MTN doivent être vérifiées sur l'automatisation du lancement USSD et la lecture des SMS.
- Les données personnelles (numéros, historique) relèvent de la loi camerounaise sur la protection des données. Une politique de confidentialité est requise pour le Play Store.
- L'usage de `CALL_PHONE` et du `NotificationListenerService` doit être justifié dans la fiche Play Store.

Un transfert entre particuliers est payant pour l'expéditeur et soumis à la taxe de 0,2 % : l'app l'annonce avant chaque transfert.

## Risques et inconnues à valider

Deux inconnues sont bloquantes et doivent être levées par le prototype avant tout autre développement.

| Risque | Impact | Validation |
| --- | --- | --- |
| Absence de chaîne USSD de paiement marchand en une seule ligne chez un opérateur | Bloquant : le client doit naviguer dans les menus | Tests réels sur comptes marchands OM et MoMo |
| Format des SMS de crédit marchand instable ou incomplet | Bloquant pour la confirmation automatique | Collecte d'une vingtaine de SMS réels par opérateur |
| Orange ou MTN lancent un QR dynamique avec montant | Le différenciateur se réduit à l'agrégation | Veille sur MaxIt et l'app MoMo |
| Refus Play Store pour `CALL_PHONE` ou le listener de notifications | Retard de publication | Déclaration anticipée et distribution APK en pilote |
| Faible part de téléphones NFC chez les clients | Le QR devient le canal principal | Mesure pendant le pilote |
| Fabricants Android qui tuent les services en arrière-plan (Tecno, Infinix, Itel) | Confirmations manquées | Tests d'optimisation batterie par marque |
| Requalification réglementaire en prestataire de paiement | Arrêt ou partenariat obligatoire | Avis juridique avant le lancement public |
| Frais de transfert mal compris par les payeurs | Perte de confiance dans Eifi | Frais affichés avant validation (C7) et tests auprès des payeurs du pilote |

## Plan de livraison

Le projet avance en trois phases, et chaque phase ne démarre que si la précédente a atteint son critère de sortie.

1. **Prototype de validation.** Une app brute pour générer un QR, lancer le USSD (paiement marchand et transfert) et lire les notifications SMS, testée sur un téléphone Tecno double SIM avec de vrais comptes marchands OM et MoMo.
   - Critère de sortie : un paiement réel réussi par opérateur et par type d'opération, sans saisie manuelle du montant, et un SMS de crédit correctement parsé.
2. **Pilote sans backend.** L'app Eifi complète (onboarding, Payer, Encaisser), sans backend, déployée auprès de 10 à 20 marchands à Yaoundé, avec distribution APK hors Play Store.
   - Critère de sortie : temps de paiement inférieur au circuit actuel, et taux de confirmation automatique mesuré.
3. **Lancement public et évolutions.** Publication Play Store, backend (sauvegarde, multi-appareil, mise à jour à distance), page web de secours complète, puis étude de l'app iOS et du paiement par carte via un partenaire agréé.
