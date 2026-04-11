# Changelog

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
