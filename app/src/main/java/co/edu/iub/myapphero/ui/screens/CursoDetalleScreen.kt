package co.edu.iub.myapphero.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.iub.myapphero.domain.model.Curso

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursoDetalleScreen(
    curso: Curso,
    onBack: () -> Unit,
    onActualizarClick: () -> Unit,
    onEliminarClick: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Detalle del curso") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Identificador: ${curso.id}")
            Text(curso.titulo)
            Text(curso.descripcion)
            Text(if (curso.completado) "Estado: Completada" else "Estado: Pendiente")
            Button(onClick = onActualizarClick) { Text("Actualizar curso") }
            Button(onClick = onEliminarClick) { Text("Eliminar curso") }
            OutlinedButton(onClick = onBack) { Text("Volver al listado") }
        }
    }
}
