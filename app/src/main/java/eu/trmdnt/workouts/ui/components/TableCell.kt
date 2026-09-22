package eu.trmdnt.workouts.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float
) {
    Text(
        text = text,
        Modifier
            .border(0.5.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
            .weight(weight)
            .padding(4.dp)
    )
}

@Composable
fun RowScope.TableHeader(
    text: String,
    weight: Float
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLargeEmphasized,
        modifier = Modifier
            .border(0.5.dp, MaterialTheme.colorScheme.onBackground)
            .weight(weight)
            .padding(4.dp)
    )
}