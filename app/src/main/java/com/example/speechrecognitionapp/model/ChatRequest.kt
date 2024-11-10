package com.example.speechrecognitionapp.model

data class ChatRequest(
    val inputs: String,
    val parameters: Parameters = Parameters()
)

data class Parameters(
    val max_length: Int = 1000,
    val temperature: Double = 0.7,
    val return_full_text: Boolean = true
)