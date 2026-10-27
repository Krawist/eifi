package com.eifi.core.design.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiDimens
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme

/** Dots showing the [current] step (from 0) out of [count]; purely visual, hidden from accessibility. */
@Composable
fun EifiPageIndicator(count: Int, current: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(EifiSpacing.Small),
    ) {
        repeat(count) { index ->
            val selected = index == current
            val width by animateDpAsState(if (selected) EifiDimens.PageDotSelected else EifiDimens.PageDot)
            Box(
                modifier = Modifier
                    .width(width)
                    .height(EifiDimens.PageDot)
                    .background(if (selected) EifiColors.Ink else InactiveDot, CircleShape),
            )
        }
    }
}

private val InactiveDot = EifiColors.Muted.copy(alpha = 0.3f)

@Preview(showBackground = true, backgroundColor = 0xFFF6F8F7)
@Composable
private fun EifiPageIndicatorPreview() {
    EifiTheme {
        Column(
            modifier = Modifier.padding(EifiSpacing.Gutter),
            verticalArrangement = Arrangement.spacedBy(EifiSpacing.Medium),
        ) {
            EifiPageIndicator(count = 3, current = 0)
            EifiPageIndicator(count = 3, current = 1)
        }
    }
}
