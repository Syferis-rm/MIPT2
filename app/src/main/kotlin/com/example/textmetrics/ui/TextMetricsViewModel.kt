package com.example.textmetrics.ui

import androidx.lifecycle.ViewModel
import com.example.textmetrics.core.DefaultTextMetricsCalculator
import com.example.textmetrics.core.SampleSentencesProvider
import com.example.textmetrics.core.TextMetrics
import com.example.textmetrics.core.TextMetricsCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Istorijos įrašo duomenų modelis.
 */
data class HistoryItem(
    val id: Long = System.nanoTime(),
    val text: String,
    val metrics: TextMetrics
) {
    /**
     * Reikalaujamas formatas: „Tekstas“ => S, Ž, Ženklai, Sk
     */
    val displayFormat: String get() = metrics.toHistoryEntry(text)
}

/**
 * Vartotojo sąsajos būsenos modelis.
 */
data class TextMetricsUiState(
    val inputText: String = "",
    val isInputError: Boolean = false,
    val currentMetrics: TextMetrics? = null,
    val history: List<HistoryItem> = emptyList(),
    val selectedLanguage: String = "lt" // "lt" arba "en"
)

/**
 * ViewModel valdanti teksto analizės būseną, validaciją ir vykdymo laiko istoriją.
 */
class TextMetricsViewModel(
    private val calculator: TextMetricsCalculator = DefaultTextMetricsCalculator(),
    private val sampleProvider: SampleSentencesProvider = SampleSentencesProvider()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TextMetricsUiState())
    val uiState: StateFlow<TextMetricsUiState> = _uiState.asStateFlow()

    fun onInputTextChanged(newText: String) {
        _uiState.update { current ->
            current.copy(
                inputText = newText,
                isInputError = if (newText.isNotBlank()) false else current.isInputError
            )
        }
    }

    /**
     * Vykdo metrikų skaičiavimą (1 ir 4 reikalavimai).
     * @return true jei skaičiavimas sėkmingas, false jei įvestis tuščia.
     */
    fun calculateMetrics(): Boolean {
        val currentText = _uiState.value.inputText

        if (currentText.isBlank()) {
            _uiState.update { it.copy(isInputError = true) }
            return false
        }

        val calculated = calculator.calculate(currentText)
        val historyEntry = HistoryItem(text = currentText, metrics = calculated)

        _uiState.update { current ->
            current.copy(
                isInputError = false,
                currentMetrics = calculated,
                history = (listOf(historyEntry) + current.history).take(100) // Naujausi viršuje, maks 100 įrašų
            )
        }
        return true
    }

    /**
     * Sugeneruoja pavyzdinį testinį sakinį į įvesties lauką (2 reikalavimas).
     */
    fun generateSample() {
        val sample = sampleProvider.getNextSample()
        _uiState.update { current ->
            current.copy(
                inputText = sample,
                isInputError = false
            )
        }
    }

    /**
     * Išvalo teksto lauką.
     */
    fun clearInput() {
        _uiState.update { current ->
            current.copy(
                inputText = "",
                isInputError = false,
                currentMetrics = null
            )
        }
    }

    /**
     * Išvalo skaičiavimų istoriją.
     */
    fun clearHistory() {
        _uiState.update { it.copy(history = emptyList()) }
    }

    /**
     * Atstato paspaustą istorijos įrašą į įvesties lauką.
     */
    fun selectHistoryItem(item: HistoryItem) {
        _uiState.update { current ->
            current.copy(
                inputText = item.text,
                currentMetrics = item.metrics,
                isInputError = false
            )
        }
    }

    /**
     * Perjungia kalbą tarp lietuvių ir anglų (6 reikalavimas).
     */
    fun toggleLanguage() {
        _uiState.update { current ->
            val nextLang = if (current.selectedLanguage == "lt") "en" else "lt"
            current.copy(selectedLanguage = nextLang)
        }
    }
}
