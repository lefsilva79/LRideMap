package com.lefsilva.lridemap

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class SettingsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Constants.ACTION_UPDATE_MAP_SETTINGS && context != null) {
            // Envia um broadcast local para o serviço
            LocalBroadcastManager.getInstance(context).sendBroadcast(
                Intent(FloatingButtonService.INTERNAL_UPDATE_SETTINGS)
            )
        }
    }
}