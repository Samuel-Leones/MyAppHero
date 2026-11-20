package co.edu.iub.myapphero.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import co.edu.iub.myapphero.domain.model.Curso

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursoActualizarScreen(
    curso: Curso,
    onGuardar: (String, String, Boolean) -> Unit,
    onCancelar: () -> Unit
) {
    // El formulario usa un borrador: cancelar no modifica el curso original.
    var titulo by rememberSaveable(curso.id) { mutableStateOf(curso.titulo) }
    var descripcion by rememberSaveable(curso.id) { mutableStateOf(curso.descripcion) }
    var completado by rememberSaveable(curso.id) { mutableStateOf(curso.completado) }
    var mostrarErrores by rememberSaveable(curso.id) { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Actualizar curso") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Identificador: ${curso.id}")
            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Título") },
                singleLine = true,
                isError = mostrarErrores && titulo.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            if (mostrarErrores && titulo.isBlank()) {
                Text("El título es obligatorio")
            }
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                isError = mostrarErrores && descripcion.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            if (mostrarErrores && descripcion.isBlank()) {
                Text("La descripción es obligatoria")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = completado, onCheckedChange = { completado = it })
                Text("Completada")
            }
            Button(onClick = {
                mostrarErrores = true
                if (titulo.isNotBlank() && descripcion.isNotBlank()) {
                    onGuardar(titulo.trim(), descripcion.trim(), completado)
                }
            }) { Text("Guardar") }
            OutlinedButton(onClick = onCancelar) { Text("Cancelar") }
        }
    }
}
