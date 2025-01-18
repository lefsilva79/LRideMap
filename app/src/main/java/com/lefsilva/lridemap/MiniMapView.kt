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
import android.view.View
import android.content.Intent
import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.*

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
    private val handler = Handler(Looper.getMainLooper())
    private val hideRunnable = Runnable {
        visibility = View.GONE
    }

    companion object {
        private const val TAG = "MiniMapView"

        private fun getCurrentUTCTime(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(Date())
        }
    }

    init {
        Log.d(TAG, """MiniMapView Initialization
            Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): ${getCurrentUTCTime()}
        """.trimIndent())

        try {
            MapsInitializer.initialize(context.applicationContext, MapsInitializer.Renderer.LATEST) { }
            inflate(context, R.layout.mini_map_layout, this)
            preferences = AppPreferences(context)
            initializeViews()
            checkGooglePlayServicesAndInitMap()
            updateVisibility()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MiniMapView", e)
        }
    }

    private fun initializeViews() {
        try {
            Log.d(TAG, """Initializing views
                Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): ${getCurrentUTCTime()}
            """.trimIndent())

            mapView = findViewById(R.id.mini_map_view)
            savedInstanceState = Bundle()
            mapView.onCreate(savedInstanceState)
            mapView.onStart()
            mapView.onResume()

            findViewById<ImageButton>(R.id.close_button).setOnClickListener {
                Log.d(TAG, """Close button clicked
                    Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): ${getCurrentUTCTime()}
                """.trimIndent())
                onCloseListener?.invoke()
            }

            findViewById<ImageButton>(R.id.settings_button).setOnClickListener {
                Log.d(TAG, """Settings button clicked
                    Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): ${getCurrentUTCTime()}
                """.trimIndent())
                val intent = Intent(context, SettingsActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }

            Log.d(TAG, "Views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views", e)
        }
    }

    private fun checkGooglePlayServicesAndInitMap() {
        val availability = GoogleApiAvailability.getInstance()
        val result = availability.isGooglePlayServicesAvailable(context)

        if (result == com.google.android.gms.common.ConnectionResult.SUCCESS) {
            Log.d(TAG, """Google Play Services is available
                Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): ${getCurrentUTCTime()}
            """.trimIndent())
            initializeMap()
        } else {
            Log.e(TAG, """Google Play Services is NOT available: $result
                Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): ${getCurrentUTCTime()}
            """.trimIndent())
        }
    }

    private fun updateVisibility() {
        when (preferences.minimapDisplayMode) {
            AppPreferences.MinimapDisplayMode.AUTO_DETECT -> {
                visibility = View.VISIBLE
                findViewById<ImageButton>(R.id.close_button)?.visibility = View.GONE
                // No modo AUTO_DETECT, não queremos que suma após 5s
                handler.removeCallbacks(hideRunnable)
            }
            AppPreferences.MinimapDisplayMode.FLOATING_BUTTON -> {
                visibility = View.VISIBLE
                findViewById<ImageButton>(R.id.close_button)?.visibility = View.VISIBLE
                // No modo FLOATING_BUTTON, mantém o comportamento de sumir após 5s
                handler.removeCallbacks(hideRunnable)
                handler.postDelayed(hideRunnable, 5000)
            }
        }

        Log.d(TAG, """Visibility updated
            Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): ${getCurrentUTCTime()}
            Display Mode: ${if (preferences.minimapDisplayMode == AppPreferences.MinimapDisplayMode.AUTO_DETECT) "Auto Detect" else "Floating Button"}
            Visibility: ${if (visibility == View.VISIBLE) "Visible" else "Gone"}
        """.trimIndent())
    }



    fun setOnSettingsClickListener(listener: () -> Unit) {
        onSettingsListener = listener
        Log.d(TAG, """Settings listener set
            Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): ${getCurrentUTCTime()}
        """.trimIndent())
    }

    private fun initializeMap() {
        try {
            mapView.getMapAsync { map ->
                Log.d(TAG, """Initial map async callback received
                    Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): ${getCurrentUTCTime()}
                """.trimIndent())
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
        Log.d(TAG, """Close listener set
            Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): ${getCurrentUTCTime()}
        """.trimIndent())
    }

    fun showRoute(current: LatLng, destination: LatLng) {
        Log.d(TAG, """showRoute called
            Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): ${getCurrentUTCTime()}
            Current Location: $current
            Destination: $destination
        """.trimIndent())

        try {
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

            updateVisibility()

        } catch (e: Exception) {
            Log.e(TAG, "Error showing route", e)
        }
    }

    private fun displayRoute(current: LatLng, destination: LatLng) {
        try {
            Log.d(TAG, """Displaying route
                Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): ${getCurrentUTCTime()}
                Settings:
                - Origin color: ${preferences.originMarkerColor}
                - Destination color: ${preferences.destinationMarkerColor}
                - Line thickness: ${preferences.lineThickness}
                - Map type: ${preferences.mapType}
            """.trimIndent())

            googleMap.clear()

            googleMap.addMarker(
                MarkerOptions()
                    .position(current)
                    .title("Origem")
                    .icon(BitmapDescriptorFactory.defaultMarker(preferences.originMarkerColor))
            )

            googleMap.addMarker(
                MarkerOptions()
                    .position(destination)
                    .title("Destino")
                    .icon(BitmapDescriptorFactory.defaultMarker(preferences.destinationMarkerColor))
            )

            googleMap.addPolyline(
                PolylineOptions()
                    .add(current, destination)
                    .width(preferences.lineThickness.toFloat())
                    .color(Color.BLUE)
            )

            val bounds = LatLngBounds.Builder()
                .include(current)
                .include(destination)
                .build()

            val padding = 300
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(10f))

        } catch (e: Exception) {
            Log.e(TAG, "Error displaying route", e)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        try {
            if (::mapView.isInitialized) {
                mapView.onStart()
                mapView.onResume()
                Log.d(TAG, """MapView resumed on attach
                    Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): ${getCurrentUTCTime()}
                """.trimIndent())
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
            }
            // Importante: remover callbacks pendentes ao destruir a view
            handler.removeCallbacks(hideRunnable)
            Log.d(TAG, """MapView destroyed on detach
                Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): ${getCurrentUTCTime()}
            """.trimIndent())
        } catch (e: Exception) {
            Log.e(TAG, "Error destroying MapView", e)
        }
    }


    fun updateMapSettings() {
        Log.d(TAG, """Updating map settings
            Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): ${getCurrentUTCTime()}
            Settings:
            - Map Type: ${preferences.mapType}
            - Minimap Mode: ${if (preferences.minimapDisplayMode == AppPreferences.MinimapDisplayMode.AUTO_DETECT) "Auto Detect" else "Floating Button"}
        """.trimIndent())

        if (::googleMap.isInitialized) {
            try {
                val mapType = when (preferences.mapType) {
                    MapType.SATELLITE.value -> GoogleMap.MAP_TYPE_SATELLITE
                    MapType.HYBRID.value -> GoogleMap.MAP_TYPE_HYBRID
                    else -> GoogleMap.MAP_TYPE_NORMAL
                }
                googleMap.mapType = mapType
                updateVisibility()

                if (::lastCurrentLocation.isInitialized && ::lastDestination.isInitialized) {
                    displayRoute(lastCurrentLocation, lastDestination)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating map settings", e)
            }
        } else {
            Log.d(TAG, "Google Map not initialized yet")
        }
    }

    fun onSaveInstanceState(outState: Bundle) {
        mapView.onSaveInstanceState(outState)
    }

    fun onLowMemory() {
        mapView.onLowMemory()
        Log.d(TAG, """MapView low memory
            Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): ${getCurrentUTCTime()}
        """.trimIndent())
    }

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
        Log.d(TAG, """Marker colors updated
            Current Date and Time (UTC - YYYY-MM-DD HH:MM:SS formatted): ${getCurrentUTCTime()}
            Colors:
            - Origin: $originColor
            - Destination: $destinationColor
        """.trimIndent())
    }
}