package com.sylvester.careerbot.network

const val GEMINI_API_KEY = "API_KEY" // Replace with your Gemini API key
suspend fun getCareerAdvice(userInput: String): Result<String> {
    val promptText = "You're a career coach. Answer helpfully: \"$userInput\""
    val prompt = Prompt(
        contents = listOf(Content(parts = listOf(Part(text = promptText))))
    )

    return try {
        val response = GeminiClient.service.getAnswer(prompt, GEMINI_API_KEY)
        val answer = response.candidates
            .firstOrNull()
            ?.content
            ?.parts
            ?.firstOrNull()
            ?.text

        if (answer != null) {
            Result.success(answer)
        } else {
            Result.failure(Exception("No answer found in Gemini response"))
        }
    } catch (e: retrofit2.HttpException) {
        Result.failure(Exception("HTTP error: ${e.code()} - ${e.message()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

