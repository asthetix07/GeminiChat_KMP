package org.example.geminichat

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.PlatformImage
import dev.shreyaspatil.ai.client.generativeai.type.content
import dev.shreyaspatil.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel: ViewModel() {

    private val _uiState: MutableStateFlow<HomeUIState> = MutableStateFlow(HomeUIState.Initial)
    val uiState = _uiState.asStateFlow()
    private lateinit var generativeModel: GenerativeModel

    init {
        val config = generationConfig {
            temperature = 0.70f // Balanced between creativity and making sense.
        }

        generativeModel = GenerativeModel(
            modelName = "gemini-2.0-flash",
            apiKey = AppConfig.apiKey,
            generationConfig = config
        )
    }


    fun questioning(userInput: String, selectImages: List<ByteArray>){
        _uiState.value = HomeUIState.Loading
        val prompt = when {
            userInput.isNotBlank() && selectImages.isNotEmpty() ->
                "The user has asked a question and also uploaded image(s). Use both the image(s) and the text to provide a helpful answer. :$userInput"

            userInput.isNotBlank() ->
                "The user has asked a question. Provide a helpful, accurate, and concise answer. :$userInput"

            selectImages.isNotEmpty() ->
                "The user has uploaded image(s). Describe, analyze, or provide useful information about them.:$userInput"

            else -> "Awaiting input from user."
        }


        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content = content {
                    for(byteArray in selectImages) {
                        image(PlatformImage(byteArray)) //used to send image to AI in KMP (of type ByteArray)
                    }
                    text(prompt)
                }
                var output = ""
                generativeModel.generateContentStream(content).collect() {  //Capturing response and serve to UI
                    output += it.text
                    _uiState.value = HomeUIState.Success(output)
                }
            }
            catch (e: Exception){
                _uiState.value = HomeUIState.Error(e.localizedMessage ?: "Unknown Error")
            }
        }
    }
}

sealed interface HomeUIState{  // UI State for Home Screen

    object Initial: HomeUIState

    object Loading: HomeUIState

    data class Success(
        val outputText: String
    ): HomeUIState

    data class Error(
        val error: String
    ): HomeUIState

}