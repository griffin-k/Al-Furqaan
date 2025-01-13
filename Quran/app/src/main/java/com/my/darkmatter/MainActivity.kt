package com.my.darkmatter

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.my.darkmatter.ui.theme.DarkMatterTheme
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var exoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DarkMatterTheme {
                SurahAudioPlayer { surahNumber, isPlaying ->
                    if (isPlaying) {
                        stopSurahAudio()
                    } else {
                        playSurahAudio(surahNumber)
                    }
                }
            }
        }
    }

    private fun initializeExoPlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(this).build()
        }
    }

    private fun playSurahAudio(surahNumber: String) {
        val edition = "ar.alafasy"
        val bitrate = "128"
        val url = "https://cdn.islamic.network/quran/audio-surah/$bitrate/$edition/$surahNumber.mp3"

        initializeExoPlayer()

        try {
            exoPlayer?.let { player ->
                val mediaItem = MediaItem.fromUri(url)
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()

                Toast.makeText(this@MainActivity, "Playing Surah $surahNumber", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopSurahAudio() {
        exoPlayer?.stop()
        Toast.makeText(this@MainActivity, "Audio Stopped", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
        exoPlayer = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurahAudioPlayer(onPlayClicked: (String, Boolean) -> Unit) {
    var surahNumber by remember { mutableStateOf(TextFieldValue("")) }
    var isPlaying by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Surah Audio Player", color = Color.White, modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {

            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Image(
                        painter = painterResource(id = R.drawable.plyer),
                        contentDescription = "Surah Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )

                    Text(
                        text = "Enter Surah Number",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 16.dp),
                        color = Color.Black
                    )


                    OutlinedTextField(
                        value = surahNumber,
                        onValueChange = { surahNumber = it },
                        label = { Text("Surah Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(24.dp))


                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val input = surahNumber.text.trim()
                                if (input.isNotEmpty() && input.toIntOrNull() in 1..114) {
                                    onPlayClicked(input, isPlaying)
                                    isPlaying = !isPlaying
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Please enter a valid Surah number (1-114)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(Color(0xFFF3EBEB), Color(0xFFFFFFFF))
                                )
                            ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isPlaying) "Stop Surah" else "Play Surah",
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SurahAudioPlayerPreview() {
    DarkMatterTheme {
        SurahAudioPlayer { _, _ -> }
    }
}
