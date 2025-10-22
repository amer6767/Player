
package com.aiplayer.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.aiplayer.data.DataCollector
import com.aiplayer.store.AppDatabase
import com.aiplayer.store.Experience
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class CaptureService : Service() {
    private val TAG = "CaptureService"
    private var projection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var handler: Handler? = null
    private var captureFps = 30
    private var inferenceEveryN = 3
    private var frameCounter = 0
    private lateinit var dataCollector: DataCollector
    private lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification("Idle"))
        val ht = HandlerThread("cap-thread"); ht.start()
        handler = Handler(ht.looper)
        dataCollector = DataCollector(this)
        db = AppDatabase.getInstance(this)
    }

    private fun createNotification(text:String): Notification {
        val channelId = "ai_player_capture"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel(channelId, "Capture", NotificationManager.IMPORTANCE_LOW))
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("AI Player").setContentText(text).setSmallIcon(android.R.drawable.ic_media_play).build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            captureFps = it.getIntExtra("capture_fps", 30)
            inferenceEveryN = it.getIntExtra("inference_every_n", 3)
            val data = it.getParcelableExtra<Intent>("media_intent")
            if (data != null) {
                val mpm = getSystemService(MediaProjectionManager::class.java)
                projection = mpm.getMediaProjection(RESULT_OK, data)
                setupImageReader()
                startForeground(1, createNotification("Capturing @ ${captureFps}fps"))
            }
        }
        return START_STICKY
    }

    private fun setupImageReader() {
        val width = 1280; val height = 720
        imageReader = ImageReader.newInstance(width, height, android.graphics.PixelFormat.RGBA_8888, 2)
        projection?.createVirtualDisplay("ai-cap", width, height, resources.displayMetrics.densityDpi, 0, imageReader?.surface, null, handler)
        imageReader?.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener
            frameCounter += 1
            val bytes = imageToJpeg(image)
            image.close()
            if (bytes != null) {
                val ts = System.currentTimeMillis()
                CoroutineScope(Dispatchers.IO).launch {
                    val fname = dataCollector.saveFrame("session_auto", bytes, ts)
                    // persist minimal experience meta to Room for replay
                    val exp = Experience(0, ts, fname, 0, 0)
                    db.experienceDao().insert(exp)
                }
                // inference control
                if (frameCounter % inferenceEveryN == 0) {
                    // TODO: call TFLite inference pipeline
                    Log.d(TAG, "Would run inference on frame at $ts")
                }
            }
        }, handler)
    }

    private fun imageToJpeg(image: Image): ByteArray? {
        try {
            val plane = image.planes[0]; val buffer = plane.buffer
            val pixelStride = plane.pixelStride; val rowStride = plane.rowStride
            val rowPadding = rowStride - pixelStride * image.width
            val bitmap = Bitmap.createBitmap(image.width + rowPadding / pixelStride, image.height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(buffer)
            val baos = ByteArrayOutputStream(); bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            return baos.toByteArray()
        } catch (e: Exception) {
            Log.e(TAG, "imageToJpeg failed", e); return null
        }
    }

    override fun onDestroy() {
        imageReader?.close(); projection?.stop(); super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
