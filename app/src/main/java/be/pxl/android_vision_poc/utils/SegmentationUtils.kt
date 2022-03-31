package be.pxl.android_vision_poc.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.RectF
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

    for (i in maskArray.indices) {
        val color = colors[maskArray[i].toInt()]
        pixels[i] = color
    }

    return Bitmap.createBitmap(
        pixels, maskTensor.width, maskTensor.height,
        Bitmap.Config.ARGB_8888
    )
}

fun Segmentation.extractMaskAndFilteredMask(colors: IntArray, filteredColor: Int, originalWidth: Int, originalHeight: Int): Pair<Bitmap, RectF?> {
    val maskTensor = masks[0]
    val width = maskTensor.width
    val height = maskTensor.height
    val maskArray = maskTensor.buffer.array()
    val pixels = IntArray(maskArray.size)
    IntArray(maskArray.size)

    var top = -1
    var left = -1
    var right = -1
    var bottom = -1

    var nonMaskCount = 0
    val nonMaskThreshold = originalWidth * 10


    for (i in maskArray.indices) {
        val colorIndex = maskArray[i].toInt()
        val color = colors[colorIndex]
        pixels[i] = color

        if (colorIndex == 2) {
            if (nonMaskCount >= nonMaskThreshold) {
                top = -1
                left = -1
                right = -1
                bottom = -1
            }

            nonMaskCount = 0

            val row = i / width
            val col = i - row * width

            if (top == -1) {
                top = row
                bottom = row
                left = col
                right = col
            }
            else {
                bottom = row
                if (col < left) {
                    left = col
                }
                else if (col > right)
                    right = col
            }
        }
        else {
            nonMaskCount++
        }
    }

    val maskBitmap = Bitmap.createBitmap(
        pixels, width, height,
        Bitmap.Config.ARGB_8888
    )

    if (top == -1) {
        return Pair(maskBitmap, null)
    }

    val floatHeight = height.toFloat()
    val floatWidth = width.toFloat()

    val offset = (originalWidth * 0.05).toFloat()

    return Pair(maskBitmap, RectF(left / floatWidth * originalWidth - offset, top / floatHeight * originalHeight - offset, right / floatWidth * originalWidth + offset, bottom / floatHeight * originalHeight + offset))
}