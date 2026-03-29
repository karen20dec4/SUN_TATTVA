# SUN TATTVA — Plan de Upgrade

## 🔮 Feature 4: Card cu „Recomandarea Zilei"

### Descriere
Card pe ecranul principal care oferă o recomandare zilnică bazată pe combinația curentă:
- **Tattva curentă** (Akasha, Vayu, Tejas, Apas, Prithivi)
- **Nakshatra curentă** (cele 27 de mansiuni lunare)
- **Planeta Orară** (Sun, Moon, Mercury, Venus, Mars, Jupiter, Saturn)

### Exemple de recomandări
- 🧘 „Moment excelent pentru meditație" (Akasha + Hora Jupiter)
- 🚫 „Evitați călătoriile lungi" (Vayu + Nakshatra Ashlesha + Hora Saturn)
- 💼 „Favorabil pentru negocieri de afaceri" (Prithivi + Hora Mercury)
- 🔥 „Energie bună pentru exerciții fizice" (Tejas + Hora Mars)
- 💧 „Perioadă bună pentru practici de purificare" (Apas + Hora Moon)

### Plan de Implementare

#### Pas 1: Baza de date cu recomandări
- **Fișier nou**: `app/src/main/java/com/android/sun/domain/recommendations/DailyRecommendation.kt`
  ```kotlin
  data class DailyRecommendation(
      val emoji: String,           // 🧘, 💼, 🔥, etc.
      val titleRo: String,         // Titlu în română
      val titleEn: String,         // Titlu în engleză
      val descriptionRo: String,   // Descriere detaliată RO
      val descriptionEn: String,   // Descriere detaliată EN
      val category: Category       // MEDITATION, BUSINESS, TRAVEL, HEALTH, CREATIVITY
  )
  
  enum class Category { MEDITATION, BUSINESS, TRAVEL, HEALTH, CREATIVITY, EXERCISE, STUDY, REST }
  ```

#### Pas 2: Motor de recomandări
- **Fișier nou**: `app/src/main/java/com/android/sun/domain/recommendations/RecommendationEngine.kt`
  - Matrice 5×27×7 (Tattva × Nakshatra × Planet) — simplificată prin gruparea Nakshatrelor în categorii (Dev, Manushya, Rakshasa) și a planetelor în benefice/malefice
  - Logică de selecție bazată pe:
    1. **Tattva** → domeniul principal (spiritual, mental, fizic, emoțional, material)
    2. **Natura Nakshatra** → favorabil/nefavorabil/neutru
    3. **Planeta Orară** → amplificator (benefică: Jupiter, Venus, Moon / malefică: Saturn, Mars)
  - Returnează 1–3 recomandări ordonate după relevanță

#### Pas 3: Componenta UI
- **Fișier nou**: `app/src/main/java/com/android/sun/ui/components/DailyRecommendationCard.kt`
  - Card compact pe MainScreen (sub CombinedTattvaCard)
  - Header: 🔮 „Recomandarea zilei" / „Daily Recommendation"
  - Body: emoji + titlu + descriere scurtă (1-2 rânduri)
  - Expandable: detalii complete + explicație de ce (bazat pe Tattva + Nakshatra + Planet)
  - Animație: aceeași ca la celelalte carduri (expandVertically + fadeIn cu tween)
  - Culoare accent: gradient subtil bazat pe Tattva curentă

#### Pas 4: Integrare
- Adăugare în `MainScreen.kt` între CombinedTattvaCard și PlanetaryHoursCard
- Date din `AstroData` existente (tattvaInfo, nakshatraResult, planetaryHour)
- Adăugare string resources (RO + EN) pentru toate recomandările
- Fără bază de date Room — totul calculat în memorie pe baza regulilor

#### Pas 5: Testare
- Verificare recomandări corecte pentru diverse combinații
- Test visual pe Light/Dark theme
- Test localizare RO/EN

### Estimare: 2-3 sesiuni de implementare

---

## 📅 Feature 7: Calendar Lunar Complet (Vizualizare Lunară)

### Descriere
Ecran nou cu calendar lunar bird's-eye view care arată, zi de zi:
- **Tattva la răsărit** (prima Tattva a zilei, colorată)
- **Nakshatra zilei** (mansiunea lunară dominantă)
- **Luna Plină** 🌕 / **Luna Nouă** 🌑
- **Shivaratri** 🙏
- **Tripura Sundari** ✨

### Plan de Implementare

#### Pas 1: Calculator pentru date lunare
- **Fișier nou**: `app/src/main/java/com/android/sun/domain/calculator/MonthlyCalendarCalculator.kt`
  ```kotlin
  data class CalendarDayData(
      val date: Calendar,
      val sunriseTattva: TattvaType,      // Prima Tattva la răsărit
      val dominantNakshatra: String,       // Nakshatra la ora 12:00
      val nakshatraNumber: Int,            // Număr 1-27
      val isFullMoon: Boolean,
      val isNewMoon: Boolean,
      val isShivaratri: Boolean,
      val isTripuraSundari: Boolean,
      val moonIllumination: Double,        // 0.0 - 1.0
      val polarityAtSunrise: Int           // +1 Pingala / -1 Ida
  )
  
  fun calculateMonth(
      year: Int, month: Int,
      latitude: Double, longitude: Double, timeZone: Double
  ): List<CalendarDayData>
  ```
  - Refolosește calculatoarele existente (AstroCalculator, TattvaCalculator, NakshatraCalculator, MoonPhaseCalculator)
  - Calculează toate datele pentru 28-31 zile ale lunii selectate
  - Cache rezultate per lună (recalculare doar la schimbare lună/locație)

