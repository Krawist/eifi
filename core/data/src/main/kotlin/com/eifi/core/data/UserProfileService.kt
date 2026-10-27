package com.eifi.core.data

import javax.inject.Inject

/** Rules of a [UserProfile]. */
class UserProfileService @Inject constructor() {

    /** Name used to greet the user: the business name first. */
    fun greetingName(profile: UserProfile): String = profile.businessName ?: profile.personalName.orEmpty()

    /** @throws IllegalArgumentException without any name, or with a blank or too long name. */
    fun validate(profile: UserProfile) {
        require(!profile.personalName.isNullOrBlank() || !profile.businessName.isNullOrBlank()) {
            "A personal or business name is required"
        }
        listOfNotNull(profile.personalName, profile.businessName).forEach { name ->
            require(name.isNotBlank() && name.length <= MAX_NAME_LENGTH) {
                "A name must have 1 to $MAX_NAME_LENGTH characters"
            }
        }
    }

    companion object {
        /** Same limit as the display name of the payment URL, which the business name may become (EIFI-100). */
        const val MAX_NAME_LENGTH = 30
    }
}
