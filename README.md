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

The desktop client therefore never sends `album` for the main endpoint (measured:
`Shape of You` returns 200 without `al` and 401 with it), always sends duration
when known, and applies its 401 backoff **per endpoint** so one uncached endpoint
cannot silence the QQ/Portato endpoint, which maintains its own cache (verified
200 with QRC word timings). The Android app used to apply that backoff to the
whole host for 10 minutes; see the fix in the Android repository.

### Can I get an API key?

**Not right now.** The official docs state "API keys are not being issued right
now" because each cache miss hits a paid upstream ([authentication docs](https://docs.betterlyrics.org/docs/authentication)).
The key carried by the official browser extension is exclusive to it — it is
"not handed out to other projects and it is not for sale" ([API README](https://github.com/better-lyrics/api#the-better-lyrics-key)).
The setting added to the Android app is ready for anyone who has a key from a
self-hosted instance; the backend is GPL-3.0 and open source, and its maintainer
asks people who want to run their own instance to [get in touch via Discord](http://discord.gg/UsHE3d5fWF).

Practical workarounds without a key:

1. Cached songs need no key — that is the majority of popular music.
2. The cache key includes album and duration, so for some entries adding `al`
   helps; for older entries it turns a hit into a miss. The client errs on the
   side of omitting it, which measured better.
3. Play an uncached song once with the [Better Lyrics extension](https://better-lyrics.boidu.dev/)
   installed: it fetches through the same upstream, so the response becomes
   cached and any client can then read it keylessly.
4. `/qq/getLyrics` and `/kugou/getLyrics` keep separate caches and do not
   require a key on the docs' main endpoint rules; the desktop client tries all
   three endpoints.
5. Contribute lyrics yourself at [unison.boidu.dev](https://unison.boidu.dev/).

## Windows installers

CI publishes **four separate artifacts** (EXE and MSI, each for x64 and arm64),
so a user downloads one installer instead of a 354 MB bundle containing both
formats:

| Artifact | Contents |
| --- | --- |
| `lunartune-windows-x64-exe` | `LunarTune-5.2.0-x64.exe` |
| `lunartune-windows-x64-msi` | `LunarTune-5.2.0-x64.msi` |
| `lunartune-windows-arm64-exe` | `LunarTune-5.2.0-arm64.exe` |
| `lunartune-windows-arm64-msi` | `LunarTune-5.2.0-arm64.msi` |

x64 builds on `windows-latest`, arm64 builds natively on `windows-11-vs2026-arm`.
Each architecture has its own MSI upgrade code. `-PlunarTuneArch=x64|arm64`
selects it locally; without the flag the build uses the current CPU.

### Size

The first foundation build produced a 178 MB EXE plus a 178 MB MSI. The runtime
classpath was 154 MB, dominated by three jars that were pure waste on Windows:

- `skiko-awt-runtime-all` (42 MB) — natives for six platforms. `compose.desktop.currentOs`
  already supplies the exact OS/arch jar, so the all-platform jar is excluded.
- `material-icons-extended` (36 MB) — ~10k icons for the six glyphs the UI uses.
  Replaced with `material-icons-core` (0.8 MB) plus one hand-defined Pause vector.
- optional lavaplayer modules (`test-samples`, `ext-format-xm`, `stream-merger`,
  `ext-youtube-rotator`) — excluded.

Packaging uses a jlink runtime (`includeAllModules = false`,
`java.base/java.desktop/java.logging/java.xml/jdk.crypto.ec`) instead of a full
JDK. `java.xml` is required by the bundled TTML DOM parser and comes in via
`java.desktop`. CI prints each installer's size in the "Report installer sizes"
step, so regressions are visible in the build log.

> Note: `lavaplayer-natives-fork` (26 MB) is kept as-is for safety. It has no
> `win-aarch64` native, so the arm64 build falls back to pure-Java decoding;
> AAC streams still play. Stripping it is a possible next step.

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
