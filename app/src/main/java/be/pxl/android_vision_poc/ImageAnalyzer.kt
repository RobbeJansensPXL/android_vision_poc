package be.pxl.android_vision_poc

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage

class ImageAnalyzer(
    private val imageClassifier: ImageClassifier
) : ImageAnalysis.Analyzer {
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val image = imageProxy.image
        if(image != null) {
            val inputImage = InputImage.fromMediaImage(image, rotationDegrees)
        }
        imageProxy.close()
    }
}