package com.lefsilva.lridemap

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout

class SettingsButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var settingsButton: ImageButton

    init {
        orientation = HORIZONTAL
        LayoutInflater.from(context).inflate(R.layout.settings_button_layout, this, true)
        settingsButton = findViewById(R.id.settings_button)
    }

    fun setOnSettingsClickListener(listener: () -> Unit) {
        settingsButton.setOnClickListener { listener.invoke() }
    }
}