package com.sylvester.careerbot.network

import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

data class Part(val text: String)
data class Content(val parts: List<Part>)
data class Prompt(val contents: List<Content>)

data class GeminiResponse(val candidates: List<Candidate>)
data class Candidate(val content: Content)

interface GeminiService {
    @POST("v1beta/models/gemini-2.0-flash:generateContent")
    suspend fun getAnswer(
        @Body prompt: Prompt,
        @Query("key") key: String
    ): GeminiResponse
}
