package com.eifi.core.design.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.eifi.core.design.R
import com.eifi.core.design.format.formatFcfa
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme

/** Currency size relative to the amount, as in the mockups (« 2 500 FCFA »). */
private val CurrencyScale = 0.34.em
private val MinAmountSize = 32.sp

/**
 * Large amount followed by a smaller muted « FCFA », baseline aligned.
 * The text shrinks down to 32 sp to stay on one line (2 000 000 on a narrow screen).
 */
@Composable
fun AmountDisplay(
    amount: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayLarge,
) {
    val currency = stringResource(R.string.currency_fcfa)
    val text = buildAnnotatedString {
        append(formatFcfa(amount))
        withStyle(SpanStyle(fontSize = CurrencyScale, color = EifiColors.Muted, letterSpacing = 0.em)) {
            append(" ")
            append(currency)
        }
    }
    BasicText(
        text = text,
        modifier = modifier,
        style = style.copy(color = EifiColors.Ink, textAlign = TextAlign.Center),
        maxLines = 1,
        softWrap = false,
        autoSize = TextAutoSize.StepBased(minFontSize = MinAmountSize, maxFontSize = style.fontSize),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F8F7, widthDp = 360)
@Composable
private fun AmountDisplayPreview() {
    EifiTheme {
        Column(modifier = Modifier.padding(EifiSpacing.Gutter)) {
            AmountDisplay(amount = 2_500, modifier = Modifier.fillMaxWidth())
            AmountDisplay(amount = 2_000_000, modifier = Modifier.fillMaxWidth())
        }
    }
}
