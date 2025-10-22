
package com.aiplayer.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import java.util.concurrent.LinkedBlockingQueue
import kotlin.concurrent.thread

class PlayerAccessibilityService : AccessibilityService() {
    private val TAG = "PlayerAccessibility"
    private val queue = LinkedBlockingQueue<GestureDescription>(3)

    override fun onServiceConnected() {
        Log.i(TAG, "Accessibility service connected")
        thread(start = true) {
            while (true) {
                val g = queue.take()
                dispatchGesture(g, object: GestureResultCallback() {
                    override fun onCompleted(gestureDescription: GestureDescription?) { Log.i(TAG, "Gesture completed") }
                    override fun onCancelled(gestureDescription: GestureDescription?) { Log.i(TAG, "Gesture cancelled") }
                }, null)
                Thread.sleep(50)
            }
        }
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {}
    override fun onInterrupt() {}

    fun enqueueTap(x:Int, y:Int, dur:Long = 30) {
        val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
        val g = GestureDescription.Builder().addStroke(GestureDescription.StrokeDescription(path, 0, dur)).build()
        queue.offer(g)
    }
}
