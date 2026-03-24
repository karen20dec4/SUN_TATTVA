# Calculul Nakshatrelor - Documentație Tehnică

## Versiune: 2.20 (versionCode 14)

---

## Ce sunt Nakshatrele?

Nakshatrele sunt cele **27 de constelații lunare** din astrologia vedică. Luna traversează fiecare Nakshatra în circa 1 zi (variabil), completând un ciclu complet în ~27.3 zile.

- Fiecare Nakshatra = **13°20' = 13.333°** (360° / 27)
- Zodiacul sideral începe de la **0° Berbec (Mesha)**
- Ordinea celor 27: Ashwini → Bharani → Krittika → ... → Revati

---

## Arhitectura Calculului

### Fișiere Implicate

| Fișier | Rol |
|--------|-----|
| `NakshatraCalculator.kt` | Algoritm de calcul: `calculateNakshatra()` + `calculateFutureNakshatras()` |
| `AstroRepository.kt` | Orchestrare: interoghează Swiss Ephemeris, apelează calculatorul |
| `NakshatraCard.kt` | UI: cardul expandabil cu toate cele 27 Nakshatre |
| `NakshatraDetailScreen.kt` | UI: ecranul detaliat al unei Nakshatre individuale |
| `LocalizationHelpers.kt` | Localizare: traduceri pentru proprietățile Nakshatrelor |

### Fluxul de Date

```
Swiss Ephemeris (swisseph.jar)
    ↓
AstroRepository.calculateAstroData()
    ↓ (apelează swe_calc_ut() pentru longitudine + viteză Lună)
    ↓
NakshatraCalculator.calculateNakshatra()
    → NakshatraResult (nakshatra curentă, start/end times, countdown)
    ↓
NakshatraCalculator.calculateFutureNakshatras()
    → List<NakshatraTimeSlot> (27 intervale calculate cu efemeridă)
    ↓
NakshatraCard (UI) / NakshatraDetailScreen (UI)
```

---

## Calculul Nakshatrei Curente

### `calculateNakshatra()`

**Input:**
- `moonLongitude` — longitudinea siderală curentă a Lunii (0°–360°)
- `currentTime` — momentul curent (Calendar)
- `referenceMoonLongitude` — longitudinea Lunii la un moment de referință (ex: sunrise)
- `referenceTime` — momentul de referință (ex: sunrise time)
- `moonSpeedDegreesPerDay` — viteza reală a Lunii din Swiss Ephemeris

**Algoritm:**
1. Normalizează longitudinea la 0–360°
2. Calculează indexul Nakshatra: `index = floor(longitude / 13.333)`
3. Calculează `zeroReferenceTime` (momentul când Luna a trecut limita de 0° a Nakshatrei curente)
   - Folosește `referenceMoonLongitude` la `referenceTime` (nu poziția curentă)
   - Aceasta previne drift-ul la recalculări succesive
4. Calculează `startTime` și `endTime` din `zeroReferenceTime`
5. Calculează countdown-ul (timp rămas)

**Output:** `NakshatraResult` cu:
- Nakshatra curentă (tip, număr, nume)
- Start/End times + countdown
- Zero Reference Time (moment ancoră)
- Poziția Lunii în zodiac (grade/minute/secunde + semn zodiacal)
- Viteza Lunii (°/zi)
- `futureNakshatras: List<NakshatraTimeSlot>` — intervalele viitoare

### Prevenirea Drift-ului

**Problema:** Dacă se folosește poziția curentă a Lunii la fiecare recalculare, erorile mici de timing se acumulează.

**Soluția:** Se folosește poziția Lunii la **sunrise** (un moment fix pe zi) ca referință:
```kotlin
// AstroRepository:
val (referenceMoonLon, _) = swissEphWrapper.calculateMoonLongitudeWithSpeed(
    sunriseJD  // Julian Day la momentul sunrise
)

// NakshatraCalculator:
calculateNakshatra(
    moonLongitude = currentMoonLon,        // Poziția curentă (pt. determinare Nakshatra)
    referenceMoonLongitude = sunriseMoonLon, // Referință stabilă (pt. timing)
    referenceTime = sunriseTime              // Momentul referinței
)
```

