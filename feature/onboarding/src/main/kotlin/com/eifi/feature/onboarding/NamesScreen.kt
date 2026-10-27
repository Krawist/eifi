package com.eifi.feature.onboarding

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.design.component.EifiBackButton
import com.eifi.core.design.component.EifiTextField
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme

/** Third onboarding screen: personal name, business name, or both. */
@Composable
internal fun NamesScreen(
    uiState: NamesUiState,
    onPersonalNameChange: (String) -> Unit,
    onBusinessNameChange: (String) -> Unit,
    onFinish: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OnboardingLayout(
        step = 2,
        buttonLabel = stringResource(R.string.names_finish),
        onButtonClick = onFinish,
        buttonEnabled = uiState.canFinish,
        modifier = modifier,
    ) {
        EifiBackButton(onClick = onBack)
        Text(
            text = stringResource(R.string.names_title),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(top = EifiSpacing.XLarge),
        )
        Text(
            text = stringResource(R.string.names_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        EifiTextField(
            value = uiState.personalName,
            onValueChange = onPersonalNameChange,
            label = stringResource(R.string.names_personal),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next,
            ),
            modifier = Modifier.padding(top = EifiSpacing.Large),
        )
        EifiTextField(
            value = uiState.businessName,
            onValueChange = onBusinessNameChange,
            label = stringResource(R.string.names_business),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { onFinish() }),
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun NamesScreenPreview() {
    EifiTheme {
        NamesScreen(
            uiState = NamesUiState(personalName = "Paul"),
            onPersonalNameChange = {},
            onBusinessNameChange = {},
            onFinish = {},
            onBack = {},
        )
    }
}
