package be.pxl.android_vision_poc.analyzers

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import be.pxl.android_vision_poc.utils.extractBitmap
import be.pxl.android_vision_poc.utils.extractMaskAndFilteredMask
import be.pxl.android_vision_poc.utils.rotate
import be.pxl.android_vision_poc.utils.toBitmap
import be.pxl.android_vision_poc.vision.Classifier
import be.pxl.android_vision_poc.vision.ObjectSegmenter
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.segmenter.Segmentation
import java.util.*
import kotlin.math.log

//TODO: Clean Code
class BottleSegmentationAnalyzer (
    private val bottleSegmenter: ObjectSegmenter,
    private val labelClassifier: Classifier,
    private val bottleSegmentationAnalyzationHandler: (Bitmap, Bitmap, MutableList<Classifications>) -> Unit
) : ImageAnalysis.Analyzer {
    private var previousTime = System.currentTimeMillis()
    private lateinit var colors: IntArray
    private lateinit var filteredColors: IntArray

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (imageProxy.image != null) {
            //convert image
            val targetImage = imageProxy.image!!.toBitmap()
            val image = targetImage.rotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            val tensorImage = TensorImage.fromBitmap(image)

            //calculate
            val segmentationResult = bottleSegmenter.detect(tensorImage)?.get(0) ?: return

            if (!this::colors.isInitialized) {
                initializeColors(segmentationResult)
            }


            val delta = System.currentTimeMillis() - previousTime
            Log.d("FPS", (1000.0 / delta).toString())
            previousTime = System.currentTimeMillis()

            //test
            var (segmentationBitmap, filteredSegmentationBitmap) = segmentationResult.extractMaskAndFilteredMask(colors, filteredColors)
            filteredSegmentationBitmap = Bitmap.createScaledBitmap(filteredSegmentationBitmap, image.width, image.height, false)

            val labelBitmap = Bitmap.createBitmap(filteredSegmentationBitmap.width, filteredSegmentationBitmap.height, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(labelBitmap)
            canvas.drawBitmap(image, 0.0f, 0.0f, null)
            canvas.drawBitmap(filteredSegmentationBitmap, 0.0f, 0.0f, null)

            val classificationResult = labelClassifier.detect(TensorImage.fromBitmap(labelBitmap))

            Log.d("classification", classificationResult.toString())

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

    private fun initializeColors(segmentation: Segmentation) {
        colors = IntArray(segmentation.coloredLabels.size)
        filteredColors = IntArray(3)

        filteredColors[0] = Color.BLACK
        filteredColors[1] = Color.BLACK
        filteredColors[2] = Color.TRANSPARENT

        for ((cnt, coloredLabel) in segmentation.coloredLabels.withIndex()) {
            val rgb = coloredLabel.argb
            colors[cnt] = Color.argb(255, Color.red(rgb), Color.green(rgb), Color.blue(rgb))
        }
    }
}