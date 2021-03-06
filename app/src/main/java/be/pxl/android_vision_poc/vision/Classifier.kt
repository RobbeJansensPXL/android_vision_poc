package be.pxl.android_vision_poc.vision

import android.content.Context
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier

class Classifier (private val model: String, private val context: Context) {
    private val baseOptions = BaseOptions.builder().useGpu().build()

    private val detectorOptions = ImageClassifier.ImageClassifierOptions.builder()
        .setBaseOptions(baseOptions)
        .setScoreThreshold(0.3f)
        .build()

    private val classifier by lazy {
        ImageClassifier.createFromFileAndOptions(
            context,
            model,
            detectorOptions
        )
    }

    fun detect(tensorImage: TensorImage): MutableList<Classifications>? {
        return classifier.classify(tensorImage)
    }
}