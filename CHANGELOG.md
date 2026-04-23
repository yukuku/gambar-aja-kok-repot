# Changelog

## [4.0.2] - 2026-04-23

### Added
- **Web loading screen now shows a progress bar.** The ~10 MB wasm bundle used to sit behind a silent "Loading…" text for several seconds. The loading screen now displays a colorful rainbow progress bar (with gliding glitter stripes) that tracks WASM download progress, plus the full version and build time — rendered in the viewer's local timezone — so you can tell at a glance which build is being served.

## [4.0.1] - 2026-04-19

### Changed
- **Web fullscreen re-engages on every touch.** Previously, leaving the tab (or pressing Esc) dropped fullscreen and the app didn't try to restore it. Now any tap, touch, or click brings the browser back to fullscreen — a much better experience for toddlers who frequently switch tabs or accidentally exit fullscreen.

## [4.0.0] - 2026-04-18

### Added
- **Web version** built with Kotlin Multiplatform + Compose Multiplatform 1.8.2, targeting Kotlin/Wasm. The Android app and the web app share the same drawing canvas, toolbars, stamps, and view-model — only storage and the host entry point are platform-specific.
- **GitHub Pages deployment** (`.github/workflows/pages.yml`) publishes the Wasm build on every push to `main`, live at <https://yukuku.github.io/gambar-aja-kok-repot/>.
- **Hidden settings dialog** with the version, git hash, and build timestamp (rendered in the viewer's local time with UTC offset). Opened via a two-finger combo — press the grey color and the smiley stamp simultaneously, then tap the cog that briefly appears at the top of the screen. Designed so random toddler button-mashing can't trigger it.
- **Browser fullscreen** on the web version now engages on the first user interaction (matching the Android app's immersive mode), with retry across multiple fullscreen targets for Chrome Android / Opera Mobile compatibility.

### Changed
- `:app` was converted from an Android-only module to a Kotlin Multiplatform module with `androidTarget` and `wasmJs` targets. The APK output path (`app/build/outputs/apk/release/app-release.apk`) is unchanged.
- `DrawingViewModel` is now a plain multiplatform class (no `AndroidViewModel`) that owns a `CoroutineScope` directly.
- On web, the drawing is persisted in `localStorage` as a compact text format. On Android, the existing binary file format is preserved so existing users keep their drawings.

## [3.0.0] - 2026-04-16

### Added
- **Keep screen on** while the app is open, so a toddler's drawing session is never interrupted by the screen locking mid-stroke.
- **Sticky immersive fullscreen** hides the status and navigation bars so toddlers can't accidentally pull notifications or navigate away. Bars transiently reappear on an edge swipe for parents who need them.
- **Auto-save and restore** — the drawing (and the current pan position) is silently saved to disk after every stroke, stamp, undo, and redo, and reloaded on the next launch. No save button, no dialog, no lost masterpieces when the app is closed or killed.
- **Haptic feedback** on every tool, color, stamp, and undo/redo tap, plus a soft tick on each stamp placement. Tactile confirmation that toddlers love.

### Changed
- **Smoother strokes** — stroke rendering now uses quadratic Bézier curves between sample midpoints instead of straight line segments, eliminating polyline jaggies on fast strokes.
- The back gesture is now **fully swallowed** so a toddler cannot accidentally exit the app mid-drawing.

## [2.1.3] - 2026-04-16

### Fixed
- Smiley stamp eyes and mouth now have a border halo matching the face outline, so the face stays visible when using dark colors (e.g. purple) on the toolbar's dark selection background

## [2.1.2] - 2026-04-13

### Changed
- Right toolbar is now scrollable when the screen is too short to show every tool (e.g. landscape). Thickness, stamps, and undo/redo all scroll together — nothing is pinned.

### Fixed
- Stamp buttons now hide while the eraser is the active tool, since you can't stamp with an eraser anyway.

## [2.1.1] - 2026-04-13

### Changed
- Eraser icon redrawn to look like a classic tilted rubber eraser — coral body with a yellow stripe running along its length and a white "rubber" cap at the end
- Heart stamp body is now plumper and rounder (fuller lobes, more pronounced tip)
- Stamp selection borders now follow each stamp's own silhouette (heart, star, square, spiral curve, smiley face) instead of a square rounded-rectangle around the button

## [2.1.0] - 2026-04-13

### Changed
- Toolbar backgrounds no longer flip between light and dark when the drawing color changes — they stay on a fixed light-grey background
- Each tool (color, thickness, stamp) now has a 1dp contrasting border so it stays visible against the toolbar no matter what color it uses
- Selection indicator is now a dark-grey background behind the selected tool; thickness and stamp icons no longer invert to black/white when selected
- Selecting a color tool now triggers the same brief blinking selection animation as thickness and stamp tools
- Thickness and stamp icons now render white while the eraser is the active tool, hinting that the eraser paints white
- Undo/redo icons are now permanently black (matching the new fixed background)

### Fixed
- Eraser icon redrawn as a classic pink-and-blue school eraser instead of the previous generic layers-clear icon
- Selection blink animation no longer plays for the default tool at app startup — it now plays only in response to user taps
- Redo button fully hides its icon when redo is unavailable instead of showing a greyed-out arrow

## [2.0.0] - 2026-04-11

### Changed
- Package name changed from `com.gambaraja.kokrepot` to `yuku.gambaraja.kokrepot`
- Toolbar selection state is now much more prominent with full colored background and 1-second blink animation on selection
- Thickness and stamp selector icon colors now follow the currently selected drawing color
- Toolbar background adapts to contrast with the selected color for readability
- Stamp tools and thickness tools are now mutually exclusive — selecting a stamp deselects thickness, and vice versa
- Stamp size is now fixed (no longer tied to brush thickness)
- Dragging with a stamp tool now sprinkles stamps continuously with 10dp spacing between edges
- Upgraded Compose BOM to 2025.12.00 (Compose UI 1.10.0) and Kotlin to 2.1.20

## [1.0.2] - 2026-04-11

### Fixed
- Left toolbar no longer gets overdrawn by canvas strokes (toolbars now render above the canvas)
- Canvas extends fullscreen behind system bars and navigation bar for an immersive drawing experience
- Stamp toolbar icons now use the same renderer as actual stamps drawn on the canvas

## [1.0.1] - 2026-04-11

### Fixed
- Drawing after panning now paints at the correct position under the finger instead of the unpanned position
- Undo/redo now immediately rerenders the canvas without requiring an additional draw action

## [1.0.0] - Initial release

### Added
- Infinite canvas with stroke-based rendering
- 12 color palette with eraser
- 5 brush thickness options
- 5 stamp types: Heart, Star, Spiral, Smiley, Square
- 3-finger pan gesture for infinite canvas navigation
- Undo/redo support
