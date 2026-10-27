package com.eifi.core.data

import kotlinx.serialization.Serializable

/**
 * Names given in the onboarding: a personal name, a business name, or both. Validated by [UserProfileService].
 * Stored as JSON (see [JsonSerializer]): a new property needs a default value.
 *
 * @property personalName first name or full name of the user, `null` when not given.
 * @property businessName name of the shop or business, `null` when not given; greets the user first.
 */
@Serializable
data class UserProfile(val personalName: String?, val businessName: String?)
