package be.pxl.android_vision_poc

import android.content.Context
import org.tensorflow.lite.support.image.TensorImage

interface ImageDetectionInterface {
    fun detect(tensorImage: TensorImage)
}