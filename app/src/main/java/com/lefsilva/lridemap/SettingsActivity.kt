package com.lefsilva.lridemap

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val TAG = "Settings_ColorTracker"

class SettingsActivity : AppCompatActivity() {
    private lateinit var preferences: AppPreferences
    private lateinit var originColorGroup: RadioGroup
    private lateinit var destinationColorGroup: RadioGroup
    private lateinit var lineWidthGroup: RadioGroup
    private lateinit var mapTypeGroup: RadioGroup
    private lateinit var mapSizeSeekBar: SeekBar
    private lateinit var mapSizeText: TextView
    private lateinit var saveButton: Button

    companion object {
        const val DEFAULT_MAP_SIZE = 850

        private fun getCurrentUTCTime(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.format(Date())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "======= Settings Activity Started =======")
        Log.d(TAG, "onCreate called at ${System.currentTimeMillis()}")
        setContentView(R.layout.activity_settings)

        preferences = AppPreferences(this)
        initializeViews()
        setupUI()
        loadSavedSettings()
        setupSaveButton()
    }

    private fun initializeViews() {
        Log.d(TAG, "Initializing views...")
        originColorGroup = findViewById(R.id.originColorGroup)
        destinationColorGroup = findViewById(R.id.destinationColorGroup)
        lineWidthGroup = findViewById(R.id.lineWidthGroup)
        mapTypeGroup = findViewById(R.id.mapTypeGroup)
        mapSizeSeekBar = findViewById(R.id.mapSizeSeekBar)
        mapSizeText = findViewById(R.id.mapSizeText)
        saveButton = findViewById(R.id.saveButton)
        Log.d(TAG, "Views initialized successfully")
    }

    private fun setupUI() {
        Log.d(TAG, "Setting up UI components...")

        // Configurar grupos de cores
        Log.d(TAG, "Configuring origin color group...")
        setupColorGroup(originColorGroup) { color ->
            Log.d(TAG, "Origin color change - Selected: ${color.name}, Value: ${color.colorValue}")
            val previousColor = preferences.originMarkerColor
            preferences.originMarkerColor = color.colorValue
            Log.d(TAG, "Origin color updated - Previous: $previousColor, New: ${color.colorValue}")
        }

        Log.d(TAG, "Configuring destination color group...")
        setupColorGroup(destinationColorGroup) { color ->
            Log.d(
                TAG,
                "Destination color change attempt - Selected: ${color.name}, Value: ${color.colorValue}"
            )
            val previousColor = preferences.destinationMarkerColor
            preferences.destinationMarkerColor = color.colorValue
            Log.d(
                TAG,
                "Destination color update verification - Previous: $previousColor, New: ${color.colorValue}"
            )

            // Verificação adicional após a mudança
            val savedColor = preferences.destinationMarkerColor
            if (savedColor != color.colorValue) {
                Log.e(
                    TAG,
                    "Destination color mismatch - Expected: ${color.colorValue}, Actual: $savedColor"
                )
            }
        }

        // Configurar espessura da linha
        lineWidthGroup.setOnCheckedChangeListener { _, checkedId ->
            val thickness = when (checkedId) {
                R.id.thinLine -> 5
                R.id.thickLine -> 15
                else -> 10
            }
            Log.d(TAG, "Line thickness changed to: $thickness")
            preferences.lineThickness = thickness
        }

        // Configurar tipo de mapa
        mapTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            val mapType = when (checkedId) {
                R.id.satelliteMapType -> MapType.SATELLITE.value
                R.id.hybridMapType -> MapType.HYBRID.value
                else -> MapType.NORMAL.value
            }
            Log.d(TAG, "Map type changed to: $mapType")
            preferences.mapType = mapType
        }

