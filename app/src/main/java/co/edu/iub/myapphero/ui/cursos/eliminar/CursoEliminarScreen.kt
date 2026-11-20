package co.edu.iub.myapphero.ui.cursos.eliminar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CursoEliminarScreen(
    estado: CursoEliminarUiState,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Eliminar curso") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("¿Deseas eliminar este curso?")
            Text(estado.curso?.titulo.orEmpty())
            Text("Esta acción no se puede deshacer.")
            if (estado.error != null) {
                Text(estado.error, color = MaterialTheme.colorScheme.error)
            }
            Button(
                enabled = !estado.guardando && !estado.eliminado,
                onClick = onConfirmar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) { Text(if (estado.guardando) "Eliminando..." else "Confirmar eliminación") }
            OutlinedButton(onClick = onCancelar, enabled = !estado.guardando && !estado.eliminado) { Text("Cancelar") }
        }
    }
}
