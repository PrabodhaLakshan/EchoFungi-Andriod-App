package com.example.myshroom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myshroom.databinding.FragmentScheduleBinding
import java.util.Timer
import java.util.TimerTask
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ScheduleFragment : Fragment() {
    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private val preferenceHelper by lazy { PreferenceHelper(requireContext()) }
    private var scheduleTimer: Timer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load saved values
        loadSavedValues()

        binding.btnSaveSchedule.setOnClickListener {
            saveSchedule()
        }

        binding.btnSaveAutoMode.setOnClickListener {
            saveAutoModeSettings()
        }

        binding.switchAutoMode.setOnCheckedChangeListener { _, isChecked ->
            binding.btnSaveAutoMode.isEnabled = true
        }

        // Start schedule monitoring
        startScheduleMonitoring()
    }

    private fun loadSavedValues() {
        val startTime = preferenceHelper.getScheduleStartTime()
        val endTime = preferenceHelper.getScheduleEndTime()
        val autoModeEnabled = preferenceHelper.isAutoModeEnabled()
        val maxTemp = preferenceHelper.getMaxTemp()

        startTime?.let { binding.etStartTime.setText(it) }
        endTime?.let { binding.etEndTime.setText(it) }
        binding.switchAutoMode.isChecked = autoModeEnabled
        binding.etMaxTemp.setText(maxTemp.toString())
        updateAutoModeStatus()
    }

    private fun saveSchedule() {
        val startTime = binding.etStartTime.text?.toString()?.trim()
        val endTime = binding.etEndTime.text?.toString()?.trim()

        if (startTime.isNullOrEmpty() || endTime.isNullOrEmpty()) {
            Toast.makeText(context, "Please enter both start and end times", Toast.LENGTH_SHORT).show()
            return
        }

        preferenceHelper.saveScheduleStartTime(startTime)
        preferenceHelper.saveScheduleEndTime(endTime)
        Toast.makeText(context, "Schedule saved successfully!", Toast.LENGTH_SHORT).show()

        // Restart monitoring with new schedule
        startScheduleMonitoring()
    }

    private fun saveAutoModeSettings() {
        val maxTemp = binding.etMaxTemp.text?.toString()?.toFloatOrNull()

        if (maxTemp == null) {
            Toast.makeText(context, "Please enter a valid max temperature", Toast.LENGTH_SHORT).show()
            return
        }

        preferenceHelper.setAutoModeEnabled(binding.switchAutoMode.isChecked)
        preferenceHelper.saveMaxTemp(maxTemp)
        Toast.makeText(context, "Auto mode settings saved!", Toast.LENGTH_SHORT).show()
        updateAutoModeStatus()

        // Restart monitoring
        startScheduleMonitoring()
    }

    private fun updateAutoModeStatus() {
        val enabled = preferenceHelper.isAutoModeEnabled()
        val maxTemp = preferenceHelper.getMaxTemp()
        binding.tvAutoModeStatus.text = if (enabled) {
            "Auto Mode: ON (Max Temp: ${maxTemp}°C)"
        } else {
            "Auto Mode: OFF"
        }
    }

    private fun startScheduleMonitoring() {
        scheduleTimer?.cancel()
        scheduleTimer = Timer()

        scheduleTimer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    checkScheduleTime()
                }
            }
        }, 0, 60000) // Check every minute
    }

    private fun checkScheduleTime() {
        val startTime = preferenceHelper.getScheduleStartTime() ?: return
        val endTime = preferenceHelper.getScheduleEndTime() ?: return
        val relayPin = preferenceHelper.getPin(PreferenceHelper.RELAY_PIN) ?: return
        val token = preferenceHelper.getBlynkToken() ?: return

        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Calendar.getInstance().time)
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())

        try {
            val current = sdf.parse(currentTime)
            val start = sdf.parse(startTime)
            val end = sdf.parse(endTime)

            if (current != null && start != null && end != null) {
                val isWithinSchedule = (current.after(start) || current.equals(start)) && current.before(end)

                // Control relay based on schedule (this is for spray system)
                // You can extend this logic as needed
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        loadSavedValues()
        startScheduleMonitoring()
    }

    override fun onPause() {
        super.onPause()
        scheduleTimer?.cancel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scheduleTimer?.cancel()
        _binding = null
    }
}



