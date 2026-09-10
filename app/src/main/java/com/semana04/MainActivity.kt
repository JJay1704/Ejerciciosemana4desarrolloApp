package com.semana04

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.semana04.ui.theme.Semana04Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Semana04Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MostrarDatos(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MostrarDatos(modifier: Modifier = Modifier) {
    val listpost = remember { mutableStateListOf<Post>() }
    val database = Firebase.firestore
    val posts = database.collection("posts")
    val lastSnapshot = remember { mutableStateOf<DocumentSnapshot?>(null) }

    LaunchedEffect(Unit) {
        posts.orderBy("fecha", Query.Direction.ASCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    listpost.clear()
                    for (dta in snapshot.documents) {
                        val newpost = dta.toObject(Post::class.java)
                        if (newpost != null) {
                            listpost.add(newpost)
                        }
                    }
                    lastSnapshot.value = snapshot.documents.lastOrNull()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FIRESTORE", "Error al obtener datos", e)
            }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(listpost) { item ->
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = item.texto,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = item.fecha.toDate().toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    val currentSnapshot = lastSnapshot.value
                    if (currentSnapshot != null) {
                        posts.orderBy("fecha", Query.Direction.ASCENDING)
                            .startAfter(currentSnapshot)
                            .limit(5)
                            .get()
                            .addOnSuccessListener { snapshot ->
                                if (!snapshot.isEmpty) {
                                    for (dta in snapshot.documents) {
                                        val newpost = dta.toObject(Post::class.java)
                                        if (newpost != null) {
                                            listpost.add(newpost)
                                        }
                                    }
                                    lastSnapshot.value = snapshot.documents.lastOrNull()
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("FIRESTORE", "Error al obtener datos", e)
                            }
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("CARGAR MÁS")
            }
        }
    }
}
