package be.pxl.android_vision_poc.analyzers

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import be.pxl.android_vision_poc.utils.cropRectangle
import be.pxl.android_vision_poc.utils.extractMaskAndFilteredMask
import be.pxl.android_vision_poc.utils.rotate
import be.pxl.android_vision_poc.utils.toBitmap
import be.pxl.android_vision_poc.vision.Classifier
import be.pxl.android_vision_poc.vision.ObjectSegmenter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.segmenter.Segmentation

class BottleSegmentationAnalyzer (
    private val bottleSegmenter: ObjectSegmenter,
    private val labelClassifier: Classifier,
    private val bottleSegmentationAnalyzationHandler: (Bitmap, Bitmap, MutableList<Classifications>?) -> Unit
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

            //segment
            val segmentationResult = bottleSegmenter.detect(tensorImage)?.get(0) ?: return

            if (!this::colors.isInitialized) {
                initializeColors(segmentationResult)
            }

            //calculate performance
            val delta = System.currentTimeMillis() - previousTime
            Log.d("FPS", (1000.0 / delta).toString())
            previousTime = System.currentTimeMillis()

            GlobalScope.launch {
                val (segmentationBitmap, labelRectangle) = segmentationResult.extractMaskAndFilteredMask(colors, -16744448, image.width, image.height)

                //classify
                var classificationResult : MutableList<Classifications>? = null

                if (labelRectangle != null) {
                    val labelBitmap = image.cropRectangle(labelRectangle)
                    classificationResult = labelClassifier.detect(TensorImage.fromBitmap(labelBitmap))!!
                }

                //return results
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

            if (rgb != Color.BLACK) {
                colors[cnt] = Color.argb(120, Color.red(rgb), Color.green(rgb), Color.blue(rgb))
            }
            else {
                colors[cnt] = Color.TRANSPARENT
            }
        }
    }
}