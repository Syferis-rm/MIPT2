# 📊 Teksto Metrikų Skaičiuoklė (Text Metrics)

> Android programėlė, analizuojanti vartotojo įvestą tekstą ir skaičiuojanti sakinių, žodžių, rašybos ženklų bei skaičių kiekius. Sukurta naudojant **Kotlin**, **Jetpack Compose** ir **Material Design 3**.

---

## ✨ Funkcionalumas

- 🔢 **Teksto analizė** — suskaičiuoja sakinius, žodžius, rašybos ženklus ir skaičius
- 📝 **Testinių sakinių generavimas** — iš anksto paruošti lietuviški pavyzdiniai sakiniai su skaičiais, skyryba ir specialiais atvejais
- 📜 **Skaičiavimų istorija** — vykdymo metu kaupiami visi ankstesni rezultatai formatu `„Tekstas" => S, Ž, Ženklai, Sk`
- ⚠️ **Įvesties validacija** — informatyvus pranešimas, jei bandoma analizuoti tuščią tekstą
- ♻️ **Perpanaudojamas modulis** — metrikų skaičiavimo logika atskirta į nepriklausomą Kotlin JVM biblioteką (`:metrics`)
- 🌍 **Lokalizacija** — pilnas lietuvių ir anglų kalbų palaikymas su kalbos perjungimu programėlės viduje

---

## 📋 Reikalavimų Realizacijos Matrica

| Nr. | Reikalavimas | Būsena | Realizacija |
|:---:|:---|:---:|:---|
| **1** | Skaičiuoti sakinius, žodžius, rašybos ženklus ir skaičius | ✅ | `DefaultTextMetricsCalculator` — regex pagrindu veikiantis algoritmas su lietuviškų raidžių palaikymu |
| **2** | Sugeneruoti testavimo sakinius | ✅ | `SampleSentencesProvider` — 9 pavyzdiniai sakiniai, cikliškai pateikiami mygtuku |
| **3** | Atliktų skaičiavimų istorija (runtime) | ✅ | `TextMetricsViewModel.history` — iki 100 įrašų, naujausi viršuje, spustelėjus atkuriamas tekstas |
| **4** | Tikrinti ar įvestis netuščia ir informuoti vartotoją | ✅ | Raudonas rėmelis + klaidos tekstas po lauku + `Snackbar` pranešimas |
| **5** | Metrikų skaičiavimas atskirame modulyje | ✅ | Gradle modulis `:metrics` — gryna Kotlin JVM biblioteka, be Android SDK priklausomybių |
| **6** | Lengva grafinių elementų lokalizacija | ✅ | `strings.xml` resursai (`values/` — EN, `values-lt/` — LT) + kalbos perjungiklis `LT`/`EN` |

---

## 🏗️ Architektūra

### Projekto struktūra

```
text-metrics-app/
├── 📁 app/                                  # Android Jetpack Compose modulis
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── kotlin/com/example/textmetrics/
│       │   ├── MainActivity.kt              # Pagrindinė veikla, lokalizacijos kontekstas
│       │   └── ui/
│       │       ├── TextMetricsScreen.kt      # Compose UI ekranas
│       │       ├── TextMetricsViewModel.kt   # Būsenos valdymas (MVVM)
│       │       └── theme/                    # Material 3 tema ir spalvos
│       └── res/
│           ├── values/strings.xml            # Anglų k. (numatytoji)
│           └── values-lt/strings.xml         # Lietuvių k.
│
├── 📁 metrics/                              # Nepriklausomas skaičiavimo modulis
│   ├── build.gradle.kts
│   └── src/
│       ├── main/kotlin/com/example/textmetrics/core/
│       │   ├── TextMetrics.kt               # Duomenų modelis (data class)
│       │   ├── TextMetricsCalculator.kt     # Sąsaja + realizacija
│       │   └── SampleSentencesProvider.kt   # Testinių sakinių tiekėjas
│       └── test/kotlin/com/example/textmetrics/core/
│           └── TextMetricsCalculatorTest.kt # JUnit 5 vienetų testai
│
├── 📁 gradle/
│   ├── libs.versions.toml                   # Centralizuotas priklausomybių katalogas
│   └── wrapper/
├── build.gradle.kts                         # Šakninis Gradle failas
├── settings.gradle.kts                      # Modulių registracija
└── README.md                                # Šis failas
```

### Architektūros diagrama

