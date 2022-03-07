package be.pxl.android_vision_poc

import android.content.Context
import android.util.Log
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.detector.ObjectDetector

class ImageObjectDetector(private val model: String, private val context: Context, private val detectionDrawer: DetectionDrawer) : ImageDetectionInterface {
    private val baseOptions = BaseOptions.builder().useGpu().build()
    private var previousTime = System.currentTimeMillis()

    private val detectorOptions = ObjectDetector.ObjectDetectorOptions.builder()
        .setBaseOptions(baseOptions)
        .setScoreThreshold(0.6f)
        .build()

    private val objectDetector by lazy {
        ObjectDetector.createFromFileAndOptions(
            context,
            model,
            detectorOptions
        )
    }

    override fun detect(tensorImage: TensorImage) {
        val results = objectDetector.detect(tensorImage)
        detectionDrawer.drawDetections(results, tensorImage.width, tensorImage.height)

        val delta = System.currentTimeMillis() - previousTime
        Log.d("FPS", (1000.0 / delta).toString())
        previousTime = System.currentTimeMillis()
    }
}