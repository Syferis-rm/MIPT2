package com.example.textmetrics.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.textmetrics.R
import com.example.textmetrics.core.TextMetrics
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextMetricsScreen(
    viewModel: TextMetricsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val emptyErrorMessage = stringResource(R.string.error_empty_input)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = stringResource(R.string.app_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Kalbos perjungiklis (6 reikalavimas - SSOT per ViewModel)
                    FilterChip(
                        selected = true,
                        onClick = { viewModel.toggleLanguage() },
                        label = {
                            Text(
                                text = if (uiState.selectedLanguage == "lt") "LT" else "EN",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = stringResource(R.string.language_switcher),
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // 1 & 4 Reikalavimas: Teksto įvesties laukas su validacija
            OutlinedTextField(
                value = uiState.inputText,
                onValueChange = { viewModel.onInputTextChanged(it) },
                label = { Text(stringResource(R.string.input_label)) },
                placeholder = { Text(stringResource(R.string.input_hint)) },
                isError = uiState.isInputError,
                supportingText = {
                    if (uiState.isInputError) {
                        Text(
                            text = emptyErrorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            text = "${stringResource(R.string.metric_characters)}: ${uiState.inputText.length}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                trailingIcon = {
                    if (uiState.inputText.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearInput() }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.clear_button)
                            )
                        }
                    }
                },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Veiksmų mygtukai
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Skaičiuoti mygtukas
                Button(
                    onClick = {
                        val success = viewModel.calculateMetrics()
                        if (!success) {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = emptyErrorMessage,
                                    withDismissAction = true
                                )
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.calculate_button),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // 2 Reikalavimas: Sugeneruoti pavyzdį mygtukas
                OutlinedButton(
                    onClick = { viewModel.generateSample() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.generate_sample_button),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1 Reikalavimas: Einamųjų metrikų atvaizdavimas (kortelė)
            uiState.currentMetrics?.let { metrics ->
                CurrentMetricsCard(metrics = metrics)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 3 Reikalavimas: Atliktų skaičiavimų istorija (runtime)
            HistorySection(
                history = uiState.history,
                onClearHistory = { viewModel.clearHistory() },
                onItemClick = { viewModel.selectHistoryItem(it) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 1 Reikalavimo kortelė su 4 metrikų blokais.
 */
@Composable
fun CurrentMetricsCard(metrics: TextMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = stringResource(R.string.metrics_title),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    title = stringResource(R.string.metric_sentences),
                    value = metrics.sentences.toString(),
                    modifier = Modifier.weight(1f)
                )
                MetricItem(
                    title = stringResource(R.string.metric_words),
                    value = metrics.words.toString(),
                    modifier = Modifier.weight(1f)
                )
                MetricItem(
                    title = stringResource(R.string.metric_characters),
                    value = metrics.characters.toString(),
                    modifier = Modifier.weight(1f)
                )
                MetricItem(
                    title = stringResource(R.string.metric_numbers),
                    value = metrics.numbers.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MetricItem(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 3 Reikalavimas: Vykdymo laiko (runtime) istorijos sąrašas.
 * Atvaizduoja tiksliai reikalaujamą formatą:
 * „Labas rytas“ => 1, 2, 11, 0
 * „Rugsėjo 1 prasidėjo karas. Liūdna“ => 2, 4, 33, 1
 */
@Composable
fun HistorySection(
    history: List<HistoryItem>,
    onClearHistory: () -> Unit,
    onItemClick: (HistoryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.history_title),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            if (history.isNotEmpty()) {
                IconButton(onClick = onClearHistory) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.clear_history_button),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.history_format_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )

        Spacer(modifier = Modifier.height(6.dp))

        if (history.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.history_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(item) },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = item.displayFormat,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