```
┌─────────────────────────────────────────────────────────┐
│                     :app modulis                        │
│                                                         │
│  ┌──────────────┐    ┌────────────────────────────────┐ │
│  │ MainActivity │───▶│    TextMetricsViewModel        │ │
│  │  (Compose)   │    │  ┌──────────┐  ┌───────────┐  │ │
│  └──────────────┘    │  │ UiState  │  │ History[] │  │ │
│         │            │  └──────────┘  └───────────┘  │ │
│         ▼            └────────────┬───────────────────┘ │
│  ┌──────────────┐                 │                     │
│  │  Screen.kt   │                 │ naudoja              │
│  │  (Compose UI)│                 ▼                     │
│  └──────────────┘  ┌──────────────────────────────────┐ │
│                    │         :metrics modulis          │ │
│                    │                                    │ │
│                    │  TextMetricsCalculator (interface) │ │
│                    │         ▲                          │ │
│                    │         │ implements               │ │
│                    │  DefaultTextMetricsCalculator      │ │
│                    │         │                          │ │
│                    │         ▼                          │ │
│                    │    TextMetrics (data class)        │ │
│                    │  SampleSentencesProvider           │ │
│                    └──────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

---

## 📖 API Dokumentacija

### `TextMetrics` — duomenų modelis

```kotlin
data class TextMetrics(
    val sentences: Int,     // Sakinių skaičius
    val words: Int,         // Žodžių skaičius (skaičiai neįskaičiuojami)
    val characters: Int,    // Rašybos ženklų skaičius (visi simboliai, įskaitant tarpus)
    val numbers: Int        // Skaičių skaičius (atskiri skaitiniai tokenai)
)
```

| Metodas | Aprašymas | Pavyzdys |
|:---|:---|:---|
| `toFormattedString()` | Grąžina metrikas formatu `S, Ž, Ženklai, Sk` | `"1, 2, 11, 0"` |
| `toHistoryEntry(text)` | Grąžina pilną istorijos formatą | `"„Labas rytas" => 1, 2, 11, 0"` |

### `TextMetricsCalculator` — sąsaja

```kotlin
interface TextMetricsCalculator {
    fun calculate(text: String): TextMetrics
}
```

### `DefaultTextMetricsCalculator` — realizacija

Standartinė realizacija su šiomis taisyklėmis:

| Metrika | Taisyklė |
|:---|:---|
| **Rašybos ženklai** | Visi simboliai, įskaitant tarpus (`text.length`) |
| **Skaičiai** | Atskiri skaitiniai žetonai: `1`, `2026`, `3.14`, `-5` |
| **Žodžiai** | Žetonai, turintys bent vieną raidę (skaičiai **neįskaičiuojami**) |
| **Sakiniai** | Skiriami ženklais `.` `!` `?` `;` arba nauja eilute. Taškas skaičiaus viduje (pvz. `3.14`) **nėra** sakinio pabaiga |

#### Specialūs atvejai

- **Valiutos ir procentai** — `2.50€`, `$1.80`, `20%` teisingai atpažįstami kaip skaičiai
- **Minkšti eilučių lūžiai** — po kablelio ar prieš mažąją raidę eilutės lūžis nelaikomas sakinio pabaiga
- **Lietuviškos raidės** — `ą`, `č`, `ę`, `ė`, `į`, `š`, `ų`, `ū`, `ž` pilnai palaikomos

---

## 🔢 Skaičiavimo Pavyzdžiai

| Įvestis | Sakiniai | Žodžiai | Ženklai | Skaičiai | Rezultatas |
|:---|:---:|:---:|:---:|:---:|:---|
| `Labas rytas` | 1 | 2 | 11 | 0 | `1, 2, 11, 0` |
| `Rugsėjo 1 prasidėjo karas. Liūdna` | 2 | 4 | 33 | 1 | `2, 4, 33, 1` |
| `10 20 30 40 50` | 1 | 0 | 14 | 5 | `1, 0, 14, 5` |
| `Kava kainuoja 2.50€, arbata – $1.80, o nuolaida siekia net 20%!` | 1 | 7 | 62 | 3 | `1, 7, 62, 3` |

---

## 🚀 Diegimas ir Paleidimas

### Reikalavimai

| Įrankis | Minimali versija |
|:---|:---|
| **Android Studio** | Ladybug arba naujesnė |
| **JDK** | 21 |
| **Android SDK** | API 37 (compileSdk), min API 24 |
| **Kotlin** | 2.2.10 |
| **Gradle** | per gradle-wrapper (automatiškai) |

### 1. Atidarymas Android Studio

```
1. Paleiskite Android Studio
2. File → Open... → pasirinkite projekto katalogą
3. Palaukite Gradle sinchronizacijos
4. Pasirinkite emuliatorių arba prijungtą įrenginį
5. Spauskite Run ▶ (Shift + F10)
```

### 2. Kompiliavimas komandinėje eilutėje

```powershell
# Sukompiliuoti debug APK
.\gradlew.bat assembleDebug

# Rezultatas:
# app/build/outputs/apk/debug/app-debug.apk
```

### 3. Vienetų testų paleidimas

```powershell
# Paleisti :metrics modulio JUnit 5 testus
.\gradlew.bat :metrics:test

# Testų ataskaita (HTML):
# metrics/build/reports/tests/test/index.html
```

