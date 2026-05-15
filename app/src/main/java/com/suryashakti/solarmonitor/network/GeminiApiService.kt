package com.suryashakti.solarmonitor.network

import retrofit2.http.Body
import retrofit2.http.POST

interface GeminiApiService {
    // Switching to v1 as it's more stable for general availability of gemini-1.5-flash
    @POST("v1/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(@Body request: GeminiRequest): GeminiResponse
}

data class GeminiRequest(val contents: List<Content>)
data class Content(val role: String = "user", val parts: List<Part>)
data class Part(val text: String)

data class GeminiResponse(val candidates: List<Candidate>?)
data class Candidate(val content: ContentResponse?)
data class ContentResponse(val parts: List<PartResponse>?)
data class PartResponse(val text: String?)
