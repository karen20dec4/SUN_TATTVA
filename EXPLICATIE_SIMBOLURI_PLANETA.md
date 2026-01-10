# Explicație: Simboluri Planetare în Notificare

## Problema Raportată
Utilizatorul vede un icon generic (soare) în loc de simbolul planetei (♂, ☿, etc.) când este activată doar "Planetary Hour in Status Bar".

## Clarificare Importantă: Două Zone în Notificare

Android are DOUĂ zone separate în notificarea din status bar:

### 1. Small Icon (Iconița Mică) - În Status Bar Lângă Ceas
- **Locație**: Sus în status bar, lângă ceas (13:56 [ICON])
- **Tip**: Drawable resource (fișier XML/PNG din res/drawable)
- **Limitare Android**: Nu poate afișa simboluri Unicode (♂, ☿, 🔵)
- **Ce afișează**: 
  - Când tattva activat: Icon tattva colorat
  - Când doar planet activat: Icon soare generic (`R.drawable.icon`)
  - Android NU permite simboluri Unicode aici!

### 2. Notification Title (Titlul Notificării) - În Notification Shade
- **Locație**: În panoul de notificări când tragi status bar-ul în jos
- **Tip**: Text/String (poate conține Unicode)
- **Ce afișează**: 
  - Când tattva activat: 🔵 (emoji tattva)
  - Când planet activat: ♂ (simbol planetă)
  - Când ambele active: 🔵♂ (ambele simboluri)

## Ce Funcționează Corect în Cod

Codul este CORECT! Iată cum funcționează:

```kotlin
// Linia 296: Obține simbolul planetei
val planetEmoji = planetType?.code ?: ""  // ♂, ☿, ☉, etc.

// Liniile 299-302: Construiește titlul cu simboluri
val collapsedTitle = buildString {
    if (showTattva) append(tattvaEmoji)      // 🔵
    if (showPlanet) append(planetEmoji)      // ♂
}
// Rezultat când doar planet: collapsedTitle = "♂"
// Rezultat când ambele: collapsedTitle = "🔵♂"

// Linia 345: Setează titlul notificării
builder.setContentTitle(collapsedTitle)  // Aici apare ♂
```

## Cum Să Verifici

1. **Activează doar "Planetary Hour in Status Bar"** în Settings
2. **Force stop aplicația** (Settings > Apps > SUN TATTVA > Force Stop)
3. **Repornește aplicația**
4. **Trage status bar-ul în jos** (swipe down din partea de sus)
5. **Privește notificarea SUN TATTVA**:
   - Titlul ar trebui să fie: **♂** (sau ☿, ☉, etc. în funcție de planeta curentă)
   - Sub titlu: "SUN TATTVA - Paris"

## De Ce Iconița Mică Este Generic Sun Icon?

Iconița mică din status bar (lângă ceas) TREBUIE să fie un drawable resource. Android nu acceptă simboluri Unicode acolo.

**Opțiuni**:
1. **Status quo** (recomandat): Icon soare generic când doar planet
2. **Creare iconuri planetare**: Ar trebui create 7 drawable-uri XML pentru fiecare planetă (☉, ☽, ☿, ♀, ♂, ♃, ♄)

## Verificare cu Logcat

Am adăugat log la linia 305:
```
📱 Notification Title: '♂' (showTattva=false, showPlanet=true, planet=Marte, planetSymbol=♂)
```

Rulează `adb logcat | grep "Notification Title"` pentru a vedea ce se construiește.

## Concluzie

Simbolul planetei **♂ APARE DEJA** în titlul notificării! Trebuie să tragi status bar-ul în jos pentru a-l vedea în notification shade. Iconița mică din status bar (lângă ceas) va rămâne un icon drawable generic din cauza limitărilor Android.
