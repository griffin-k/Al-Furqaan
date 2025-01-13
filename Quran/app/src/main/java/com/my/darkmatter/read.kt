package com.my.darkmatter

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.my.darkmatter.ui.theme.DarkMatterTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class read : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DarkMatterTheme {
                HadithScreen()
            }
        }
    }
}

@Composable
fun HadithScreen() {
    var hadithText by remember { mutableStateOf("Fetching Hadith...") }
    val context = LocalContext.current
    val apiKey = context.getString(R.string.hadith_api_key)

    LaunchedEffect(Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val api = RetrofitInstance.apiService
            try {
                val response = api.getRandomHadith(apiKey)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val hadith = response.body()?.hadiths?.data?.randomOrNull()?.hadithEnglish
                        if (!hadith.isNullOrEmpty()) {
                            hadithText = hadith
                        } else {
                            hadithText = "No Hadith found."
                        }
                    } else {
                        hadithText = "Failed to fetch Hadith: ${response.message()}"
                        Toast.makeText(context, "Error fetching Hadith", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    hadithText = "Error: ${e.message}"
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    HadithCardUI(hadith = hadithText)
}

@Composable
fun HadithCardUI(hadith: String) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black // Set background color to black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White) // Card background color
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hadith",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = hadith,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

// Retrofit API Service
object RetrofitInstance {
    private const val BASE_URL = "https://www.hadithapi.com/api/"

    val apiService: HadithApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HadithApiService::class.java)
    }
}

interface HadithApiService {
    @GET("hadiths/")
    suspend fun getRandomHadith(
        @Query("apiKey") apiKey: String
    ): retrofit2.Response<HadithResponse>
}

// Data models
data class HadithResponse(
    val hadiths: HadithDataContainer
)

data class HadithDataContainer(
    val data: List<HadithItem>
)

data class HadithItem(
    val hadithEnglish: String
)

@Preview(showBackground = true)
@Composable
fun HadithCardPreview() {
    DarkMatterTheme {
        HadithCardUI(hadith = "This is a sample Hadith text for display purposes.")
    }
}
