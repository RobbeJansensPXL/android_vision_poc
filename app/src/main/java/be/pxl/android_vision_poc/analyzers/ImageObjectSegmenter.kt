package be.pxl.android_vision_poc.analyzers

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import be.pxl.android_vision_poc.utils.rotate
import be.pxl.android_vision_poc.utils.toBitmap
import be.pxl.android_vision_poc.vision.ObjectSegmenter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.segmenter.Segmentation

class ImageObjectSegmenter(
    private val segmenter: ObjectSegmenter,
    private val segmentationHandler: (result: MutableList<Segmentation>?) -> Unit
) : ImageAnalysis.Analyzer {
    private var previousTime = System.currentTimeMillis()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (imageProxy.image != null) {
            //convert image
            val targetImage = imageProxy.image!!.toBitmap()
            val image = targetImage.rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            val tensorImage = TensorImage.fromBitmap(image)

            val result = segmenter.detect(tensorImage)

            val delta = System.currentTimeMillis() - previousTime
            Log.d("FPS", (1000.0 / delta).toString())
            previousTime = System.currentTimeMillis()

            //return results to MainActivity
            segmentationHandler(
                result
            )

            //Close image proxy
            imageProxy.close()
        }
    }
}