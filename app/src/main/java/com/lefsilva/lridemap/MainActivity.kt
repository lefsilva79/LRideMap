package com.lefsilva.lridemap

import android.Manifest
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.lefsilva.lridemap.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private val OVERLAY_PERMISSION_REQUEST_CODE = 1234
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            checkAndStartService()
        } else {
            Toast.makeText(
                this,
                "Permissões de localização são necessárias para o funcionamento completo",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInitialState()
        checkAndStartService()
    }

    private fun setupInitialState() {
        // Configura os listeners dos botões
        binding.accessibilityButton.setOnClickListener { openAccessibilitySettings() }
        binding.locationButton.setOnClickListener { requestLocationPermissions() }
        binding.overlayButton.setOnClickListener { requestOverlayPermission() }

        // Define os checkboxes como não clicáveis
        binding.accessibilityCheck.isClickable = false
        binding.locationCheck.isClickable = false
        binding.overlayCheck.isClickable = false
    }

    private fun setupRecyclerView() {
        val services = listOf(
            ServiceItem(
                name = "Lyft Ride Map",
                iconResId = R.drawable.ic_map,
                isEnabled = isServiceRunning(FloatingButtonService::class.java)
            )
        )

        val adapter = ServiceAdapter(
            this,
            services,
            onServiceToggle = { serviceName, isEnabled ->
                when (serviceName) {
                    "Lyft Ride Map" -> {
                        toggleFloatingButtonService(isEnabled)
                    }
                }
            },
            onSettingsClick = { serviceName ->
                when (serviceName) {
                    "Lyft Ride Map" -> {
                        val intent = Intent(this, SettingsActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            this.adapter = adapter
        }
    }

    private fun toggleFloatingButtonService(enabled: Boolean) {
        val serviceIntent = Intent(this, FloatingButtonService::class.java)
        if (enabled) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
            Toast.makeText(this, "Serviço iniciado", Toast.LENGTH_SHORT).show()
        } else {
            stopService(serviceIntent)
            Toast.makeText(this, "Serviço parado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun areAllPermissionsGranted(): Boolean {
        return isAccessibilityServiceEnabled() &&
                Settings.canDrawOverlays(this) &&
                areLocationPermissionsGranted()
    }

    private fun areLocationPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestLocationPermissions() {
        requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        val serviceName = "$packageName/.LyftAddressAccessibilityService"
        return enabledServices.any { it.id.contains(serviceName) }
    }

    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
            Toast.makeText(
                this,
                "Por favor, ative o serviço 'LRideMap' nas configurações de acessibilidade",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Erro ao abrir configurações de acessibilidade",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE)
    }

    private fun checkAndStartService() {
        if (areAllPermissionsGranted()) {
            binding.setupCard.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            setupRecyclerView()
        } else {
            binding.setupCard.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
            updateCheckboxStates()
        }
    }

    private fun updateCheckboxStates() {
        binding.apply {
            accessibilityCheck.isChecked = isAccessibilityServiceEnabled()
            locationCheck.isChecked = areLocationPermissionsGranted()
            overlayCheck.isChecked = Settings.canDrawOverlays(this@MainActivity)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            checkAndStartService()
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndStartService()
        updateServiceState()
    }

    private fun updateServiceState() {
        if (binding.recyclerView.adapter is ServiceAdapter) {
            val services = listOf(
                ServiceItem(
                    name = "Lyft Ride Map",
                    iconResId = R.drawable.ic_map,
                    isEnabled = isServiceRunning(FloatingButtonService::class.java)
                )
            )
            binding.recyclerView.adapter = ServiceAdapter(
                this,
                services,
                onServiceToggle = { serviceName, isEnabled ->
                    when (serviceName) {
                        "Lyft Ride Map" -> {
                            toggleFloatingButtonService(isEnabled)
                        }
                    }
                },
                onSettingsClick = { serviceName ->
                    when (serviceName) {
                        "Lyft Ride Map" -> {
                            val intent = Intent(this, SettingsActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
            )
        }
    }
}