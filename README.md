# LunarTune Desktop

Desktop edition of LunarTune, built with **Compose Multiplatform**. Windows first,
but the Gradle build also declares Linux/macOS distribution formats.

> Status: **foundational stage**. The app shell, shared models, word-synced
> lyrics, YouTube Music search, stream resolution and JVM playback are in place.
> Library, account, offline and settings screens are future increments.

## Layout

```
desktop/
 ├── src/commonMain/   shared Compose UI, models, LRC parser, state holder
 ├── src/commonTest/   shared parser/model tests
 ├── src/desktopMain/  JVM window, LavaPlayer engine, InnerTube catalog,
 │                    BetterLyrics client, TTML/QRC decoder
 └── src/desktopTest/  TTML word-timing + BetterLyrics envelope tests
core/                 InnerTube/YouTube Music client (same JVM library as Android)
morideobfuscator/     stream signature deobfuscator (core dependency)
lyrics/               multi-provider lyrics library (same JVM library as Android)
app/, lastfm/, …      Android app sources kept for reference only; they are not
                      part of the desktop Gradle build
```

`core`, `morideobfuscator` and `lyrics` are git submodules pinned to the same
commits as the Android app. Clone with `--recurse-submodules` (CI does this).

The three-pane shell follows the large-screen desktop layout: navigation rail on
the left, search/results in the middle, a now-playing panel with word-synced
lyrics on the right, and a persistent player bar at the bottom. Space toggles
playback.

## Playback

- Search uses the shared InnerTube client (`YouTube.search`, song filter).
- `YouTubeStreamResolver` walks PoToken-free player clients, picks the best
  audio-only format and deobfuscates its URL through `NewPipeUtils` — the same
  libraries and approach the Android app uses. Web clients are excluded because
  a desktop app cannot mint a PoToken and their URLs are guaranteed to 403.
- `LavaplayerController` decodes the resolved URL with LavaPlayer and writes PCM
  frames to a Java Sound `SourceDataLine`. Its 100 ms position ticker drives the
  karaoke highlight.

## Lyrics and word sync

Lyrics come from the **same** provider library as the Android app, in this order:
BetterLyrics (TTML/QRC), LrcLib, KuGou, Unison, SimpMusic, Paxsenix, YouLyPlus.
TTML and QRC are parsed with the library's own `TTMLParser` / `QRCParser`, and
standard/enhanced LRC by a pure-Kotlin parser in `commonMain`. Both produce one
`LyricsDocument` with per-word `startMs`/`endMs`, which the shared Compose UI
colours word by word as playback advances. Clicking a line seeks.

### BetterLyrics status (checked against the live API)

`https://api.betterlyrics.org/getLyrics` is cache-first:

- cached query (`s=Shape of You&a=Ed Sheeran&d=234`) → **200** with a JSON
  envelope containing word-timed TTML (`itunes:timing="Word"`);
- uncached query → **401** `API key required` — an API key is needed for misses;
- the legacy `lyrics-api.boidu.dev` host answers the same way.

The desktop client therefore never sends `album` (it turns cache hits into
misses), always sends duration when known, and applies its 401 backoff **per
endpoint** so one uncached endpoint cannot silence the Kugou/Portato endpoints
that may hold a cached result. The Android app currently applies that backoff to
the whole host for 10 minutes; see the fix in the Android repository.

## Build

Requires JDK 21.

```bash
./gradlew :desktop:compileKotlinDesktop   # compile
./gradlew :desktop:desktopTest            # shared + desktop tests
./gradlew :desktop:run                    # run the app
./gradlew :desktop:packageMsi             # Windows installer (on Windows)
```

CI verifies the shared/desktop sources on Linux and builds the Windows installers
with `desktop-windows.yml`.
