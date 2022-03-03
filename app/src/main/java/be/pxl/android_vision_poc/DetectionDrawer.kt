package be.pxl.android_vision_poc

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import org.tensorflow.lite.task.vision.detector.Detection

class DetectionDrawer(context: Context?, attributeSet: AttributeSet?) : View(context, attributeSet) {
    private val detections: MutableList<Detection> = mutableListOf()
    private val paint: Paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 15f
        color = Color.WHITE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        detections.forEach {
            canvas.drawRect(
                it.boundingBox.left / 720 * this.width,
                it.boundingBox.top / 1280 * this.height,
                it.boundingBox.right / 720 * this.width,
                it.boundingBox.bottom / 1280 * this.height, paint)
        }
    }

    fun drawDetections(detections: List<Detection>) {
        this.detections.clear()
        this.detections.addAll(detections)
        invalidate()
    }
}