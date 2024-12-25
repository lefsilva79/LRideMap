package com.lefsilva.lridemap

import android.content.Context
import android.graphics.Color
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MiniMapView(context: Context) : FrameLayout(context) {
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private var onCloseListener: (() -> Unit)? = null
    private lateinit var distanceText: TextView
    private lateinit var timeText: TextView

    init {
        inflate(context, R.layout.mini_map_layout, this)

        mapView = findViewById(R.id.mini_map_view)
        mapView.onCreate(null)

        distanceText = findViewById(R.id.distance_text)
        timeText = findViewById(R.id.time_text)

        findViewById<ImageButton>(R.id.close_button).setOnClickListener {
            onCloseListener?.invoke()
        }
    }

    fun setOnCloseClickListener(listener: () -> Unit) {
        onCloseListener = listener
    }

    fun showRoute(current: LatLng, destination: LatLng) {
        mapView.getMapAsync { map ->
            googleMap = map

            // Configura o mapa
            map.apply {
                mapType = GoogleMap.MAP_TYPE_NORMAL
                uiSettings.apply {
                    isZoomControlsEnabled = true
                    isZoomGesturesEnabled = true
                    isScrollGesturesEnabled = true
                }
            }

            // Adiciona marcadores
            map.addMarker(MarkerOptions().position(current).title("Origem"))
            map.addMarker(MarkerOptions().position(destination).title("Destino"))

            // Desenha a linha
            map.addPolyline(PolylineOptions()
                .add(current, destination)
                .width(5f)
                .color(Color.BLUE))

            // Ajusta a câmera
            val bounds = LatLngBounds.Builder()
                .include(current)
                .include(destination)
                .build()

            map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))

            // Calcula e mostra a distância
            calculateDistanceAndTime(current, destination)
        }
    }

    private fun calculateDistanceAndTime(current: LatLng, destination: LatLng) {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            current.latitude, current.longitude,
            destination.latitude, destination.longitude,
            results
        )

        val distanceInKm = results[0] / 1000
        val timeInMinutes = (distanceInKm / 50.0) * 60 // Estimativa baseada em velocidade média de 50km/h

        distanceText.text = "Distância: %.2f km".format(distanceInKm)
        timeText.text = "Tempo estimado: %.0f min".format(timeInMinutes)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mapView.onResume()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mapView.onPause()
        mapView.onDestroy()
    }
}