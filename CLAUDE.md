# Gambar Aja Kok Repot

A toddler-friendly drawing app, built with Kotlin Multiplatform + Compose Multiplatform. Runs natively on Android and in the browser (Kotlin/Wasm).

## Build

```bash
# Android debug build
./gradlew assembleDebug

# Android release build (signed APK)
./gradlew assembleRelease

# Web (Wasm) production distribution
./gradlew wasmJsBrowserDistribution
```

The release APK is output to `app/build/outputs/apk/release/app-release.apk`. The web distribution is output to `app/build/dist/wasmJs/productionExecutable/` (open `index.html` via a local web server — not `file://`, which blocks Wasm).

### Android SDK setup (for environments without it)

The default build machine does not have the Android SDK pre-configured. Claude must set it up before building. The `sdkmanager` may not work due to proxy settings in `JAVA_TOOL_OPTIONS`, so download SDK packages manually with `curl`:

```bash
ANDROID_HOME=/home/user/android-sdk
mkdir -p $ANDROID_HOME/build-tools $ANDROID_HOME/platforms

# Platform tools
curl -sL "https://dl.google.com/android/repository/platform-tools-latest-linux.zip" -o /tmp/platform-tools.zip
cd $ANDROID_HOME && unzip -qo /tmp/platform-tools.zip

# Build tools 35.0.0 (required by compileSdk 35)
curl -sL "https://dl.google.com/android/repository/build-tools_r35_linux.zip" -o /tmp/build-tools-35.zip
cd /tmp && unzip -qo build-tools-35.zip && mv android-*/ $ANDROID_HOME/build-tools/35.0.0

# Build tools 34.0.0 (required by AGP 8.7.3 internally)
curl -sL "https://dl.google.com/android/repository/build-tools_r34-linux.zip" -o /tmp/build-tools-34.zip
cd /tmp && unzip -qo build-tools-34.zip && mv android-*/ $ANDROID_HOME/build-tools/34.0.0

# Platform android-35
curl -sL "https://dl.google.com/android/repository/platform-35_r02.zip" -o /tmp/platform-35.zip
cd /tmp && unzip -qo platform-35.zip && mv android-*/ $ANDROID_HOME/platforms/android-35

# Accept licenses
mkdir -p $ANDROID_HOME/cmdline-tools/latest/bin && yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses 2>/dev/null || true
mkdir -p $ANDROID_HOME/licenses
echo -e "\n24333f8a63b6825ea9c5514f83c2829b004d1fee" > $ANDROID_HOME/licenses/android-sdk-license

# Point Gradle to the SDK
echo "sdk.dir=$ANDROID_HOME" > /home/user/gambar-aja-kok-repot/local.properties
```

**Important:** Unset `JAVA_TOOL_OPTIONS` before running Gradle to avoid proxy interference:

```bash
unset JAVA_TOOL_OPTIONS
export ANDROID_HOME=/home/user/android-sdk
export ANDROID_SDK_ROOT=/home/user/android-sdk
./gradlew assembleDebug
```

### Build verification requirement

**All code changes must compile before committing and pushing.** Claude must install the Android SDK (if not already set up) and run both `./gradlew assembleDebug` and `./gradlew assembleRelease` to confirm the builds succeed before creating any commit, push, or PR. The release build includes lint checks and R8 minification that can catch issues not found in the debug build.

## Requirements

- **JDK**: 17+
- **Android SDK**: Platform 35, Build Tools 35.0.0
- **Min Android version**: 10 (API 29)
- **Gradle**: 8.11.1 (wrapper included)

## Project Structure

The `:app` module is a Kotlin Multiplatform project with `androidTarget` (APK) and `wasmJs` (browser) targets.

