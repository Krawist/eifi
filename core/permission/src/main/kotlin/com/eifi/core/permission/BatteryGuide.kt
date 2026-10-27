package com.eifi.core.permission

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eifi.core.design.component.EifiCard
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing

/**
 * Extra steps for the brands that stop background apps despite the battery exemption.
 *
 * The menu names are the most likely ones on HiOS, XOS and itel OS but vary between versions: to verify on real
 * phones (EIFI-300) before the pilot.
 */
@Composable
internal fun BatteryGuide(brand: DeviceBrand, modifier: Modifier = Modifier) {
    EifiCard(shape = EifiRadius.CardLarge, color = EifiColors.Soft, modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(EifiSpacing.Small)) {
            Text(
                text = stringResource(R.string.battery_guide_title, stringResource(brand.displayName)),
                style = MaterialTheme.typography.titleMedium,
                color = EifiColors.OnSoft,
            )
            brand.steps.forEachIndexed { index, step ->
                Row(horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Small)) {
                    Text(text = "${index + 1}.", style = MaterialTheme.typography.bodyLarge, color = EifiColors.OnSoft)
                    Text(
                        text = stringResource(step),
                        style = MaterialTheme.typography.bodyLarge,
                        color = EifiColors.OnSoft,
                    )
                }
            }
        }
    }
}

private val DeviceBrand.displayName: Int
    get() = when (this) {
        DeviceBrand.Tecno -> R.string.battery_guide_brand_tecno
        DeviceBrand.Infinix -> R.string.battery_guide_brand_infinix
        DeviceBrand.Itel -> R.string.battery_guide_brand_itel
    }

/** Only the auto-start manager differs between the brands; the battery and recents steps are shared. */
private val DeviceBrand.steps: List<Int>
    get() = listOf(
        when (this) {
            DeviceBrand.Tecno -> R.string.battery_guide_autostart_tecno
            DeviceBrand.Infinix -> R.string.battery_guide_autostart_infinix
            DeviceBrand.Itel -> R.string.battery_guide_autostart_itel
        },
        R.string.battery_guide_no_restriction,
        R.string.battery_guide_lock_recents,
    )
