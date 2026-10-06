package eu.trmdnt.workouts.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import eu.trmdnt.workouts.R

@Composable
fun TopAppBarWithBackButton(
    title: String,
    onBack: () -> Unit,
    onAlternativeAction: (() -> Unit)? = null,
    alternativeIcon: (@Composable () -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(title)
        },
        navigationIcon = {
            IconButton(onClick = {
                onBack()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.go_back_to_previous_screen)
                )
            }
        },
        actions = {
            if (onAlternativeAction != null && alternativeIcon != null) {
                IconButton(onClick = { onAlternativeAction() }) {
                    alternativeIcon()
                }
            }
        }
    )
}