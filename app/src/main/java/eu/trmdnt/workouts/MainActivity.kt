package eu.trmdnt.workouts


import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import eu.trmdnt.workouts.ui.main.MainScreen
import eu.trmdnt.workouts.ui.theme.AppTheme
import eu.trmdnt.workouts.ui.theme.isDarkMode

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1
            )
        }

        setContent {
            val viewModel: MainActivityViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            splashScreen.setKeepOnScreenCondition { uiState.showSplashScreen }

            val systemBarStyle = if (isDarkMode(uiState.theme)) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            }

            enableEdgeToEdge(
                statusBarStyle = systemBarStyle,
                navigationBarStyle = systemBarStyle
            )

            AppTheme(theme = uiState.theme, dynamicColor = uiState.useDynamicColors) {
                MainScreen(
                    showTimer = uiState.showTimer,
                    timerText = uiState.timerText,
                    onTimerCancelPressed = {
                        viewModel.onTimerCancelPressed()
                    },
                    onTimerAddTimePressed = {
                        viewModel.onTimerAddTimePressed()
                    },
                    addTimer = { workoutId, time ->
                        viewModel.addTimer(workoutId, time)
                    },
                    showTimerPickerButton = uiState.showTimerPickerButton,
                    timerDefaultValue = uiState.timerDefaultValue,
                )
            }
        }
    }
}