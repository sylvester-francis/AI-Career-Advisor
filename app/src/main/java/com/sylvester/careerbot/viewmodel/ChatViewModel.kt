package com.sylvester.careerbot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sylvester.careerbot.data.Message
import com.sylvester.careerbot.data.MessageType
import com.sylvester.careerbot.network.NetworkException
import com.sylvester.careerbot.network.getCareerAdvice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import retrofit2.HttpException

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChatViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(userInput: String) {
        // Add user message
        val userMessage = Message(
            content = userInput,
            type = MessageType.USER,
            timestamp = System.currentTimeMillis()
        )

        _uiState.update { currentState ->
            currentState.copy(
                messages = currentState.messages + userMessage,
                isLoading = true,
                error = null
            )
        }

        // Get bot response
        viewModelScope.launch {
            try {
                val result = getCareerAdvice(userInput)

                result.fold(
                    onSuccess = { response ->
                        val botMessage = Message(
                            content = response,
                            type = MessageType.BOT,
                            timestamp = System.currentTimeMillis()
                        )

                        _uiState.update { currentState ->
                            currentState.copy(
                                messages = currentState.messages + botMessage,
                                isLoading = false
                            )
                        }
                    },
                    onFailure = { exception ->
                        handleError(exception)
                    }
                )
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun handleError(exception: Throwable) {
        val errorMessage = when (exception) {
            is HttpException -> {
                when (exception.code()) {
                    401 -> "Invalid API key. Please check your configuration."
                    429 -> "Too many requests. Please try again later."
                    500, 502, 503 -> "Server error. Please try again later."
                    else -> "Network error: ${exception.message}"
                }
            }
            is UnknownHostException -> "No internet connection. Please check your network."
            is NetworkException -> exception.message
            else -> "An unexpected error occurred: ${exception.localizedMessage}"
        }

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                error = errorMessage
            )
        }

        // Add error message to chat
        val errorBotMessage = Message(
            content = "I'm having trouble connecting right now. $errorMessage",
            type = MessageType.BOT,
            timestamp = System.currentTimeMillis()
        )

        _uiState.update { currentState ->
            currentState.copy(
                messages = currentState.messages + errorBotMessage
            )
        }
    }

    fun clearError() {
        _uiState.update { currentState ->
            currentState.copy(error = null)
        }
    }

    fun clearChat() {
        _uiState.update { ChatUiState() }
    }
}