package com.eifi.feature.receive.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.eifi.core.design.R as DesignR
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.feature.receive.R
import com.eifi.feature.receive.profile.phone.PhoneCountry
import java.util.Locale

/** Flag and calling code of the selected country; the menu lists the supported countries with their name. */
@Composable
internal fun CountrySelector(
    selected: PhoneCountry,
    countries: List<PhoneCountry>,
    onSelect: (PhoneCountry) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val description = stringResource(R.string.receiving_profile_country, countryLabel(selected))
    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            shape = EifiRadius.Button,
            color = EifiColors.Background,
            contentColor = EifiColors.Ink,
            modifier = Modifier
                .heightIn(min = EifiDimens.TextFieldHeight)
                .semantics { contentDescription = description },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = EifiSpacing.Medium),
                horizontalArrangement = Arrangement.spacedBy(EifiSpacing.XSmall),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = selected.flag, style = MaterialTheme.typography.titleLarge)
                Text(text = selected.callingCode, style = MaterialTheme.typography.bodyLarge)
                Icon(
                    painter = painterResource(DesignR.drawable.ic_eifi_chevron_down),
                    contentDescription = null,
                    tint = EifiColors.Muted,
                    modifier = Modifier.size(EifiDimens.PointIcon),
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            shape = EifiRadius.Button,
            containerColor = EifiColors.Surface,
        ) {
            countries.forEach { country ->
                DropdownMenuItem(
                    text = { Text(text = countryLabel(country)) },
                    onClick = {
                        onSelect(country)
                        expanded = false
                    },
                )
            }
        }
    }
}

/** e.g. "🇨🇲 Cameroun (CM) +237", the name in the language of the phone. */
@Composable
private fun countryLabel(country: PhoneCountry): String {
    val locale = LocalConfiguration.current.locales[0]
    val name = remember(country, locale) {
        Locale.Builder().setRegion(country.regionCode).build().getDisplayCountry(locale)
    }
    return stringResource(
        R.string.receiving_profile_country_item,
        country.flag,
        name,
        country.regionCode,
        country.callingCode,
    )
}
