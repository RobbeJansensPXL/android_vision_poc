package be.pxl.android_vision_poc.utils

import android.graphics.*
import android.media.Image
import java.io.ByteArrayOutputStream

fun Image.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer
    val vuBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val vuSize = vuBuffer.remaining()

    val nv21 = ByteArray(ySize + vuSize)

    yBuffer.get(nv21, 0, ySize)
    vuBuffer.get(nv21, ySize, vuSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
    val imageBytes = out.toByteArray()

    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}

fun Bitmap.rotate(degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.cropRectangle(rectangle: RectF) : Bitmap {
    val left = rectangle.left.toInt().coerceAtLeast(0)
    val top = rectangle.top.toInt().coerceAtLeast(0)
    val right = rectangle.right.toInt().coerceAtMost(this.width)
    val bottom = rectangle.bottom.toInt().coerceAtMost(this.height)
    val width = (right - left).coerceAtMost(this.width - right).coerceAtLeast(1)
    val height = (bottom - top).coerceAtMost(this.height - top).coerceAtLeast(1)
    return Bitmap.createBitmap(this, left, top, width, height)
}