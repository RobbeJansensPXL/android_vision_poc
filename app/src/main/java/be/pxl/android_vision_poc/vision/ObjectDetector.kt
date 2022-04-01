package be.pxl.android_vision_poc.vision

import android.content.Context
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class ObjectDetector(private val model: String, private val context: Context) {
    private val baseOptions = BaseOptions.builder().useGpu().build()

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
        return objectDetector.detect(tensorImage)
    }
}