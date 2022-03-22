package be.pxl.android_vision_poc.utils

import android.graphics.Bitmap
import android.graphics.Color
import org.tensorflow.lite.task.vision.segmenter.Segmentation

fun Segmentation.extractBitmap(): Bitmap {
    val colors = IntArray(coloredLabels.size)

    for ((cnt, coloredLabel) in coloredLabels.withIndex()) {
        val rgb = coloredLabel.argb
        colors[cnt] = Color.argb(255, Color.red(rgb), Color.green(rgb), Color.blue(rgb))
    }

    val maskTensor = masks[0]
    val maskArray = maskTensor.buffer.array()
    val pixels = IntArray(maskArray.size)
    val itemsFound = HashMap<String, Int>()
    for (i in maskArray.indices) {
        val color = colors[maskArray[i].toInt()]
        pixels[i] = color
        itemsFound[coloredLabels[maskArray[i].toInt()].getlabel()] = color
    }

    return Bitmap.createBitmap(
        pixels, maskTensor.width, maskTensor.height,
        Bitmap.Config.ARGB_8888
    )
}

fun Segmentation.extractMaskAndFilteredMask(colors: IntArray, filteredColors: IntArray): Pair<Bitmap, Bitmap> {
    val maskTensor = masks[0]
    val maskArray = maskTensor.buffer.array()
    val pixels = IntArray(maskArray.size)
    val filteredPixels = IntArray(maskArray.size)
    for (i in maskArray.indices) {
        val index = maskArray[i].toInt()
        pixels[i] = colors[index]
        filteredPixels[i] = filteredColors[index]
    }

    val maskBitmap = Bitmap.createBitmap(
        pixels, maskTensor.width, maskTensor.height,
        Bitmap.Config.ARGB_8888
    )

    val filteredMaskBitmap = Bitmap.createBitmap(
        filteredPixels, maskTensor.width, maskTensor.height,
        Bitmap.Config.ARGB_8888
    )

    return Pair(maskBitmap, filteredMaskBitmap)
}