```
app/src/
├── commonMain/kotlin/yuku/gambaraja/kokrepot/
│   ├── DrawingApp.kt            # Root composable: LeftToolbar | Canvas | RightToolbar
│   ├── DrawingViewModel.kt      # State: actions list, undo/redo, tool/color/thickness, pan offset
│   ├── DrawingStorage.kt        # `expect class DrawingStorage` (load/save DrawingSnapshot)
│   ├── model/
│   │   ├── DrawingAction.kt     # Sealed class: Stroke (points+color+thickness) and Stamp (center+type+color+size)
│   │   └── Enums.kt             # Tool enum (BRUSH, ERASER, STAMP_*), StampType enum
│   ├── ui/
│   │   ├── canvas/DrawingCanvas.kt    # Compose Canvas: viewport culling, touch handling (1-finger draw, 3-finger pan)
│   │   └── toolbar/{LeftToolbar,RightToolbar,ToolbarCommon}.kt
│   └── stamp/StampRenderer.kt   # DrawScope extensions: drawHeart, drawStar, drawSpiral, drawSmiley, drawSquare
├── androidMain/
│   ├── AndroidManifest.xml
│   ├── res/                     # Android launcher icons, themes, backup rules
│   └── kotlin/yuku/gambaraja/kokrepot/
│       ├── MainActivity.kt              # Single activity, edge-to-edge, immersive mode
│       └── DrawingStorage.android.kt    # `actual` — binary file in app's internal filesDir
└── wasmJsMain/
    ├── kotlin/
    │   ├── Main.kt                      # `fun main()` — ComposeViewport(document.body)
    │   └── yuku/gambaraja/kokrepot/DrawingStorage.wasmJs.kt    # `actual` — text format in localStorage
    └── resources/index.html             # Web entry page
```

## Architecture

**Stroke-based infinite canvas** — no bitmap stored in RAM.

- All drawing is stored as a `List<DrawingAction>` (strokes and stamps)
- Each action has a lazily-computed bounding `Rect`
- Rendering: only actions whose bounds overlap the current viewport are drawn
- Canvas is truly infinite — panning changes a viewport offset, drawing coordinates are in world space
- Undo/redo: pop/push from the actions list and redo stack

### Touch handling (in DrawingCanvas.kt)

- **1 finger**: draw stroke or place stamp on tap
- **3 fingers**: pan — once 3 pointers are detected, the gesture locks into pan mode and cancels any in-progress stroke
- Coordinate conversion: `worldPos = screenPos - panOffset`

## UI Layout

```
+----------+---------------------------------+----------+
|  Left    |                                 |  Right   |
| Toolbar  |        Drawing Canvas           | Toolbar  |
|          |       (white background)        |          |
| 12 color |                                 | 5 thick  |
| circles  |    1-finger: draw/stamp         | -------- |
|          |    3-finger: pan                | 5 stamps |
| -------- |                                 | -------- |
| Eraser   |                                 | Undo     |
|          |                                 | Redo     |
+----------+---------------------------------+----------+
```

### Left toolbar
12 predefined colors: Red, Orange, Yellow, Lime Green, Green, Sky Blue, Blue, Purple, Hot Pink, Brown, Black, Gray. Selected color shows highlighted border + slight scale. Eraser button at bottom (paints white).

### Right toolbar
5 brush thicknesses (4, 8, 14, 22, 32). Then 5 stamp types: Heart, Star, Spiral, Smiley, Square. Then Undo and Redo at the bottom. Stamp size = thickness * 2.5.

## Web deployment

The web (Wasm) build is automatically deployed to GitHub Pages by `.github/workflows/pages.yml` on every push to `main` (and to `claude/add-web-kmp-*` branches for preview builds). The live URL is configured in the repository's Pages settings.

## Signing

The release keystore is committed to `keystore/release.jks` intentionally. Credentials are in `keystore.properties`:
- Alias: `release`
- Store/key password: `gambaraja123`

## Release Process

Claude (the AI assistant) is responsible for triggering releases. The user provides the version number when requesting a release. Steps:

1. Update `CHANGELOG.md` with the new version, date, and list of changes.
2. Update `versionCode` (increment by 1) and `versionName` in `app/build.gradle.kts`.
3. Commit and push the changes to a feature branch.
4. Create a PR to `main` and merge it. The push to `main` triggers the GitHub Actions workflow (`.github/workflows/release.yml`), which builds a signed release APK and publishes it as a GitHub Release with the version from `build.gradle.kts`.
5. **After releasing, always merge the feature branch to `main`.** This is mandatory — never leave a release branch unmerged.
6. **Verify the release appears on GitHub Releases.** After the merge to `main`, check that the GitHub Actions workflow has created the release with the correct version tag and the APK asset attached. If the release is missing, create it manually.
