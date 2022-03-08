package be.pxl.android_vision_poc.vision

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class ObjectDetector(private val model: String, private val context: Context) {
    private val baseOptions = BaseOptions.builder().useGpu().build()
    private var previousTime = System.currentTimeMillis()

    private val detectorOptions = ObjectDetector.ObjectDetectorOptions.builder()
        .setBaseOptions(baseOptions)
        .setScoreThreshold(0.60f)
        .build()

    private val objectDetector by lazy {
        ObjectDetector.createFromFileAndOptions(
            context,
            model,
            detectorOptions
        )
    }

    fun detect(tensorImage: TensorImage): MutableList<Detection>? {
        val results = objectDetector.detect(tensorImage)

        return results

        val bitmap = tensorImage.bitmap
        results.forEach{result ->
            val left = result.boundingBox.left.toInt().coerceAtLeast(0)
            val top = result.boundingBox.top.toInt().coerceAtLeast(0)
            val right = result.boundingBox.right.toInt().coerceAtMost(bitmap.width)
            val bottom = result.boundingBox.bottom.toInt().coerceAtMost(bitmap.height)
            val width = (right - left).coerceAtMost(bitmap.width - right).coerceAtLeast(1)
            val height = (bottom - top).coerceAtMost(bitmap.height - top).coerceAtLeast(1)
            val bottleBitmap = Bitmap.createBitmap(bitmap, left, top, width, height)

            //imageClassifier.detect(TensorImage.fromBitmap(bottleBitmap))
        }


        val delta = System.currentTimeMillis() - previousTime
        Log.d("FPS", (1000.0 / delta).toString())
        previousTime = System.currentTimeMillis()
    }
}