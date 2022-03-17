package be.pxl.android_vision_poc

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio.RATIO_4_3
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import be.pxl.android_vision_poc.analyzers.BottleObjectDetectionAnalyzer
import be.pxl.android_vision_poc.analyzers.BottleSegmentationAnalyzer
import be.pxl.android_vision_poc.analyzers.ImageClassifier
import be.pxl.android_vision_poc.analyzers.ImageObjectSegmenter
import be.pxl.android_vision_poc.databinding.ActivityMainBinding
import be.pxl.android_vision_poc.drawers.DetectionDrawer
import be.pxl.android_vision_poc.utils.extractBitmap
import be.pxl.android_vision_poc.vision.Classifier
import be.pxl.android_vision_poc.vision.ObjectDetector
import be.pxl.android_vision_poc.vision.ObjectSegmenter
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.segmenter.Segmentation
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

//TODO: Add back FPS counter
class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding
    private lateinit var cameraExecutor: ExecutorService

    private val cameraExecutorService: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    private val imageAnalyzer by lazy {
        ImageAnalysis.Builder()
            .setTargetAspectRatio(RATIO_4_3)
            .build()
            .also {
                it.setAnalyzer(
                    cameraExecutorService,
                    BottleSegmentationAnalyzer(ObjectSegmenter("bottle_segmentation.tflite", this), Classifier("label_classifier.tflite", this), ::bottleSegmentationAnalyzationHandler)
                )
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        supportActionBar?.hide()

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                val preview = Preview.Builder()
                    .setTargetAspectRatio(RATIO_4_3)
                    .build()
                    .also { it.setSurfaceProvider(viewBinding.pvCameraPreview.surfaceProvider) }
                cameraProviderFuture.get().bind(preview, imageAnalyzer)
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun ProcessCameraProvider.bind(
        preview: Preview,
        analyzer: ImageAnalysis
    ) = try {
        unbindAll()
        bindToLifecycle(
            this@MainActivity,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analyzer
        )
    } catch(exc: Exception) {
        Log.e(TAG, "Use case binding failed", exc)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun objectDetectorResultHandler(image: Bitmap, detections: MutableList<Detection>?) {
        if (detections != null) {
            this.findViewById<DetectionDrawer>(R.id.detectionDrawer).drawDetections(detections, image.width, image.height)
        }
    }

    private fun classificationHandler(classifications: MutableList<Classifications>?) {
        classifications?.forEach { classification ->
            Log.d("classification_result", classification.categories.toString())
        }
    }

    private fun segmentationHandler(segmentations: MutableList<Segmentation>?) {
        if (segmentations != null) {
            this.findViewById<DetectionDrawer>(R.id.detectionDrawer).drawBitmap(segmentations[0].extractBitmap())
        }
        segmentations?.forEach{ segmentation ->
            Log.d("segmentation", segmentation.toString())
        }
    }

    private fun bottleObjectDetectionAnalyzationHandler(image: Bitmap, detections: MutableList<Detection>?, categories: MutableList<Category?>) {
        if (detections != null) {
            this.findViewById<DetectionDrawer>(R.id.detectionDrawer).drawDetectionsWithClassification(detections, image.width, image.height, categories)
            this@MainActivity.runOnUiThread(java.lang.Runnable {
                this.findViewById<TextView>(R.id.tv_result).text = categories.toString()
            })
        }
    }

    private fun bottleSegmentationAnalyzationHandler(image: Bitmap, segmentationMask: Bitmap, classification_result:MutableList<Classifications>) {
        this.findViewById<DetectionDrawer>(R.id.detectionDrawer).drawBitmap(segmentationMask)
        this@MainActivity.runOnUiThread(java.lang.Runnable {
            this.findViewById<TextView>(R.id.tv_result).text = classification_result[0].categories.toString()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "Android Vision POC"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).toTypedArray()
    }
}
