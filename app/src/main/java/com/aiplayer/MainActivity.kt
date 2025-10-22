
package com.aiplayer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import com.aiplayer.service.CaptureService

class MainActivity : AppCompatActivity() {
    private val REQ_MEDIA_PROJ = 4231
    private lateinit var tvStatus: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvStatus = findViewById(R.id.tv_status)
        findViewById<Button>(R.id.btn_start_capture).setOnClickListener { requestProjectionPermission() }
        findViewById<Button>(R.id.btn_start_ai).setOnClickListener { startAI() }
        val sb = findViewById<SeekBar>(R.id.sb_inference_rate)
        val tv = findViewById<TextView>(R.id.tv_inference)
        sb.progress = 2
        sb.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) { tv.text = "Inference every ${p1+1} frames" }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })
    }

    private fun requestProjectionPermission() {
        val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mpm.createScreenCaptureIntent(), REQ_MEDIA_PROJ)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_MEDIA_PROJ && resultCode == Activity.RESULT_OK && data != null) {
            val svc = Intent(this, CaptureService::class.java)
            svc.putExtra("media_intent", data)
            svc.putExtra("capture_fps", 30)
            svc.putExtra("inference_every_n", 3) // default: every 3rd frame => 10 fps inference
            startForegroundService(svc)
            tvStatus.text = "Status: capturing"
        } else {
            tvStatus.text = "Status: permission denied"
        }
    }

    private fun startAI() {
        val svc = Intent(this, CaptureService::class.java)
        svc.putExtra("start_ai", true)
        startForegroundService(svc)
        tvStatus.text = "Status: AI running"
    }
}
