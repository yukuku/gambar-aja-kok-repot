# Changelog

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
