package com.lefsilva.lridemap

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    object MinimapDisplayMode {
        const val FLOATING_BUTTON = 0
        const val AUTO_DETECT = 1
    }

    companion object {
        private const val TAG = "AppPreferences_Tracker"
        private const val PREFS_NAME = "MapSettings"

        // Chaves
        private const val KEY_ORIGIN_COLOR = "origin_color"
        private const val KEY_DESTINATION_COLOR = "destination_color"
        private const val KEY_LINE_THICKNESS = "line_thickness"
        private const val KEY_MAP_WIDTH = "map_width"
        private const val KEY_MAP_HEIGHT = "map_height"
        private const val KEY_MAP_TYPE = "map_type"
        private const val KEY_MINIMAP_DISPLAY_MODE = "minimap_display_mode"

        // Valores padrão
        private const val DEFAULT_ORIGIN_COLOR = 120f    // Verde (HSV)
        private const val DEFAULT_DESTINATION_COLOR = 270f // Roxo (HSV)
        private const val DEFAULT_LINE_THICKNESS = 10
        private const val DEFAULT_MAP_SIZE = 850
        private const val DEFAULT_MAP_TYPE = 1 // Normal
        private const val DEFAULT_MINIMAP_DISPLAY_MODE = MinimapDisplayMode.FLOATING_BUTTON

        private fun getCurrentUTCTime(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(Date())
        }
    }

    init {
        logPreferencesContent("Initialization")
    }

    private fun logPreferencesContent(tag: String) {
        Log.d(TAG, """
            ===== Preferences Content ($tag) =====
            Time (UTC): ${getCurrentUTCTime()}
            Map Settings:
            - Width: ${mapWidth} (default: ${mapWidth == DEFAULT_MAP_SIZE})
            - Height: ${mapHeight} (default: ${mapHeight == DEFAULT_MAP_SIZE})
            - Type: $mapType (default: ${mapType == DEFAULT_MAP_TYPE})
            
            Colors:
            - Origin: ${originMarkerColor} (default: ${originMarkerColor == DEFAULT_ORIGIN_COLOR})
            - Destination: ${destinationMarkerColor} (default: ${destinationMarkerColor == DEFAULT_DESTINATION_COLOR})
            
            Other:
            - Line Thickness: ${lineThickness} (default: ${lineThickness == DEFAULT_LINE_THICKNESS})
            - Minimap Display Mode: ${minimapDisplayMode} (default: ${minimapDisplayMode == DEFAULT_MINIMAP_DISPLAY_MODE})
            
            Keys Present:
            - Map Width: ${prefs.contains(KEY_MAP_WIDTH)}
            - Map Height: ${prefs.contains(KEY_MAP_HEIGHT)}
            - Map Type: ${prefs.contains(KEY_MAP_TYPE)}
            - Origin Color: ${prefs.contains(KEY_ORIGIN_COLOR)}
            - Destination Color: ${prefs.contains(KEY_DESTINATION_COLOR)}
            - Line Thickness: ${prefs.contains(KEY_LINE_THICKNESS)}
            - Minimap Display Mode: ${prefs.contains(KEY_MINIMAP_DISPLAY_MODE)}
            =====================================
        """.trimIndent())
    }

    var originMarkerColor: Float
        get() = prefs.getFloat(KEY_ORIGIN_COLOR, DEFAULT_ORIGIN_COLOR)
        set(value) {
            val coercedValue = value.coerceIn(0f, 360f)
            prefs.edit().putFloat(KEY_ORIGIN_COLOR, coercedValue).commit()
            logPreferencesContent("After origin color update")
        }

    var destinationMarkerColor: Float
        get() = prefs.getFloat(KEY_DESTINATION_COLOR, DEFAULT_DESTINATION_COLOR)
        set(value) {
            val coercedValue = value.coerceIn(0f, 360f)
            prefs.edit().putFloat(KEY_DESTINATION_COLOR, coercedValue).commit()
            logPreferencesContent("After destination color update")
        }

    var lineThickness: Int
        get() = prefs.getInt(KEY_LINE_THICKNESS, DEFAULT_LINE_THICKNESS)
        set(value) {
            prefs.edit().putInt(KEY_LINE_THICKNESS, value).commit()
            logPreferencesContent("After line thickness update")
        }

    var mapWidth: Int
        get() = prefs.getInt(KEY_MAP_WIDTH, DEFAULT_MAP_SIZE)
        set(value) {
            prefs.edit().putInt(KEY_MAP_WIDTH, value).commit()
            logPreferencesContent("After map width update")
        }

    var mapHeight: Int
        get() = prefs.getInt(KEY_MAP_HEIGHT, DEFAULT_MAP_SIZE)
        set(value) {
            prefs.edit().putInt(KEY_MAP_HEIGHT, value).commit()
            logPreferencesContent("After map height update")
        }

    var mapType: Int
        get() = prefs.getInt(KEY_MAP_TYPE, DEFAULT_MAP_TYPE)
        set(value) {
            prefs.edit().putInt(KEY_MAP_TYPE, value).commit()
            logPreferencesContent("After map type update")
        }

    var minimapDisplayMode: Int
        get() = prefs.getInt(KEY_MINIMAP_DISPLAY_MODE, DEFAULT_MINIMAP_DISPLAY_MODE)
        set(value) {
            prefs.edit().putInt(KEY_MINIMAP_DISPLAY_MODE, value).commit()
            logPreferencesContent("After minimap display mode update")
        }

    fun verifySettings() {
        Log.d(TAG, """
            ===== Settings Full Verification =====
            Time (UTC): ${getCurrentUTCTime()}
            
            Current Values:
            - Map Size: ${mapWidth}x${mapHeight}
            - Map Type: $mapType
            - Origin Color: $originMarkerColor
            - Destination Color: $destinationMarkerColor
            - Line Thickness: $lineThickness
            - Minimap Display Mode: $minimapDisplayMode
            
            Using Defaults:
            - Map Size: ${mapWidth == DEFAULT_MAP_SIZE || mapHeight == DEFAULT_MAP_SIZE}
            - Map Type: ${mapType == DEFAULT_MAP_TYPE}
            - Origin Color: ${originMarkerColor == DEFAULT_ORIGIN_COLOR}
            - Destination Color: ${destinationMarkerColor == DEFAULT_DESTINATION_COLOR}
            - Line Thickness: ${lineThickness == DEFAULT_LINE_THICKNESS}
            - Minimap Display Mode: ${minimapDisplayMode == DEFAULT_MINIMAP_DISPLAY_MODE}
            
            Keys Present:
            - Map Width/Height: ${prefs.contains(KEY_MAP_WIDTH)} / ${prefs.contains(KEY_MAP_HEIGHT)}
            - Map Type: ${prefs.contains(KEY_MAP_TYPE)}
            - Colors: ${prefs.contains(KEY_ORIGIN_COLOR)} / ${prefs.contains(KEY_DESTINATION_COLOR)}
            - Line Thickness: ${prefs.contains(KEY_LINE_THICKNESS)}
            - Minimap Display Mode: ${prefs.contains(KEY_MINIMAP_DISPLAY_MODE)}
            ===================================
        """.trimIndent())
    }
}