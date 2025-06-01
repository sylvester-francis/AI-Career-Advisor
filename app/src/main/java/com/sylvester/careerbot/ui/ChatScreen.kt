package com.sylvester.careerbot.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.sylvester.careerbot.network.getCareerAdvice

@Composable
fun ChatScreen() {
    var input by remember { mutableStateOf("") }
    val messages = remember { mutableStateListOf<Pair<String, String>>() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { (userMsg, botMsg) ->
                Text("You: $userMsg", style = MaterialTheme.typography.bodyLarge)
                Text("Bot: $botMsg", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask a career question...") }
            )

            Button(onClick = {
                val userMessage = input.trim()
                if (userMessage.isNotEmpty()) {
                    input = ""
                    messages.add(userMessage to "Loading...")

                    scope.launch {
                        try {
                            val responseResult = getCareerAdvice(userMessage)
                            val response = responseResult.getOrElse { throwable ->
                                "Failed to get response: ${throwable.message ?: "Unknown error"}"
                            }
                            messages[messages.lastIndex] = userMessage to response
                        } catch (e: Exception) {
                            e.printStackTrace()
                            messages[messages.lastIndex] = userMessage to "Error: ${e.message ?: "Unknown error"}"
                        }
                    }
                }
            }) {
                Text("Send")
            }
        }
    }
}
