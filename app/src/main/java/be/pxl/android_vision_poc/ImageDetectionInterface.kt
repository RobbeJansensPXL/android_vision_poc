package be.pxl.android_vision_poc

import org.tensorflow.lite.support.image.TensorImage

interface ImageDetectionInterface {
    fun detect(tensorImage: TensorImage)
}