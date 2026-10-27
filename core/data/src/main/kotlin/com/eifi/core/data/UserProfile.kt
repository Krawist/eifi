package com.eifi.core.data

import kotlinx.serialization.Serializable

/**
 * Names given in the onboarding: a personal name, a business name, or both. Blank names are not allowed.
 * Stored as JSON (see [JsonSerializer]): a new property needs a default value.
 */
@Serializable
data class UserProfile(val personalName: String?, val businessName: String?) {
    init {
        require(!personalName.isNullOrBlank() || !businessName.isNullOrBlank()) {
            "A personal or business name is required"
        }
        listOfNotNull(personalName, businessName).forEach { name ->
            require(name.isNotBlank() && name.length <= MAX_NAME_LENGTH) {
                "A name must have 1 to $MAX_NAME_LENGTH characters"
            }
        }
    }

    /** Name used to greet the user: the business name first. */
    val greetingName: String
        get() = businessName ?: personalName.orEmpty()

    companion object {
        /** Same limit as the display name of the payment URL, which the business name may become (EIFI-100). */
        const val MAX_NAME_LENGTH = 30
    }
}