        // Configurar tamanho do mapa
        mapSizeSeekBar.apply {
            max = 100
            val initialProgress =
                ((preferences.mapWidth.toFloat() / DEFAULT_MAP_SIZE) * 100).toInt()
            Log.d(TAG, "Initial map size progress: $initialProgress")
            progress = initialProgress

            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val size = (DEFAULT_MAP_SIZE * (progress / 100f)).toInt()
                    mapSizeText.text = "$progress% ($size x $size)"
                    if (fromUser) {
                        Log.d(TAG, "Map size changed to: $size")
                        preferences.mapWidth = size
                        preferences.mapHeight = size
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    Log.d(TAG, "Started tracking map size")
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    Log.d(TAG, "Stopped tracking map size")
                }
            })
        }
        Log.d(TAG, "UI setup completed")
    }

    private fun setupSaveButton() {
        saveButton.setOnClickListener {
            Log.d(TAG, """Pre-Save Configuration Values
Current Date and Time (UTC): ${getCurrentUTCTime()}

----- Configuration Values -----
Map Settings:
- Size: ${preferences.mapWidth}x${preferences.mapHeight}
- Type: ${MapType.fromValue(preferences.mapType).name}

Color Settings:
- Origin: ${MarkerColor.fromColorValue(preferences.originMarkerColor).name} (${preferences.originMarkerColor})
- Destination: ${MarkerColor.fromColorValue(preferences.destinationMarkerColor).name} (${preferences.destinationMarkerColor})

Other Settings:
- Line Thickness: ${preferences.lineThickness}
=========================================="""
            )

            // Verificar configurações
            preferences.verifySettings()

            // Broadcast para atualizar o mapa
            val intent = Intent(Constants.ACTION_UPDATE_MAP_SETTINGS)
            sendBroadcast(intent)

            Log.d(TAG, """Final Settings Verification
Current Date and Time (UTC): ${getCurrentUTCTime()}

----- Configuration Values -----
Map Settings:
- Size: ${preferences.mapWidth}x${preferences.mapHeight}
- Type: ${MapType.fromValue(preferences.mapType).name}

Color Settings:
- Origin: ${MarkerColor.fromColorValue(preferences.originMarkerColor).name} (${preferences.originMarkerColor})
- Destination: ${MarkerColor.fromColorValue(preferences.destinationMarkerColor).name} (${preferences.destinationMarkerColor})

Other Settings:
- Line Thickness: ${preferences.lineThickness}
=========================================="""
            )

            Toast.makeText(this, "Configurações salvas com sucesso!", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun setupColorGroup(group: RadioGroup, onColorSelected: (MarkerColor) -> Unit) {
        Log.d(
            TAG,
            "Setting up color group: ${if (group.id == R.id.originColorGroup) "Origin" else "Destination"}"
        )
        group.removeAllViews()

        MarkerColor.values().forEach { color ->
            val radioButton = RadioButton(this).apply {
                text = color.name.toLowerCase().capitalize()
                id = View.generateViewId()
                layoutParams = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                )
            }
            group.addView(radioButton)
            Log.d(
                TAG,
                "Added radio button for ${if (group.id == R.id.originColorGroup) "Origin" else "Destination"} color: ${color.name}"
            )
        }

        group.setOnCheckedChangeListener { radioGroup, checkedId ->
            val selectedButton = radioGroup.findViewById<RadioButton>(checkedId)
            val colorName = selectedButton.text.toString().toUpperCase()
            val color = MarkerColor.valueOf(colorName)
            Log.d(
                TAG,
                "${if (group.id == R.id.originColorGroup) "Origin" else "Destination"} color selected: ${color.name}"
            )
            onColorSelected(color)
        }
    }

    private fun loadSavedSettings() {
        Log.d(
            TAG, """
        ======= Loading Saved Settings =======
        Time (UTC): ${getCurrentUTCTime()}
        
        --- Starting Configuration Load ---
    """.trimIndent()
        )

        // Carregar cores salvas
        val originColor = MarkerColor.fromColorValue(preferences.originMarkerColor)
        val destinationColor = MarkerColor.fromColorValue(preferences.destinationMarkerColor)

        Log.d(
            TAG, """
    Color Configuration:
    Origin Marker:
    - Color Name: ${originColor.name}
    - HSV Value: ${originColor.colorValue}
    
    Destination Marker:
    - Color Name: ${destinationColor.name}
    - HSV Value: ${destinationColor.colorValue}
""".trimIndent()
        )

        // Selecionar cores nos grupos
        selectColorInGroup(originColorGroup, originColor)
        selectColorInGroup(destinationColorGroup, destinationColor)

        // Verificar se as cores foram selecionadas corretamente
        val selectedOriginColor = preferences.originMarkerColor
        val selectedDestinationColor = preferences.destinationMarkerColor

        Log.d(
            TAG, """
        Color Selection Verification:
        Origin Marker:
        - Expected HSV: ${originColor.colorValue}
        - Actual HSV: $selectedOriginColor
        - Match: ${originColor.colorValue == selectedOriginColor}
        
        Destination Marker:
        - Expected HSV: ${destinationColor.colorValue}
        - Actual HSV: $selectedDestinationColor
        - Match: ${destinationColor.colorValue == selectedDestinationColor}
    """.trimIndent()
        )

        // Carregar espessura da linha
        val lineButtonId = when (preferences.lineThickness) {
            5 -> R.id.thinLine
            15 -> R.id.thickLine
            else -> R.id.mediumLine
        }
        lineWidthGroup.check(lineButtonId)
        Log.d(
            TAG, """
        Line Configuration:
        - Thickness: ${preferences.lineThickness}
        - Button ID: $lineButtonId
    """.trimIndent()
        )

        // Carregar tipo de mapa
        val mapTypeButtonId = when (MapType.fromValue(preferences.mapType)) {
            MapType.SATELLITE -> R.id.satelliteMapType
            MapType.HYBRID -> R.id.hybridMapType
            else -> R.id.normalMapType
        }
        mapTypeGroup.check(mapTypeButtonId)
        Log.d(
            TAG, """
        Map Type Configuration:
        - Type: ${MapType.fromValue(preferences.mapType).name}
        - Value: ${preferences.mapType}
        - Button ID: $mapTypeButtonId
    """.trimIndent()
        )

        // Carregar tamanho do mapa
        val progress = ((preferences.mapWidth.toFloat() / DEFAULT_MAP_SIZE) * 100).toInt()
        mapSizeSeekBar.progress = progress
        mapSizeText.text = "$progress% (${preferences.mapWidth} x ${preferences.mapHeight})"

        Log.d(
            TAG, """
        Map Size Configuration:
        - Width: ${preferences.mapWidth}
        - Height: ${preferences.mapHeight}
        - Progress: $progress%
        - Default Size: $DEFAULT_MAP_SIZE
        - Size Ratio: ${preferences.mapWidth.toFloat() / DEFAULT_MAP_SIZE}
    """.trimIndent()
        )

        // Verificação final
        Log.d(
            TAG, """
        ======= Settings Load Summary =======
        Time (UTC): ${getCurrentUTCTime()}
        
        Final Configuration:
        1. Colors:
           - Origin: ${originColor.name} (${originColor.colorValue})
           - Destination: ${destinationColor.name} (${destinationColor.colorValue})
        
        2. Map Settings:
           - Size: ${preferences.mapWidth}x${preferences.mapHeight}
           - Type: ${MapType.fromValue(preferences.mapType).name}
           - Line Thickness: ${preferences.lineThickness}
        
        All Settings Loaded Successfully
        ===============================
    """.trimIndent()
        )
    }

    private fun selectColorInGroup(group: RadioGroup, color: MarkerColor) {
        Log.d(
            TAG,
            "Selecting color in ${if (group.id == R.id.originColorGroup) "Origin" else "Destination"} group: ${color.name}"
        )
        for (i in 0 until group.childCount) {
            val button = group.getChildAt(i) as RadioButton
            if (button.text.toString().toUpperCase() == color.name) {
                button.isChecked = true
                Log.d(
                    TAG,
                    "Selected radio button for ${if (group.id == R.id.originColorGroup) "Origin" else "Destination"} color: ${color.name}"
                )
                break
            }
        }
    }
}