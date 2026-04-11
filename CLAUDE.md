# Gambar Aja Kok Repot

A toddler-friendly drawing app for Android, built with Jetpack Compose.

## Build

```bash
# Debug build
./gradlew assembleDebug

# Release build (signed APK)
./gradlew assembleRelease
```

The release APK is output to `app/build/outputs/apk/release/app-release.apk`.

### Android SDK setup (for environments without it)

The default build machine does not have the Android SDK installed. Claude must install it before building:

```bash
cd /opt
curl -fsSL -o cmdline-tools.zip "https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"
unzip -q cmdline-tools.zip
mkdir -p /opt/android-sdk/cmdline-tools
mv cmdline-tools /opt/android-sdk/cmdline-tools/latest
export ANDROID_HOME=/opt/android-sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH
yes | sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
```

### Build verification requirement

**All code changes must compile before committing and pushing.** Claude must run `./gradlew assembleDebug` (installing the Android SDK first if needed) and confirm the build succeeds before creating any commit, push, or PR.

## Requirements

- **JDK**: 17+
- **Android SDK**: Platform 35, Build Tools 35.0.0
- **Min Android version**: 10 (API 29)
- **Gradle**: 8.11.1 (wrapper included)

## Project Structure

```
app/src/main/java/com/gambaraja/kokrepot/
├── MainActivity.kt              # Single activity, edge-to-edge
├── DrawingApp.kt                # Root composable: LeftToolbar | Canvas | RightToolbar
├── DrawingViewModel.kt          # State: actions list, undo/redo, tool/color/thickness, pan offset
├── model/
│   ├── DrawingAction.kt         # Sealed class: Stroke (points+color+thickness) and Stamp (center+type+color+size)
│   └── Enums.kt                 # Tool enum (BRUSH, ERASER, STAMP_*), StampType enum
├── ui/
│   ├── canvas/DrawingCanvas.kt  # Compose Canvas: renders actions with viewport culling, touch handling
│   └── toolbar/
│       ├── LeftToolbar.kt       # 12 color circles + eraser button
│       └── RightToolbar.kt      # 5 thickness buttons + 5 stamp buttons + undo/redo
└── stamp/StampRenderer.kt       # DrawScope extensions: drawHeart, drawStar, drawSpiral, drawSmiley, drawSquare
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
