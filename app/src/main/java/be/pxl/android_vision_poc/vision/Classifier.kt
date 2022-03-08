package be.pxl.android_vision_poc.vision

import android.content.Context
import android.util.Log
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

class Classifier (private val model: String, private val context: Context) {
    private val baseOptions = BaseOptions.builder().useGpu().build()
    private var previousTime = System.currentTimeMillis()

    private val detectorOptions = ImageClassifier.ImageClassifierOptions.builder()
        .setBaseOptions(baseOptions)
        .setScoreThreshold(0.45f)
        .build()

    private val classifier by lazy {
        ImageClassifier.createFromFileAndOptions(
            context,
            model,
            detectorOptions
        )
    }

    fun detect(tensorImage: TensorImage): MutableList<Classifications>? {
        var results = classifier.classify(tensorImage)

        return results

        results.forEach { classifications ->
            classifications.categories.forEach{ category ->
                Log.d("classification", category.label)
            }
        }

        val delta = System.currentTimeMillis() - previousTime
        Log.d("FPS", (1000.0 / delta).toString())
        previousTime = System.currentTimeMillis()
    }
}