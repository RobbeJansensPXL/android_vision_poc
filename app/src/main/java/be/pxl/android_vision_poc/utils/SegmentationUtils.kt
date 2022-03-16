package be.pxl.android_vision_poc.utils

import android.graphics.Bitmap
import android.graphics.Color
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.segmenter.ColoredLabel
import org.tensorflow.lite.task.vision.segmenter.Segmentation

fun Segmentation.extractBitmap(): Bitmap {
    val tensorMask = masks[0]
    val rawMask = tensorMask.tensorBuffer.intArray

    val pixelData = IntArray(rawMask.size * 3)
    val coloredLabels: List<ColoredLabel> = coloredLabels

    for (i in rawMask.indices) {
        val color = coloredLabels[rawMask[i]].color.toArgb()

        pixelData[3 * i] = Color.red(color)
        pixelData[3 * i + 1] = Color.green(color)
        pixelData[3 * i + 2] = Color.blue(color)
    }

    val shape = intArrayOf(tensorMask.width, tensorMask.height, 3)
    val maskImage = TensorImage()
    maskImage.load(pixelData, shape)

    return maskImage.bitmap
}