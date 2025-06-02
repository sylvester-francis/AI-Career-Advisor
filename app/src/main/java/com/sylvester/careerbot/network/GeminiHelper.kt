package com.sylvester.careerbot.network

import android.util.Log
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException

import com.sylvester.careerbot.BuildConfig

// API key is now securely stored in BuildConfig
private val GEMINI_API_KEY = BuildConfig.GEMINI_API_KEY

private const val NETWORK_TIMEOUT_MS = 30000L // 30 seconds
private const val MAX_INPUT_LENGTH = 1000
private const val MAX_RETRY_ATTEMPTS = 2

suspend fun getCareerAdvice(userInput: String): Result<String> {
    // Input validation
    if (userInput.isBlank()) {
        return Result.failure(NetworkException("Please enter a question"))
    }

    if (userInput.length > MAX_INPUT_LENGTH) {
        return Result.failure(NetworkException("Question is too long. Please keep it under $MAX_INPUT_LENGTH characters"))
    }

    // Sanitize input
    val sanitizedInput = userInput.trim()
        .replace("\n", " ")
        .replace("\r", " ")
        .replace("\t", " ")

    val enhancedPrompt = buildCareerPrompt(sanitizedInput)

    return retryWithBackoff(MAX_RETRY_ATTEMPTS) {
        executeApiCall(enhancedPrompt)
    }
}

private fun buildCareerPrompt(userInput: String): String {
    return """
        You are CareerBot, a professional career advisor with expertise in:
        - Career development and transitions
        - Resume and cover letter writing
        - Interview preparation
        - Salary negotiation
        - Professional networking
        - Work-life balance
        
        Guidelines:
        1. Provide specific, actionable advice
        2. Be encouraging and supportive
        3. Use clear, professional language
        4. Format responses with bullet points when listing items
        5. Keep responses concise but comprehensive
        6. Ask clarifying questions when needed
        
        User Question: "$userInput"
        
        Please provide helpful career advice:
    """.trimIndent()
}

private suspend fun executeApiCall(promptText: String): Result<String> {
    val prompt = Prompt(
        contents = listOf(Content(parts = listOf(Part(text = promptText))))
    )

    return try {
        withTimeout(NETWORK_TIMEOUT_MS) {
            val response = GeminiClient.service.getAnswer(prompt, GEMINI_API_KEY)

            val answer = response.candidates
                .firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text

            when {
                answer.isNullOrBlank() -> {
                    Log.e("GeminiHelper", "Empty response from API")
                    Result.failure(NetworkException("Received empty response from AI"))
                }
                answer.length < 10 -> {
                    Log.w("GeminiHelper", "Suspiciously short response: $answer")
                    Result.failure(NetworkException("Response seems incomplete. Please try again"))
                }
                else -> {
                    Log.d("GeminiHelper", "Successful response received")
                    Result.success(answer.trim())
                }
            }
        }
    } catch (e: TimeoutCancellationException) {
        Log.e("GeminiHelper", "Request timeout", e)
        Result.failure(NetworkException("Request timed out. Please check your connection and try again"))
    } catch (e: HttpException) {
        Log.e("GeminiHelper", "HTTP error: ${e.code()}", e)
        Result.failure(e)
    } catch (e: IOException) {
        Log.e("GeminiHelper", "Network error", e)
        Result.failure(NetworkException("Network error. Please check your internet connection"))
    } catch (e: Exception) {
        Log.e("GeminiHelper", "Unexpected error", e)
        Result.failure(NetworkException("An unexpected error occurred: ${e.localizedMessage}"))
    }
}

private suspend fun <T> retryWithBackoff(
    times: Int,
    initialDelay: Long = 1000L,
    factor: Double = 2.0,
    maxDelay: Long = 10000L,
    block: suspend () -> Result<T>
): Result<T> {
    var currentDelay = initialDelay
    repeat(times - 1) { attempt ->
        val result = block()
        if (result.isSuccess) return result

        // Don't retry on certain errors
        val exception = result.exceptionOrNull()
        if (exception is HttpException && exception.code() in listOf(401, 403, 404)) {
            return result
        }

        Log.d("GeminiHelper", "Retry attempt ${attempt + 1} after ${currentDelay}ms")
        kotlinx.coroutines.delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }

    // Last attempt
    return block()
}

// Extension function to safely get error message
fun Throwable.getSafeMessage(): String {
    return when (this) {
        is HttpException -> "Network error: ${code()}"
        is IOException -> "Connection error"
        else -> localizedMessage ?: "Unknown error"
    }
}