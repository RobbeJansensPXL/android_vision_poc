package be.pxl.android_vision_poc.fragments

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import be.pxl.android_vision_poc.MainActivity
import be.pxl.android_vision_poc.R
import be.pxl.android_vision_poc.analyzers.BottleSegmentationAnalyzer
import be.pxl.android_vision_poc.api.UntappdInstance
import be.pxl.android_vision_poc.drawers.DetectionDrawer
import be.pxl.android_vision_poc.room.FavoriteBeerModel
import be.pxl.android_vision_poc.utils.extractBitmap
import be.pxl.android_vision_poc.vision.Classifier
import be.pxl.android_vision_poc.vision.ObjectSegmenter
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.detector.Detection
import org.tensorflow.lite.task.vision.segmenter.Segmentation
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class SearchFragment : Fragment() {
    private val CLIENT_ID = "3AB342D9C5E0E2D86269BC0D3EF27BEFECB2501A"
    private val CLIENT_SECRET = "E8DB4CE6AB30D7DDED14F2CEE461B784EBAAE415"

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var latestClassificationName: String
    private lateinit var viewBinding: View

    private val cameraExecutorService: ExecutorService by lazy { Executors.newSingleThreadExecutor() }

    private val imageAnalyzer by lazy {
        ImageAnalysis.Builder()
            .build()
            .also {
                it.setAnalyzer(
                    cameraExecutorService,
                    BottleSegmentationAnalyzer(ObjectSegmenter("bottle_segmentation.tflite", requireActivity().applicationContext), Classifier("label_classifier.tflite", requireActivity().applicationContext), ::bottleSegmentationAnalyzationHandler)
                )
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(), MainActivity.REQUIRED_PERMISSIONS, MainActivity.REQUEST_CODE_PERMISSIONS
            )
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        viewBinding = inflater.inflate(R.layout.fragment_search, container, false)

        val retrieveInfoButton = viewBinding.findViewById<Button>(R.id.btn_retrieve_info)
        retrieveInfoButton.setOnClickListener(infoClickListener)

        val favoriteButton = viewBinding.findViewById<Button>(R.id.btn_favorite)
        favoriteButton.setOnClickListener(favoriteClickListener)

        // Inflate the layout for this fragment
        return viewBinding
    }

    private fun updateBeerDescription(description: String) {
        runOnUiThread {
            viewBinding.findViewById<TextView>(R.id.tv_beer_description)?.text  = description
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity().applicationContext)
        cameraProviderFuture.addListener(
            {
                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .build()
                    .also { it.setSurfaceProvider(viewBinding.findViewById<PreviewView>(R.id.pv_camera_preview).surfaceProvider) }
                cameraProviderFuture.get().bind(preview, imageAnalyzer)
            },
            ContextCompat.getMainExecutor(requireActivity().applicationContext)
        )
    }

    private val infoClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.btn_retrieve_info -> {
                if (this::latestClassificationName.isInitialized) {
                    lifecycleScope.launchWhenCreated {
                        Log.d(MainActivity.TAG, latestClassificationName)
                        val response = try {
                            UntappdInstance.api.search(latestClassificationName, CLIENT_ID, CLIENT_SECRET)
                        } catch (e: IOException) {
                            Log.e(MainActivity.TAG, "IOException, check internet connection")
                            return@launchWhenCreated
                        } catch (e: HttpException) {
                            Log.e(MainActivity.TAG, "Http Exception")
                            return@launchWhenCreated
                        }

                        val body = response.body()

                        if (response.isSuccessful && body != null) {
                            if (body.response.beers.items.isNotEmpty()) {
                                updateBeerDescription(body.response.beers.items[0].beer.beer_style)
                            }
                            Log.d(MainActivity.TAG, body.toString())
                        }
                        else {
                            Log.e(MainActivity.TAG, "Response not OK")
                        }
                    }
                }
            }
        }
    }

    private val favoriteClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.btn_favorite -> {
                if (this::latestClassificationName.isInitialized) {
                    (activity as MainActivity).beerViewModel.insert(FavoriteBeerModel(latestClassificationName))
                }
            }
        }
    }

    private fun allPermissionsGranted() = MainActivity.REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity().baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun ProcessCameraProvider.bind(
        preview: Preview,
        analyzer: ImageAnalysis
    ) = try {
        unbindAll()
        bindToLifecycle(
            requireActivity(),
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            analyzer
        )
    } catch(exc: Exception) {
        Log.e(MainActivity.TAG, "Use case binding failed", exc)
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == MainActivity.REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(requireActivity().applicationContext,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
        }
    }

    fun Fragment?.runOnUiThread(action: () -> Unit) {
        this ?: return
        if (!isAdded) return // Fragment not attached to an Activity
        activity?.runOnUiThread(action)
    }

    private fun objectDetectorResultHandler(image: Bitmap, detections: MutableList<Detection>?) {
        if (detections != null) {
            requireView().findViewById<DetectionDrawer>(R.id.detectionDrawer).drawDetections(detections, image.width, image.height)
        }
    }

    private fun classificationHandler(classifications: MutableList<Classifications>?) {
        classifications?.forEach { classification ->
            Log.d("classification_result", classification.categories.toString())
        }
    }

    private fun segmentationHandler(segmentations: MutableList<Segmentation>?) {
        if (segmentations != null) {
            requireView().findViewById<DetectionDrawer>(R.id.detectionDrawer).drawBitmap(segmentations[0].extractBitmap())
        }
        segmentations?.forEach{ segmentation ->
            Log.d("segmentation", segmentation.toString())
        }
    }

    private fun bottleObjectDetectionAnalyzationHandler(image: Bitmap, detections: MutableList<Detection>?, categories: MutableList<Category?>) {
        if (detections != null) {
            requireView().findViewById<DetectionDrawer>(R.id.detectionDrawer).drawDetectionsWithClassification(detections, image.width, image.height, categories)
            runOnUiThread {
                requireView().findViewById<TextView>(R.id.tv_result).text = categories.toString()
            }
        }
    }

    private fun bottleSegmentationAnalyzationHandler(image: Bitmap, segmentationMask: Bitmap, classification_result:MutableList<Classifications>?) {
        viewBinding.findViewById<DetectionDrawer>(R.id.detectionDrawer).drawBitmap(segmentationMask)

        runOnUiThread {
            if (classification_result != null) {
                requireView().findViewById<TextView>(R.id.tv_result).text =
                    classification_result[0].categories.toString()

                if (classification_result[0].categories.size > 0) {
                    this.latestClassificationName = classification_result[0].categories[0].label.replace(" ", "-").lowercase()
                }
            }
            else {
                requireView().findViewById<TextView>(R.id.tv_result).text = ""
            }
        }
    }
}