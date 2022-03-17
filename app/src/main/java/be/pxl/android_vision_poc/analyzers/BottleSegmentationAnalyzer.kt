package be.pxl.android_vision_poc.analyzers

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import be.pxl.android_vision_poc.utils.extractBitmap
import be.pxl.android_vision_poc.utils.rotate
import be.pxl.android_vision_poc.utils.toBitmap
import be.pxl.android_vision_poc.vision.Classifier
import be.pxl.android_vision_poc.vision.ObjectSegmenter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.Classifications
import kotlin.math.log

//TODO: Clean Code
class BottleSegmentationAnalyzer (
    private val bottleSegmenter: ObjectSegmenter,
    private val labelClassifier: Classifier,
    private val bottleSegmentationAnalyzationHandler: (Bitmap, Bitmap, MutableList<Classifications>) -> Unit
) : ImageAnalysis.Analyzer {
    private var previousTime = System.currentTimeMillis()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (imageProxy.image != null) {
            //convert image
            val targetImage = imageProxy.image!!.toBitmap()
            val image = targetImage.rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            val tensorImage = TensorImage.fromBitmap(image)

            //calculate
            val segmentationResult = bottleSegmenter.detect(tensorImage)?.get(0)

            //test
            val segmentationBitmap = segmentationResult?.extractBitmap() ?: return
            val resizedImage = Bitmap.createScaledBitmap(image, segmentationBitmap.width, segmentationBitmap.height, false)

            val labelBitmap = Bitmap.createBitmap(segmentationBitmap.width, segmentationBitmap.height, Bitmap.Config.ARGB_8888)

            //betere manier zoeken (NIET PERFORMANT)
            for (x in 0 until segmentationBitmap.width) {
                for (y in 0 until segmentationBitmap.height) {
                    if (segmentationBitmap.getPixel(x, y) == -16744448) {
                        labelBitmap.setPixel(x, y, resizedImage.getPixel(x, y))
                    }
                }
            }

            val classificationResult = labelClassifier.detect(TensorImage.fromBitmap(labelBitmap))

            Log.d("classification", classificationResult.toString())

            val delta = System.currentTimeMillis() - previousTime
            Log.d("FPS", (1000.0 / delta).toString())
            previousTime = System.currentTimeMillis()

            Log.d("segmentation", segmentationResult.toString())

            //return results
            if (classificationResult != null) {
                bottleSegmentationAnalyzationHandler(
                    image,
                    segmentationBitmap,
                    classificationResult
                )
            }

            //Close image proxy
            imageProxy.close()
        }
    }
}