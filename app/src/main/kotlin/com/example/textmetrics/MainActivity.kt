package com.example.textmetrics

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.textmetrics.ui.TextMetricsScreen
import com.example.textmetrics.ui.TextMetricsViewModel
import com.example.textmetrics.ui.theme.TextMetricsTheme
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: TextMetricsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val currentLanguage = uiState.selectedLanguage

            // Sukuriame lokalizuotą kontekstą dinamiškam kalbos keitimui pagal ViewModel būseną (SSOT)
            val localizedContext = remember(currentLanguage) {
                createLocalizedContext(this, currentLanguage)
            }
            val localizedConfiguration = remember(currentLanguage) {
                val config = Configuration(resources.configuration)
                val locale = Locale.forLanguageTag(currentLanguage)
                config.setLocale(locale)
                config
            }

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedConfiguration
            ) {
                TextMetricsTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        TextMetricsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    private fun createLocalizedContext(context: Context, languageCode: String): Context {
        val locale = Locale.forLanguageTag(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
