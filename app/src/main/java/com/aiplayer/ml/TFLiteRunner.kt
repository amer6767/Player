
package com.aiplayer.ml

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteRunner(private val ctx: Context) {
    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    private val TAG = "TFLiteRunner"

    fun loadModelFromAssets(name:String) {
        try {
            val afd = ctx.assets.openFd(name)
            val stream = FileInputStream(afd.fileDescriptor)
            val channel = stream.channel
            val mapped: MappedByteBuffer = channel.map(FileChannel.MapMode.READ_ONLY, afd.startOffset, afd.length)
            // Try GPU delegate
            try {
                gpuDelegate = GpuDelegate()
                val opts = Interpreter.Options().addDelegate(gpuDelegate)
                interpreter = Interpreter(mapped, opts)
                Log.i(TAG, "Loaded model with GPU delegate")
            } catch (e: Exception) {
                interpreter = Interpreter(mapped)
                Log.i(TAG, "Loaded model on CPU fallback")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Model load failed", e)
        }
    }

    fun run(input: Any, output: Any) {
        interpreter?.run(input, output)
    }

    fun close() {
        interpreter?.close()
        gpuDelegate?.close()
    }
}
