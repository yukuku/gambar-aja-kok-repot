# Changelog

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
