package com.eifi.feature.onboarding

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.design.R as DesignR
import com.eifi.core.design.component.EifiCard
import com.eifi.core.design.component.EifiIconTile
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme

/** First onboarding screen ("Bienvenue"): what Eifi does, in three points. */
@Composable
internal fun WelcomeScreen(onStart: () -> Unit, modifier: Modifier = Modifier) {
    OnboardingLayout(
        step = 0,
        buttonLabel = stringResource(R.string.welcome_start),
        onButtonClick = onStart,
        modifier = modifier,
    ) {
        // The logo is not chosen yet (see docs/design/90-logo-pistes.png): the contactless icon stands in.
        EifiIconTile(
            icon = painterResource(DesignR.drawable.ic_eifi_contactless),
            containerColor = EifiColors.Primary,
            contentColor = EifiColors.OnPrimary,
            size = EifiDimens.IconTileHero,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = EifiSpacing.XXLarge),
        )
        Text(
            text = stringResource(R.string.welcome_title),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = EifiSpacing.Large),
        )
        WelcomePoint(
            icon = DesignR.drawable.ic_eifi_contactless,
            iconBackground = EifiColors.Soft,
            title = R.string.welcome_gesture_title,
            body = R.string.welcome_gesture_body,
        )
        WelcomePoint(
            icon = DesignR.drawable.ic_eifi_wallet,
            iconBackground = EifiColors.AccentSoft,
            title = R.string.welcome_operators_title,
            body = R.string.welcome_operators_body,
        )
        WelcomePoint(
            icon = DesignR.drawable.ic_eifi_shield,
            iconBackground = EifiColors.PrimarySoft,
            title = R.string.welcome_safety_title,
            body = R.string.welcome_safety_body,
        )
    }
}

@Composable
private fun WelcomePoint(@DrawableRes icon: Int, iconBackground: Color, @StringRes title: Int, @StringRes body: Int) {
    EifiCard(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Large)) {
            EifiIconTile(icon = painterResource(icon), containerColor = iconBackground)
            Column(verticalArrangement = Arrangement.spacedBy(EifiSpacing.XSmall)) {
                Text(text = stringResource(title), style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun WelcomeScreenPreview() {
    EifiTheme { WelcomeScreen(onStart = {}) }
}
