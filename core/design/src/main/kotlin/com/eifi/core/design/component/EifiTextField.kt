package com.eifi.core.design.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import com.eifi.core.design.theme.EifiColors
import com.eifi.core.design.theme.EifiRadius
import com.eifi.core.design.theme.EifiSpacing
import com.eifi.core.design.theme.EifiTheme

/**
 * Single-line white field with a floating label, without underline.
 * [containerColor] is grey inside a white card.
 */
@Composable
fun EifiTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    containerColor: Color = EifiColors.Surface,
    keyboardOptions: KeyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label) },
        supportingText = supportingText?.let { { Text(text = it) } },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = EifiRadius.Button,
        textStyle = MaterialTheme.typography.bodyLarge,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedLabelColor = EifiColors.OnPrimary,
            unfocusedLabelColor = EifiColors.Muted,
            cursorColor = EifiColors.OnPrimary,
            focusedSupportingTextColor = EifiColors.Muted,
            unfocusedSupportingTextColor = EifiColors.Muted,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F8F7)
@Composable
private fun EifiTextFieldPreview() {
    EifiTheme {
        Column(modifier = Modifier.padding(EifiSpacing.Gutter)) {
            EifiTextField(value = "Paul", onValueChange = {}, label = "Votre nom")
            EifiTextField(
                value = "",
                onValueChange = {},
                label = "Nom de votre commerce",
                supportingText = "Facultatif",
                modifier = Modifier.padding(top = EifiSpacing.Medium),
            )
        }
    }
}