---

## Calculul Nakshatrelor Viitoare (v2.18+)

### Problema Originală (v2.17 și anterior)

Codul vechi folosea **o singură viteză constantă** pentru a extrapola toate 27 Nakshatrele:

```kotlin
// ❌ GREȘIT - viteză constantă
val avgDegreesPerHour = moonSpeed / 24.0
for (i in 0 until 27) {
    hoursToStart = i * 13.333 / avgDegreesPerHour
    hoursToEnd = (i + 1) * 13.333 / avgDegreesPerHour
}
```

**De ce nu funcționa:** Viteza Lunii variază semnificativ:
- **Minim:** ~11.8°/zi (la apogeu)
- **Maxim:** ~15.2°/zi (la perigeu)
- **Medie:** ~13.2°/zi

Eroarea cumulată pe 27 de zile ajungea la **~19 ore!**

### Soluția: Forward-Stepping cu Newton-Raphson (v2.18+)

#### `calculateFutureNakshatras()`

**Input:**
- `currentMoonLongitude` — longitudinea curentă
- `currentMoonSpeedDegreesPerDay` — viteza curentă (estimare inițială)
- `currentTime` — momentul curent
- `getMoonPositionAndSpeed: (Calendar) -> Pair<Double, Double>` — funcție Lambda ce interogă Swiss Ephemeris

**Algoritm detaliat:**

```
1. INIȚIALIZARE:
   - Determină Nakshatra curentă (index = floor(lon / 13.333))
   - Calculează gradul parcurs în Nakshatra curentă
   - Calculează startTime al Nakshatrei curente (înapoi în timp)

2. PENTRU FIECARE DIN CELE 27 NAKSHATRE (i = 0..26):
   
   a. Determină limita de sfârșit (endBoundaryDeg):
      endBoundaryDeg = ((nakshatraIdx + 1) * 13.333) % 360
   
   b. Estimează timpul de traversare:
      degreesToTraverse = 13.333° (sau restul pentru Nakshatra curentă)
      hoursToTraverse = degreesToTraverse / (moonSpeed / 24.0)
   
   c. RAFINARE CU EFEMERIDĂ (max 3 iterații):
      - Interogă Swiss Ephemeris la momentul estimat
      - Obține (actualLon, actualSpeed)
      - Calculează eroarea: error = actualLon - endBoundaryDeg
      - Gestionează wrap-around 360°/0°
      - Dacă |error| < 0.01° → STOP (precizie ~1 minut)
      - Ajustează: timeAdjust = -error / speedPerSecond (Newton's method)
      - Actualizează viteza pentru estimarea următoare
   
   d. Salvează NakshatraTimeSlot(nakshatra, startTime, refinedEndTime)
   
   e. Sfârșitul acestei Nakshatre = Începutul următoarei
```

#### Gestionarea Wrap-Around 360°/0°

Când Luna trece de la 359° la 1°, diferența simplă ar fi 358°. Corecția:

```kotlin
var error = normalizedActualLon - endBoundaryDeg
if (error > 180.0) error -= 360.0    // 359° - 1° = 358° → -2°
if (error < -180.0) error += 360.0   // 1° - 359° = -358° → 2°
```

#### Siguranță și Backward Compatibility

- Viteza este clampată: `speed.coerceIn(10.0, 16.0)` — previne valori aberante
- Ajustarea timpului este clampată: `coerceIn(-86400, 86400)` — maxim ±1 zi per iterație
- Dacă interogarea efemeridei eșuează, se revine la estimarea cu ultima viteză cunoscută
- Dacă `futureNakshatras` este goală, `NakshatraCard` folosește metoda veche (backward compat)

---

## Precizie și Performanță

### Precizie
- **Criteriu de convergență:** |error| < 0.01° (≈ 1 minut de timp)
- **Iterații necesare:** De obicei 1–2 per limită (maxim 3)
- **Consistență:** Recalcularea a doua zi produce aceleași intervale (±1 minut)

