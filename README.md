# Gym Helper

Android app for interval (timed circuit) training and weight training with per-day exercise lists.

## Features

- **Interval training** — programs with multiple exercise days, configurable round/rest timers, gong between phases, pause and stop.
- **Weight training** — programs with exercise days, sets and reps per exercise, log weight per set, sessions saved to Room; last session weight is suggested next time.
- **Copy day** — clipboard button on each exercise day (weight: name + sets × reps; interval: day settings + exercise names).

## Build

```bash
cd gym-helper
./gradlew assembleDebug
```

Open the `gym-helper` folder in Android Studio or install the APK from `app/build/outputs/apk/debug/`.

## Stack

- Kotlin, Jetpack Compose, Material 3
- Room (local database)
- Navigation Compose
