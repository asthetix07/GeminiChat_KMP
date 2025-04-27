package org.example.geminichat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import kotlinx.coroutines.launch


@Composable
fun AppContent(viewModel: HomeViewModel, byteArrayFactory: ByteArrayFactory) {

    val appUiState = viewModel.uiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    HomeScreen(uiState = appUiState.value, byteArrayFactory) { inputText, selectedItems ->

        coroutineScope.launch {
            viewModel.questioning(userInput = inputText, selectImages = selectedItems)

        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUIState = HomeUIState.Loading,
    byteArrayFactory: ByteArrayFactory,
    onSendClicked: (String, List<ByteArray>) -> Unit
) {

    var userQues by rememberSaveable {
        mutableStateOf("")
    }

    val imagesList = remember {
        mutableStateListOf<ByteArray>()
    }


    val coroutineScope = rememberCoroutineScope()
    var showFilePicker by remember { mutableStateOf(false) }

    val fileTypes = listOf("jpg", "png")
    FilePicker(show = showFilePicker, fileExtensions = fileTypes) { mpFile ->
        showFilePicker = false
        coroutineScope.launch {
            if (mpFile != null) {
                val byteArray = byteArrayFactory.getByteArray(mpFile)
                if (byteArray != null) {
                    imagesList.add(byteArray)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Gemini Chat KMP ") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                )
            )
        },
        bottomBar = {
            Column (
                modifier = Modifier.padding(8.dp)
                    .navigationBarsPadding()
            ) {

                if (imagesList.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .height(80.dp)
                    ) {
                        items(imagesList) { list ->
                            Box(modifier = Modifier.padding(end = 8.dp)) {
                                AsyncImage(
                                    model = list,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(70.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                )
                                IconButton(
                                    onClick = { imagesList.remove(list) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(15.dp)
                                        .background(Color.White, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    //Add img
                    IconButton(onClick = {
                        showFilePicker = true
                    }
                    ) {
                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = "Add Image")
                    }

                    //Input text
                    OutlinedTextField(
                        value = userQues,
                        onValueChange = { userQues = it },
                        label = { Text(text = "User Input") },
                        placeholder = { Text(text = "Upload image or ask ...") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp)
                    )

                    //Send click
                    IconButton(
                        onClick = {
                            if (userQues.isNotBlank() || imagesList.isNotEmpty()) {
                                onSendClicked(userQues, imagesList.toList())
                                userQues = ""
                                imagesList.clear()
                            }
                        },
                        enabled = userQues.isNotBlank() || imagesList.isNotEmpty()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (userQues.isNotBlank() || imagesList.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            item {
                when (uiState) {

                    is HomeUIState.Initial -> {
                        Text(text = "👋 Hi! Ask me anything or upload an image to get started.",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    is HomeUIState.Loading -> {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = ProgressIndicatorDefaults.circularColor)
                        }
                    }

                    is HomeUIState.Success -> {
                        Card(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .fillMaxWidth(),
                            shape = MaterialTheme.shapes.large
                        ) {
                            SelectionContainer {
                                Text(text = uiState.outputText,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }

                    is HomeUIState.Error -> {
                        Card(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = uiState.error,
                                modifier = Modifier.padding(16.dp)

                            )
                        }
                    }
                }
            }
        }
    }
}