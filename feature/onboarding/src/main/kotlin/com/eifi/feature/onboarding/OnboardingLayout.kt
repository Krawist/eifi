package com.eifi.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eifi.core.design.component.EifiPageIndicator
import com.eifi.core.design.component.EifiPrimaryButton
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiSpacing

/** Steps shown by the page dots: welcome, mode choice, names. */
internal const val ONBOARDING_STEPS = 3

/** Scrolling [content], with the page dots and the main button kept at the bottom. */
@Composable
internal fun OnboardingLayout(
    step: Int,
    buttonLabel: String,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(modifier = modifier.fillMaxSize(), color = EifiColors.Background) {
        Column(modifier = Modifier.safeDrawingPadding().padding(EifiSpacing.Gutter)) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(EifiSpacing.Medium),
                content = content,
            )
            EifiPageIndicator(
                count = ONBOARDING_STEPS,
                current = step,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = EifiSpacing.XLarge),
            )
            EifiPrimaryButton(text = buttonLabel, onClick = onButtonClick, enabled = buttonEnabled)
        }
    }
}
