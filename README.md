# Null Plate

An intermittent-fasting timer for Android. Tracks both sides of the clock — the fast and the eating window that follows it — with a playful, physiology-flavored commentary track instead of a plain countdown.

It is a port of [omfasty](https://github.com/anders81fin/omfasty), the same timer written as a bar widget for the [Omarchy](https://omarchy.org) shell. The rules are the same; the plumbing is not, for reasons described below.

## Features

- **Ongoing notification** counting up from the moment the fast started, with the current physiology stage as its text.
- **Fasting : eating ratio presets** — 14:10, 16:8, 18:6, 20:4, 22:2, and UMAD? (24:0, the joke option).
- **Eating-window tracking** — once a fast ends, the same counter runs against the eating-window target implied by the ratio you picked (`24 - fasting hours`).
- **Progress bar** and a **streak counter** for fasts that hit their target.
- **Physiology-stage commentary** — a tongue-in-cheek line that updates through the fast (blood sugar, glycogen, the metabolic switch, ketosis, autophagy) and through the eating window (fueling up, window closing, into overtime). Not medical advice — it's a timer, not a lab.
- **Recent history** — the last three completed fasts of at least 12h, with actual vs. target hours.
- **Longest fasts** — the three longest fasts ever measured, regardless of when they happened.
- **Hourly nudges** — a notification on the hour, both while fasting and during the eating window.

## How it works

The desktop widget polled: a one-second timer recomputed the elapsed time and watched for a crossed hour boundary, which is affordable when a shell is always running. Android will not keep that alive, so both halves are handed to the platform instead.

The counter is an ongoing notification built with `setWhen(startedAt)` and `setUsesChronometer(true)`. The system redraws the elapsed time on its own, so no process of ours needs to stay awake for the timer to keep moving.

The nudges are scheduled rather than detected. Every whole-hour mark is computable in advance from the start instant, so the app enqueues the next one with WorkManager and re-arms it when it fires. Nothing watches the clock, and a nudge that arrives a minute late still reports the right hour.

State lives in two DataStore files — the current fast and the completed history — and the domain rules that read them (`app/src/main/java/io/github/anders81fin/nullplate/domain/`) are plain Kotlin with no Android dependencies, so they are covered by ordinary unit tests.

## Permissions

No `INTERNET` permission: the app never talks to a network, and there is nothing to collect.

`POST_NOTIFICATIONS` and `RECEIVE_BOOT_COMPLETED` are declared directly. WorkManager additionally merges in `ACCESS_NETWORK_STATE`, `WAKE_LOCK` and `FOREGROUND_SERVICE`; none of them are used by this app's own code.

## Building

Requires a JDK (17+) and the Android SDK.

```
./gradlew test          # unit tests, no device needed
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## License

MIT — see [LICENSE](LICENSE).
