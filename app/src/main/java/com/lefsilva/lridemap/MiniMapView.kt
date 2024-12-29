package com.lefsilva.lridemap

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import android.view.LayoutInflater
import com.google.android.material.slider.Slider
import androidx.appcompat.app.AlertDialog
import android.widget.SeekBar
import android.widget.Toast
import android.content.Intent


private const val TAG = "MiniMapView"

class MiniMapView(context: Context) : FrameLayout(context) {
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var preferences: AppPreferences
    private var onCloseListener: (() -> Unit)? = null
    private var onSettingsListener: (() -> Unit)? = null
    private lateinit var distanceText: TextView
    private lateinit var timeText: TextView
    private var isMapReady = false
    private var savedInstanceState: Bundle? = null
    private lateinit var lastCurrentLocation: LatLng
    private lateinit var lastDestination: LatLng


    init {
        Log.d(TAG, "Initializing MiniMapView")
        try {
            // Inicializa o MapsInitializer de forma assíncrona
            MapsInitializer.initialize(context.applicationContext, MapsInitializer.Renderer.LATEST) { }

            inflate(context, R.layout.mini_map_layout, this)

            // Inicializa as preferências após o inflate
            preferences = AppPreferences(context)

            // Inicializa as views
            initializeViews()

            // Verifica disponibilidade do Google Play Services e inicializa o mapa
            checkGooglePlayServicesAndInitMap()

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MiniMapView", e)
        }
    }

