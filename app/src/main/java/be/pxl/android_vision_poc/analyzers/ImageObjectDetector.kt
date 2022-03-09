package be.pxl.android_vision_poc.analyzers

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import be.pxl.android_vision_poc.utils.rotate
import be.pxl.android_vision_poc.utils.toBitmap
import be.pxl.android_vision_poc.vision.ObjectDetector
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection

class ImageObjectDetector(
    private val objectDetector: ObjectDetector,
    private val objectDetectorResultHandler: (Bitmap, MutableList<Detection>?) -> Unit
) : ImageAnalysis.Analyzer {
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (imageProxy.image != null) {
            //convert image
            val targetImage = imageProxy.image!!.toBitmap()
            val image = targetImage.rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            val tensorImage = TensorImage.fromBitmap(image)

            //return results to MainActivity
            objectDetectorResultHandler(
                image,
                objectDetector.detect(tensorImage)
            )

            //Close image proxy
            imageProxy.close()
        }
    }
}