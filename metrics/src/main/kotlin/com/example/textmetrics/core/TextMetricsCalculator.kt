package com.example.textmetrics.core

/**
 * Sąsaja teksto metrikų skaičiavimui.
 * Gali būti perpanaudojama bet kurioje Kotlin/Java programoje.
 */
interface TextMetricsCalculator {
    /**
     * Suskaičiuoja sakinių, žodžių, simbolių ir skaičių kiekius pateiktame tekste.
     *
     * @param text Analizuotinas tekstas.
     * @return [TextMetrics] objektas su suskaičiuotomis reikšmėmis.
     */
    fun calculate(text: String): TextMetrics
}

/**
 * Standartinė [TextMetricsCalculator] realizacija.
 *
 * Taisyklės:
 * - Rašybos ženklai: visi simboliai, įskaitant tarpus (text.length).
 * - Skaičiai: atskiri skaitiniai žetonai (pvz., "1", "2026", "3.14", "-5").
 * - Žodžiai: žetonai, turintys bent vieną raidę (skaičiai neįskaičiuojami).
 * - Sakiniai: atskirti sakinio pabaigos ženklais (. ! ? ; arba nauja eilutė).
 *   Taškas skaičiaus viduje (pvz. 3.14) nelaikomas sakinio pabaiga.
 */
class DefaultTextMetricsCalculator : TextMetricsCalculator {

    companion object {
        private val NUMBER_REGEX = Regex("^[+-]?\\d+([.,]\\d+)?$")
        private val WHITESPACE_REGEX = Regex("\\s+")

        // Sakinio skyrikliai: ! ? ; arba taškai, kurie nėra trupmeninio skaičiaus viduje,
        // bei atskiros eilutės / pastraipų lūžiai
        private val SENTENCE_DELIMITER_REGEX = Regex("(?<!\\d)\\.+|\\.+(?!\\d)|[!?;]+|\\r?\\n+")

        // Šablonas aptikti minkštą eilutės lūžį sakinio viduje:
        // jei po eilutės lūžio seka mažoji raidė arba prieš lūžį buvo kablelis ar brūkšnys
        private val SOFT_LINE_BREAK_REGEX = Regex("(?<=,|-|–|—)\\s*\\r?\\n\\s*|\\r?\\n(?=\\s*\\p{Ll})")

        // Skyrybos ir simbolių ženklai, kurie gali būti prikibę prie žodžio ar skaičiaus kraštų
        // (įskaitant valiutas €, $, £, ¥ ir procentus %, kad skaičiai su jais nebūtų prarandami)
        private val PUNCTUATION_TO_TRIM = charArrayOf(
            '.', ',', '!', '?', ';', ':', '"', '\'', '„', '“', '”', '«', '»',
            '(', ')', '[', ']', '{', '}', '—', '–', '/', '€', '$', '£', '¥', '%', '‰', '°', '№'
        )
    }

    override fun calculate(text: String): TextMetrics {
        if (text.isEmpty()) {
            return TextMetrics(sentences = 0, words = 0, characters = 0, numbers = 0)
        }

        val characters = text.length

        // Jei tekstas sudarytas tik iš tarpų, kitos metrikos yra 0
        if (text.isBlank()) {
            return TextMetrics(sentences = 0, words = 0, characters = characters, numbers = 0)
        }

        val sentences = countSentences(text)
        val (words, numbers) = countWordsAndNumbers(text)

        return TextMetrics(
            sentences = sentences,
            words = words,
            characters = characters,
            numbers = numbers
        )
    }

    private fun countSentences(text: String): Int {
        // Sujungiame minkštus eilučių lūžius sakinio viduje į tarpą, kad nebūtų klaidingai suskaidyta
        val normalizedText = text.replace(SOFT_LINE_BREAK_REGEX, " ")
        val parts = normalizedText.split(SENTENCE_DELIMITER_REGEX)
        return parts.count { part ->
            part.any { it.isLetterOrDigit() }
        }
    }

    private fun countWordsAndNumbers(text: String): Pair<Int, Int> {
        val tokens = text.trim().split(WHITESPACE_REGEX)
        var wordCount = 0
        var numberCount = 0

        for (rawToken in tokens) {
            val token = rawToken.trim { it in PUNCTUATION_TO_TRIM }
            if (token.isEmpty()) continue

            if (NUMBER_REGEX.matches(token)) {
                numberCount++
            } else if (token.any { it.isLetter() }) {
                wordCount++
            }
        }

        return Pair(wordCount, numberCount)
    }
}
