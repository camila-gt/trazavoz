# Trazavoz

App Android nativa, gratuita y de código abierto, para que niños aprendan lectura silábica en español mediante arrastre táctil de letras, con apoyo visual de pictogramas de [ARASAAC](https://arasaac.org/) y lectura en voz alta (TTS).

Incluye un modo Tutor protegido por PIN para administrar el catálogo de palabras, organizar tableros temáticos y ver el progreso del niño.

## Descargar

Los `.apk` de cada versión se publican en [Releases](../../releases). No requiere Google Play ni cuenta de ningún tipo.

## Funcionalidades

- **Modo niño**: selección por abecedario o por tablero temático, juego de arrastrar letras con lectura en voz alta de sílabas y palabras, celebración al completar.
- **Modo Tutor** (PIN de 4 dígitos): buscar y añadir palabras desde ARASAAC con silabeo automático editable, administrar tableros, y ver estadísticas de progreso (partidas jugadas, errores promedio, palabras más difíciles).
- Funciona sin conexión una vez guardadas las palabras (las imágenes se cachean en disco).
- Interfaz responsiva (teléfono y tablet, cualquier orientación) con Material You (color dinámico en Android 12+).

## Stack técnico

Kotlin + Jetpack Compose (Material 3), Room, Retrofit, Coil, Hilt, Navigation Compose, DataStore, TextToSpeech nativo. Arquitectura MVVM con capas `data`/`domain`/`ui`.

## Compilar

```bash
./gradlew assembleDebug
```

Requiere JDK 21 y Android SDK (`compileSdk`/`targetSdk` 35, `minSdk` 24).

## Contribuir

Los mensajes de commit siguen [Conventional Commits](https://www.conventionalcommits.org/) en español (`feat:`, `fix:`, `refactor:`, `chore:`, `docs:`, `test:`) y el flujo de ramas `main` ← `develop` ← `feature/*`. Los releases se etiquetan como `vMAJOR.MINOR.PATCH` sobre `main`.

## Licencia

Este proyecto está bajo la licencia [GNU General Public License v3.0 (GPLv3)](LICENSE).

Los pictogramas utilizados son propiedad del Gobierno de Aragón y han sido creados por Sergio Palao para [ARASAAC](https://arasaac.org/), distribuidos bajo licencia [Creative Commons BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/).
