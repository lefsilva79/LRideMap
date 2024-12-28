package com.lefsilva.lridemap

import android.Manifest
import android.app.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.graphics.Color
import android.location.Geocoder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import java.io.IOException
import kotlin.concurrent.thread
import android.app.AlertDialog
import android.widget.Button

class FloatingButtonService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingButton: View
    private lateinit var closeButton: View
    private var initialX: Int = 0
    private var initialY: Int = 0
    private var initialTouchX: Float = 0f
    private var initialTouchY: Float = 0f
    private var isCloseBtnShowing = false
    private var miniMapView: MiniMapView? = null

    private val ATTRACTION_THRESHOLD = 300
    private val ATTRACTION_FORCE = 0.6f

    companion object {
        private const val TAG = "FloatingButtonService"
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_CHANNEL_ID = "LRideMap_Channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        setupFloatingButton()
        setupCloseButton()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "LRideMap Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Serviço do LRideMap em execução"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("LRideMap")
            .setContentText("Serviço em execução")
            .setSmallIcon(R.drawable.ic_map)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun setupFloatingButton() {
        floatingButton = LayoutInflater.from(this).inflate(R.layout.layout_floating_button, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }

        windowManager.addView(floatingButton, params)
        setupTouchListener(params)
    }

    private fun setupCloseButton() {
        closeButton = LayoutInflater.from(this).inflate(R.layout.layout_close_button, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = 100
        }

        closeButton.visibility = View.GONE
        windowManager.addView(closeButton, params)

        closeButton.findViewById<ImageView>(R.id.closeButton)?.let { closeImageView ->
            closeImageView.setColorFilter(Color.GRAY)
        }
    }

    private fun setupTouchListener(params: WindowManager.LayoutParams) {
        val button = floatingButton.findViewById<ImageButton>(R.id.floatingButton)

        button.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    showCloseButton()
                    Log.d("FloatingButtonService", "ACTION_DOWN")
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    try {
                        windowManager.updateViewLayout(floatingButton, params)

                        val isNear = isNearCloseButton()
                        Log.d("FloatingButtonService", "Is near close button: $isNear")

                        if (isNear) {
                            applyAttractionForce(params)
                            closeButton.findViewById<ImageView>(R.id.closeButton)?.let { closeImageView ->
                                closeImageView.setColorFilter(Color.RED)
                                closeImageView.scaleX = 1.3f
                                closeImageView.scaleY = 1.3f
                            }
                            closeButton.alpha = 0.7f
                        } else {
                            closeButton.findViewById<ImageView>(R.id.closeButton)?.let { closeImageView ->
                                closeImageView.setColorFilter(Color.GRAY)
                                closeImageView.scaleX = 1.0f
                                closeImageView.scaleY = 1.0f
                            }
                            closeButton.alpha = 1.0f
                        }
                    } catch (e: Exception) {
                        Log.e("FloatingButtonService", "Error in ACTION_MOVE", e)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    Log.d("FloatingButtonService", "ACTION_UP")
                    hideCloseButton()

                    val isNear = isNearCloseButton()
                    Log.d("FloatingButtonService", "Is near close button on UP: $isNear")

                    if (isNear) {
                        Log.d("FloatingButtonService", "Trying to stop service...")
                        try {
                            stopSelf()
                            Log.d("FloatingButtonService", "Service stopped")
                        } catch (e: Exception) {
                            Log.e("FloatingButtonService", "Error stopping service", e)
                        }
                    } else {
                        val moved = Math.abs(event.rawX - initialTouchX) > 5 ||
                                Math.abs(event.rawY - initialTouchY) > 5
                        if (!moved) {
                            handleButtonClick()
                        }
                    }
                    true
                }
                else -> false
            }
        }

        button.setOnClickListener(null)
    }

    private fun isNearCloseButton(): Boolean {
        if (!::closeButton.isInitialized) return false

        val closeLocation = IntArray(2)
        closeButton.getLocationOnScreen(closeLocation)

        val buttonLocation = IntArray(2)
        floatingButton.getLocationOnScreen(buttonLocation)

        val distance = Math.sqrt(
            Math.pow((closeLocation[0] - buttonLocation[0]).toDouble(), 2.0) +
                    Math.pow((closeLocation[1] - buttonLocation[1]).toDouble(), 2.0)
        ).toFloat()

        Log.d("FloatingButtonService", "Distance to close button: $distance") // Adicione este log
        return distance < ATTRACTION_THRESHOLD
    }

    private fun applyAttractionForce(params: WindowManager.LayoutParams) {
        val closeLocation = IntArray(2)
        val buttonLocation = IntArray(2)

        closeButton.getLocationOnScreen(closeLocation)
        floatingButton.getLocationOnScreen(buttonLocation)

        val deltaX = closeLocation[0] - buttonLocation[0]
        val deltaY = closeLocation[1] - buttonLocation[1]

        params.x += (deltaX * ATTRACTION_FORCE).toInt()
        params.y += (deltaY * ATTRACTION_FORCE).toInt()

        try {
            windowManager.updateViewLayout(floatingButton, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showCloseButton() {
        if (!isCloseBtnShowing && ::closeButton.isInitialized) {
            closeButton.visibility = View.VISIBLE
            isCloseBtnShowing = true
        }
    }

    private fun hideCloseButton() {
        if (isCloseBtnShowing && ::closeButton.isInitialized) {
            closeButton.visibility = View.GONE
            isCloseBtnShowing = false

            closeButton.findViewById<ImageView>(R.id.closeButton)?.let { closeImageView ->
                closeImageView.setColorFilter(Color.GRAY)
                closeImageView.scaleX = 1.0f
                closeImageView.scaleY = 1.0f
            }
        }
    }

    private fun handleButtonClick() {
        val address = LyftAddressAccessibilityService.detectAddressNow()
        if (address.isNotEmpty()) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("address", address)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Endereço copiado: $address", Toast.LENGTH_SHORT).show()

            thread {
                val destinationCoords = getCoordinatesFromAddress(address)
                if (destinationCoords != null) {
                    if (checkLocationPermission()) {
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                val currentLocation = LatLng(location.latitude, location.longitude)
                                Handler(Looper.getMainLooper()).post {
                                    showMiniMap(currentLocation, destinationCoords)
                                }
                            } else {
                                Toast.makeText(this,
                                    "Não foi possível obter sua localização atual",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this,
                            "Permissão de localização necessária",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "Nenhum endereço detectado na tela atual", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMiniMap(current: LatLng, destination: LatLng) {
        try {
            val miniMap = MiniMapView(this)
            miniMapView = miniMap

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.CENTER
                width = resources.displayMetrics.widthPixels - 32
                height = (resources.displayMetrics.heightPixels * 0.7).toInt()
            }

            miniMap.setOnCloseClickListener {
                windowManager.removeView(miniMap)
                miniMapView = null
            }


            windowManager.addView(miniMap, params)
            miniMap.showRoute(current, destination)
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao mostrar mini mapa", e)
        }
    }


    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getCoordinatesFromAddress(address: String): LatLng? {
        try {
            val geocoder = Geocoder(this)
            val results = geocoder.getFromLocationName(address, 1)
            if (!results.isNullOrEmpty()) {
                val location = results[0]
                val streetNumber = address.split(" ").firstOrNull()?.toIntOrNull()
                val resultNumber = location.getAddressLine(0)?.split(" ")?.firstOrNull()?.toIntOrNull()

                if (streetNumber != null && resultNumber != null) {
                    if (Math.abs(streetNumber - resultNumber) > 2) {
                        return null
                    }
                }
                return LatLng(location.latitude, location.longitude)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onDestroy() {
        Log.d("FloatingButtonService", "onDestroy called")
        try {
            if (::floatingButton.isInitialized) {
                windowManager.removeView(floatingButton)
            }
            if (::closeButton.isInitialized) {
                windowManager.removeView(closeButton)
            }
            miniMapView?.let {
                windowManager.removeView(it)
            }
        } catch (e: Exception) {
            Log.e("FloatingButtonService", "Error in onDestroy", e)
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopForeground(true)
        stopSelf()
    }

}