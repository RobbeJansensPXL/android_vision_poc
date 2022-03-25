package be.pxl.android_vision_poc.analyzers

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import be.pxl.android_vision_poc.utils.cropRectangle
import be.pxl.android_vision_poc.utils.rotate
import be.pxl.android_vision_poc.utils.toBitmap
import be.pxl.android_vision_poc.vision.Classifier
import be.pxl.android_vision_poc.vision.ObjectDetector
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.detector.Detection

class BottleObjectDetectionAnalyzer(
    private val bottleObjectDetector: ObjectDetector,
    private val bottleClassifier: Classifier,
    private val bottleAnalyzationHandler: (Bitmap, MutableList<Detection>?, MutableList<Category?>) -> Unit
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
            val detectorResults = bottleObjectDetector.detect(tensorImage)
            val detections = calculateClassificationList(image, detectorResults)

            val delta = System.currentTimeMillis() - previousTime
            Log.d("FPS", (1000.0 / delta).toString())
            previousTime = System.currentTimeMillis()

            Log.d("detection", detections.toString())

            //return results
            bottleAnalyzationHandler(
                image,
                detectorResults,
                detections
            )

            //Close image proxy
            imageProxy.close()
        }
    }

    private fun calculateClassificationList(image: Bitmap, objectDetectionResult: MutableList<Detection>?): MutableList<Category?> {
        val classificationList = mutableListOf<Category?>()

        objectDetectionResult?.forEach { result ->
            val bitmap = image.cropRectangle(result.boundingBox)
            val tensorImage = TensorImage.fromBitmap(bitmap)

            val results = bottleClassifier.detect(tensorImage)

            classificationList.add(getMostProbableClass(results))
        }

        return classificationList
    }

    private fun getMostProbableClass(classifications: MutableList<Classifications>?) : Category? {
        classifications?.forEach{ classification ->
            return classification.categories.maxByOrNull{ category ->
                category.score
            }
        }
        return null
    }
}