### Performanță
- **Apeluri Swiss Ephemeris per calcul:** ~27–81 (1–3 per Nakshatra)
- **Timp de calcul:** < 100ms pe un dispozitiv modern
- **Cache:** Rezultatele sunt stocate în `NakshatraResult.futureNakshatras`

---

## Structuri de Date

### NakshatraType (Enum, 27 valori)

```kotlin
enum class NakshatraType(
    val displayName: String,      // "Ashwini"
    val number: Int,              // 1–27
    val deity: String,            // "Ashwini Kumara"
    val symbol: String,           // "🐎 cap de cal"
    val animal: String,           // "cal"
    val planet: String,           // "Ketu"
    val nature: String,           // "ușoară / rapidă"
    val degreeRange: String       // "0°–13°20′ Berbec"
)
```

### NakshatraTimeSlot

```kotlin
data class NakshatraTimeSlot(
    val nakshatra: NakshatraType,
    val startTime: Calendar,
    val endTime: Calendar
)
```

### NakshatraResult

```kotlin
data class NakshatraResult(
    val nakshatra: NakshatraType,   // Nakshatra curentă
    val startTime: Calendar,        // Start interval curent
    val endTime: Calendar,          // End interval curent
    val remainingTime: Long,        // Secunde rămase
    val number: Int,                // 1–27
    val name: String,               // displayName
    val code: String,               // "NK1"..."NK27"
    val zeroReferenceTime: Calendar, // Moment ancoră
    val moonZodiacPosition: String, // "13°30'00\""
    val moonZodiacSignIndex: Int,   // 0=Berbec..11=Pești
    val moonSpeedDegreesPerDay: Double,
    val futureNakshatras: List<NakshatraTimeSlot> // Pre-calculat
)
```

---

## Afișarea în UI

### NakshatraCard (Cardul Expandabil)

- **Compact:** Nakshatra curentă + countdown (actualizat la fiecare secundă)
- **Expandat:** Toate cele 27 Nakshatre cu intervale (start → end)
- **Sursă de date:** Folosește `NakshatraResult.futureNakshatras` (pre-calculat cu efemeridă)
- **Fallback:** Dacă lista este goală, calculează cu viteză constantă (backward compat)

### NakshatraDetailScreen

- **Header:** Numele + degree range cu gradient colorat pe Tattva
- **Info Cards:** Deity, Symbol, Animal, Planet, Nature
- **Description:** Text extins cu **Ce se face:** / **Ce nu se face:** (bold)
- **Formatare:** `formatNakshatraDescription()` detectează markerii și aplică `SpanStyle(fontWeight = Bold)`

---

## Debug și Testare

### Debug Date Override (v2.19+)

Setările aplicației conțin o secțiune Debug care permite setarea manuală a datei:

1. **Settings → Debug → Alege dată** — selectează o dată
2. Aplicația recalculează tot cu data selectată
3. Banner portocaliu pe MainScreen: `🐛 DEBUG: 24 Mar 2026, 12:00`
4. **Dezactivează** pentru a reveni la modul real-time

**Implementare:**
- `SettingsPreferences`: `debug_date_enabled` (Boolean), `debug_date_millis` (Long)
- `AstroRepository.calculateAstroData()`: parametru opțional `debugCalendar`
- `MainViewModel`: citește data debug din preferences

### Verificare Consistență

Pentru a verifica calculul corect:
1. Activează Debug Date → selectează o dată (ex: 24 Mar 2026)
2. Notează intervalele tuturor celor 27 Nakshatre
3. Schimbă data (ex: 25 Mar 2026)
4. Verifică: Nakshatrele viitoare au aceleași intervale (±1 minut)

---

## Istoric Versiuni Nakshatra

| Versiune | Modificare |
|----------|-----------|
| v2.10 | Implementarea inițială cu viteză medie constantă (13.2°/zi) |
| v2.16 | Adăugare viteză reală din efemeridă pentru Nakshatra curentă |
| v2.17 | Fix drift: referință la sunrise în loc de current time |
| v2.18 | **Fix major:** Forward-stepping cu Newton-Raphson pentru viitor |
| v2.19 | Debug date override pentru testare |
| v2.20 | Descrieri extinse (Ce se face / Ce nu se face) pentru toate 27 |
