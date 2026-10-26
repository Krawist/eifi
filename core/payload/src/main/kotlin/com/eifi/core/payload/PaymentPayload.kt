package com.eifi.core.payload

/**
 * Format de l'URL de paiement : `https://pay.<domaine>/p#v1.{données base64url}`.
 *
 * TODO(EIFI-020) : modèle complet (identifiant, nom affiché ≤ 30 caractères, montant,
 * moyen de réception par opérateur, expiration, somme de contrôle), encodeur et décodeur.
 */
object PaymentPayload {
    const val VERSION_PREFIX = "v1."
    const val MAX_DISPLAY_NAME_LENGTH = 30
}
