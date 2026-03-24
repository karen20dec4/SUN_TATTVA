# Rezolvare Bug Nakshatra - Calculul Nakshatrelor Viitoare

## Versiune: 2.18 (versionCode 12)

---

## Problema

Când se expandează cardul Nakshatra pentru a vedea toate cele 27 de Nakshatre viitoare, intervalele de timp afișate se schimbau semnificativ de la o zi la alta.

**Exemplu concret:**
- Pe 23 Mar 2026 (bug-nakshatra-1.jpg), Nakshatra curentă era calculată corect, dar Nakshatrele viitoare (#3, #4, #5, ...) aveau intervale de timp diferite față de:
- Pe 24 Mar 2026 (bug-nakshatra-2.jpg), aceleași Nakshatre viitoare arătau intervale complet diferite.

**Diferențele creșteau progresiv** - cu cât Nakshatra era mai departe în viitor, cu atât diferența era mai mare.

---

## Cauza Principală

### Viteza Lunii NU este constantă

Luna se mișcă pe orbita ei cu o viteză variabilă:
- **Viteza minimă:** ~11.8°/zi (la apogeu - punctul cel mai depărtat de Pământ)
- **Viteza maximă:** ~15.2°/zi (la perigeu - punctul cel mai aproape de Pământ)
- **Media:** ~13.2°/zi

### Codul vechi (Bug)

Codul anterior folosea **o singură viteză constantă** (viteza curentă a Lunii) pentru a calcula intervalele TUTUROR celor 27 de Nakshatre viitoare:

```kotlin
// ❌ VECHI - în NakshatraCard.kt
val avgDegreesPerHour = nakshatraResult.moonSpeedDegreesPerDay / 24.0  // O SINGURĂ viteză!

// Pentru FIECARE Nakshatra viitoare (inclusiv cele de peste 10-20 zile):
val hoursToStart = (nakshatraIndex + cycleOffset) * nakshatraDegrees / avgDegreesPerHour
val hoursToEnd = (nakshatraIndex + cycleOffset + 1) * nakshatraDegrees / avgDegreesPerHour
```

**Problema:** Aceasta presupune că Luna se mișcă cu aceeași viteză timp de ~27 de zile, ceea ce este FALS.

### De ce difereau rezultatele de la o zi la alta

- Pe 23 Mar, viteza Lunii era (de exemplu) 14.2°/zi → toate Nakshatrele calculate cu 14.2°/zi
- Pe 24 Mar, viteza Lunii era (de exemplu) 13.8°/zi → toate Nakshatrele recalculate cu 13.8°/zi

Diferența de 0.4°/zi, proiectată pe 27 de Nakshatre (~27 zile), acumula ore sau chiar zile de eroare!

**Calcul eroare:**
- 1 Nakshatra la 14.2°/zi = 13.333° / (14.2°/24h) = **22.5 ore**
- 1 Nakshatra la 13.8°/zi = 13.333° / (13.8°/24h) = **23.2 ore**
- Diferență per Nakshatra: **0.7 ore**
- Eroare cumulată la Nakshatra #27: **0.7 × 27 ≈ 19 ore!**

---

## Soluția Implementată (v2.18)

### Calculul pozițiilor reale din efemeride (Swiss Ephemeris)

În loc să folosim o singură viteză constantă, acum calculăm **poziția reală a Lunii** la fiecare limită de Nakshatra folosind Swiss Ephemeris.

### Algoritmul: Forward-Stepping cu Newton-Raphson

```
Pentru fiecare din cele 27 de Nakshatre:
  1. ESTIMARE: Folosim viteza curentă cunoscută pentru a estima 
     când Luna va ajunge la limita următoarei Nakshatre
  
  2. INTEROGARE EFEMERIDĂ: Cerem Swiss Ephemeris poziția și viteza 
     REALĂ a Lunii la momentul estimat
  
  3. RAFINARE (Newton-Raphson): 
     - Calculăm eroarea: poziția_reală - limita_dorită
     - Ajustăm timpul: timp_nou = timp_estimat - eroare / viteză_reală
     - Repetăm dacă eroarea > 0.01° (maxim 3 iterații)
  
  4. ACTUALIZARE VITEZĂ: Folosim viteza REALĂ de la acest punct 
     pentru estimarea următoarei Nakshatre
```

### Fișierele Modificate

| Fișier | Modificare |
|--------|-----------|
| `NakshatraCalculator.kt` | Adăugat `NakshatraTimeSlot` data class și metoda `calculateFutureNakshatras()` |
| `AstroRepository.kt` | Apelează `calculateFutureNakshatras()` cu acces la Swiss Ephemeris |
| `NakshatraCard.kt` | Folosește intervalele pre-calculate în loc de extrapolarea cu viteză constantă |
| `app/build.gradle.kts` | Versiune actualizată la 2.18 (versionCode 12) |

---

## Detalii Tehnice

### Variabilele folosite

1. **`currentMoonLongitude`** (Double, grade siderale 0-360°)
   - Longitudinea curentă a Lunii din Swiss Ephemeris
   - Determină în ce Nakshatra ne aflăm acum

2. **`currentMoonSpeedDegreesPerDay`** (Double, °/zi)
   - Viteza curentă a Lunii (din `swe_calc_ut()`, array xx[3])
   - Folosită doar ca estimare inițială, NU ca viteză constantă

3. **`getMoonPositionAndSpeed`** (funcție Lambda)
   - Primește un Calendar și returnează `Pair<longitude, speed>`
   - Interogează Swiss Ephemeris pentru momentul dat
   - Folosită la fiecare limită de Nakshatra pentru date reale

4. **`endBoundaryDeg`** (Double, grade)
   - Limita de sfârșit a fiecărei Nakshatre: `(index + 1) * 13.333° % 360°`
   - Gestionează wrap-around la 360°/0°

### Cum se calculează fiecare Nakshatra viitoare

```
Nakshatra curentă (i=0):
  - Start: currentTime - (gradeElapse / viteză) secunde
  - End: currentTime + (gradeRămase / viteză) secunde
  - Rafinat cu efemeridă la momentul estimat de End

Nakshatra viitoare (i=1..26):
  - Start: End-ul Nakshatrei anterioare (calculat precis)
  - End: Start + (13.333° / vitezaReală) secunde
  - Rafinat cu efemeridă la momentul estimat de End
  - vitezaReală = viteza din efemeridă la punctul rafinat
```

### Gestionarea Wrap-Around 360°/0°

Când eroarea de poziție traversează limita 360°/0°:
```kotlin
var error = normalizedActualLon - endBoundaryDeg
if (error > 180.0) error -= 360.0   // ex: 359° - 1° = 358° → -2°
if (error < -180.0) error += 360.0  // ex: 1° - 359° = -358° → 2°
```

### Precizie

- **Criteriu de convergență:** |error| < 0.01° (≈ 1 minut de timp)
- **Iterații maxime:** 3 per limită (de obicei converge în 1-2)
- **Total apeluri efemeridă:** ~27-81 (1-3 per Nakshatra)

---

## Înainte vs. După

### Înainte (v2.17)
- ❌ O singură viteză (ex: 14.2°/zi) pentru toate cele 27 Nakshatre
- ❌ Eroare cumulativă crescând cu fiecare Nakshatra viitoare
- ❌ Rezultate diferite la recalculare a doua zi (viteză diferită)

### După (v2.18)
- ✅ Viteză reală din efemeridă la fiecare limită de Nakshatra
- ✅ Precizie < 0.01° (~1 minut) la fiecare limită
- ✅ Rezultate consistente - recalcularea a doua zi dă aceleași intervale
- ✅ Backward compatible - dacă efemerida nu e disponibilă, revine la metoda veche

---

## Verificare

Pentru a verifica că fix-ul funcționează:
1. Deschide aplicația pe 23 Mar și notează intervalele tuturor Nakshatrelor
2. Deschide aplicația pe 24 Mar și verifică că intervalele Nakshatrelor VIITOARE (cele care nu au trecut încă) sunt **identice** sau foarte aproape (diferență < 5 minute)
3. Nakshatrele care au trecut deja nu vor mai apărea (s-au mutat la finalul listei)
