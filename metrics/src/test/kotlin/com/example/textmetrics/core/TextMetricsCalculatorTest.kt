package com.example.textmetrics.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TextMetricsCalculatorTest {

    private lateinit var calculator: TextMetricsCalculator

    @BeforeEach
    fun setUp() {
        calculator = DefaultTextMetricsCalculator()
    }

    @Test
    fun `test example 1 from specification - Labas rytas`() {
        val input = "Labas rytas"
        val metrics = calculator.calculate(input)

        // „Labas rytas“ => 1, 2, 11, 0
        assertEquals(1, metrics.sentences, "Sakiniai")
        assertEquals(2, metrics.words, "Žodžiai")
        assertEquals(11, metrics.characters, "Rašybos ženklai")
        assertEquals(0, metrics.numbers, "Skaičiai")
        assertEquals("1, 2, 11, 0", metrics.toFormattedString())
        assertEquals("„Labas rytas“ => 1, 2, 11, 0", metrics.toHistoryEntry(input))
    }

    @Test
    fun `test example 2 from specification - Rugsejo 1 prasidejo karas Liudna`() {
        val input = "Rugsėjo 1 prasidėjo karas. Liūdna"
        val metrics = calculator.calculate(input)

        // „Rugsėjo 1 prasidėjo karas. Liūdna“ => 2, 4, 33, 1
        assertEquals(2, metrics.sentences, "Sakiniai")
        assertEquals(4, metrics.words, "Žodžiai")
        assertEquals(33, metrics.characters, "Rašybos ženklai")
        assertEquals(1, metrics.numbers, "Skaičiai")
        assertEquals("2, 4, 33, 1", metrics.toFormattedString())
        assertEquals("„Rugsėjo 1 prasidėjo karas. Liūdna“ => 2, 4, 33, 1", metrics.toHistoryEntry(input))
    }

    @Test
    fun `test empty and blank inputs`() {
        val emptyMetrics = calculator.calculate("")
        assertEquals(0, emptyMetrics.sentences)
        assertEquals(0, emptyMetrics.words)
        assertEquals(0, emptyMetrics.characters)
        assertEquals(0, emptyMetrics.numbers)

        val blankMetrics = calculator.calculate("    ")
        assertEquals(0, blankMetrics.sentences)
        assertEquals(0, blankMetrics.words)
        assertEquals(4, blankMetrics.characters)
        assertEquals(0, blankMetrics.numbers)
    }

    @Test
    fun `test decimal numbers and negative numbers`() {
        val input = "Prekė kainuoja 12.99 arba -5 eurų nuolaida."
        val metrics = calculator.calculate(input)

        assertEquals(1, metrics.sentences)
        assertEquals(5, metrics.words) // Prekė, kainuoja, arba, eurų, nuolaida
        assertEquals(2, metrics.numbers) // 12.99, -5
    }

    @Test
    fun `test multiple sentences with different punctuation`() {
        val input = "Labas! Kaip sekasi? Viskas gerai; tikrai taip... Puiku."
        val metrics = calculator.calculate(input)

        assertEquals(5, metrics.sentences)
    }

    @Test
    fun `test lithuanian diacritic letters`() {
        val input = "Ąžuolas, čiobrelis, eglė, įvairumas, šuo, ūkas, žąsis."
        val metrics = calculator.calculate(input)

        assertEquals(1, metrics.sentences)
        assertEquals(7, metrics.words)
        assertEquals(0, metrics.numbers)
    }

    @Test
    fun `test text with only numbers`() {
        val input = "10 20 30 40 50"
        val metrics = calculator.calculate(input)

        assertEquals(1, metrics.sentences)
        assertEquals(0, metrics.words)
        assertEquals(5, metrics.numbers)
    }

    @Test
    fun `test numbers with currencies and percentages`() {
        val input = "Kava kainuoja 2.50€, arbata – $1.80, o nuolaida siekia net 20%!"
        val metrics = calculator.calculate(input)

        // Sakiniai: 1
        // Žodžiai: Kava, kainuoja, arbata, o, nuolaida, siekia, net (7)
        // Skaičiai: 2.50, 1.80, 20 (3)
        assertEquals(1, metrics.sentences, "Sakiniai")
        assertEquals(7, metrics.words, "Žodžiai")
        assertEquals(3, metrics.numbers, "Skaičiai")
    }

    @Test
    fun `test multiline text with soft line breaks within sentence`() {
        val input = "Labas rytas,\nmielas bičiuli!\nKaip sekasi?"
        val metrics = calculator.calculate(input)

        // Pirmas sakinys tęsiasi per dvi eilutes po kablelio ir mažosios raidės
        // Antras sakinys: Kaip sekasi?
        assertEquals(2, metrics.sentences, "Sakiniai")
        assertEquals(6, metrics.words, "Žodžiai") // Labas, rytas, mielas, bičiuli, Kaip, sekasi (6 žodžiai)
    }

    @Test
    fun `test multiline text with separate independent lines without period`() {
        val input = "Pirmas sakinys\nAntras sakinys\nTrečias sakinys"
        val metrics = calculator.calculate(input)

        assertEquals(3, metrics.sentences, "Sakiniai")
        assertEquals(6, metrics.words, "Žodžiai")
        assertEquals(0, metrics.numbers, "Skaičiai")
    }
}
