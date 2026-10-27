package com.eifi.core.permission

import android.os.Build

/** Brands that stop background apps aggressively and get their own battery guide (all Transsion phones). */
internal enum class DeviceBrand(private val keyword: String) {
    Tecno("tecno"),
    Infinix("infinix"),
    Itel("itel"),
    ;

    companion object {
        /** The brand of [manufacturer] or [brand], as reported by `Build`, or `null` for any other phone. */
        fun of(manufacturer: String, brand: String): DeviceBrand? {
            val names = listOf(manufacturer, brand).map(String::lowercase)
            return entries.firstOrNull { entry -> names.any { it.contains(entry.keyword) } }
        }

        fun current(): DeviceBrand? = of(manufacturer = Build.MANUFACTURER.orEmpty(), brand = Build.BRAND.orEmpty())
    }
}
