package com.lefsilva.lridemap

import android.content.Context
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Marker Colors
    var originMarkerColor: Float
        get() = prefs.getFloat(KEY_ORIGIN_COLOR, BitmapDescriptorFactory.HUE_RED)
        set(value) = prefs.edit().putFloat(KEY_ORIGIN_COLOR, value).apply()

    var destinationMarkerColor: Float
        get() = prefs.getFloat(KEY_DESTINATION_COLOR, BitmapDescriptorFactory.HUE_GREEN)
        set(value) = prefs.edit().putFloat(KEY_DESTINATION_COLOR, value).apply()

    // Line Thickness
    var lineThickness: Float
        get() = prefs.getFloat(KEY_LINE_THICKNESS, 5f)
        set(value) = prefs.edit().putFloat(KEY_LINE_THICKNESS, value).apply()

    // Map Size
    var mapWidth: Int
        get() = prefs.getInt(KEY_MAP_WIDTH, 850)
        set(value) = prefs.edit().putInt(KEY_MAP_WIDTH, value).apply()

    var mapHeight: Int
        get() = prefs.getInt(KEY_MAP_HEIGHT, 850)
        set(value) = prefs.edit().putInt(KEY_MAP_HEIGHT, value).apply()

    // Map Zoom
    var defaultZoom: Float
        get() = prefs.getFloat(KEY_DEFAULT_ZOOM, 16f)
        set(value) = prefs.edit().putFloat(KEY_DEFAULT_ZOOM, value).apply()

    companion object {
        private const val PREFS_NAME = "LRideMapPreferences"
        private const val KEY_ORIGIN_COLOR = "origin_color"
        private const val KEY_DESTINATION_COLOR = "destination_color"
        private const val KEY_LINE_THICKNESS = "line_thickness"
        private const val KEY_MAP_WIDTH = "map_width"
        private const val KEY_MAP_HEIGHT = "map_height"
        private const val KEY_DEFAULT_ZOOM = "default_zoom"
    }
}