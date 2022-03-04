package be.pxl.android_vision_poc

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.segmenter.ColoredLabel
import org.tensorflow.lite.task.vision.segmenter.Segmentation

class DetectionDrawer(context: Context?, attributeSet: AttributeSet?) : View(context, attributeSet) {
    private val detections: MutableList<Detection> = mutableListOf()
    private var bitmap : Bitmap? = null
    private val boundingBoxPaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 15f
        color = Color.WHITE
    }
    private val transparentBitmapPaint: Paint = Paint().apply {
        alpha = 200
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        detections.forEach {
            canvas.drawRect(
                // TODO: Make it possible for other resolutions to work
                it.boundingBox.left / 480 * this.width,
                it.boundingBox.top / 640 * this.height,
                it.boundingBox.right / 480 * this.width,
                it.boundingBox.bottom / 640 * this.height, boundingBoxPaint)
        }
        val bitmapRectangle = RectF(0F, 0F, this.width.toFloat(), this.height.toFloat())
        if (bitmap != null) {

            canvas.drawBitmap(bitmap!!, null, bitmapRectangle, transparentBitmapPaint)
        }
    }

    fun drawDetections(detections: List<Detection>) {
        this.detections.clear()
        this.detections.addAll(detections)
        invalidate()
    }

    fun drawBitmap(segmentationResultList : List<Segmentation>) {
        val segmentationResult: Segmentation = segmentationResultList[0]
        val tensorMask = segmentationResult.masks[0]
        val rawMask = tensorMask.tensorBuffer.intArray

        val pixelData = IntArray(rawMask.size * 3)
        val coloredLabels: List<ColoredLabel> = segmentationResult.coloredLabels
        for (i in rawMask.indices) {
            var color = coloredLabels[rawMask[i]].color.toArgb()
            pixelData[3 * i] = Color.red(color)
            pixelData[3 * i + 1] = Color.green(color)
            pixelData[3 * i + 2] = Color.blue(color)
        }
        val shape = intArrayOf(tensorMask.width, tensorMask.height, 3)
        val maskImage = TensorImage()
        maskImage.load(pixelData, shape)

        this.bitmap = maskImage.bitmap
        invalidate()
    }
}