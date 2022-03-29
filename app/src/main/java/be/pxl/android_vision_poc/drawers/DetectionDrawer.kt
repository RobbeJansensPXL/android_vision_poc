package be.pxl.android_vision_poc.drawers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.vision.detector.Detection

class DetectionDrawer(context: Context?, attributeSet: AttributeSet?) : View(context, attributeSet) {
    private val detections: MutableList<Detection> = mutableListOf()
    private val categories: MutableList<Category> = mutableListOf()
    private var imageWidth: Int = 480
    private var imageHeight: Int = 640

    private val boundingBoxPaint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 15f
        color = Color.WHITE
    }

    private val textPaint: Paint = Paint().apply {
        style = Paint.Style.FILL
        textSize = 50f
        color = Color.WHITE
    }

    private var bitmap : Bitmap? = null
    private val transparentBitmapPaint: Paint = Paint().apply {
        alpha = 200
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in 0 until detections.size) {
            canvas.drawRect(
                detections[i].boundingBox.left / this.imageWidth * this.width,
                detections[i].boundingBox.top / this.imageHeight * this.height,
                detections[i].boundingBox.right / this.imageWidth * this.width,
                detections[i].boundingBox.bottom / this.imageHeight * this.height, boundingBoxPaint)

            if (categories.size != 0) {
                canvas.drawText(
                    "  " + categories[i].label,
                    detections[i].boundingBox.left / this.imageWidth * this.width,
                    (detections[i].boundingBox.centerY() / this.imageHeight * this.height),
                    textPaint)
            }
        }
        val bitmapRectangle = RectF(0F, 0F, this.width.toFloat(), this.height.toFloat())
        if (bitmap != null) {

            canvas.drawBitmap(bitmap!!, null, bitmapRectangle, transparentBitmapPaint)
        }
    }

    fun drawDetections(detections: List<Detection>, imageWidth: Int, imageHeight: Int) {
        clear()
        this.detections.addAll(detections)
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        invalidate()
    }

    fun drawDetectionsWithClassification(detections: List<Detection>, imageWidth: Int, imageHeight: Int, categories: MutableList<Category?>) {
        clear()
        this.imageWidth = imageWidth
        this.imageHeight = imageHeight
        for (i in 0 until categories.size) {
            if (categories[i] != null) {
                this.detections.add(detections[i])
                //Todo: Fix drawing category text
                this.categories.add(categories[i]!!)
            }
        }
        invalidate()
    }

    fun drawBitmap(bitmap: Bitmap) {
        clear()
        this.bitmap = bitmap
        invalidate()
    }

    private fun clear() {
        this.detections.clear()
        this.categories.clear()
        this.bitmap = null
    }
}