    private fun initializeViews() {
        try {
            // Inicializa o MapView
            mapView = findViewById(R.id.mini_map_view)
            savedInstanceState = Bundle()
            mapView.onCreate(savedInstanceState)
            mapView.onStart()
            mapView.onResume()

            // Configura os botões
            findViewById<ImageButton>(R.id.close_button).setOnClickListener {
                Log.d(TAG, "Close button clicked")
                onCloseListener?.invoke()
            }

            // Configura o botão de configurações para abrir a SettingsActivity
            findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
                Log.d(TAG, "Settings button clicked")

                // Cria e inicia a SettingsActivity
                val intent = Intent(context, SettingsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)

                // Após retornar das configurações, atualiza a rota se existir
                if (::lastCurrentLocation.isInitialized && ::lastDestination.isInitialized) {
                    showRoute(lastCurrentLocation, lastDestination)
                }
            }

            Log.d(TAG, "Views initialized")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
        }
    }

    private fun checkGooglePlayServicesAndInitMap() {
        val availability = GoogleApiAvailability.getInstance()
        val result = availability.isGooglePlayServicesAvailable(context)

        if (result == com.google.android.gms.common.ConnectionResult.SUCCESS) {
            Log.d(TAG, "Google Play Services is available")
            initializeMap()
        } else {
            Log.e(TAG, "Google Play Services is NOT available: $result")
        }
    }

    fun setOnSettingsClickListener(listener: () -> Unit) {
        onSettingsListener = listener
        Log.d(TAG, "Settings listener set")
    }

    private fun initializeMap() {
        try {
            mapView.getMapAsync { map ->
                Log.d(TAG, "Initial map async callback received")
                googleMap = map
                isMapReady = true
                map.apply {
                    mapType = GoogleMap.MAP_TYPE_NORMAL
                    uiSettings.apply {
                        isZoomControlsEnabled = true
                        isZoomGesturesEnabled = true
                        isScrollGesturesEnabled = true
                    }
                }
                Log.d(TAG, "Map initially configured")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in initial map setup", e)
        }
    }

    fun setOnCloseClickListener(listener: () -> Unit) {
        onCloseListener = listener
        Log.d(TAG, "Close listener set")
    }

    /**
     * Shows the route between two points on the map
     * Created by lefsilva79
     * Date: 2024-12-28 07:52:43 UTC
     */
    fun showRoute(current: LatLng, destination: LatLng) {
        Log.d(TAG, "showRoute called with current: $current, destination: $destination")
        try {
            // Salva as localizações para uso posterior
            lastCurrentLocation = current
            lastDestination = destination

            if (!::mapView.isInitialized) {
                Log.e(TAG, "MapView not initialized")
                return
            }

            if (!isMapReady) {
                Log.d(TAG, "Map not ready, waiting for initialization")
                mapView.getMapAsync { map ->
                    googleMap = map
                    isMapReady = true
                    displayRoute(current, destination)
                }
            } else {
                displayRoute(current, destination)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error showing route", e)
        }
    }

    private fun displayRoute(current: LatLng, destination: LatLng) {
        try {
            Log.d(TAG, "Displaying route on map")

            // Configura o mapa
            googleMap.apply {
                clear() // Limpa marcadores anteriores
                mapType = GoogleMap.MAP_TYPE_NORMAL
                uiSettings.apply {
                    isZoomControlsEnabled = true
                    isZoomGesturesEnabled = true
                    isScrollGesturesEnabled = true
                }
            }

            // Adiciona o marcador de origem
            googleMap.addMarker(
                MarkerOptions()
                    .position(current)
                    .title("Origem")
                    .icon(BitmapDescriptorFactory.defaultMarker(preferences.originMarkerColor))
            )

            // Adiciona o marcador de destino
            googleMap.addMarker(
                MarkerOptions()
                    .position(destination)
                    .title("Destino")
                    .icon(BitmapDescriptorFactory.defaultMarker(preferences.destinationMarkerColor))
            )

            // Desenha a linha entre os pontos usando a espessura definida nas preferências
            googleMap.addPolyline(
                PolylineOptions()
                    .add(current, destination)
                    .width(preferences.lineThickness)
                    .color(Color.BLUE)
            )

            // Ajusta a câmera para mostrar os dois pontos
            val bounds = LatLngBounds.Builder()
                .include(current)
                .include(destination)
                .build()

            val padding = 300 // O padding que estava funcionando antes

            // Move a câmera para mostrar os bounds com padding
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))

            // Aplica zoom inicial
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(11f))

            // Calcula e mostra a distância
            calculateDistanceAndTime(current, destination)

        } catch (e: Exception) {
            Log.e(TAG, "Error displaying route", e)
        }
    }


    private fun calculateDistanceAndTime(current: LatLng, destination: LatLng) {
        try {
            val results = FloatArray(1)
            android.location.Location.distanceBetween(
                current.latitude, current.longitude,
                destination.latitude, destination.longitude,
                results
            )

            // Converter metros para milhas (1 milha = 1609.34 metros)
            val distanceInMiles = results[0] / 1609.34
            // Tempo estimado considerando velocidade média de 30 mph
            val timeInMinutes = (distanceInMiles / 30.0) * 60

            distanceText.text = "Distância: %.2f mi".format(distanceInMiles)
            timeText.text = "Tempo estimado: %.0f min".format(timeInMinutes)
            Log.d(TAG, "Distance calculation completed: $distanceInMiles miles, $timeInMinutes min")
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating distance and time", e)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        try {
            if (::mapView.isInitialized) {
                mapView.onStart()
                mapView.onResume()
                Log.d(TAG, "MapView resumed on attach")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error resuming MapView", e)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        try {
            if (::mapView.isInitialized) {
                mapView.onPause()
                mapView.onStop()
                mapView.onDestroy()
                Log.d(TAG, "MapView destroyed on detach")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying MapView", e)
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        mapView.onSaveInstanceState(outState)
    }

    fun onLowMemory() {
        mapView.onLowMemory()
        Log.d(TAG, "MapView low memory")
    }

    // No MiniMapView.kt
    fun getOriginMarkerColor(): Float {
        return preferences.originMarkerColor
    }

    fun getDestinationMarkerColor(): Float {
        return preferences.destinationMarkerColor
    }

    fun setMarkerColors(originColor: Float, destinationColor: Float) {
        preferences.apply {
            originMarkerColor = originColor
            destinationMarkerColor = destinationColor
        }
    }
}