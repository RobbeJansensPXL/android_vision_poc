package be.pxl.android_vision_poc.analyzers

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import be.pxl.android_vision_poc.rotate
import be.pxl.android_vision_poc.toBitmap
import be.pxl.android_vision_poc.vision.ObjectSegmenter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.segmenter.Segmentation

class ImageObjectSegmenter(
    private val segmenter: ObjectSegmenter,
    private val segmentationHandler: (result: MutableList<Segmentation>?) -> Unit
) : ImageAnalysis.Analyzer {
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (imageProxy.image != null) {
            //convert image
            val targetImage = imageProxy.image!!.toBitmap()
            val image = targetImage.rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            val tensorImage = TensorImage.fromBitmap(image)

            //return results to MainActivity
            segmentationHandler(
                segmenter.detect(tensorImage)
            )

            //Close image proxy
            imageProxy.close()
        }
    }
}