package co.edu.iub.myapphero.ui.cursos.listado

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.iub.myapphero.ui.components.CursoCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursosScreen(
    estado: CursosUiState,
    onCursoClick: (Int) -> Unit,
    onCrearClick: () -> Unit,
    onReintentar: () -> Unit
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
                Button(onClick = onCrearClick, enabled = !estado.cargando) { Text("Crear curso") }
            }
            item {
                Button(onClick = onReintentar, enabled = !estado.cargando) { Text("Actualizar listado") }
            }
            if (estado.cargando) {
                item { CircularProgressIndicator() }
                item { Text("Cargando cursos...") }
            }
            if (estado.error != null) {
                item { Text(estado.error, color = MaterialTheme.colorScheme.error) }
                item { Button(onClick = onReintentar) { Text("Reintentar") } }
            }
            if (!estado.cargando && estado.error == null && estado.cursos.isEmpty()) {
                item { Text("No hay cursos registrados") }
            }
            items(items = estado.cursos, key = { curso -> curso.id }) { curso ->
                CursoCard(curso = curso, onClick = { onCursoClick(curso.id) })
            }
        }
    }
}