#### Pas 2: ViewModel dedicat
- **Fișier nou**: `app/src/main/java/com/android/sun/viewmodel/CalendarViewModel.kt`
  - State: `selectedMonth: YearMonth`, `calendarData: List<CalendarDayData>`, `isLoading: Boolean`
  - Calcul pe `Dispatchers.Default` (CPU-intensive, ~1-2s per lună)
  - Navigare lună precedentă/următoare
  - Refolosește locația din `LocationPreferences`

#### Pas 3: Ecran Calendar
- **Fișier nou**: `app/src/main/java/com/android/sun/ui/screens/LunarCalendarScreen.kt`

  **Layout:**
  ```
  ┌──────────────────────────────────┐
  │  ◀  Martie 2026  ▶              │  ← Header cu navigare luni
  ├──────────────────────────────────┤
  │  Lu  Ma  Mi  Jo  Vi  Sâ  Du    │  ← Zilele săptămânii
  ├──────────────────────────────────┤
  │  ┌──┐ ┌──┐ ┌──┐ ┌──┐ ┌──┐     │
  │  │1 │ │2 │ │3 │ │4 │ │5 │ ... │  ← Celule zi
  │  │🔵│ │🟡│ │🔵│ │🔴│ │🟣│     │  ← Tattva la răsărit (culoare)
  │  │As│ │Bh│ │Kr│ │Ro│ │Mr│     │  ← Nakshatra (abreviere 2 litere)
  │  │  │ │  │ │🌕│ │  │ │  │     │  ← Iconiță eveniment
  │  └──┘ └──┘ └──┘ └──┘ └──┘     │
  │  ...                            │
  ├──────────────────────────────────┤
  │  📋 Legenda:                    │  ← Legendă expandabilă
  │  🟣 Akasha  🔵 Vayu  🔴 Tejas  │
  │  ⚫ Apas    🟤 Prithivi         │
  │  🌕 Luna Plină  🌑 Luna Nouă   │
  │  🙏 Shivaratri  ✨ Tripura     │
  └──────────────────────────────────┘
  ```

  **Componente:**
  - `CalendarHeader` — Luna curentă + săgeți navigare
  - `WeekDayLabels` — Lu, Ma, Mi, Jo, Vi, Sâ, Du (localizat RO/EN)
  - `CalendarDayCell` — Celulă individuală cu:
    - Număr zi
    - Indicator Tattva (cerc colorat mic sau background)
    - Abreviere Nakshatra (2-3 litere)
    - Iconiță eveniment (🌕, 🌑, 🙏, ✨)
    - Highlight pe ziua curentă
  - `CalendarLegend` — Legendă expandabilă cu explicații culori/icoane
  - Click pe zi → navigare la AllDayScreen cu data respectivă

#### Pas 4: Navigare
- Adăugare rută `"lunar_calendar"` în `AppNavigation`
- Buton acces din MainScreen (iconița calendar 📅 lângă butonul All Day)
- Navigare de la click pe zi → `"customday/{year}/{month}/{day}"`

#### Pas 5: String Resources
- Adăugare texte RO + EN:
  - Numele lunilor, zilelor săptămânii (dacă nu sunt deja)
  - Legendă, titluri, navigare
  - Abrevieri Nakshatra (2-3 litere)

#### Pas 6: Optimizare performanță
- `LazyVerticalGrid` pentru grila calendarului (6 rânduri × 7 coloane)
- Calcul pe background thread cu `produceState` sau `LaunchedEffect`
- Cache lunar (nu recalcula dacă utilizatorul revine la aceeași lună)
- Loading skeleton/shimmer în timp ce se calculează

#### Pas 7: Testare
- Verificare corectitudine zile per lună (feb leap year, etc.)
- Test navigare luni precedente/următoare
- Test tap pe zi → All Day view
- Test Light/Dark theme
- Test localizare RO/EN
- Test cu diferite fusuri orare

### Estimare: 3-4 sesiuni de implementare

---

## Priorități sugerate

| # | Feature | Complexitate | Impact UX |
|---|---------|-------------|-----------|
| 1 | Recomandarea Zilei | ⭐⭐ Medie | 🔥🔥🔥 Mare |
| 2 | Calendar Lunar | ⭐⭐⭐ Mare | 🔥🔥🔥🔥 Foarte mare |

**Ordinea recomandată**: Recomandarea Zilei mai întâi (mai simplă, impact imediat), apoi Calendar Lunar.
