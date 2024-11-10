package com.example.speechrecognitionapp.api

import com.example.speechrecognitionapp.model.ChatRequest
import com.example.speechrecognitionapp.model.ChatResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface HuggingFaceService {
    @Headers(
        "Authorization: Bearer ${BuildConfig.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}",
        "Content-Type: application/json"
    )
    @POST("models/microsoft/DialoGPT-medium")
    fun getChatResponse(@Body request: ChatRequest): Call<List<ChatResponse>>