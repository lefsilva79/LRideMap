package com.lefsilva.lridemap

import android.content.Context
import com.google.android.gms.maps.model.BitmapDescriptorFactory

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Mantém as propriedades existentes
    var originMarkerColor: Float
        get() = prefs.getFloat(KEY_ORIGIN_COLOR, BitmapDescriptorFactory.HUE_RED)
        set(value) = prefs.edit().putFloat(KEY_ORIGIN_COLOR, value).apply()

    var destinationMarkerColor: Float
        get() = prefs.getFloat(KEY_DESTINATION_COLOR, BitmapDescriptorFactory.HUE_GREEN)
        set(value) = prefs.edit().putFloat(KEY_DESTINATION_COLOR, value).apply()

    // Adiciona a nova configuração para espessura da linha
    var lineThickness: Float
        get() = prefs.getFloat(KEY_LINE_THICKNESS, 5f) // valor padrão de 5
        set(value) = prefs.edit().putFloat(KEY_LINE_THICKNESS, value).apply()

    companion object {
        private const val PREFS_NAME = "LRideMapPreferences"
        private const val KEY_ORIGIN_COLOR = "origin_color"
        private const val KEY_DESTINATION_COLOR = "destination_color"
        private const val KEY_LINE_THICKNESS = "line_thickness" // nova chave
    }

}