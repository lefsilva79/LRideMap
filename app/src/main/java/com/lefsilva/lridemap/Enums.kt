package com.lefsilva.lridemap

import android.graphics.Color

enum class MarkerColor(val colorValue: Float) {
    RED(0f),         // Vermelho
    GREEN(120f),     // Verde
    BLUE(240f),      // Azul
    YELLOW(60f),     // Amarelo
    PURPLE(270f),    // Roxo
    ORANGE(30f);     // Laranja

    fun toHSVFloat(): Float = colorValue

    fun toColor(): Int {
        return Color.HSVToColor(floatArrayOf(colorValue, 1f, 1f))
    }

    companion object {
        fun fromColorValue(value: Float): MarkerColor {
            val normalizedValue = value % 360
            return values().find {
                Math.abs(it.colorValue - normalizedValue) < 0.001f
            } ?: RED
        }

        fun getColorInfo(value: Float): String {
            val color = fromColorValue(value)
            return """
                Color Value: $value
                Normalized: ${value % 360}
                Mapped To: ${color.name}
                HSV Value: ${color.toHSVFloat()}
            """.trimIndent()
        }
    }
}

enum class MapType(val value: Int) {
    NORMAL(1),       // GoogleMap.MAP_TYPE_NORMAL
    SATELLITE(2),    // GoogleMap.MAP_TYPE_SATELLITE
    HYBRID(4);       // GoogleMap.MAP_TYPE_HYBRID

    companion object {
        fun fromValue(value: Int): MapType {
            return values().find { it.value == value } ?: NORMAL
        }
    }
}