package com.lefsilva.lridemap

import android.content.Context
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("map_preferences", Context.MODE_PRIVATE)

    var originMarkerColor: Float
        get() = prefs.getFloat("origin_marker_color", BitmapDescriptorFactory.HUE_GREEN)
        set(value) = prefs.edit().putFloat("origin_marker_color", value).apply()

    var destinationMarkerColor: Float
        get() = prefs.getFloat("destination_marker_color", BitmapDescriptorFactory.HUE_RED)
        set(value) = prefs.edit().putFloat("destination_marker_color", value).apply()
}