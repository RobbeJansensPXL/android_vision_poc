package be.pxl.android_vision_poc

import android.content.Context
import android.util.Log
import androidx.viewbinding.ViewBinding
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class ImageObjectDetector(private val context: Context, val detectionDrawer: DetectionDrawer) {
    private val baseOptions = BaseOptions.builder().useGpu().build()

    private val detectorOptions = ObjectDetector.ObjectDetectorOptions.builder()
        .setBaseOptions(baseOptions)
        .setScoreThreshold(0.55f)
        .build()

    private val objectDetector by lazy {
        ObjectDetector.createFromFileAndOptions(
            context, // the application context
            "beer_bottles.tflite", // must be same as the filename in assets folder
            detectorOptions
        )
    }

    fun detectObjects(tensorImage: TensorImage) {
        val results = objectDetector.detect(tensorImage)
        detectionDrawer.drawDetections(results)
        for (detection in results) {
            Log.d("Detection", detection.boundingBox.toShortString())
        }
        Log.d("Detection_Results", results.toString())
    }
}