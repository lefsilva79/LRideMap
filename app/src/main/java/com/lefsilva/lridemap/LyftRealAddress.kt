package com.lefsilva.lridemap

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class LyftRealAddress : AccessibilityService() {
    companion object {
        private const val TAG = "LyftRealAddress"
        private var instance: LyftRealAddress? = null
        var lastDetectedAddress: String = ""
            private set

        fun detectAddressNow(): String {
            return instance?.findCurrentAddress() ?: ""
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Serviço de acessibilidade conectado")
    }

    private fun findCurrentAddress(): String {
        lastDetectedAddress = ""  // Reset do último endereço
        try {
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                // Procurar especificamente por TextViews que contêm "&"
                val addressNodes = rootNode.findAccessibilityNodeInfosByText("&")
                    ?.filter { node ->
                        node.className?.contains("TextView") == true &&
                                node.text?.contains("&") == true &&
                                node.text?.contains(",") == true
                    }
                    ?: emptyList()

                // Pegar o segundo endereço (destino)
                if (addressNodes.size >= 2) {
                    lastDetectedAddress = addressNodes[1].text.toString()
                    Log.d(TAG, "Endereço de destino encontrado: $lastDetectedAddress")
                }

                rootNode.recycle()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar endereço", e)
        }
        return lastDetectedAddress
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Vazio pois a detecção é feita apenas no clique do botão
    }

    override fun onInterrupt() {
        Log.d(TAG, "Serviço de acessibilidade interrompido")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "Serviço de acessibilidade destruído")
    }
}