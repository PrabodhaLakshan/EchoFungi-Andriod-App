package com.example.myshroom

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.myshroom.databinding.BottomSheetBotBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BotBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetBotBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private lateinit var interpreter: Interpreter
    private var inputSize = 224 // default, will read from model input

    private val labels = listOf(
        "Orange Mold",
        "Black Mold",
        "Bacterial Blotch",
        "Worms Model",
        "Good Model",
        "Abalone Mushrooms",
        "Button Mushrooms",
        "Shiitake Mushrooms",
        "Termite Mushrooms",
        "Lentinus giganteus",
        "American Oyster",
        "Bhutanese Oyster",
        "Pink Oyster",
        "Green Mold"
    )

    private val advice = mapOf(
        "Orange Mold" to "Isolate affected blocks, reduce humidity spikes, improve fresh air exchange, and remove infected substrate.",
        "Black Mold" to "Remove contaminated blocks, disinfect area, and reduce standing water. Increase airflow.",
        "Bacterial Blotch" to "Lower humidity, improve airflow, avoid direct water on caps. Sanitize tools and surfaces.",
        "Worms Model" to "Use physical barriers, keep grow area clean, and remove heavily infested blocks.",
        "Good Model" to "Looks healthy. Keep monitoring temperature, humidity, and cleanliness.",
        "Abalone Mushrooms" to "Edible variety detected. Maintain steady humidity and fresh air.",
        "Button Mushrooms" to "Common edible variety. Keep humidity stable and avoid pooling water.",
        "Shiitake Mushrooms" to "Maintain cooler temps and good airflow for best quality.",
        "Termite Mushrooms" to "Wild/edible type. Ensure proper identification before consumption.",
        "Lentinus giganteus" to "Edible type. Keep substrate clean and monitor for contamination.",
        "American Oyster" to "Oyster species. Maintain clean airflow and harvest before caps flatten.",
        "Bhutanese Oyster" to "Oyster species. Provide fresh air to avoid long stems.",
        "Pink Oyster" to "Warm-loving oyster. Keep temps a bit higher and humidity consistent.",
        "Green Mold" to "Trichoderma risk. Remove infected blocks, clean room, and avoid over-wetting."
    )

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    imageUri = uri
                    binding.ivPreview.setImageURI(uri)
                    binding.tvResult.text = "Image selected. Tap Analyze."
                    binding.tvAdvice.text = ""
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetBotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadModel()
        binding.btnChoose.setOnClickListener { pickImage() }
        binding.btnAnalyze.setOnClickListener { analyzeImage() }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun loadModel() {
        val model = FileUtil.loadMappedFile(requireContext(), "model_unquant.tflite")
        interpreter = Interpreter(model)
        try {
            val inputShape = interpreter.getInputTensor(0).shape() // [1, h, w, c]
            if (inputShape.size >= 3) {
                inputSize = inputShape[1]
            }
        } catch (e: Exception) {
            inputSize = 224
        }
    }

    private fun analyzeImage() {
        val uri = imageUri
        if (uri == null) {
            Toast.makeText(requireContext(), "Please choose an image first", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progress.visibility = View.VISIBLE
        binding.tvResult.text = "Analyzing..."
        binding.tvAdvice.text = ""

        try {
            val bitmap = requireContext().contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            } ?: run {
                Toast.makeText(requireContext(), "Unable to load image", Toast.LENGTH_SHORT).show()
                binding.progress.visibility = View.GONE
                return
            }

            val scaled = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true)
            val inputBuffer = convertBitmapToBuffer(scaled)
            val output = Array(1) { FloatArray(labels.size) }

            interpreter.run(inputBuffer, output)

            val best = output[0].withIndex().maxByOrNull { it.value }
            val bestIndex = best?.index ?: 0
            val bestScore = best?.value ?: 0f
            val label = labels.getOrNull(bestIndex) ?: "Unknown"
            val confidence = bestScore * 100

            if (confidence >= 85f) {
                binding.tvResult.text = "$label (%.1f%%)".format(confidence)
                binding.tvAdvice.text = advice[label] ?: ""
            } else {
                binding.tvResult.text = "Low confidence (%.1f%%). Try a clearer photo.".format(confidence)
                binding.tvAdvice.text = ""
            }
        } catch (e: Exception) {
            binding.tvResult.text = "Error: ${e.message}"
        } finally {
            binding.progress.visibility = View.GONE
        }
    }

    private fun convertBitmapToBuffer(bitmap: Bitmap): ByteBuffer {
        val inputChannels = 3
        val buffer =
            ByteBuffer.allocateDirect(4 * inputSize * inputSize * inputChannels).order(ByteOrder.nativeOrder())
        val pixels = IntArray(inputSize * inputSize)
        bitmap.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize)
        var index = 0
        for (y in 0 until inputSize) {
            for (x in 0 until inputSize) {
                val pixel = pixels[index++]
                val r = ((pixel shr 16) and 0xFF) / 255f
                val g = ((pixel shr 8) and 0xFF) / 255f
                val b = (pixel and 0xFF) / 255f
                buffer.putFloat(r)
                buffer.putFloat(g)
                buffer.putFloat(b)
            }
        }
        buffer.rewind()
        return buffer
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (this::interpreter.isInitialized) {
            interpreter.close()
        }
        _binding = null
    }
}

