
package com.aiplayer.data

import android.content.Context
import android.util.Log
import java.io.File

class DataCollector(private val ctx: Context) {
    private val TAG = "DataCollector"
    private val baseDir: File = ctx.getExternalFilesDir(null) ?: ctx.filesDir

    fun saveFrame(sessionId:String, bytes:ByteArray, timestamp:Long):String {
        val dir = File(baseDir, "sessions/$sessionId")
        dir.mkdirs()
        val name = "frame_%d.jpg".format(timestamp)
        File(dir, name).writeBytes(bytes)
        Log.i(TAG, "Saved frame $name")
        return "sessions/$sessionId/$name"
    }
}
