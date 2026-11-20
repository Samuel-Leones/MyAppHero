package co.edu.iub.myapphero.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.iub.myapphero.domain.model.Curso
import co.edu.iub.myapphero.ui.components.CursoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursoScreen(
    cursos: List<Curso>,
    onCursoClick: (Int) -> Unit,
    onCrearClick: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Cursos") }) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(onClick = onCrearClick) { Text("Crear curso") }
            }
            if (cursos.isEmpty()) {
                item { Text("No hay cursos registrados") }
            }
            items(items = cursos, key = { curso -> curso.id }) { curso ->
                CursoCard(curso = curso, onClick = { onCursoClick(curso.id) })
            }
        }
    }
}
