package com.gambaraja.kokrepot.model

enum class StampType {
    HEART, STAR, SPIRAL, SMILEY, SQUARE
}

enum class Tool {
    BRUSH,
    ERASER,
    STAMP_HEART,
    STAMP_STAR,
    STAMP_SPIRAL,
    STAMP_SMILEY,
    STAMP_SQUARE;

    val isStamp: Boolean
        get() = this in listOf(STAMP_HEART, STAMP_STAR, STAMP_SPIRAL, STAMP_SMILEY, STAMP_SQUARE)

    val stampType: StampType?
        get() = when (this) {
            STAMP_HEART -> StampType.HEART
            STAMP_STAR -> StampType.STAR
            STAMP_SPIRAL -> StampType.SPIRAL
            STAMP_SMILEY -> StampType.SMILEY
            STAMP_SQUARE -> StampType.SQUARE
            else -> null
        }
}
