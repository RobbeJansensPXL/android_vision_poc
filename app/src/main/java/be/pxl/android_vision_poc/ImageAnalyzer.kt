package be.pxl.android_vision_poc

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.tensorflow.lite.support.image.TensorImage


class ImageAnalyzer(private val objectDetector: ImageObjectDetector) : ImageAnalysis.Analyzer {
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (imageProxy.image != null) {
            val targetImage = imageProxy.image!!
            val targetBitmap = Bitmap.createBitmap(targetImage.width, targetImage.height, Bitmap.Config.ARGB_8888)

            val tensorImage = TensorImage.fromBitmap(targetBitmap)

            objectDetector.detectObjects(tensorImage)
            imageProxy.close()
        }
    }
}
