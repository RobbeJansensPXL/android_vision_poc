package be.pxl.android_vision_poc

import android.content.Context
import android.util.Log
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter


class ImageObjectSegmenter(private val model: String, private val context: Context, private val detectionDrawer: DetectionDrawer) : ImageDetectionInterface {
    private val baseOptions = BaseOptions.builder().useGpu().build()
    private var previousTime = System.currentTimeMillis()

    private val detectorOptions = ImageSegmenter.ImageSegmenterOptions.builder()
        .setBaseOptions(baseOptions)
        .build()

    private val objectSegmenter by lazy {
        ImageSegmenter.createFromFileAndOptions(
            context, // the application context
            model, // must be same as the filename in assets folder
            detectorOptions
        )
    }

    override fun detect(tensorImage: TensorImage) {
        var result = objectSegmenter.segment(tensorImage)

        detectionDrawer.drawBitmap(result)

        val delta = System.currentTimeMillis() - previousTime
        Log.d("FPS", (1000.0 / delta).toString())
        previousTime = System.currentTimeMillis()
    }
}