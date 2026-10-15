package com.example.textmetrics.core

/**
 * Teikia paruoštus testinius sakinius (2 reikalavimas).
 */
class SampleSentencesProvider {

    private val samples = listOf(
        "Labas rytas",
        "Rugsėjo 1 prasidėjo karas. Liūdna",
        "Vilnius yra Lietuvos sostinė nuo 1323 metų. Mieste gyvena apie 600000 gyventojų.",
        "Krepšinio rungtynės baigėsi rezultatu 89 prieš 75! Tai buvo nuostabi pergalė.",
        "Šiandien nusipirkau 2 knygas po 14.99 eurų ir 3 sąsiuvinius. Iš viso sumokėjau 35 eurus.",
        "Greita ruda lapė peršoko per 9 miegančius šunis... Ar tai tiesa?!",
        "1, 2, 3, 4, 5 – skaičiuojame nuo vieno iki penkių.",
        "Kava kainuoja 2.50€, arbata – $1.80, o nuolaida siekia net 20%!",
        "Programavimas Kotlin kalba yra malonus ir efektyvus."
    )

    private var currentIndex = 0

    /**
     * Grąžina sekantį testinį sakinį paeiliui.
     */
    fun getNextSample(): String {
        val sample = samples[currentIndex]
        currentIndex = (currentIndex + 1) % samples.size
        return sample
    }

    /**
     * Grąžina visų testinių sakinių sąrašą.
     */
    fun getAllSamples(): List<String> = samples
}
