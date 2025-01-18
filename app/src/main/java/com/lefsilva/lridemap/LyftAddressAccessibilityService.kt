package com.lefsilva.lridemap

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class LyftAddressAccessibilityService : AccessibilityService() {
    companion object {
        private const val TAG = "LyftAddressAccessibilityService"
        private var instance: LyftAddressAccessibilityService? = null
        var lastDetectedAddress: String = ""
            private set

        fun detectAddressNow(): String {
            return instance?.findCurrentAddress() ?: ""
        }

        fun detectAddressTopHalf(): String {
            return instance?.findAddressInTopHalf() ?: ""
        }
    }

    private var screenHeight: Int = 0
    private var screenWidth: Int = 0
    private var foundFirstTimeAddress = false
    private var lastTimeFound = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "Serviço de acessibilidade conectado")

        val displayMetrics = resources.displayMetrics
        screenHeight = displayMetrics.heightPixels
        screenWidth = displayMetrics.widthPixels
    }

    private fun findCurrentAddress(): String {
        lastDetectedAddress = ""
        foundFirstTimeAddress = false
        lastTimeFound = false
        try {
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                findSecondTimeAddressSequence(rootNode)
                rootNode.recycle()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar endereço", e)
        }
        return lastDetectedAddress
    }

    private fun findSecondTimeAddressSequence(node: AccessibilityNodeInfo) {
        try {
            if (node.className?.contains("android.widget.TextView") == true) {
                val rect = android.graphics.Rect()
                node.getBoundsInScreen(rect)

                node.text?.toString()?.let { text ->
                    // Verifica se é um tempo usando apenas o bullet point (•) como identificador
                    if (text.contains("•")) {
                        if (!foundFirstTimeAddress) {
                            lastTimeFound = true
                            Log.d(TAG, "Primeiro tempo encontrado: $text")
                        } else {
                            lastTimeFound = true
                            Log.d(TAG, "Segundo tempo encontrado: $text")
                        }
                    } else {
                        // Se encontrou um tempo antes
                        if (lastTimeFound) {
                            if (!foundFirstTimeAddress) {
                                foundFirstTimeAddress = true
                                lastTimeFound = false
                                Log.d(TAG, "Primeiro endereço encontrado: $text")
                            } else {
                                lastDetectedAddress = text
                                Log.d(TAG, "Segundo endereço encontrado: $text")
                                return
                            }
                        } else {
                            // Não encontrou um tempo antes, continua procurando
                        }
                    }
                }
            }

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { child ->
                    findSecondTimeAddressSequence(child)
                    if (lastDetectedAddress.isNotEmpty()) {
                        child.recycle()
                        return
                    } else {
                        child.recycle()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao procurar endereço", e)
        }
    }

    private fun findAddressInTopHalf(): String {
        lastDetectedAddress = ""
        try {
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                findAddressNodesInTopHalf(rootNode)
                rootNode.recycle()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao buscar endereço na parte superior", e)
        }
        return lastDetectedAddress
    }

    private fun findAddressNodesInTopHalf(node: AccessibilityNodeInfo) {
        try {
            val rect = android.graphics.Rect()
            node.getBoundsInScreen(rect)

            if (rect.top < screenHeight / 2) {
                node.text?.toString()?.let { text ->
                    if (text.isNotEmpty()) {
                        lastDetectedAddress = text
                        Log.d(TAG, "Endereço detectado na parte superior: $lastDetectedAddress")
                    }
                }
            }

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { child ->
                    findAddressNodesInTopHalf(child)
                    child.recycle()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao procurar nós de endereço na parte superior", e)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            if (it.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                findAddToQueueText()
            }
        }
    }


    private fun findAddToQueueText() {
        try {
            val rootNode = rootInActiveWindow
            rootNode?.let { node ->
                findAddToQueueInNode(node)
                node.recycle()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao procurar texto 'Add to queue'", e)
        }
    }

    private fun findAddToQueueInNode(node: AccessibilityNodeInfo) {
        try {
            node.text?.toString()?.let { text ->
                if (text.contains("Add to queue", ignoreCase = true)) {
                    val intent = Intent("com.lefsilva.lridemap.ADD_TO_QUEUE_DETECTED")
                    sendBroadcast(intent)
                    return
                }
            }

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { child ->
                    findAddToQueueInNode(child)
                    child.recycle()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erro ao procurar em nó", e)
        }
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