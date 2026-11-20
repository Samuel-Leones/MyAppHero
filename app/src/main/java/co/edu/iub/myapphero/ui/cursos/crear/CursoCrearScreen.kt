package co.edu.iub.myapphero.ui.cursos.crear

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursoCrearScreen(
    estado: CursoCrearUiState,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Crear curso") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = estado.titulo,
                onValueChange = onTituloChange,
                enabled = !estado.guardando && !estado.creado,
                label = { Text("Título") },
                singleLine = true,
                isError = estado.mostrarErrores && estado.titulo.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            if (estado.mostrarErrores && estado.titulo.isBlank()) {
                Text("El título es obligatorio")
            }
            OutlinedTextField(
                value = estado.descripcion,
                onValueChange = onDescripcionChange,
                enabled = !estado.guardando && !estado.creado,
                label = { Text("Descripción") },
                isError = estado.mostrarErrores && estado.descripcion.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )
            if (estado.mostrarErrores && estado.descripcion.isBlank()) {
                Text("La descripción es obligatoria")
            }
            Text("Estado inicial: Pendiente")
            if (estado.error != null) {
                Text(estado.error, color = MaterialTheme.colorScheme.error)
            }
            Button(enabled = !estado.guardando && !estado.creado, onClick = onGuardar) {
                Text(if (estado.guardando) "Guardando..." else "Guardar")
            }
            OutlinedButton(onClick = onCancelar, enabled = !estado.guardando && !estado.creado) { Text("Cancelar") }
        }
    }
}
