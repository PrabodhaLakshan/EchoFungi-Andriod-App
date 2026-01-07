package com.example.myshroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myshroom.databinding.FragmentDashboardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Timer
import java.util.TimerTask

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val apiService = BlynkApiService()
    private val preferenceHelper by lazy { PreferenceHelper(requireContext()) }
    private var refreshTimer: Timer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRefresh.setOnClickListener {
            fetchData()
        }

        // Auto-refresh every 5 seconds if token is available
        startAutoRefresh()
    }

    private fun startAutoRefresh() {
        refreshTimer?.cancel()
        refreshTimer = Timer()
        refreshTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                if (preferenceHelper.getBlynkToken() != null) {
                    fetchData()
                }
            }
        }, 0, 5000) // Refresh every 5 seconds
    }

    private fun fetchData() {
        val token = preferenceHelper.getBlynkToken()
        val tempPin = preferenceHelper.getPin(PreferenceHelper.TEMP_PIN)
        val humidityPin = preferenceHelper.getPin(PreferenceHelper.HUMIDITY_PIN)

        if (token == null || token.isEmpty()) {
            binding.tvStatus.text = "Please configure Blynk token in API section"
            binding.tvTemperature.text = "-- °C"
            binding.tvHumidity.text = "-- %"
            return
        }

        if (tempPin == null || humidityPin == null) {
            binding.tvStatus.text = "Please configure pin numbers in API section"
            return
        }

        binding.tvStatus.text = "Fetching data..."

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val tempValue = withContext(Dispatchers.IO) {
                    apiService.getPinValue(token, tempPin)
                }
                val humidityValue = withContext(Dispatchers.IO) {
                    apiService.getPinValue(token, humidityPin)
                }

                if (tempValue != null && tempValue.isNotEmpty()) {
                    binding.tvTemperature.text = "$tempValue °C"
                } else {
                    binding.tvTemperature.text = "-- °C"
                }

                if (humidityValue != null && humidityValue.isNotEmpty()) {
                    binding.tvHumidity.text = "$humidityValue %"
                } else {
                    binding.tvHumidity.text = "-- %"
                }

                binding.tvStatus.text = "Last updated: ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}"

                // Check auto mode and control relay if enabled
                checkAutoMode(tempValue)

            } catch (e: Exception) {
                binding.tvStatus.text = "Error: ${e.message}"
                Toast.makeText(context, "Error fetching data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkAutoMode(tempValue: String?) {
        if (!preferenceHelper.isAutoModeEnabled()) {
            return
        }

        val maxTemp = preferenceHelper.getMaxTemp()
        val currentTemp = tempValue?.toFloatOrNull() ?: return
        val relayPin = preferenceHelper.getPin(PreferenceHelper.RELAY_PIN) ?: return
        val token = preferenceHelper.getBlynkToken() ?: return

        CoroutineScope(Dispatchers.Main).launch {
            val shouldTurnOn = currentTemp > maxTemp
            val relayValue = if (shouldTurnOn) "1" else "0"

            withContext(Dispatchers.IO) {
                apiService.setPinValue(token, relayPin, relayValue)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchData()
        startAutoRefresh()
    }

    override fun onPause() {
        super.onPause()
        refreshTimer?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        refreshTimer?.cancel()
        _binding = null
    }
}

