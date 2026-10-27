package com.eifi.core.design.theme

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith

/**
 * Screen transitions, as on iOS: the new screen slides in from the right while the previous one moves a quarter
 * of the way to the left and fades; going back plays the same movement in reverse.
 */
object EifiTransitions {
    const val DURATION_MS = 350

    val Enter: EnterTransition = slideInHorizontally(tween(DURATION_MS, easing = FastOutSlowInEasing)) { it }

    val Exit: ExitTransition =
        slideOutHorizontally(tween(DURATION_MS, easing = FastOutSlowInEasing)) { -it / PARALLAX_DIVISOR } +
            fadeOut(tween(DURATION_MS), targetAlpha = COVERED_ALPHA)

    val PopEnter: EnterTransition =
        slideInHorizontally(tween(DURATION_MS, easing = FastOutSlowInEasing)) { -it / PARALLAX_DIVISOR } +
            fadeIn(tween(DURATION_MS), initialAlpha = COVERED_ALPHA)

    val PopExit: ExitTransition = slideOutHorizontally(tween(DURATION_MS, easing = FastOutSlowInEasing)) { it }

    /** Changes of state that are not a navigation, e.g. loading then content. */
    val Fade: ContentTransform = fadeIn(tween(DURATION_MS)) togetherWith fadeOut(tween(DURATION_MS))

    /** For a step shown inside a screen: [Enter] and [Exit] when going forward, [PopEnter] and [PopExit] back. */
    fun step(forward: Boolean): ContentTransform = if (forward) {
        Enter togetherWith Exit
    } else {
        PopEnter togetherWith PopExit
    }

    private const val PARALLAX_DIVISOR = 4
    private const val COVERED_ALPHA = 0.5f
}