---

## 🧪 Testavimas

### Vienetų testai (`TextMetricsCalculatorTest`)

Modulyje `:metrics` yra **10 testų**, padengiančių:

| Testas | Ką tikrina |
|:---|:---|
| `test example 1 — Labas rytas` | Bazinis vieno sakinio atvejis |
| `test example 2 — Rugsėjo 1 prasidėjo karas. Liūdna` | Du sakiniai su skaičiumi |
| `test empty and blank inputs` | Tuščias ir tik tarpų tekstas |
| `test decimal numbers and negative numbers` | `12.99`, `-5` — trupmeniniai ir neigiami skaičiai |
| `test multiple sentences with different punctuation` | `!`, `?`, `;`, `...` — skirtingi skyrikliai |
| `test lithuanian diacritic letters` | Ą, Č, Ę, Ė, Į, Š, Ų, Ū, Ž — lietuviškos raidės |
| `test text with only numbers` | Tekstas tik iš skaičių (0 žodžių) |
| `test numbers with currencies and percentages` | `2.50€`, `$1.80`, `20%` |
| `test multiline text with soft line breaks` | Minkšti eilučių lūžiai po kablelio |
| `test multiline text with separate lines` | Nepriklausomos eilutės = atskiri sakiniai |

### Testų paleidimas

```powershell
.\gradlew.bat :metrics:test
```

Visi testai turi praeiti sėkmingai ✅

---

## 🌍 Lokalizacija

Programėlė palaiko **dvi kalbas** su galimybe perjungti vykdymo metu:

| Kalba | Resursų failas | Perjungimo kodas |
|:---|:---|:---|
| 🇬🇧 Anglų (numatytoji) | `app/src/main/res/values/strings.xml` | `"en"` |
| 🇱🇹 Lietuvių | `app/src/main/res/values-lt/strings.xml` | `"lt"` |

### Kalbos perjungimas

Programėlės viršutinėje juostoje yra **`LT`/`EN` mygtukas**. Paspaudus:
1. `TextMetricsViewModel.toggleLanguage()` pakeičia kalbos kodą
2. `MainActivity` sukuria naują lokalizuotą kontekstą
3. Visi `stringResource(...)` tekstai automatiškai atsinaujina

> **Pastaba:** Kalba keičiama per `ViewModel` (`Single Source of Truth`), todėl pasirinkimas išlieka ir pasukus ekraną.

### Naujos kalbos pridėjimas

1. Sukurkite katalogą `app/src/main/res/values-XX/` (kur `XX` — kalbos kodas)
2. Nukopijuokite `strings.xml` ir išverskite visus `<string>` elementus
3. Pridėkite kalbos kodą į `TextMetricsViewModel.toggleLanguage()` logiką

---

## 🔧 Technologijų Kaminas

| Technologija | Paskirtis |
|:---|:---|
| **Kotlin** 2.2.10 | Programavimo kalba |
| **Jetpack Compose** + Material 3 | Deklaratyvi vartotojo sąsaja |
| **ViewModel** + `StateFlow` | Reaktyvus būsenos valdymas (MVVM) |
| **Gradle Version Catalog** (`libs.versions.toml`) | Centralizuotas priklausomybių valdymas |
| **JUnit Jupiter** 5.10.2 | Vienetų testavimo karkasas |
| **Android Gradle Plugin** 9.4.1 | Android kompiliavimas |

---

## 🔍 Kodo Kokybės Patobulinimai

Atlikus išsamų kodo auditą, buvo realizuoti šie esminiai patobulinimai:

1. **Lokalizacijos SSOT (Single Source of Truth)** — pašalinta būsenos desinchronizacija tarp `MainActivity` ir `TextMetricsViewModel`. Pasukus ekraną kalbos pasirinkimas išlieka, o sąsaja atnaujinama sinchroniškai.

2. **Tikslus sakinių ir skaičių atpažinimas:**
   - `SOFT_LINE_BREAK_REGEX` — kelių eilučių sakinys (pvz. su kableliais ar mažosiomis raidėmis) nebeskaidomas klaidingai
   - Valiutų ir procentų (`€`, `$`, `£`, `%`) palaikymas — žetonai kaip `50€` ar `20%` teisingai skaičiuojami
   - Reguliariosios išraiškos iškeltos į `companion object` — vengiant pakartotinio kompiliavimo

3. **UI/UX patobulinimai:**
   - `imePadding()` — sklandus veikimas iššokus virtualiai klaviatūrai
   - `weight(1f)` istorijos sąrašui — dinamiškas erdvės paskirstymas
   - Istorijos limitas — iki 100 įrašų, seniausieji automatiškai pašalinami

---

## 📄 Licencija

Šis projektas sukurtas mokymo tikslais.

---

## 👤 Autorius

Sukurta kaip Android programavimo užduoties realizacija.
