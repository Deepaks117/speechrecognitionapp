package com.example.speechrecognitionapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.speechrecognitionapp.api.ApiClient
import com.example.speechrecognitionapp.model.ChatRequest
import com.example.speechrecognitionapp.model.ChatResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {
    private lateinit var recordButton: Button
    private lateinit var transcriptionText: TextView
    private val SPEECH_REQUEST_CODE = 0
    private val RECORD_AUDIO_PERMISSION_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recordButton = findViewById(R.id.recordButton)
        transcriptionText = findViewById(R.id.transcriptionText)

        recordButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RECORD_AUDIO_PERMISSION_CODE)
            } else {
                startSpeechRecognition()
            }
        }
    }

    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
        }
        try {
            startActivityForResult(intent, SPEECH_REQUEST_CODE)
        } catch (e: Exception) {
            Toast.makeText(this, "Speech recognition not available on this device", Toast.LENGTH_LONG).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            val spokenText: String? =
                data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { results ->
                    results[0]
                }
            transcriptionText.text = spokenText ?: "Speech not recognized. Please try again."

            spokenText?.let { sendToHuggingFaceAPI(it) }
        } else if (resultCode == RESULT_CANCELED) {
            transcriptionText.text = "Speech recognition cancelled"
        }
    }

    private fun sendToHuggingFaceAPI(input: String) {
        val request = ChatRequest(inputs = input)

        ApiClient.service.getChatResponse(request).enqueue(object : Callback<List<ChatResponse>> {
            override fun onResponse(call: Call<List<ChatResponse>>, response: Response<List<ChatResponse>>) {
                if (response.isSuccessful) {
                    val replies = response.body()
                    if (!replies.isNullOrEmpty()) {
                        val reply = replies[0].generated_text
                        runOnUiThread {
                            transcriptionText.append("\n\nAI Response: $reply")
                        }
                    } else {
                        showError("Empty response from API")
                    }
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        showError("API Error ${response.code()}: $errorBody")
                    } catch (e: Exception) {
                        showError("API Error ${response.code()}")
                    }
                }
            }

            override fun onFailure(call: Call<List<ChatResponse>>, t: Throwable) {
                showError("Request failed: ${t.message}")
            }
        })
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            transcriptionText.append("\n\nError: $message")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechRecognition()
            } else {
                Toast.makeText(this, "Permission denied. Speech recognition is not available.", Toast.LENGTH_LONG).show()
            }
        }
    }
}