package com.example.async.ui.components.inputs

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import com.example.async.ui.components.text.BodyMedium
import com.example.async.ui.components.text.LabelMedium

/**
 * Text input components for the Async music player.
 * Provides consistent text field styling and music-specific input fields.
 */

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium,
    label: (@Composable () -> Unit)? = null,
    placeholder: (@Composable () -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    supportingText: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle,
        label = label,
        placeholder = placeholder,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        maxLines = maxLines,
        minLines = minLines,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors
    )
}

@Composable
fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search music...",
    onSearch: (String) -> Unit = {},
    onClear: () -> Unit = {},
    active: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    AppTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        enabled = enabled,
        singleLine = true,
        placeholder = {
            BodyMedium(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = KeyboardOptions.Default,
        keyboardActions = KeyboardActions(
            onSearch = { onSearch(query) }
        ),
        colors = if (active) {
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        } else {
            TextFieldDefaults.colors()
        }
    )
}

@Composable
fun PlaylistNameField(
    name: String,
    onNameChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    AppTextField(
        value = name,
        onValueChange = onNameChange,
        modifier = modifier.fillMaxWidth(),
        label = {
            LabelMedium("Playlist Name")
        },
        placeholder = {
            BodyMedium(
                text = "Enter playlist name",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        singleLine = true,
        isError = isError,
        supportingText = errorMessage?.let { 
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

@Composable
fun PlaylistDescriptionField(
    description: String,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AppTextField(
        value = description,
        onValueChange = onDescriptionChange,
        modifier = modifier.fillMaxWidth(),
        label = {
            LabelMedium("Description (Optional)")
        },
        placeholder = {
            BodyMedium(
                text = "Add a description for your playlist",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        maxLines = 3,
        minLines = 2
    )
}

@Composable
fun TagInputField(
    tags: String,
    onTagsChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AppTextField(
        value = tags,
        onValueChange = onTagsChange,
        modifier = modifier.fillMaxWidth(),
        label = {
            LabelMedium("Tags")
        },
        placeholder = {
            BodyMedium(
                text = "Add tags separated by commas",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        supportingText = {
            Text(
                text = "Use tags to organize and find your music easier",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        singleLine = true
    )
}

@Composable
fun UrlInputField(
    url: String,
    onUrlChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "URL",
    placeholder: String = "Enter URL",
    isError: Boolean = false,
    errorMessage: String? = null
) {
    AppTextField(
        value = url,
        onValueChange = onUrlChange,
        modifier = modifier.fillMaxWidth(),
        label = {
            LabelMedium(label)
        },
        placeholder = {
            BodyMedium(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        singleLine = true,
        isError = isError,
        supportingText = errorMessage?.let { 
            {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        keyboardOptions = KeyboardOptions.Default
    )
} 