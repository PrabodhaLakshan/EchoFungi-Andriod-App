package com.example.myshroom

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myshroom.databinding.FragmentApiBinding

class ApiFragment : Fragment() {
    private var _binding: FragmentApiBinding? = null
    private val binding get() = _binding!!
    private val preferenceHelper by lazy { PreferenceHelper(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load saved values
        loadSavedValues()

        binding.btnConnect.setOnClickListener {
            connectToBlynk()
        }

        binding.btnSavePins.setOnClickListener {
            savePinConfiguration()
        }
    }

    private fun loadSavedValues() {
        val token = preferenceHelper.getBlynkToken()
        val tempPin = preferenceHelper.getPin(PreferenceHelper.TEMP_PIN)
        val humidityPin = preferenceHelper.getPin(PreferenceHelper.HUMIDITY_PIN)
        val relayPin = preferenceHelper.getPin(PreferenceHelper.RELAY_PIN)
        val buzzerPin = preferenceHelper.getPin(PreferenceHelper.BUZZER_PIN)

        token?.let { binding.etToken.setText(it) }
        tempPin?.let { binding.etTempPin.setText(it) }
        humidityPin?.let { binding.etHumidityPin.setText(it) }
        relayPin?.let { binding.etRelayPin.setText(it) }
        buzzerPin?.let { binding.etBuzzerPin.setText(it) }
    }

    private fun connectToBlynk() {
        val token = binding.etToken.text?.toString()?.trim()

        if (token.isNullOrEmpty()) {
            Toast.makeText(context, "Please enter Blynk token", Toast.LENGTH_SHORT).show()
            return
        }

        preferenceHelper.saveBlynkToken(token)
        Toast.makeText(context, "Token saved successfully!", Toast.LENGTH_SHORT).show()
    }

    private fun savePinConfiguration() {
        val tempPin = binding.etTempPin.text?.toString()?.trim()
        val humidityPin = binding.etHumidityPin.text?.toString()?.trim()
        val relayPin = binding.etRelayPin.text?.toString()?.trim()
        val buzzerPin = binding.etBuzzerPin.text?.toString()?.trim()

        if (tempPin.isNullOrEmpty() || humidityPin.isNullOrEmpty()) {
            Toast.makeText(context, "Please enter at least Temperature and Humidity pins", Toast.LENGTH_SHORT).show()
            return
        }

        tempPin?.let { preferenceHelper.savePin(PreferenceHelper.TEMP_PIN, it) }
        humidityPin?.let { preferenceHelper.savePin(PreferenceHelper.HUMIDITY_PIN, it) }
        relayPin?.let { preferenceHelper.savePin(PreferenceHelper.RELAY_PIN, it) }
        buzzerPin?.let { preferenceHelper.savePin(PreferenceHelper.BUZZER_PIN, it) }

        Toast.makeText(context, "Pin configuration saved!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



