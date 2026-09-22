package eu.trmdnt.workouts.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TextFieldWithCustomPadding(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    modifier: Modifier = Modifier.Companion,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    label: String = "",
    readOnly: Boolean = false,
    placeholder: String = "",
    suffix: String? = null,
    padding: Dp = 10.dp,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    showReadOnly: Boolean = readOnly,
) {
    val colors = OutlinedTextFieldDefaults.colors()

    val textColor = colors.textColor(true, isError, true)

    // Merge with your typography but force the correct color
    val textStyle = LocalTextStyle.current
        .merge(MaterialTheme.typography.bodyMedium)
        .copy(color = textColor)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        textStyle = textStyle,
        cursorBrush = SolidColor(colors.cursorColor(isError = isError)),
        interactionSource = interactionSource
    ) { innerTextField ->
        OutlinedTextFieldDefaults.DecorationBox(
            value = value,
            innerTextField = innerTextField,
            enabled = !showReadOnly,
            singleLine = true,
            isError = isError,
            visualTransformation = VisualTransformation.None,
            label = {
                Text(label)
            },
            placeholder = {
                Text(placeholder)
            },
            trailingIcon = null,
            leadingIcon = null,
            suffix = {
                suffix?.let {
                    Text(it)
                }
            },
            interactionSource = interactionSource,
            contentPadding = PaddingValues(
                padding
            ),
            colors = colors,
            container = {
                OutlinedTextFieldDefaults.Container(
                    enabled = !showReadOnly,
                    isError = isError,
                    interactionSource = interactionSource,
                    colors = colors,
                )
            }
        )
    }
}