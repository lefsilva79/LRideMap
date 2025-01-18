// Created by lefsilva79 on 2025-01-18 20:05:46 UTC

package com.lefsilva.lridemap

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AddToQueueReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "com.lefsilva.lridemap.ADD_TO_QUEUE_DETECTED") {
            context?.let {
                Toast.makeText(it, "Add to queue detectado!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}