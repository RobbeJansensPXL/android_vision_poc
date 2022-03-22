package be.pxl.android_vision_poc.vision

import android.content.Context
import android.util.Log
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.segmenter.ImageSegmenter
import org.tensorflow.lite.task.vision.segmenter.Segmentation


class ObjectSegmenter(private val model: String, private val context: Context) {
    private val baseOptions = BaseOptions.builder().setNumThreads(4).useNnapi().useGpu().build()

    private val detectorOptions = ImageSegmenter.ImageSegmenterOptions.builder()
        .setBaseOptions(baseOptions)
        .build()

    private val objectSegmenter by lazy {
        ImageSegmenter.createFromFileAndOptions(
            context,
            model,
            detectorOptions
        )
    }

    fun detect(tensorImage: TensorImage): MutableList<Segmentation>? {
        var result = objectSegmenter.segment(tensorImage)

        return result
    }
}