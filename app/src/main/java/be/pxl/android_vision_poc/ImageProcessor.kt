package be.pxl.android_vision_poc

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.support.image.TensorImage


class ImageProcessor(private val objectDetector: ImageObjectDetector) : ImageAnalysis.Analyzer {
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (imageProxy.image != null) {
            val targetImage = imageProxy.image!!.toBitmap()
            val image = targetImage.rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            Log.d("test", image.width.toString())
            Log.d("test", image.height.toString())


            val tensorImage = TensorImage.fromBitmap(image)

            objectDetector.detectObjects(tensorImage)
            imageProxy.close()
        }
    }
}