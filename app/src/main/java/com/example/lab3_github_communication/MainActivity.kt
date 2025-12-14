package com.example.lab3_github_communication

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.lab3_github_communication.data.api.RetrofitInstance
import com.example.lab3_github_communication.data.model.Repository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "App Started - Force Update 3")
        setContent {
            MaterialTheme {
                GitHubTrendingScreen()
            }
        }
    }

    @Composable
    fun GitHubTrendingScreen() {
        var searchQuery by remember { mutableStateOf("") }
        var statusMessage by remember { mutableStateOf("Entrez un langage et cliquez sur le bouton.") }
        var repositories by remember { mutableStateOf<List<Repository>>(emptyList()) }
        var selectedRepo by remember { mutableStateOf<Repository?>(null) }

        if (selectedRepo != null) {
            DetailsScreen(
                repository = selectedRepo!!,
                onBack = { selectedRepo = null }
            )
        } else {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "GitHub Trending",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Langage (ex: kotlin, python)...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (searchQuery.isNotEmpty()) {
                                performSearch(searchQuery) { status, repos ->
                                    statusMessage = status
                                    repositories = repos
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Text("Voir les plus populaires (30j)")
                    }

                    Text(
                        text = statusMessage,
                        fontStyle = FontStyle.Italic,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        items(repositories) { repo ->
                            // Changement du nom du composable pour forcer la mise à jour
                            RepositoryItem(
                                repository = repo,
                                onClick = { selectedRepo = repo }
                            )
                            Divider(color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun RepositoryItem(repository: Repository, onClick: () -> Unit) {
        // Suppression explicite de l'indication pour éviter tout crash de compatibilité
        val interactionSource = remember { MutableInteractionSource() }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .padding(12.dp)
        ) {
            Text(
                text = repository.name,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )

            Row(
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = "⭐ ${repository.stargazers_count}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(end = 16.dp)
                )

                Text(
                    text = repository.language ?: "N/A",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray
                )
            }
        }
    }

    @Composable
    fun DetailsScreen(repository: Repository, onBack: () -> Unit) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = repository.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "par ${repository.owner.login}",
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "⭐ ${repository.stargazers_count}",
                    fontSize = 18.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Langage : ${repository.language ?: "N/A"}",
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Description:",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = repository.description ?: "Pas de description",
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                ) {
                    Text("Retour")
                }
            }
        }
    }

    private fun performSearch(
        language: String,
        onResult: (String, List<Repository>) -> Unit
    ) {
        onResult("Recherche en cours...", emptyList())

        lifecycleScope.launch {
            try {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val oneMonthAgo = dateFormat.format(calendar.time)

                val q = "language:$language created:>$oneMonthAgo"

                Log.d("MainActivity", "Recherche avec query: $q")

                val response = RetrofitInstance.api.searchRepositories(
                    query = q,
                    sort = "stars",
                    order = "desc"
                )

                val repos = response.items ?: emptyList()

                Log.d("MainActivity", "Résultats reçus: ${repos.size} repos")

                onResult("✅ Trouvé ${response.total_count} projets populaires !", repos)

            } catch (e: Exception) {
                Log.e("MainActivity", "Erreur API complète", e)
                e.printStackTrace()
                onResult("❌ Erreur : ${e.message ?: "Erreur inconnue"}", emptyList())
            }
        }
    }
}