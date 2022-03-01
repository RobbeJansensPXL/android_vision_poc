package be.pxl.android_vision_poc

import android.content.Context
import android.util.Log
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class ImageObjectDetector(context: Context) {
    private val baseOptions = BaseOptions.builder().useGpu().build()

    private val detectorOptions = ObjectDetector.ObjectDetectorOptions.builder()
        .setBaseOptions(baseOptions)
        .setScoreThreshold(0.5f)
        .build()

    private val objectDetector by lazy {
        ObjectDetector.createFromFileAndOptions(
            context, // the application context
            "test2.tflite", // must be same as the filename in assets folder
            detectorOptions
        )
    }

    fun detectObjects(tensorImage: TensorImage) {
        val results = objectDetector.detect(tensorImage)
        Log.d("test", results.toString())
    }
}