package com.example.textmetrics.core

/**
 * Teksto metrikų duomenų modelis.
 *
 * @property sentences Sakinių skaičius.
 * @property words Žodžių skaičius (skaičiai neįskaičiuojami į žodžius).
 * @property characters Rašybos ženklų skaičius (visi simboliai, įskaitant tarpus).
 * @property numbers Skaičių skaičius (atskiri skaitiniai tokenai).
 */
data class TextMetrics(
    val sentences: Int,
    val words: Int,
    val characters: Int,
    val numbers: Int
) {
    /**
     * Suformatuoja metrikas užduotyje nurodytu formatu: "S, Ž, Ženklai, Sk"
     * Pvz.: "1, 2, 11, 0"
     */
    fun toFormattedString(): String = "$sentences, $words, $characters, $numbers"

    /**
     * Pilnas istorijos įrašo formatas: „Tekstas“ => S, Ž, Ženklai, Sk
     */
    fun toHistoryEntry(text: String): String = "„$text“ => ${toFormattedString()}"
}
